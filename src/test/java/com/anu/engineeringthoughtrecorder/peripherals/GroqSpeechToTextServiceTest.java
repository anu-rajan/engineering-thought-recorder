package com.anu.engineeringthoughtrecorder.peripherals;

public class GroqSpeechToTextServiceTest {

    public static void main(String[] args) {

        // Test Whisper JSON parsing without calling the Groq API.
        GroqSpeechToTextServiceTestMethods tests =
                new GroqSpeechToTextServiceTestMethods();

        // Verify that multiple Whisper segments are converted
        // into transcript segments with correct text and timestamps.
        tests.testValidTranscriptionResponse();
        System.out.println("PASS: Valid transcription response");

        // Verify that a response without a segments array is rejected.
        tests.testMissingSegments();
        System.out.println("PASS: Missing transcript segments rejected");

        // Verify that invalid timestamp ranges are rejected.
        tests.testInvalidTimestampRange();
        System.out.println("PASS: Invalid timestamp range rejected");

        // Verify that missing timestamps are rejected.
        tests.testMissingTimestamp();
        System.out.println("PASS: Missing timestamp rejected");

        // Verify that string timestamps are rejected.
        tests.testMalformedTimestamp();
        System.out.println("PASS: Malformed timestamp rejected");

        // Verify that recording metadata is preserved.
        tests.testAudioInputRecordingStartTime();
        System.out.println("PASS: AudioInput preserves recording start time");

        // Verify that transcript timestamps use the recording start time.
        tests.testTranscriptUsesRecordingStartTime();
        System.out.println("PASS: Transcript uses recording start time");

        System.out.println("All Groq speech-to-text parsing tests passed.");
    }
}