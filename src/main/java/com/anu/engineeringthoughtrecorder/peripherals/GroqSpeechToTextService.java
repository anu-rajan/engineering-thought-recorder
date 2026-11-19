package com.anu.engineeringthoughtrecorder.peripherals;

import com.anu.engineeringthoughtrecorder.domainObjects.Transcript;

import java.net.http.HttpClient;
import java.nio.file.Files;
import java.nio.file.Path;

import java.io.IOException;
import java.net.http.HttpRequest;

import java.net.URI;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.UUID;

import com.anu.engineeringthoughtrecorder.domainObjects.domainUtils.TranscriptSegment;
import com.anu.engineeringthoughtrecorder.domainObjects.domainUtils.TimestampRange;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Instant;

public class GroqSpeechToTextService implements SpeechToTextService {

    private final HttpClient httpClient = HttpClient.newHttpClient();

    private final String apiKey = System.getenv("GROQ_API_KEY");

    @Override
    public Transcript transcribe(AudioInput audioInput) {

        // 1. Validate the supplied audio input.
        if (audioInput == null ||
                audioInput.getFilePath() == null ||
                audioInput.getFilePath().isBlank()) {

            throw new IllegalArgumentException(
                    "Audio input must contain a valid file path"
            );
        }

        // 2. Resolve and validate the audio file.
        Path audioPath = Path.of(audioInput.getFilePath());

        if (!Files.isRegularFile(audioPath) ||
                !Files.isReadable(audioPath)) {

            throw new IllegalArgumentException(
                    "Audio file does not exist or cannot be read: " + audioPath
            );
        }

        // 3. Validate Groq API configuration.
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "GROQ_API_KEY environment variable is not configured"
            );
        }

        // 4. Use the actual recording start time when available.
// Otherwise, use the transcription start time as a temporary fallback
// for older callers that do not provide recording metadata.
        Instant recordingStartTime = audioInput.getRecordingStartTime();

        if (recordingStartTime == null) {
            recordingStartTime = Instant.now();
        }

// 5. Upload the audio file to Groq Whisper.
        String responseBody = callGroqWhisper(audioPath);

// 6. Convert Whisper's timestamped response into our domain model.
        return parseTranscriptionResponse(responseBody, recordingStartTime);
    }


    // Convert Groq Whisper's segment-level timestamps into our Transcript model.
    // Package-private so tests in the same package can verify
    // response parsing without making real Groq API requests.
    Transcript parseTranscriptionResponse(
            String responseBody,
            Instant recordingStartTime
    ) {
        try {
            // 1. Parse the JSON response.
            JsonNode root = new ObjectMapper().readTree(responseBody);
            JsonNode segments = root.path("segments");

            // 2. Verify that Groq returned timestamped segments.
            if (!segments.isArray()) {
                throw new IllegalStateException(
                        "Groq Whisper response does not contain transcript segments"
                );
            }

            Transcript transcript = new Transcript();

            // 3. Convert each Whisper segment into our domain model.
            for (JsonNode segment : segments) {

                // Validate timestamp fields before converting them.
                // Jackson's asDouble() can silently turn missing values into 0.0.
                JsonNode startNode = segment.path("start");
                JsonNode endNode = segment.path("end");
                JsonNode textNode = segment.path("text");

                if (!startNode.isNumber() || !endNode.isNumber()) {
                    throw new IllegalStateException(
                            "Groq Whisper segment contains missing or invalid timestamps"
                    );
                }

                if (!textNode.isTextual()) {
                    throw new IllegalStateException(
                            "Groq Whisper segment contains missing or invalid text"
                    );
                }

                double startSeconds = startNode.asDouble();
                double endSeconds = endNode.asDouble();
                String text = textNode.asText();
                // 4. Reject invalid segment data.
                if (!Double.isFinite(startSeconds)
                        || !Double.isFinite(endSeconds)
                        || startSeconds < 0
                        || endSeconds < startSeconds
                        || text.isBlank()) {

                    throw new IllegalStateException(
                            "Groq Whisper returned an invalid transcript segment"
                    );
                }

                // 5. Convert relative seconds to absolute timestamps.
                Instant startTimestamp = recordingStartTime.plusMillis(
                        Math.round(startSeconds * 1000)
                );

                Instant endTimestamp = recordingStartTime.plusMillis(
                        Math.round(endSeconds * 1000)
                );

                // 6. Add the timestamped segment to the transcript.
                transcript.addSegment(
                        new TranscriptSegment(
                                new TimestampRange(
                                        startTimestamp,
                                        endTimestamp
                                ),
                                text.trim()
                        )
                );
            }

            return transcript;

        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new IllegalStateException(
                    "Failed to parse Groq Whisper transcription response", e
            );
        }
    }
    // Upload an audio recording to Groq Whisper and return its JSON response.
    private String callGroqWhisper(Path audioPath) {

        // 1. Generate a unique multipart boundary for this request.
        String boundary = "Boundary-" + UUID.randomUUID();

        // 2. Build the multipart request containing the audio file.
        HttpRequest.BodyPublisher requestBody =
                buildMultipartBody(audioPath, boundary);

        // 3. Construct the transcription API request.
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(
                        "https://api.groq.com/openai/v1/audio/transcriptions"
                ))
                .header("Authorization", "Bearer " + apiKey)
                .header(
                        "Content-Type",
                        "multipart/form-data; boundary=" + boundary
                )
                .timeout(Duration.ofSeconds(120))
                .POST(requestBody)
                .build();

        try {
            // 4. Send the audio to Groq and receive the transcription response.
            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            // 5. Reject unsuccessful API responses.
            if (response.statusCode() != 200) {
                throw new IllegalStateException(
                        "Groq Whisper returned HTTP "
                                + response.statusCode()
                                + ": " + response.body()
                );
            }

            // 6. Return the JSON response for transcript parsing.
            return response.body();

        } catch (InterruptedException e) {
            // Preserve the thread's interrupted state.
            Thread.currentThread().interrupt();

            throw new IllegalStateException(
                    "Groq Whisper request was interrupted", e
            );

        } catch (java.io.IOException e) {
            throw new IllegalStateException(
                    "Failed to communicate with Groq Whisper", e
            );
        }
    }

    private HttpRequest.BodyPublisher buildMultipartBody(
            Path audioPath,
            String boundary
    ) {
        try {
            // 1. Build the text fields required by Groq Whisper.
            String prefix = "--" + boundary + "\r\n"
                    + "Content-Disposition: form-data; name=\"model\"\r\n\r\n"
                    + "whisper-large-v3-turbo\r\n"

                    + "--" + boundary + "\r\n"
                    + "Content-Disposition: form-data; name=\"response_format\"\r\n\r\n"
                    + "verbose_json\r\n"

                    + "--" + boundary + "\r\n"
                    + "Content-Disposition: form-data; name=\"timestamp_granularities[]\"\r\n\r\n"
                    + "segment\r\n"

                    + "--" + boundary + "\r\n"
                    + "Content-Disposition: form-data; name=\"file\"; filename=\""
                    + audioPath.getFileName()
                    + "\"\r\n"
                    + "Content-Type: application/octet-stream\r\n\r\n";

            // 2. Close the multipart request after the audio bytes.
            String suffix = "\r\n--" + boundary + "--\r\n";

            // 3. Combine the text fields, audio file, and closing boundary.
            return HttpRequest.BodyPublishers.concat(
                    HttpRequest.BodyPublishers.ofString(prefix),
                    HttpRequest.BodyPublishers.ofFile(audioPath),
                    HttpRequest.BodyPublishers.ofString(suffix)
            );

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to prepare audio file for transcription", e
            );
        }
    }
}