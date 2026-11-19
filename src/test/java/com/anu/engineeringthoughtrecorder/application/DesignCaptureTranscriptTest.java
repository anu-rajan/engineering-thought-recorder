package com.anu.engineeringthoughtrecorder.application;

public class DesignCaptureTranscriptTest {

    public static void main(String[] args) {

        DesignCaptureTranscriptTestMethods
                .testStartSessionFromTranscript();

        System.out.println(
                "PASS: Start design session from transcript"
        );

        DesignCaptureTranscriptTestMethods
                .testContinueSessionFromTranscript();

        System.out.println(
                "PASS: Continue design session from transcript"
        );

        DesignCaptureTranscriptTestMethods
                .testAudioToTranscriptContinuation();

        System.out.println(
                "PASS: Audio to transcript continuation"
        );

        DesignCaptureTranscriptTestMethods
                .testTranscriptToAudioContinuation();

        System.out.println(
                "PASS: Transcript to audio continuation"
        );

        System.out.println();
        System.out.println("=== SESSION PRINTER TEST ===");

// We will use a separate test method to create
// and print a session without making Groq API calls.
        DesignCaptureTranscriptTestMethods.testSessionPrinter();

    }
}