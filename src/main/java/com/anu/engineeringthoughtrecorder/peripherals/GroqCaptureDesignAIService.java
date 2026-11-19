package com.anu.engineeringthoughtrecorder.peripherals;

import com.anu.engineeringthoughtrecorder.domainObjects.DesignDraft;
import com.anu.engineeringthoughtrecorder.domainObjects.domainUtils.DraftItem;
import com.anu.engineeringthoughtrecorder.domainObjects.domainUtils.DraftSectionType;
import com.anu.engineeringthoughtrecorder.domainObjects.domainUtils.TranscriptSegment;
import com.anu.engineeringthoughtrecorder.peripherals.peripheralUtils.SegmentAIResult;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.time.Duration;
import com.fasterxml.jackson.databind.ObjectMapper;import com.fasterxml.jackson.databind.JsonNode;
import java.util.UUID;



public class GroqCaptureDesignAIService implements CaptureDesignAIService {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final String apiKey = System.getenv("GROQ_API_KEY");
    private static final int MAX_RATE_LIMIT_RETRIES = 3;
    private static final long INITIAL_RETRY_DELAY_MS = 3000;


    @Override
    public SegmentAIResult getDesignDraftDelta(
            TranscriptSegment segment,
            DesignDraft designDraft) {

        String prompt = buildPrompt(segment, designDraft);

        String responseBody = callGroq(prompt);

        String generatedContent = extractGeneratedContent(responseBody);

        return parseSegmentAIResult(generatedContent);
    }

    private String buildDraftContext(DesignDraft designDraft) {
        StringBuilder context = new StringBuilder();

        for (DraftSectionType sectionType : DraftSectionType.values()) {
            context.append(sectionType.name()).append(":\n");

            for (DraftItem item : designDraft.getDraftSection(sectionType).getItems()) {
                context.append("- ID: ")
                        .append(item.getId())
                        .append(", Content: ")
                        .append(item.getContent())
                        .append("\n");
            }
        }

        return context.toString();
    }

    private String buildPrompt(
            TranscriptSegment segment,
            DesignDraft designDraft) {

        return """
            You are analyzing an engineering design discussion.

            CURRENT DESIGN DRAFT:
            %s

            NEW TRANSCRIPT SEGMENT:
            %s

            Identify one design draft item that this segment introduces
            or updates.

            Classify it into exactly one section:
            FUNCTIONAL_REQUIREMENTS
            NON_FUNCTIONAL_REQUIREMENTS
            ASSUMPTIONS
            DECISIONS
            OPEN_QUESTIONS

            Rules:
            - Before creating a new item, compare the transcript segment
              against every existing item in the current design draft.
            - If the segment adds details, constraints, or clarification
              to an existing item, update that item and reuse its exact ID.
            - Create a new item only when the segment introduces a
              genuinely separate requirement, assumption, decision, or question.
            - Do not invent requirements or design decisions.
            - If updating an existing item, reuse its ID.
            - If creating a new item, use null for the ID.
            - Return only a JSON object with these fields:
              sectionType, itemId, content
              
                DESIGN ITEM CLASSIFICATION RULES:
                
                1. FUNCTIONAL_REQUIREMENTS:
                   Capabilities or behaviors the system must provide to users
                   or other systems.
                
                2. NON_FUNCTIONAL_REQUIREMENTS:
                   Quality attributes and constraints such as performance,
                   security, capacity, reliability, and availability.
                
                3. ASSUMPTIONS:
                   Conditions believed to be true but not yet verified.
                
                4. DECISIONS:
                   Explicit architectural, technology, or implementation choices.
                   Examples: synchronous versus asynchronous processing,
                   using a message queue, selecting PostgreSQL, and retry policies.
                
                5. OPEN_QUESTIONS:
                   Unresolved questions requiring future decisions.
                   Do not convert an unresolved question into a requirement.
                
                ITEM UPDATE RULES:
                
                - Update an existing item only when the new statement directly
                  revises, clarifies, or extends that same item.
                - Do not merge separate design concepts merely because they
                  are related.
                - Preserve the existing item ID when updating an item.
                - Create a new item when the statement introduces a distinct
                  requirement, decision, assumption, or question.
                - Classify the statement according to its engineering meaning,
                  not merely the words it contains.

            Example:
            {
              "sectionType": "FUNCTIONAL_REQUIREMENTS",
              "itemId": null,
              "content": "Users can upload audio recordings."
            }
            """.formatted(
                buildDraftContext(designDraft),
                segment.getText()
        );
    }

    private String callGroq(String prompt) {
        try {
            ObjectMapper mapper = new ObjectMapper();

            String requestBody = mapper.writeValueAsString(
                    java.util.Map.of(
                            "model", "openai/gpt-oss-20b",
                            "messages", java.util.List.of(
                                    java.util.Map.of(
                                            "role", "user",
                                            "content", prompt
                                    )
                            ),
                            "reasoning_effort", "low",
                            "max_completion_tokens", 1000,
                            "response_format", java.util.Map.of(
                                    "type", "json_object"
                            )
                    )
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.groq.com/openai/v1/chat/completions"))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(60))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            // Retry only when Groq reports HTTP 429.
// Four attempts maximum: one initial request + three retries.
            for (int attempt = 0; attempt <= MAX_RATE_LIMIT_RETRIES; attempt++) {

                HttpResponse<String> response = httpClient.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                );

                if (response.statusCode() == 200) {
                    return response.body();
                }

                if (response.statusCode() != 429 || attempt == MAX_RATE_LIMIT_RETRIES) {
                    throw new IllegalStateException(
                            "Groq API returned HTTP " + response.statusCode()
                                    + ": " + response.body()
                    );
                }

                // Extract the suggested delay from Groq's error message.
                String errorBody = response.body();

                java.util.regex.Matcher matcher = java.util.regex.Pattern
                        .compile("Please try again in ([0-9.]+)s")
                        .matcher(errorBody);

                long delayMs = INITIAL_RETRY_DELAY_MS;

                if (matcher.find()) {
                    delayMs = (long) Math.ceil(
                            Double.parseDouble(matcher.group(1)) * 1000
                    ) + 250; // Small safety margin.
                }

                System.out.println(
                        "Groq rate limit reached. Retrying in "
                                + delayMs + " ms..."
                );

                Thread.sleep(delayMs);
            }

            throw new IllegalStateException("Groq retry limit exceeded");

        } catch (Exception e) {
            throw new IllegalStateException("Groq API request failed", e);
        }
    }
    private String extractGeneratedContent(String responseBody) {
        try {
            JsonNode root = new ObjectMapper().readTree(responseBody);

            JsonNode content = root.path("choices")
                    .path(0)
                    .path("message")
                    .path("content");

            if (content.isMissingNode() || content.asText().isBlank()) {
                throw new IllegalStateException(
                        "Groq returned empty generated content");
            }

            return content.asText();

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to extract Groq response content", e);
        }
    }

    private SegmentAIResult parseSegmentAIResult(String generatedContent) {
        try {
            JsonNode json = new ObjectMapper().readTree(generatedContent);

            DraftSectionType sectionType = DraftSectionType.valueOf(
                    json.path("sectionType").asText()
            );

            String content = json.path("content").asText();

            if (content.isBlank()) {
                throw new IllegalArgumentException("Draft item content cannot be empty");
            }

            JsonNode itemIdNode = json.path("itemId");

            DraftItem item;

            if (itemIdNode.isMissingNode() || itemIdNode.isNull()) {
                item = new DraftItem(content);
            } else {
                UUID itemId = UUID.fromString(itemIdNode.asText());
                item = new DraftItem(itemId, content);
            }

            return new SegmentAIResult(sectionType, item);

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to parse Groq-generated draft change", e);
        }
    }
}