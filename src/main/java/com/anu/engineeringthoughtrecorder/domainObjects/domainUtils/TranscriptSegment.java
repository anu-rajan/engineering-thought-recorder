package com.anu.engineeringthoughtrecorder.domainObjects.domainUtils;

public class TranscriptSegment {
    private final TimestampRange timestampRange;
    private final String text;

    public TranscriptSegment(TimestampRange timestampRange, String text) {
        if (timestampRange == null) {
            throw new IllegalArgumentException(
                    "Timestamp range cannot be null");
        }

        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException(
                    "Transcript text cannot be empty");
        }

        this.timestampRange = timestampRange;
        this.text = text;
    }

    public TimestampRange getTimestampRange() {
        return timestampRange;
    }

    public String getText() {
        return text;
    }
}
