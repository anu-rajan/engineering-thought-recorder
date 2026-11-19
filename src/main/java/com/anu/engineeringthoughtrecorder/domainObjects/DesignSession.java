package com.anu.engineeringthoughtrecorder.domainObjects;

import com.anu.engineeringthoughtrecorder.domainObjects.domainUtils.DesignType;
import com.anu.engineeringthoughtrecorder.domainObjects.domainUtils.ProgressState;
import com.anu.engineeringthoughtrecorder.domainObjects.domainUtils.TranscriptSegment;

import java.time.Instant;
import java.util.UUID;

public class DesignSession {
    private UUID id;
    private String title;
    private DesignType designType;
    private ProgressState state;
    private Instant createdAt;
    private Instant finalizedAt;

    private Transcript transcript;
    private DesignDraft designDraft;
    private SystemSpecification systemSpecification;

    public DesignSession(String title, DesignType designType) {
        this.id = UUID.randomUUID();
        this.title = title;
        this.designType = designType;
        this.state = ProgressState.IN_PROGRESS;
        this.createdAt = Instant.now();
        this.finalizedAt = null;

        this.transcript = new Transcript();
        this.designDraft = new DesignDraft();
        this.systemSpecification = null;
    }

    public DesignSession(
            UUID id,
            String title,
            DesignType designType,
            ProgressState state,
            Instant createdAt,
            Instant finalizedAt,
            Transcript transcript,
            DesignDraft designDraft,
            SystemSpecification systemSpecification
    ) {
        this.id = id;
        this.title = title;
        this.designType = designType;
        this.state = state;
        this.createdAt = createdAt;
        this.finalizedAt = finalizedAt;
        this.transcript = transcript;
        this.designDraft = designDraft;
        this.systemSpecification = systemSpecification;
    }

    public Transcript getTranscript() {
        return transcript;
    }

    public DesignDraft getDesignDraft() {
        return designDraft;
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public DesignType getDesignType() {
        return designType;
    }

    public ProgressState getState() {
        return state;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getFinalizedAt() {
        return finalizedAt;
    }

    public SystemSpecification getSystemSpecification() {
        return systemSpecification;
    }

    public void populateTranscript(Transcript transcript) {
        if (transcript == null) {
            throw new IllegalArgumentException(
                    "Transcript cannot be null");
        }

        this.transcript = transcript;
    }

    public void markFinal() {
        if (this.state == ProgressState.FINALIZED) {
            throw new IllegalStateException("Design session is already finalized");
        }

        this.state = ProgressState.FINALIZED;
        this.finalizedAt = Instant.now();
    }

    public void setSystemSpecification(SystemSpecification systemSpecification) {
        this.systemSpecification = systemSpecification;
    }


    public void appendTranscript(Transcript newTranscript) {

        // 1. Reject invalid transcript input.
        if (newTranscript == null) {
            throw new IllegalArgumentException(
                    "Transcript cannot be null"
            );
        }

        // 2. Append only the new recording's segments,
        //    preserving all previously recorded segments.
        for (TranscriptSegment segment : newTranscript.getSegments()) {
            this.transcript.addSegment(segment);
        }
    }

}
