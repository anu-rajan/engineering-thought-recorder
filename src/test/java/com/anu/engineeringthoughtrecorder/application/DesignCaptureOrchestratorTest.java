
package com.anu.engineeringthoughtrecorder.application;

import com.anu.engineeringthoughtrecorder.domainObjects.DesignSession;

public class DesignCaptureOrchestratorTest {

    public static void main(String[] args) {

        // Create a sample transcript and capture a design session using the orchestrator.
        DesignSession session =
                DesignCaptureOrchestratorTestMethods.createCapturedDesignSession();
        System.out.println("PASS: Design session captured successfully");

        // Verify that the captured transcript contains all 4 original segments.
        DesignCaptureOrchestratorTestMethods.assertTranscriptSegmentCount(session, 4);
        System.out.println("PASS: Transcript segment count");

        // Verify that the captured design contains exactly 2 functional requirements.
        DesignCaptureOrchestratorTestMethods.assertFunctionalRequirementCount(session, 2);
        System.out.println("PASS: Functional requirement count");

        // Verify that at least one functional requirement has 2 associated thoughts.
        DesignCaptureOrchestratorTestMethods.assertUpdatedRequirementExists(session);
        System.out.println("PASS: Updated requirement thought history");

        // Verify that the captured design contains exactly 1 design decision.
        DesignCaptureOrchestratorTestMethods.assertDesignDecisionCount(session, 1);
        System.out.println("PASS: Design decision count");

        // Display the captured requirements, thought history, and decisions for inspection.
        DesignCaptureOrchestratorTestMethods.printCapturedDesignDetails(session);

        // Verify that continuing a session preserves its original UUID.
        DesignCaptureOrchestratorTestMethods.testSessionIdPreservedAfterContinuation();
        System.out.println("PASS: Session UUID preserved after continuation");

// Verify that a second recording appends transcript segments
// without removing segments from the first recording.
        DesignCaptureOrchestratorTestMethods.testTranscriptPreservedAfterContinuation();
        System.out.println("PASS: Transcript history preserved after continuation");

        // Verify that continuing a session preserves the existing DesignDraft object.
        DesignCaptureOrchestratorTestMethods.testDesignDraftPreservedAfterContinuation();
        System.out.println("PASS: Design draft preserved after continuation");

        // Verify that Day 2 updates an existing requirement without
// changing its UUID, creating duplicates, or losing thought history.
        DesignCaptureOrchestratorTestMethods.testExistingRequirementUpdatedAfterContinuation();
        System.out.println("PASS: Existing requirement updated after continuation");

        // Verify that a finalized design session cannot be continued.
        DesignCaptureOrchestratorTestMethods.testFinalizedSessionCannotBeContinued();
        System.out.println("PASS: Finalized session continuation rejected");

        // Verify that a session loaded from JSON can be continued
// without losing its original identity or transcript history.
        DesignCaptureOrchestratorTestMethods.testLoadedSessionCanBeContinued();
        System.out.println("PASS: Loaded session continued successfully");

        System.out.println("All DesignCaptureOrchestrator tests passed!");
    }
}
