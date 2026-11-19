package com.anu.engineeringthoughtrecorder.peripherals;

import com.anu.engineeringthoughtrecorder.domainObjects.Transcript;
import com.anu.engineeringthoughtrecorder.domainObjects.domainUtils.TranscriptSegment;

import java.time.Instant;

public class GroqSpeechToTextIntegrationTest {

    public static void main(String[] args) {

        // Replace this with the absolute path to your sample WAV file.
        String audioFilePath = "/Volumes/Official/PortFolio/DesignCaptureService/Design-meeting-sample.m4a";

        // Use a fixed recording start time so we can inspect
        // the absolute timestamps produced by Whisper.
        Instant recordingStartTime =
                Instant.parse("2026-11-16T10:00:00Z");

        AudioInput audioInput = new AudioInput(
                audioFilePath,
                recordingStartTime
        );

        SpeechToTextService speechToTextService =
                new GroqSpeechToTextService();

        // WARNING: This invokes the real Groq API.
        // Run this test manually, not as part of the normal test suite.
        Transcript transcript = speechToTextService.transcribe(audioInput);

        if (transcript.getSegments().isEmpty()) {
            throw new AssertionError(
                    "Whisper returned no transcript segments"
            );
        }

        // Verify that Whisper's absolute timestamps are valid
// and are anchored to the supplied recording start time.
        for (TranscriptSegment segment : transcript.getSegments()) {

            Instant start = segment.getTimestampRange().getStartTimestamp();
            Instant end = segment.getTimestampRange().getEndTimestamp();

            if (start.isBefore(recordingStartTime)) {
                throw new AssertionError(
                        "Transcript segment starts before the recording"
                );
            }

            if (end.isBefore(start)) {
                throw new AssertionError(
                        "Transcript segment ends before it starts"
                );
            }
        }

        // Display the recognized speech and its absolute timestamps.
        for (TranscriptSegment segment : transcript.getSegments()) {
            System.out.println(
                    segment.getTimestampRange().getStartTimestamp()
                            + " -> "
                            + segment.getTimestampRange().getEndTimestamp()
                            + " | "
                            + segment.getText()
            );
        }

        System.out.println("PASS: Live Groq Whisper transcription");
    }
}