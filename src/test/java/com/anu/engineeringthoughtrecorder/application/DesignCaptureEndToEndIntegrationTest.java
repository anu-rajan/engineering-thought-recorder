package com.anu.engineeringthoughtrecorder.application;

public class DesignCaptureEndToEndIntegrationTest {

    public static void main(String[] args) {

        // This test exercises the complete audio-to-design pipeline.
        // WARNING: It makes real Groq API requests.
        // Run manually, not as part of the normal test suite.

        DesignCaptureEndToEndIntegrationTestMethods tests =
                new DesignCaptureEndToEndIntegrationTestMethods();

        tests.testAudioToDesignSession();

        System.out.println(
                "PASS: End-to-end audio-to-design capture"
        );
    }
}