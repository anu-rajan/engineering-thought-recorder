package com.anu.engineeringthoughtrecorder.application;

import com.anu.engineeringthoughtrecorder.domainObjects.DesignSession;
import com.anu.engineeringthoughtrecorder.domainObjects.Transcript;
import com.anu.engineeringthoughtrecorder.domainObjects.domainUtils.DesignType;
import com.anu.engineeringthoughtrecorder.domainObjects.domainUtils.ProgressState;
import com.anu.engineeringthoughtrecorder.domainObjects.domainUtils.Thought;
import com.anu.engineeringthoughtrecorder.domainObjects.domainUtils.TranscriptSegment;
import com.anu.engineeringthoughtrecorder.peripherals.peripheralUtils.SegmentAIResult;
import com.anu.engineeringthoughtrecorder.peripherals.AudioInput;
import com.anu.engineeringthoughtrecorder.peripherals.SpeechToTextService;
import com.anu.engineeringthoughtrecorder.peripherals.CaptureDesignAIService;

public class DesignCaptureOrchestrator {

    //private DesignSession designSession;
    private final SpeechToTextService speechToTextService;
    private final CaptureDesignAIService captureDesignAIService;

    public DesignCaptureOrchestrator(SpeechToTextService speechToTextService, CaptureDesignAIService captureDesignAIService) {

        this.speechToTextService = speechToTextService;
        this.captureDesignAIService = captureDesignAIService;
    }


    public DesignSession captureDesignSession(
            String title,
            DesignType designType,
            AudioInput audioInput) {

        DesignSession designSession = new DesignSession(title, designType);

        return processAudio(designSession, audioInput);
    }

    /**
     * Creates a new design session from an existing transcript.
     *
     * This supports meeting notes tools that already provide
     * transcribed speech, avoiding the speech-to-text API call.
     *
     * The transcript must contain timestamped segments so that
     * the original engineering thought history can be preserved.
     */
    public DesignSession captureDesignSessionFromTranscript(
            String title,
            DesignType designType,
            Transcript transcript) {

        DesignSession designSession =
                new DesignSession(title, designType);

        return processTranscript(designSession, transcript);
    }

    public DesignSession continueDesignSession(
            DesignSession designSession,
            AudioInput audioInput) {

        // Validate that the existing session can still be modified.
        validateSessionForContinuation(designSession);

        // Process the new recording using the existing session,
        // preserving its design draft, UUIDs, and previous transcript.
        return processAudio(designSession, audioInput);
    }

    /**
     * Continues an existing design session using a supplied transcript.
     *
     * Supports switching from audio input on one day to transcript
     * input on another day, while preserving the same design session,
     * existing draft items, UUIDs, and thought histories.
     */
    public DesignSession continueDesignSessionFromTranscript(
            DesignSession designSession,
            Transcript transcript) {

        // Apply the same lifecycle validation used for audio continuation.
        validateSessionForContinuation(designSession);

        // Process the new transcript within the existing design session.
        return processTranscript(designSession, transcript);
    }

    /**
     * Validates that a design session can accept additional input.
     *
     * Shared by audio and transcript continuation so both input
     * types enforce the same session lifecycle rules.
     */
    private void validateSessionForContinuation(
            DesignSession designSession) {

        // 1. Validate that an existing session was supplied.
        if (designSession == null) {
            throw new IllegalArgumentException(
                    "Design session cannot be null"
            );
        }

        // 2. Ensure finalized sessions cannot be modified.
        if (designSession.getState() != ProgressState.IN_PROGRESS) {
            throw new IllegalStateException(
                    "Only in-progress design sessions can be continued"
            );
        }
    }

    /**
     * Converts audio into a transcript and delegates to the
     * shared transcript-processing pipeline.
     *
     * Works for both new sessions and multi-day continuation.
     */
    private DesignSession processAudio(
            DesignSession designSession,
            AudioInput audioInput) {

        // Convert the recording into timestamped transcript segments.
        Transcript transcript = speechToTextService.transcribe(audioInput);

        // Process the transcript within the supplied design session.
        return processTranscript(designSession, transcript);
    }

    /**
     * Processes a timestamped transcript within an existing design session.
     *
     * Shared by audio and direct-transcript input.
     * Preserves previous transcripts, draft items, and thought histories.
     */
    private DesignSession processTranscript(
            DesignSession designSession,
            Transcript transcript) {

        // 1. Append the incoming transcript without discarding
        //    transcript segments from previous days.
        designSession.appendTranscript(transcript);

        // 2. Process each segment sequentially so the AI receives
        //    the latest version of the evolving design draft.
        for (TranscriptSegment segment : transcript.getSegments()) {

            // 3. Interpret the segment using the existing design draft.
            SegmentAIResult result =
                    captureDesignAIService.getDesignDraftDelta(
                            segment,
                            designSession.getDesignDraft()
                    );

            // 4. Preserve the original statement and its timestamp.
            Thought thought = new Thought(
                    segment.getText(),
                    segment.getTimestampRange()
            );

            // 5. Add a new draft item or update an existing item,
            //    preserving its chronological thought history.
            designSession.getDesignDraft().updateDesignDraft(
                    result.getSectionType(),
                    result.getDraftItem(),
                    thought
            );
        }

        // 6. Return the same session with its updated transcript and draft.
        return designSession;
    }

}
