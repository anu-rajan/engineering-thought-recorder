package com.anu.engineeringthoughtrecorder.application;

import com.anu.engineeringthoughtrecorder.domainObjects.DesignSession;
import com.anu.engineeringthoughtrecorder.domainObjects.Transcript;
import com.anu.engineeringthoughtrecorder.domainObjects.domainUtils.*;
import com.anu.engineeringthoughtrecorder.peripherals.AudioInput;
import com.anu.engineeringthoughtrecorder.peripherals.FakeSpeechToTextService;

import java.time.Instant;
import java.util.List;

public class DesignCaptureTranscriptTestMethods {

    /**
     * Verifies that a design session can be created directly
     * from a timestamped transcript without transcribing audio.
     *
     * Uses the fake AI service to avoid external API calls.
     */
    public static void testStartSessionFromTranscript() {

        // 1. Create a transcript with a known statement and timestamp.
        Instant start = Instant.parse("2026-11-16T10:00:00Z");
        Instant end = Instant.parse("2026-11-16T10:00:05Z");

        Transcript transcript = new Transcript();
        transcript.addSegment(new TranscriptSegment(
                new TimestampRange(start, end),
                "Users should be able to upload audio recordings."
        ));

        // 2. Create the orchestrator with fake dependencies.
        DesignCaptureOrchestrator orchestrator =
                new DesignCaptureOrchestrator(
                        new FakeSpeechToTextService(transcript), new FakeCaptureDesignAIService()
                );

        // 3. Start a design session directly from the transcript.
        DesignSession session =
                orchestrator.captureDesignSessionFromTranscript(
                        "Audio Upload Design",
                        DesignType.EXPLORATORY,
                        transcript
                );

        // 4. Verify the transcript was preserved.
        if (session.getTranscript().getSegments().size() != 1) {
            throw new AssertionError(
                    "Expected exactly one transcript segment"
            );
        }

        // 5. Verify the AI created a functional requirement.
        List<DraftItem> requirements = session.getDesignDraft()
                .getDraftSection(DraftSectionType.FUNCTIONAL_REQUIREMENTS)
                .getItems();

        if (requirements.size() != 1) {
            throw new AssertionError(
                    "Expected exactly one functional requirement"
            );
        }

        // 6. Verify the original statement is recorded as a Thought.
        DraftItem item = requirements.get(0);

        if (item.getThoughtFlow().size() != 1) {
            throw new AssertionError(
                    "Expected one thought in the requirement history"
            );
        }
    }

    /**
     * Verifies that a second day's transcript updates an existing
     * requirement without losing its UUID or original thought.
     *
     * Uses fake services, so no external API calls are made.
     */
    public static void testContinueSessionFromTranscript() {

        // 1. Create the Day 1 transcript.
        Transcript day1Transcript = new Transcript();

        day1Transcript.addSegment(new TranscriptSegment(
                new TimestampRange(
                        Instant.parse("2026-11-16T10:00:00Z"),
                        Instant.parse("2026-11-16T10:00:05Z")
                ),
                "Users should be able to upload audio recordings."
        ));

        // 2. Create an orchestrator with fake dependencies.
        DesignCaptureOrchestrator orchestrator =
                new DesignCaptureOrchestrator(
                        new FakeSpeechToTextService(day1Transcript),
                        new FakeCaptureDesignAIService()
                );

        // 3. Start the session using Day 1's transcript.
        DesignSession session =
                orchestrator.captureDesignSessionFromTranscript(
                        "Audio Upload Design",
                        DesignType.EXPLORATORY,
                        day1Transcript
                );

        // 4. Remember the original requirement's UUID.
        DraftItem originalItem = session.getDesignDraft()
                .getDraftSection(DraftSectionType.FUNCTIONAL_REQUIREMENTS)
                .getItems()
                .get(0);

        var originalId = originalItem.getId();

        // 5. Create the Day 2 transcript with a revised requirement.
        Transcript day2Transcript = new Transcript();

        day2Transcript.addSegment(new TranscriptSegment(
                new TimestampRange(
                        Instant.parse("2026-11-17T10:00:00Z"),
                        Instant.parse("2026-11-17T10:00:05Z")
                ),
                "Audio uploads must support WAV and MP3 files."
        ));

        // 6. Continue the SAME session using Day 2's transcript.
        DesignSession continuedSession =
                orchestrator.continueDesignSessionFromTranscript(
                        session,
                        day2Transcript
                );

        // 7. Verify the original session object was reused.
        if (continuedSession != session) {
            throw new AssertionError(
                    "Expected continuation to reuse the existing session"
            );
        }

        // 8. Verify both days' transcript segments were preserved.
        if (continuedSession.getTranscript().getSegments().size() != 2) {
            throw new AssertionError(
                    "Expected two transcript segments across both days"
            );
        }

        // 9. Verify the requirement was updated, not duplicated.
        List<DraftItem> requirements = continuedSession.getDesignDraft()
                .getDraftSection(DraftSectionType.FUNCTIONAL_REQUIREMENTS)
                .getItems();

        if (requirements.size() != 1) {
            throw new AssertionError(
                    "Expected one updated requirement, not duplicates"
            );
        }

        DraftItem updatedItem = requirements.get(0);

        // 10. Verify the existing UUID was preserved.
        if (!updatedItem.getId().equals(originalId)) {
            throw new AssertionError(
                    "Expected the original requirement UUID to be preserved"
            );
        }

        // 11. Verify the latest requirement content.
        if (!updatedItem.getContent().equals(
                "Users should be able to upload WAV and MP3 audio files."
        )) {
            throw new AssertionError(
                    "Expected the requirement to reflect Day 2's decision"
            );
        }

        // 12. Verify both thoughts were preserved chronologically.
        if (updatedItem.getThoughtFlow().size() != 2) {
            throw new AssertionError(
                    "Expected both Day 1 and Day 2 thoughts to be preserved"
            );
        }
    }

    /**
     * Verifies that a session started from audio can be continued
     * using a transcript on another day.
     *
     * Uses fake services, so no Groq or Whisper API calls are made.
     */
    public static void testAudioToTranscriptContinuation() {

        // 1. Simulate the transcript produced from Day 1 audio.
        Transcript day1AudioTranscript = new Transcript();

        day1AudioTranscript.addSegment(new TranscriptSegment(
                new TimestampRange(
                        Instant.parse("2026-11-16T10:00:00Z"),
                        Instant.parse("2026-11-16T10:00:05Z")
                ),
                "Users should be able to upload audio recordings."
        ));

        // 2. Configure the fake speech-to-text service.
        DesignCaptureOrchestrator orchestrator =
                new DesignCaptureOrchestrator(
                        new FakeSpeechToTextService(day1AudioTranscript),
                        new FakeCaptureDesignAIService()
                );

        // 3. Start Day 1 using the AUDIO entry point.
        //    The fake service returns our prepared transcript.
        DesignSession session = orchestrator.captureDesignSession(
                "Audio Upload Design",
                DesignType.EXPLORATORY,
                new AudioInput("day1-recording.wav")
        );

        // 4. Remember the original requirement UUID.
        DraftItem originalItem = session.getDesignDraft()
                .getDraftSection(DraftSectionType.FUNCTIONAL_REQUIREMENTS)
                .getItems()
                .get(0);

        var originalId = originalItem.getId();

        // 5. Prepare Day 2's directly supplied transcript.
        Transcript day2Transcript = new Transcript();

        day2Transcript.addSegment(new TranscriptSegment(
                new TimestampRange(
                        Instant.parse("2026-11-17T10:00:00Z"),
                        Instant.parse("2026-11-17T10:00:05Z")
                ),
                "Audio uploads must support WAV and MP3 files."
        ));

        // 6. Continue the existing session using TRANSCRIPT input.
        DesignSession continuedSession =
                orchestrator.continueDesignSessionFromTranscript(
                        session,
                        day2Transcript
                );

        // 7. Verify the same session object was preserved.
        if (continuedSession != session) {
            throw new AssertionError(
                    "Expected continuation to reuse the original session"
            );
        }

        // 8. Verify both days' transcripts were retained.
        if (continuedSession.getTranscript().getSegments().size() != 2) {
            throw new AssertionError(
                    "Expected two transcript segments across both days"
            );
        }

        // 9. Verify the requirement was updated, not duplicated.
        List<DraftItem> requirements = continuedSession.getDesignDraft()
                .getDraftSection(DraftSectionType.FUNCTIONAL_REQUIREMENTS)
                .getItems();

        if (requirements.size() != 1) {
            throw new AssertionError(
                    "Expected one updated requirement"
            );
        }

        DraftItem updatedItem = requirements.get(0);

        // 10. Verify the original UUID was preserved.
        if (!updatedItem.getId().equals(originalId)) {
            throw new AssertionError(
                    "Expected the original requirement UUID to be preserved"
            );
        }

        // 11. Verify the final requirement reflects Day 2's update.
        if (!updatedItem.getContent().equals(
                "Users should be able to upload WAV and MP3 audio files."
        )) {
            throw new AssertionError(
                    "Expected updated WAV and MP3 requirement"
            );
        }

        // 12. Verify both thoughts were preserved.
        if (updatedItem.getThoughtFlow().size() != 2) {
            throw new AssertionError(
                    "Expected two thoughts in the requirement history"
            );
        }
    }

    /**
     * Verifies that a session started from a transcript can be
     * continued using audio on a later day.
     *
     * Audio transcription is simulated, so no Groq calls are made.
     */
    public static void testTranscriptToAudioContinuation() {

        // 1. Prepare Day 1's directly supplied transcript.
        Transcript day1Transcript = new Transcript();

        day1Transcript.addSegment(new TranscriptSegment(
                new TimestampRange(
                        Instant.parse("2026-11-16T10:00:00Z"),
                        Instant.parse("2026-11-16T10:00:05Z")
                ),
                "Users should be able to upload audio recordings."
        ));

        // 2. Prepare the transcript that fake speech-to-text
        //    will return when Day 2 audio is processed.
        Transcript day2AudioTranscript = new Transcript();

        day2AudioTranscript.addSegment(new TranscriptSegment(
                new TimestampRange(
                        Instant.parse("2026-11-17T10:00:00Z"),
                        Instant.parse("2026-11-17T10:00:05Z")
                ),
                "Audio uploads must support WAV and MP3 files."
        ));

        // 3. Configure the fake speech-to-text service to
        //    return Day 2's transcript when audio is supplied.
        DesignCaptureOrchestrator orchestrator =
                new DesignCaptureOrchestrator(
                        new FakeSpeechToTextService(day2AudioTranscript),
                        new FakeCaptureDesignAIService()
                );

        // 4. Start Day 1 directly from the transcript.
        DesignSession session =
                orchestrator.captureDesignSessionFromTranscript(
                        "Audio Upload Design",
                        DesignType.EXPLORATORY,
                        day1Transcript
                );

        // 5. Remember the original requirement's UUID.
        DraftItem originalItem = session.getDesignDraft()
                .getDraftSection(DraftSectionType.FUNCTIONAL_REQUIREMENTS)
                .getItems()
                .get(0);

        var originalId = originalItem.getId();

        // 6. Continue the SAME session using the audio entry point.
        //    No real audio file is needed because transcription is faked.
        DesignSession continuedSession =
                orchestrator.continueDesignSession(
                        session,
                        new AudioInput("day2-recording.wav")
                );

        // 7. Verify the original session object was reused.
        if (continuedSession != session) {
            throw new AssertionError(
                    "Expected continuation to reuse the original session"
            );
        }

        // 8. Verify both days' transcript segments were preserved.
        if (continuedSession.getTranscript().getSegments().size() != 2) {
            throw new AssertionError(
                    "Expected two transcript segments across both days"
            );
        }

        // 9. Verify the requirement was updated, not duplicated.
        List<DraftItem> requirements = continuedSession.getDesignDraft()
                .getDraftSection(DraftSectionType.FUNCTIONAL_REQUIREMENTS)
                .getItems();

        if (requirements.size() != 1) {
            throw new AssertionError(
                    "Expected one updated requirement, not duplicates"
            );
        }

        DraftItem updatedItem = requirements.get(0);

        // 10. Verify the original UUID was preserved.
        if (!updatedItem.getId().equals(originalId)) {
            throw new AssertionError(
                    "Expected original requirement UUID to be preserved"
            );
        }

        // 11. Verify Day 2's requirement replaced the original content.
        if (!updatedItem.getContent().equals(
                "Users should be able to upload WAV and MP3 audio files."
        )) {
            throw new AssertionError(
                    "Expected updated WAV and MP3 requirement"
            );
        }

        // 12. Verify both thoughts were retained.
        if (updatedItem.getThoughtFlow().size() != 2) {
            throw new AssertionError(
                    "Expected both Day 1 and Day 2 thoughts"
            );
        }
    }


    /**
     * Verifies that DesignSessionPrinter can display a session
     * containing a timestamped transcript and a design requirement.
     *
     * Uses fake services, so no Groq API calls are made.
     */
    public static void testSessionPrinter() {

        // 1. Prepare a timestamped engineering discussion.
        Transcript transcript = new Transcript();

        transcript.addSegment(new TranscriptSegment(
                new TimestampRange(
                        Instant.parse("2026-11-16T10:00:00Z"),
                        Instant.parse("2026-11-16T10:00:05Z")
                ),
                "Users should be able to upload audio recordings."
        ));

        // 2. Configure fake dependencies.
        DesignCaptureOrchestrator orchestrator =
                new DesignCaptureOrchestrator(
                        new FakeSpeechToTextService(transcript),
                        new FakeCaptureDesignAIService()
                );

        // 3. Create a real DesignSession using the transcript.
        DesignSession session =
                orchestrator.captureDesignSessionFromTranscript(
                        "Audio Upload Design",
                        DesignType.EXPLORATORY,
                        transcript
                );

        // 4. Print the complete session for visual inspection.
        DesignSessionPrinter.printSession(session);
    }
}