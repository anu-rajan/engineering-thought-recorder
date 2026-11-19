package com.anu.engineeringthoughtrecorder.peripherals;

import java.time.Instant;

public class AudioInput {

    private final String filePath;
    private final Instant recordingStartTime;

    // Existing constructor retained for backward compatibility
    // with current tests and callers.
    public AudioInput(String filePath) {
        this(filePath, null);
    }

    // Use this constructor when the actual recording start time is known.
    public AudioInput(String filePath, Instant recordingStartTime) {
        this.filePath = filePath;
        this.recordingStartTime = recordingStartTime;
    }

    public String getFilePath() {
        return filePath;
    }

    public Instant getRecordingStartTime() {
        return recordingStartTime;
    }
}