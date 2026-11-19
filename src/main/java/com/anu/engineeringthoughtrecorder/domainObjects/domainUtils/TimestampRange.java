package com.anu.engineeringthoughtrecorder.domainObjects.domainUtils;

import java.time.Instant;

public class TimestampRange {
    private final Instant startTimestamp;
    private final Instant endTimestamp;

    public TimestampRange(Instant startTimestamp, Instant endTimestamp) {
        if (startTimestamp == null || endTimestamp == null) {
            throw new IllegalArgumentException("Timestamps cannot be null");
        }

        if (endTimestamp.isBefore(startTimestamp)) {
            throw new IllegalArgumentException(
                    "End timestamp cannot be before start timestamp");
        }

        this.startTimestamp = startTimestamp;
        this.endTimestamp = endTimestamp;
    }

    public Instant getStartTimestamp() {
        return startTimestamp;
    }

    public Instant getEndTimestamp() {
        return endTimestamp;
    }
}
