package com.anu.engineeringthoughtrecorder.peripherals;

import com.anu.engineeringthoughtrecorder.domainObjects.Transcript;
import com.anu.engineeringthoughtrecorder.domainObjects.domainUtils.TranscriptSegment;

import java.time.Instant;
import java.util.List;

public class GroqSpeechToTextServiceTestMethods {

    private final GroqSpeechToTextService service =
            new GroqSpeechToTextService();

    private final Instant recordingStartTime =
            Instant.parse("2026-01-10T10:00:00Z");

    // Verify that multiple Whisper segments are converted into
    // transcript segments with correct text and absolute timestamps.
    public void testValidTranscriptionResponse() {

        String responseBody = """
                {
                  "text": "Users can upload audio. WAV files are supported.",
                  "segments": [
                    {
                      "start": 0.0,
                      "end": 2.5,
                      "text": "Users can upload audio."
                    },
                    {
                      "start": 2.5,
                      "end": 5.0,
                      "text": "WAV files are supported."
                    }
                  ]
                }
                """;

        Transcript transcript = service.parseTranscriptionResponse(
                responseBody, recordingStartTime
        );

        List<TranscriptSegment> segments = transcript.getSegments();

        assertEquals(2, segments.size(), "Segment count");

        assertEquals(
                "Users can upload audio.",
                segments.get(0).getText(),
                "First segment text"
        );

        assertEquals(
                Instant.parse("2026-01-10T10:00:00Z"),
                segments.get(0).getTimestampRange().getStartTimestamp(),
                "First segment start"
        );

        assertEquals(
                Instant.parse("2026-01-10T10:00:02.500Z"),
                segments.get(0).getTimestampRange().getEndTimestamp(),
                "First segment end"
        );

        assertEquals(
                "WAV files are supported.",
                segments.get(1).getText(),
                "Second segment text"
        );

        assertEquals(
                Instant.parse("2026-01-10T10:00:02.500Z"),
                segments.get(1).getTimestampRange().getStartTimestamp(),
                "Second segment start"
        );

        assertEquals(
                Instant.parse("2026-01-10T10:00:05Z"),
                segments.get(1).getTimestampRange().getEndTimestamp(),
                "Second segment end"
        );
    }

    // Verify that Whisper responses must contain a segments array.
    public void testMissingSegments() {

        String responseBody = """
                {
                  "text": "Users can upload audio."
                }
                """;

        assertThrows(
                () -> service.parseTranscriptionResponse(
                        responseBody, recordingStartTime
                ),
                "Missing segments"
        );
    }

    // Verify that a segment cannot end before it starts.
    public void testInvalidTimestampRange() {

        String responseBody = """
                {
                  "segments": [
                    {
                      "start": 5.0,
                      "end": 2.0,
                      "text": "Invalid timestamp range."
                    }
                  ]
                }
                """;

        assertThrows(
                () -> service.parseTranscriptionResponse(
                        responseBody, recordingStartTime
                ),
                "Invalid timestamp range"
        );
    }

    // Compare expected and actual values without requiring JUnit.
    private void assertEquals(
            Object expected,
            Object actual,
            String description
    ) {
        if (!expected.equals(actual)) {
            throw new AssertionError(
                    description + ": expected " + expected
                            + ", but got " + actual
            );
        }
    }

    // Verify that an operation throws IllegalStateException.
    private void assertThrows(Runnable action, String description) {
        try {
            action.run();
        } catch (IllegalStateException expected) {
            return;
        }

        throw new AssertionError(
                description + ": expected IllegalStateException"
        );
    }
    // Verify that missing timestamp fields are rejected rather than
// silently converted to 0.0 by Jackson.
    public void testMissingTimestamp() {

        String responseBody = """
            {
              "segments": [
                {
                  "end": 3.0,
                  "text": "Users can upload audio."
                }
              ]
            }
            """;

        assertThrows(
                () -> service.parseTranscriptionResponse(
                        responseBody, recordingStartTime
                ),
                "Missing start timestamp"
        );
    }

    // Verify that timestamps must be JSON numbers, not strings.
// Even a numeric-looking string should be rejected.
    public void testMalformedTimestamp() {

        String responseBody = """
            {
              "segments": [
                {
                  "start": "2.5",
                  "end": 4.0,
                  "text": "Users can upload audio."
                }
              ]
            }
            """;

        assertThrows(
                () -> service.parseTranscriptionResponse(
                        responseBody, recordingStartTime
                ),
                "Malformed start timestamp"
        );
    }

    // Verify that AudioInput preserves the actual recording start time
// so transcription can use it when converting relative timestamps.
    public void testAudioInputRecordingStartTime() {

        Instant expectedStartTime =
                Instant.parse("2026-01-10T10:00:00Z");

        AudioInput audioInput = new AudioInput(
                "meeting-recording.wav",
                expectedStartTime
        );

        assertEquals(
                expectedStartTime,
                audioInput.getRecordingStartTime(),
                "AudioInput recording start time"
        );
    }

    // Verify that Whisper's relative timestamps are converted using
// the recording start time supplied through AudioInput.
    public void testTranscriptUsesRecordingStartTime() {

        Instant actualRecordingStart =
                Instant.parse("2026-01-10T09:30:00Z");

        AudioInput audioInput = new AudioInput(
                "meeting-recording.wav",
                actualRecordingStart
        );

        String responseBody = """
            {
              "segments": [
                {
                  "start": 12.5,
                  "end": 16.0,
                  "text": "We decided to use PostgreSQL."
                }
              ]
            }
            """;

        Transcript transcript = service.parseTranscriptionResponse(
                responseBody,
                audioInput.getRecordingStartTime()
        );

        TranscriptSegment segment = transcript.getSegments().get(0);

        assertEquals(
                Instant.parse("2026-01-10T09:30:12.500Z"),
                segment.getTimestampRange().getStartTimestamp(),
                "Absolute segment start timestamp"
        );

        assertEquals(
                Instant.parse("2026-01-10T09:30:16Z"),
                segment.getTimestampRange().getEndTimestamp(),
                "Absolute segment end timestamp"
        );
    }
}