package com.anu.engineeringthoughtrecorder.application;

import com.anu.engineeringthoughtrecorder.domainObjects.DesignDraft;
import com.anu.engineeringthoughtrecorder.domainObjects.domainUtils.*;
import com.anu.engineeringthoughtrecorder.peripherals.CaptureDesignAIService;
import com.anu.engineeringthoughtrecorder.peripherals.peripheralUtils.SegmentAIResult;

import java.util.List;

public class FakeCaptureDesignAIService implements CaptureDesignAIService {

    @Override
    public SegmentAIResult getDesignDraftDelta(
            TranscriptSegment segment,
            DesignDraft designDraft
    ) {

        String text = segment.getText();

        // 1. Create the initial audio upload requirement.
        if (text.equals(
                "Users should be able to upload audio recordings."
        )) {
            return new SegmentAIResult(
                    DraftSectionType.FUNCTIONAL_REQUIREMENTS,
                    new DraftItem(text)
            );
        }

        // 2. Update the existing audio upload requirement.
        // Reuse its UUID so the orchestrator adds a second Thought.
        if (text.equals(
                "Audio uploads must support WAV and MP3 files."
        )) {
            List<DraftItem> requirements = designDraft
                    .getDraftSection(DraftSectionType.FUNCTIONAL_REQUIREMENTS)
                    .getItems();

            DraftItem existingItem = requirements.stream()
                    .filter(item -> item.getContent().contains("upload audio"))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError(
                            "Expected existing audio upload requirement"
                    ));

            return new SegmentAIResult(
                    DraftSectionType.FUNCTIONAL_REQUIREMENTS,
                    new DraftItem(
                            existingItem.getId(),
                            "Users should be able to upload WAV and MP3 audio files."
                    )
            );
        }

        // 3. Add a separate document export requirement.
        if (text.equals(
                "Users should be able to export their design documents as PDF files."
        )) {
            return new SegmentAIResult(
                    DraftSectionType.FUNCTIONAL_REQUIREMENTS,
                    new DraftItem(text)
            );
        }

        // 4. Record the database technology decision.
        if (text.equals(
                "We have decided to use PostgreSQL as the database."
        )) {
            return new SegmentAIResult(
                    DraftSectionType.DECISIONS,
                    new DraftItem(text)
            );
        }

        // 5. Handle a Day 2 discussion about the existing upload requirement.
// Reuse the original requirement UUID to preserve its thought history.
        if (text.equals(
                "We should review the existing audio upload requirements."
        )) {
            DraftItem existingItem = designDraft
                    .getDraftSection(DraftSectionType.FUNCTIONAL_REQUIREMENTS)
                    .getItems()
                    .stream()
                    .filter(item -> item.getContent().contains("upload WAV and MP3"))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError(
                            "Expected existing audio upload requirement"
                    ));

            return new SegmentAIResult(
                    DraftSectionType.FUNCTIONAL_REQUIREMENTS,
                    new DraftItem(
                            existingItem.getId(),
                            existingItem.getContent()
                    )
            );
        }

        // 6. Fail explicitly if the test uses an unexpected segment.
        // This prevents silently producing incorrect test results.
        throw new IllegalArgumentException(
                "No fake AI response configured for segment: " + text
        );
    }
}