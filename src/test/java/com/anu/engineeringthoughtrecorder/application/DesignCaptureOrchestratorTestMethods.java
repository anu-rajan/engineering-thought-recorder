
package com.anu.engineeringthoughtrecorder.application;

import com.anu.engineeringthoughtrecorder.domainObjects.DesignDraft;
import com.anu.engineeringthoughtrecorder.domainObjects.DesignSession;
import com.anu.engineeringthoughtrecorder.domainObjects.Transcript;
import com.anu.engineeringthoughtrecorder.domainObjects.domainUtils.*;
import com.anu.engineeringthoughtrecorder.peripherals.CaptureDesignAIService;
import com.anu.engineeringthoughtrecorder.peripherals.FakeSpeechToTextService;
import com.anu.engineeringthoughtrecorder.peripherals.GroqCaptureDesignAIService;
import com.anu.engineeringthoughtrecorder.peripherals.peripheralUtils.SegmentAIResult;
import com.anu.engineeringthoughtrecorder.persistence.JSONPersistence;

import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;

public class DesignCaptureOrchestratorTestMethods {

    // Create the sample transcript, configure services, and capture a design session.
    public static DesignSession createCapturedDesignSession() {
        Transcript transcript = createSampleTranscript();

        FakeSpeechToTextService speechToTextService =
                new FakeSpeechToTextService(transcript);

        // Use deterministic fake AI responses instead of calling Groq.
// This keeps orchestrator tests fast, repeatable, and free of API usage.
        CaptureDesignAIService aiService =
                new FakeCaptureDesignAIService();

        DesignCaptureOrchestrator orchestrator =
                new DesignCaptureOrchestrator(
                        speechToTextService,
                        aiService
                );

        return orchestrator.captureDesignSession(
                "Audio Upload Design",
                null,
                null
        );
    }

    // Create a transcript containing the four original engineering statements.
    private static Transcript createSampleTranscript() {
        Transcript transcript = new Transcript();

        transcript.addSegment(createSegment(
                "2026-10-28T10:00:00Z",
                "2026-10-28T10:00:10Z",
                "Users should be able to upload audio recordings."
        ));

        transcript.addSegment(createSegment(
                "2026-10-28T10:00:10Z",
                "2026-10-28T10:00:20Z",
                "Audio uploads must support WAV and MP3 files."
        ));

        transcript.addSegment(createSegment(
                "2026-10-28T10:00:20Z",
                "2026-10-28T10:00:30Z",
                "Users should be able to export their design documents as PDF files."
        ));

        transcript.addSegment(createSegment(
                "2026-10-28T10:00:30Z",
                "2026-10-28T10:00:40Z",
                "We have decided to use PostgreSQL as the database."
        ));

        return transcript;
    }

    // Build a transcript segment from its timestamps and spoken text.
    private static TranscriptSegment createSegment(
            String start,
            String end,
            String text
    ) {
        return new TranscriptSegment(
                new TimestampRange(
                        Instant.parse(start),
                        Instant.parse(end)
                ),
                text
        );
    }

    // Verify that all original transcript segments are retained.
    public static void assertTranscriptSegmentCount(
            DesignSession session,
            int expectedCount
    ) {
        int actualCount = session.getTranscript()
                .getSegments()
                .size();

        assertCount(
                "transcript segments",
                expectedCount,
                actualCount
        );
    }

    // Verify the number of extracted functional requirements.
    public static void assertFunctionalRequirementCount(
            DesignSession session,
            int expectedCount
    ) {
        int actualCount = session.getDesignDraft()
                .getDraftSection(DraftSectionType.FUNCTIONAL_REQUIREMENTS)
                .getItems()
                .size();

        assertCount(
                "functional requirements",
                expectedCount,
                actualCount
        );
    }

    // Verify that at least one requirement has an updated thought history.
    public static void assertUpdatedRequirementExists(
            DesignSession session
    ) {
        boolean hasUpdatedRequirement = session.getDesignDraft()
                .getDraftSection(DraftSectionType.FUNCTIONAL_REQUIREMENTS)
                .getItems()
                .stream()
                .anyMatch(item -> item.getThoughtFlow().size() == 2);

        if (!hasUpdatedRequirement) {
            throw new AssertionError(
                    "Expected an updated requirement with 2 thoughts"
            );
        }
    }

    // Verify the number of extracted design decisions.
    public static void assertDesignDecisionCount(
            DesignSession session,
            int expectedCount
    ) {
        int actualCount = session.getDesignDraft()
                .getDraftSection(DraftSectionType.DECISIONS)
                .getItems()
                .size();

        assertCount(
                "design decisions",
                expectedCount,
                actualCount
        );
    }

    // Compare expected and actual counts with a descriptive failure message.
    private static void assertCount(
            String description,
            int expected,
            int actual
    ) {
        if (expected != actual) {
            throw new AssertionError(
                    "Expected " + expected + " " + description
                            + ", but found " + actual
            );
        }
    }

    // Print captured requirements, thought histories, and decisions.
    public static void printCapturedDesignDetails(
            DesignSession session
    ) {
        System.out.println("Captured segments: "
                + session.getTranscript().getSegments().size());

        System.out.println("Functional requirements:");

        session.getDesignDraft()
                .getDraftSection(DraftSectionType.FUNCTIONAL_REQUIREMENTS)
                .getItems()
                .forEach(item -> {
                    System.out.println("ID: " + item.getId());
                    System.out.println("Content: " + item.getContent());
                    System.out.println("Thought count: "
                            + item.getThoughtFlow().size());

                    item.getThoughtFlow().forEach(thought ->
                            System.out.println(
                                    "Thought: " + thought.getDescription()
                            )
                    );
                });

        System.out.println("Decisions:");

        session.getDesignDraft()
                .getDraftSection(DraftSectionType.DECISIONS)
                .getItems()
                .forEach(item ->
                        System.out.println(
                                "Content: " + item.getContent()
                        )
                );
    }


    // Verify that continuing a session preserves its original UUID.
    public static void testSessionIdPreservedAfterContinuation() {

        // Create a session using the existing Day 1 test setup.
        DesignSession session = createCapturedDesignSession();
        UUID originalSessionId = session.getId();

        // Prepare a second recording with an empty transcript.
        // This isolates session identity from AI processing.
        Transcript secondTranscript = new Transcript();

        DesignCaptureOrchestrator orchestrator =
                createOrchestrator(secondTranscript);

        // Continue processing using the existing session.
        DesignSession continuedSession =
                orchestrator.continueDesignSession(session, null);

        if (!originalSessionId.equals(continuedSession.getId())) {
            throw new AssertionError(
                    "Session UUID changed after continuation"
            );
        }
    }

    // Verify that continuing a session appends new transcript segments
    // without removing the segments captured during Day 1.
    public static void testTranscriptPreservedAfterContinuation() {

        DesignSession session = createCapturedDesignSession();

        int originalCount =
                session.getTranscript().getSegments().size();

        Transcript secondTranscript = new Transcript();

        secondTranscript.addSegment(createSegment(
                "2026-10-29T10:00:00Z",
                "2026-10-29T10:00:10Z",
                "We should review the existing audio upload requirements."
        ));

        // Use the existing session and process the second recording.
        DesignCaptureOrchestrator orchestrator =
                createOrchestrator(secondTranscript);

        orchestrator.continueDesignSession(session, null);

        assertTranscriptSegmentCount(session, originalCount + 1);

        // Verify that the original first segment is still present.
        String originalFirstSegment =
                createSampleTranscript().getSegments().get(0).getText();

        String actualFirstSegment =
                session.getTranscript().getSegments().get(0).getText();

        if (!originalFirstSegment.equals(actualFirstSegment)) {
            throw new AssertionError(
                    "Original transcript segment was lost after continuation"
            );
        }
    }

    // Create an orchestrator using a supplied transcript.
    private static DesignCaptureOrchestrator createOrchestrator(
            Transcript transcript
    ) {
        FakeSpeechToTextService speechToTextService =
                new FakeSpeechToTextService(transcript);

        // Use deterministic fake AI responses instead of calling Groq.
// This keeps orchestrator tests fast, repeatable, and free of API usage.
        CaptureDesignAIService aiService =
                new FakeCaptureDesignAIService();

        return new DesignCaptureOrchestrator(
                speechToTextService,
                aiService
        );
    }

    // Verify that continuation reuses the existing design draft.
    public static void testDesignDraftPreservedAfterContinuation() {

        // 1. Capture the original session.
        DesignSession session = createCapturedDesignSession();

        // 2. Remember the original design draft object.
        DesignDraft originalDraft = session.getDesignDraft();

        // 3. Continue the session with an empty transcript.
        //    This isolates draft preservation from AI-generated changes.
        DesignCaptureOrchestrator orchestrator =
                createOrchestrator(new Transcript());

        DesignSession continuedSession =
                orchestrator.continueDesignSession(session, null);

        // 4. Verify that the same draft object is retained.
        if (continuedSession.getDesignDraft() != originalDraft) {
            throw new AssertionError(
                    "Design draft was replaced during session continuation"
            );
        }

        // 5. Verify that the original functional requirements remain.
        assertFunctionalRequirementCount(continuedSession, 2);
    }


    // Verify that Day 2 updates an existing requirement while preserving
    // its UUID, avoiding duplicates, and retaining its thought history.
    public static void testExistingRequirementUpdatedAfterContinuation() {

        // 1. Create a Day 1 session without calling the real AI.
        DesignSession session = new DesignSession(
                "Multi-day Design Session",
                null
        );

// Create an existing functional requirement.
        DraftItem day1Item = new DraftItem(
                "Users should be able to upload audio recordings."
        );

// Record the original Day 1 discussion.
        Thought day1Thought = new Thought(
                "Users should be able to upload audio recordings.",
                new TimestampRange(
                        Instant.parse("2026-10-28T10:00:00Z"),
                        Instant.parse("2026-10-28T10:00:10Z")
                )
        );

// Add the requirement and its original thought to the draft.
        session.getDesignDraft().updateDesignDraft(
                DraftSectionType.FUNCTIONAL_REQUIREMENTS,
                day1Item,
                day1Thought
        );

        // 2. Select an existing functional requirement and record its identity.
        DraftItem existingItem = session.getDesignDraft()
                .getDraftSection(DraftSectionType.FUNCTIONAL_REQUIREMENTS)
                .getItems()
                .get(0);

        UUID originalItemId = existingItem.getId();
        int originalThoughtCount = existingItem.getThoughtFlow().size();
        int originalRequirementCount = session.getDesignDraft()
                .getDraftSection(DraftSectionType.FUNCTIONAL_REQUIREMENTS)
                .getItems()
                .size();

        // 3. Prepare a Day 2 statement with a later timestamp.
        Transcript day2Transcript = new Transcript();

        day2Transcript.addSegment(createSegment(
                "2026-10-29T10:00:00Z",
                "2026-10-29T10:00:10Z",
                "The existing requirement has been updated."
        ));

        // 4. Use a deterministic AI response that targets the
        //    original requirement UUID with updated content.
        CaptureDesignAIService fakeAIService = (segment, designDraft) ->
                new SegmentAIResult(
                        DraftSectionType.FUNCTIONAL_REQUIREMENTS,
                        new DraftItem(
                                originalItemId,
                                "Updated requirement from Day 2"
                        )
                );

        FakeSpeechToTextService speechToTextService =
                new FakeSpeechToTextService(day2Transcript);

        DesignCaptureOrchestrator orchestrator =
                new DesignCaptureOrchestrator(
                        speechToTextService,
                        fakeAIService
                );

        // 5. Continue the existing session using Day 2's recording.
        orchestrator.continueDesignSession(session, null);

        // 6. Verify that the requirement count has not increased.
        assertFunctionalRequirementCount(
                session,
                originalRequirementCount
        );

        // 7. Find the original requirement using its UUID.
        DraftItem updatedItem = session.getDesignDraft()
                .getDraftSection(DraftSectionType.FUNCTIONAL_REQUIREMENTS)
                .getItems()
                .stream()
                .filter(item -> originalItemId.equals(item.getId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        "Original requirement UUID was lost"
                ));

        // 8. Verify that the existing requirement's content was updated.
        if (!"Updated requirement from Day 2".equals(
                updatedItem.getContent())) {
            throw new AssertionError(
                    "Existing requirement content was not updated"
            );
        }

        // 9. Verify that the original thought history was preserved
        //    and exactly one new Day 2 thought was appended.
        assertCount(
                "thoughts for updated requirement",
                originalThoughtCount + 1,
                updatedItem.getThoughtFlow().size()
        );

        if (!"The existing requirement has been updated.".equals(
                updatedItem.getThoughtFlow()
                        .get(updatedItem.getThoughtFlow().size() - 1)
                        .getDescription())) {
            throw new AssertionError(
                    "Day 2 thought was not appended correctly"
            );
        }
    }

    // Verify that finalized sessions cannot accept additional recordings.
    public static void testFinalizedSessionCannotBeContinued() {

        // 1. Create a session without invoking Groq.
        DesignSession session = new DesignSession(
                "Finalized Design Session",
                null
        );

        // 2. Finalize the session.
        session.markFinal();

        // 3. Create an orchestrator with an empty transcript.
        DesignCaptureOrchestrator orchestrator =
                createOrchestrator(new Transcript());

        // 4. Verify that continuation is rejected.
        try {
            orchestrator.continueDesignSession(session, null);

            throw new AssertionError(
                    "Expected finalized session continuation to be rejected"
            );

        } catch (IllegalStateException expected) {
            // Expected behavior: finalized sessions cannot be continued.
        }
    }


    // Verify that a saved design session can be loaded and continued
    // with a second recording while preserving its existing data.
    public static void testLoadedSessionCanBeContinued() {

        // 1. Create a Day 1 session without invoking Groq.
        DesignSession day1Session = new DesignSession(
                "Multi-day Persistence Test",
                null
        );

        Transcript day1Transcript = new Transcript();
        day1Transcript.addSegment(createSegment(
                "2026-10-28T10:00:00Z",
                "2026-10-28T10:00:10Z",
                "Users should be able to upload audio recordings."
        ));

        day1Session.appendTranscript(day1Transcript);
        // Create the Day 1 requirement with its original thought.
// This simulates the design draft produced during the first recording.
        DraftItem day1Requirement = new DraftItem(
                "Users should be able to upload audio recordings."
        );

        Thought day1Thought = new Thought(
                day1Transcript.getSegments().get(0).getText(),
                day1Transcript.getSegments().get(0).getTimestampRange()
        );

// Attach the requirement and its thought to the Day 1 design draft.
        day1Session.getDesignDraft().updateDesignDraft(
                DraftSectionType.FUNCTIONAL_REQUIREMENTS,
                day1Requirement,
                day1Thought
        );

// Remember the original UUID so we can verify it survives
// JSON serialization, reloading, and Day 2 updates.
        UUID originalRequirementId = day1Requirement.getId();

        UUID originalSessionId = day1Session.getId();

        // 2. Save Day 1's session to JSON.
        JSONPersistence persistence = new JSONPersistence(
                Path.of("data", "test-sessions")
        );

        persistence.save(day1Session);

        // 3. Load the saved session to simulate starting Day 2.
        DesignSession day2Session = persistence.load(originalSessionId);

        // 4. Prepare Day 2's recording.
        Transcript day2Transcript = new Transcript();
        day2Transcript.addSegment(createSegment(
                "2026-10-29T10:00:00Z",
                "2026-10-29T10:00:10Z",
                "Users should also be able to upload FLAC files."
        ));

        // 5. Simulate AI updating the existing Day 1 requirement.
// Reuse the original UUID so the orchestrator preserves
// the requirement's identity and appends the Day 2 thought.
        CaptureDesignAIService fakeAIService = (segment, designDraft) ->
                new SegmentAIResult(
                        DraftSectionType.FUNCTIONAL_REQUIREMENTS,
                        new DraftItem(
                                originalRequirementId,
                                "Users should be able to upload audio recordings, including FLAC files."
                        )
                );

        DesignCaptureOrchestrator orchestrator =
                new DesignCaptureOrchestrator(
                        new FakeSpeechToTextService(day2Transcript),
                        fakeAIService
                );

        // 6. Continue the loaded session and save its updated state.
        orchestrator.continueDesignSession(day2Session, null);
        persistence.save(day2Session);

        // 7. Reload the updated session to verify persistence.
        DesignSession reloadedSession =
                persistence.load(originalSessionId);

        if (!originalSessionId.equals(reloadedSession.getId())) {
            throw new AssertionError(
                    "Session UUID changed after saving and continuing"
            );
        }

        assertTranscriptSegmentCount(reloadedSession, 2);
        assertFunctionalRequirementCount(reloadedSession, 1);

        // 8. Retrieve the requirement after the second JSON reload.
        DraftItem reloadedRequirement = reloadedSession.getDesignDraft()
                .getDraftSection(DraftSectionType.FUNCTIONAL_REQUIREMENTS)
                .getItems()
                .get(0);

// 9. Verify the original Day 1 requirement UUID was preserved.
        if (!originalRequirementId.equals(reloadedRequirement.getId())) {
            throw new AssertionError(
                    "Requirement UUID changed after Day 2 continuation"
            );
        }

// 10. Verify Day 2 updated the existing requirement's content.
        if (!"Users should be able to upload audio recordings, including FLAC files."
                .equals(reloadedRequirement.getContent())) {
            throw new AssertionError(
                    "Updated requirement content was not persisted"
            );
        }

// 11. Verify both Day 1 and Day 2 thoughts survived JSON persistence.
        assertCount(
                "thoughts after multi-day JSON persistence",
                2,
                reloadedRequirement.getThoughtFlow().size()
        );

// 12. Verify the thoughts remain in chronological order.
        if (!"Users should be able to upload audio recordings."
                .equals(reloadedRequirement.getThoughtFlow().get(0).getDescription())) {
            throw new AssertionError("Day 1 thought was not preserved");
        }

        if (!"Users should also be able to upload FLAC files."
                .equals(reloadedRequirement.getThoughtFlow().get(1).getDescription())) {
            throw new AssertionError("Day 2 thought was not preserved");
        }

        // 8. Verify that both recordings are retained in order.
        if (!"Users should be able to upload audio recordings."
                .equals(reloadedSession.getTranscript()
                        .getSegments().get(0).getText())) {
            throw new AssertionError(
                    "Day 1 transcript was not preserved"
            );
        }

        if (!"Users should also be able to upload FLAC files."
                .equals(reloadedSession.getTranscript()
                        .getSegments().get(1).getText())) {
            throw new AssertionError(
                    "Day 2 transcript was not persisted"
            );
        }
    }



}
