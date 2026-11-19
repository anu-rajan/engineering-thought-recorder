
package com.anu.engineeringthoughtrecorder.peripherals;

import com.anu.engineeringthoughtrecorder.domainObjects.DesignDraft;
import com.anu.engineeringthoughtrecorder.domainObjects.domainUtils.*;
import com.anu.engineeringthoughtrecorder.peripherals.peripheralUtils.SegmentAIResult;

import java.time.Instant;
import java.util.UUID;

public class GroqCaptureDesignAITestMethods {

    private static final String EXISTING_REQUIREMENT =
            "Users can upload audio recordings.";

    private static final String NEW_INFORMATION =
            "Audio uploads must support WAV and MP3 files.";

    // Verify that new information updates an existing requirement.
    public static void testExistingRequirementUpdate() {
        GroqCaptureDesignAIService service =
                new GroqCaptureDesignAIService();

        DesignDraft draft = new DesignDraft();
        draft.addFunctionalRequirement(EXISTING_REQUIREMENT);

        DraftItem existingItem = draft
                .getDraftSection(DraftSectionType.FUNCTIONAL_REQUIREMENTS)
                .getItems()
                .get(0);

        UUID existingId = existingItem.getId();

        TranscriptSegment segment = createSegment();

        SegmentAIResult result =
                service.getDesignDraftDelta(segment, draft);

        assertFunctionalRequirement(result);

        if (!existingId.equals(result.getDraftItem().getId())) {
            throw new AssertionError(
                    "Expected Groq to reuse the existing requirement ID"
            );
        }

        applyResult(draft, segment, result);

        var items = draft
                .getDraftSection(DraftSectionType.FUNCTIONAL_REQUIREMENTS)
                .getItems();

        if (items.size() != 1) {
            throw new AssertionError(
                    "Expected 1 updated requirement, but found "
                            + items.size()
            );
        }

        DraftItem updatedItem = items.get(0);

        if (!existingId.equals(updatedItem.getId())) {
            throw new AssertionError(
                    "Updated requirement did not preserve its original ID"
            );
        }

        if (!updatedItem.getContent().contains("WAV")
                || !updatedItem.getContent().contains("MP3")) {
            throw new AssertionError(
                    "Updated requirement does not contain WAV and MP3"
            );
        }
    }

    // Verify that Groq creates a requirement when no matching item exists.
    public static void testNewRequirementCreation() {
        GroqCaptureDesignAIService service =
                new GroqCaptureDesignAIService();

        DesignDraft draft = new DesignDraft();
        TranscriptSegment segment = createSegment();

        SegmentAIResult result =
                service.getDesignDraftDelta(segment, draft);

        assertFunctionalRequirement(result);

        applyResult(draft, segment, result);

        var items = draft
                .getDraftSection(DraftSectionType.FUNCTIONAL_REQUIREMENTS)
                .getItems();

        if (items.size() != 1) {
            throw new AssertionError(
                    "Expected 1 newly created requirement, but found "
                            + items.size()
            );
        }

        DraftItem newItem = items.get(0);

        if (!newItem.getContent().contains("WAV")
                || !newItem.getContent().contains("MP3")) {
            throw new AssertionError(
                    "New requirement does not contain WAV and MP3"
            );
        }

        if (newItem.getThoughtFlow().size() != 1) {
            throw new AssertionError(
                    "Expected the new requirement to contain 1 thought, but found "
                            + newItem.getThoughtFlow().size()
            );
        }
    }

    // Create the transcript segment used in both test scenarios.
    private static TranscriptSegment createSegment() {
        return new TranscriptSegment(
                new TimestampRange(
                        Instant.parse("2026-10-28T10:00:00Z"),
                        Instant.parse("2026-10-28T10:00:10Z")
                ),
                NEW_INFORMATION
        );
    }

    // Apply Groq's result to the design draft with its source thought.
    private static void applyResult(
            DesignDraft draft,
            TranscriptSegment segment,
            SegmentAIResult result
    ) {
        Thought thought = new Thought(
                segment.getText(),
                segment.getTimestampRange()
        );

        draft.updateDesignDraft(
                result.getSectionType(),
                result.getDraftItem(),
                thought
        );
    }

    // Verify that Groq classified the segment as a functional requirement.
    private static void assertFunctionalRequirement(
            SegmentAIResult result
    ) {
        if (result == null) {
            throw new AssertionError(
                    "Groq returned a null design draft result"
            );
        }

        if (result.getSectionType()
                != DraftSectionType.FUNCTIONAL_REQUIREMENTS) {
            throw new AssertionError(
                    "Expected FUNCTIONAL_REQUIREMENTS, but found "
                            + result.getSectionType()
            );
        }

        if (result.getDraftItem() == null) {
            throw new AssertionError(
                    "Groq returned a null draft item"
            );
        }
    }
}
