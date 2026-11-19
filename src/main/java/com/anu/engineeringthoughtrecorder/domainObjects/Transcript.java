package com.anu.engineeringthoughtrecorder.domainObjects;

import com.anu.engineeringthoughtrecorder.domainObjects.domainUtils.TranscriptSegment;

import java.util.ArrayList;
import java.util.List;

public class Transcript {
    private final List<TranscriptSegment> segments;

    public Transcript() {
        this.segments = new ArrayList<>();
    }

    public void addSegment(TranscriptSegment segment) {
        if (segment == null) {
            throw new IllegalArgumentException(
                    "Transcript segment cannot be null");
        }

        segments.add(segment);
    }

    public List<TranscriptSegment> getSegments() {
        return segments;
    }
}
