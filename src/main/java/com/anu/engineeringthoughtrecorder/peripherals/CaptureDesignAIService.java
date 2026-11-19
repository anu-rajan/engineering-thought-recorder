package com.anu.engineeringthoughtrecorder.peripherals;

import com.anu.engineeringthoughtrecorder.domainObjects.DesignDraft;
import com.anu.engineeringthoughtrecorder.domainObjects.domainUtils.TranscriptSegment;
import com.anu.engineeringthoughtrecorder.peripherals.peripheralUtils.SegmentAIResult;

public interface CaptureDesignAIService {

    SegmentAIResult getDesignDraftDelta(TranscriptSegment segment, DesignDraft designDraft);
}
