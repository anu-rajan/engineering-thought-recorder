package com.anu.engineeringthoughtrecorder.application;

import com.anu.engineeringthoughtrecorder.domainObjects.DesignSession;
import com.anu.engineeringthoughtrecorder.domainObjects.domainUtils.DesignType;
import com.anu.engineeringthoughtrecorder.domainObjects.domainUtils.DraftItem;
import com.anu.engineeringthoughtrecorder.domainObjects.domainUtils.DraftSectionType;
import com.anu.engineeringthoughtrecorder.peripherals.AudioInput;
import com.anu.engineeringthoughtrecorder.peripherals.GroqCaptureDesignAIService;
import com.anu.engineeringthoughtrecorder.peripherals.GroqSpeechToTextService;

import java.time.Instant;

public class DesignCaptureEndToEndIntegrationTestMethods {

    public void testAudioToDesignSession() {

        // 1. Supply the same M4A recording used in the successful
        //    standalone Whisper integration test.
        String audioFilePath = "/Volumes/Official/PortFolio/DesignCaptureService/Design-meeting-sample.m4a";

        // 2. Use a fixed reference time for predictable timestamps.
        Instant recordingStartTime =
                Instant.parse("2026-11-16T10:00:00Z");

        AudioInput audioInput = new AudioInput(
                audioFilePath,
                recordingStartTime
        );

        // 3. Connect the real Whisper and design-analysis services.
        DesignCaptureOrchestrator orchestrator =
                new DesignCaptureOrchestrator(
                        new GroqSpeechToTextService(),
                        new GroqCaptureDesignAIService()
                );

        // 4. Process the recording into a structured design session.
        //    WARNING: This makes real Groq API requests.
        DesignSession session = orchestrator.captureDesignSession(
                "Audio Upload Design",
                DesignType.EXPLORATORY,
                audioInput
        );

        // 5. Verify that the session contains a transcript.
        if (session.getTranscript().getSegments().isEmpty()) {
            throw new AssertionError(
                    "End-to-end capture produced no transcript segments"
            );
        }

        // 6. Print the generated design draft for manual inspection.
        System.out.println("Transcript:");
        session.getTranscript().getSegments().forEach(segment ->
                System.out.println("  " + segment.getText())
        );

        // Verify that the AI created at least one structured design item.
// A successful API response alone does not guarantee a populated draft.
        boolean hasDesignItems = false;

        for (DraftSectionType sectionType : DraftSectionType.values()) {

            if (!session.getDesignDraft()
                    .getDraftSection(sectionType)
                    .getItems()
                    .isEmpty()) {

                hasDesignItems = true;
                break;
            }
        }

        if (!hasDesignItems) {
            throw new AssertionError(
                    "Groq design analysis produced no design items"
            );
        }

        // Print each generated design item, grouped by its section.
// This lets us verify what the AI extracted from the recording.
        System.out.println("Design draft:");

        for (DraftSectionType sectionType : DraftSectionType.values()) {

            for (DraftItem item : session.getDesignDraft()
                    .getDraftSection(sectionType)
                    .getItems()) {

                System.out.println("Section: " + sectionType);
                System.out.println("Item ID: " + item.getId());
                System.out.println("Content: " + item.getContent());
                System.out.println();

                // Verify that the generated design item preserves the original
// spoken statement and its recording timestamp as thought history.
                if (item.getThoughtFlow().isEmpty()) {
                    throw new AssertionError(
                            "Generated design item has no thought history"
                    );
                }
            }
        }
    }
}