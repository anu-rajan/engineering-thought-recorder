package com.anu.engineeringthoughtrecorder.peripherals.peripheralUtils;

import com.anu.engineeringthoughtrecorder.domainObjects.domainUtils.DraftItem;
import com.anu.engineeringthoughtrecorder.domainObjects.domainUtils.DraftSectionType;

public class SegmentAIResult {

    private final DraftSectionType sectionType;
    private final DraftItem draftItem;

    public SegmentAIResult(
            DraftSectionType sectionType,
            DraftItem draftItem) {

        this.sectionType = sectionType;
        this.draftItem = draftItem;
    }

    public DraftSectionType getSectionType() {
        return sectionType;
    }

    public DraftItem getDraftItem() {
        return draftItem;
    }


}
