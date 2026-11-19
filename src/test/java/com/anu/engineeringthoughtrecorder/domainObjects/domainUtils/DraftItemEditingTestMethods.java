
package com.anu.engineeringthoughtrecorder.domainObjects.domainUtils;

import java.time.Instant;
import java.util.UUID;

public class DraftItemEditingTestMethods {

    // Verify that valid content replaces the original content.
    public static void testContentUpdate() {
        DraftItem item = createDraftItem();

        item.updateContent("Updated requirement");

        assertEquals(
                "Updated requirement",
                item.getContent(),
                "Draft item content was not updated"
        );
    }

    // Verify that updating content does not change the draft item's UUID.
    public static void testIdPreservation() {
        DraftItem item = createDraftItem();
        UUID originalId = item.getId();

        item.updateContent("Updated requirement");

        assertEquals(
                originalId,
                item.getId(),
                "Draft item ID changed after editing"
        );
    }

    // Verify that blank content cannot be used to update a draft item.
    public static void testBlankContentRejection() {
        DraftItem item = createDraftItem();

        assertThrows(
                IllegalArgumentException.class,
                () -> item.updateContent("   "),
                "Expected blank content to be rejected"
        );
    }

    // Verify that a failed update leaves the previous content unchanged.
    public static void testContentPreservationAfterInvalidUpdate() {
        DraftItem item = createDraftItem();

        item.updateContent("Updated requirement");

        assertThrows(
                IllegalArgumentException.class,
                () -> item.updateContent("   "),
                "Expected invalid content to be rejected"
        );

        assertEquals(
                "Updated requirement",
                item.getContent(),
                "Invalid update unexpectedly changed the content"
        );
    }

    // Verify that content editing preserves the existing thought history.
    public static void testThoughtHistoryPreservation() {
        DraftItem item = createDraftItem();

        Thought thought = createThought();
        item.addThought(thought);

        int originalThoughtCount = item.getThoughtFlow().size();

        item.updateContent("Requirement updated again");

        assertEquals(
                originalThoughtCount,
                item.getThoughtFlow().size(),
                "Thought history size changed after editing"
        );

        if (item.getThoughtFlow().get(0) != thought) {
            throw new AssertionError(
                    "Original thought object was not preserved after editing"
            );
        }
    }

    // Create a reusable draft item for individual test scenarios.
    private static DraftItem createDraftItem() {
        return new DraftItem("Original requirement");
    }

    // Create a reusable thought with a fixed timestamp range.
    private static Thought createThought() {
        TimestampRange timestampRange = new TimestampRange(
                Instant.parse("2026-10-28T10:00:00Z"),
                Instant.parse("2026-10-28T10:00:10Z")
        );

        return new Thought(
                "Original requirement was discussed.",
                timestampRange
        );
    }

    // Compare expected and actual values with a descriptive failure message.
    private static void assertEquals(
            Object expected,
            Object actual,
            String message
    ) {
        if (!java.util.Objects.equals(expected, actual)) {
            throw new AssertionError(
                    message
                            + ". Expected: " + expected
                            + ", Actual: " + actual
            );
        }
    }

    // Verify that an operation throws the expected exception type.
    private static void assertThrows(
            Class<? extends Throwable> expectedType,
            Runnable operation,
            String message
    ) {
        try {
            operation.run();
        } catch (Throwable actual) {
            if (expectedType.isInstance(actual)) {
                return;
            }

            throw new AssertionError(
                    message + ". Unexpected exception: "
                            + actual.getClass().getSimpleName(),
                    actual
            );
        }

        throw new AssertionError(
                message + ". No exception was thrown."
        );
    }
}
