package com.anu.engineeringthoughtrecorder.persistence;

import com.anu.engineeringthoughtrecorder.domainObjects.DesignDraft;
import com.anu.engineeringthoughtrecorder.domainObjects.DesignSession;
import com.anu.engineeringthoughtrecorder.domainObjects.SystemSpecification;
import com.anu.engineeringthoughtrecorder.domainObjects.Transcript;
import com.anu.engineeringthoughtrecorder.domainObjects.domainUtils.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.nio.file.Path;
import java.io.IOException;
import java.nio.file.Files;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;

public class JSONPersistence implements DesignSessionPersistence {

    private final ObjectMapper objectMapper =
            new ObjectMapper().registerModule(new JavaTimeModule());
    private final Path storageDirectory;

    public JSONPersistence(Path storageDirectory) {
        this.storageDirectory = storageDirectory;
    }

    @Override
    public void save(DesignSession session) {
        if (session == null) {
            throw new IllegalArgumentException("Design session cannot be null");
        }

        try {
            Files.createDirectories(storageDirectory);

            Path filePath = storageDirectory.resolve(
                    session.getId().toString() + ".json"
            );

            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(filePath.toFile(), session);

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to save design session: " + session.getId(), e
            );
        }
    }

    @Override
    public DesignSession load(UUID sessionId) {
        if (sessionId == null) {
            throw new IllegalArgumentException("Session ID cannot be null");
        }

        Path filePath = storageDirectory.resolve(sessionId + ".json");

        try {
            JsonNode root = objectMapper.readTree(filePath.toFile());

            UUID id = UUID.fromString(root.get("id").asText());
            String title = root.get("title").asText();

            DesignType designType = root.get("designType").isNull()
                    ? null
                    : DesignType.valueOf(root.get("designType").asText());

            ProgressState state = ProgressState.valueOf(
                    root.get("state").asText()
            );

            Instant createdAt = objectMapper.convertValue(
                    root.get("createdAt"), Instant.class
            );

            Instant finalizedAt = root.get("finalizedAt").isNull()
                    ? null
                    : objectMapper.convertValue(
                    root.get("finalizedAt"), Instant.class
            );

            Transcript transcript = parseTranscript(root.get("transcript"));
            DesignDraft designDraft = parseDesignDraft(root.get("designDraft"));
            SystemSpecification systemSpecification =
                    root.get("systemSpecification").isNull()
                            ? null
                            : parseSystemSpecification(root.get("systemSpecification"));

            return new DesignSession(
                    id,
                    title,
                    designType,
                    state,
                    createdAt,
                    finalizedAt,
                    transcript,
                    designDraft,
                    systemSpecification
            );

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to load design session: " + sessionId, e
            );
        }
    }

    private TimestampRange parseTimestampRange(JsonNode node) {
        Instant startTimestamp = objectMapper.convertValue(
                node.get("startTimestamp"),
                Instant.class
        );

        Instant endTimestamp = objectMapper.convertValue(
                node.get("endTimestamp"),
                Instant.class
        );

        return new TimestampRange(startTimestamp, endTimestamp);
    }

    private Thought parseThought(JsonNode node) {
        String description = node.get("description").asText();

        TimestampRange timestampRange = parseTimestampRange(
                node.get("timestampRange")
        );

        return new Thought(description, timestampRange);
    }

    private DraftItem parseDraftItem(JsonNode node) {
        UUID id = UUID.fromString(node.get("id").asText());
        String content = node.get("content").asText();

        DraftItem item = new DraftItem(id, content);

        JsonNode thoughtFlowNode = node.get("thoughtFlow");

        for (JsonNode thoughtNode : thoughtFlowNode) {
            Thought thought = parseThought(thoughtNode);
            item.addThought(thought);
        }

        return item;
    }

    private DraftSection parseDraftSection(
            JsonNode node,
            DraftSectionType sectionType
    ) {
        DraftSection section = new DraftSection(sectionType);

        JsonNode itemsNode = node.get("items");

        for (JsonNode itemNode : itemsNode) {
            DraftItem item = parseDraftItem(itemNode);
            section.addItem(item);
        }

        return section;
    }

    private DesignDraft parseDesignDraft(JsonNode node) {
        DesignDraft designDraft = new DesignDraft();

        if (!node.get("goal").isNull()) {
            designDraft.updateGoal(node.get("goal").asText());
        }

        for (DraftSectionType sectionType : DraftSectionType.values()) {
            String fieldName = switch (sectionType) {
                case FUNCTIONAL_REQUIREMENTS -> "functionalRequirements";
                case NON_FUNCTIONAL_REQUIREMENTS -> "nonFunctionalRequirements";
                case ASSUMPTIONS -> "assumptions";
                case DECISIONS -> "decisions";
                case OPEN_QUESTIONS -> "openQuestions";
            };

            DraftSection savedSection = parseDraftSection(
                    node.get(fieldName),
                    sectionType
            );

            DraftSection targetSection = designDraft.getDraftSection(sectionType);

            for (DraftItem item : savedSection.getItems()) {
                targetSection.addItem(item);
            }
        }

        return designDraft;
    }

    private Transcript parseTranscript(JsonNode node) {
        Transcript transcript = new Transcript();

        JsonNode segmentsNode = node.get("segments");

        for (JsonNode segmentNode : segmentsNode) {
            TimestampRange timestampRange = parseTimestampRange(
                    segmentNode.get("timestampRange")
            );

            String text = segmentNode.get("text").asText();

            TranscriptSegment segment = new TranscriptSegment(
                    timestampRange,
                    text
            );

            transcript.addSegment(segment);
        }

        return transcript;
    }

    private DesignComponent parseDesignComponent(JsonNode node) {
        String name = node.get("name").asText();
        String responsibility = node.get("responsibility").asText();
        String technology = node.get("technology").asText();

        return new DesignComponent(
                name,
                responsibility,
                technology
        );
    }

    private API parseAPI(JsonNode node) {
        String name = node.get("name").asText();
        String method = node.get("method").asText();
        String path = node.get("path").asText();
        String purpose = node.get("purpose").asText();

        return new API(
                name,
                method,
                path,
                purpose
        );
    }

    private PerformanceMetric parsePerformanceMetric(JsonNode node) {
        String metric = node.get("metric").asText();
        String value = node.get("value").asText();
        String unit = node.get("unit").asText();

        return new PerformanceMetric(
                metric,
                value,
                unit
        );
    }

    private PerformanceCapacity parsePerformanceCapacity(JsonNode node) {
        List<PerformanceMetric> metrics = new ArrayList<>();

        JsonNode metricsNode = node.get("metrics");

        for (JsonNode metricNode : metricsNode) {
            PerformanceMetric metric = parsePerformanceMetric(metricNode);
            metrics.add(metric);
        }

        return new PerformanceCapacity(metrics);
    }

    private ExternalDependency parseExternalDependency(JsonNode node) {
        String name = node.get("name").asText();
        String purpose = node.get("purpose").asText();
        String requirement = node.get("requirement").asText();

        return new ExternalDependency(
                name,
                purpose,
                requirement
        );
    }

    private SystemSpecification parseSystemSpecification(JsonNode node) {
        List<DesignComponent> components = new ArrayList<>();

        for (JsonNode componentNode : node.get("components")) {
            components.add(parseDesignComponent(componentNode));
        }

        List<API> apis = new ArrayList<>();

        for (JsonNode apiNode : node.get("apis")) {
            apis.add(parseAPI(apiNode));
        }

        PerformanceCapacity performanceCapacity =
                node.get("performanceCapacity").isNull()
                        ? null
                        : parsePerformanceCapacity(node.get("performanceCapacity"));

        List<ExternalDependency> externalDependencies = new ArrayList<>();

        for (JsonNode dependencyNode : node.get("externalDependencies")) {
            externalDependencies.add(parseExternalDependency(dependencyNode));
        }

        return new SystemSpecification(
                components,
                apis,
                performanceCapacity,
                externalDependencies
        );
    }
}