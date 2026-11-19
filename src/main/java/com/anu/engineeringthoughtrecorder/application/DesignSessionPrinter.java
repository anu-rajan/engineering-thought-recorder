package com.anu.engineeringthoughtrecorder.application;

import com.anu.engineeringthoughtrecorder.domainObjects.DesignSession;
import com.anu.engineeringthoughtrecorder.domainObjects.domainUtils.DraftItem;
import com.anu.engineeringthoughtrecorder.domainObjects.domainUtils.DraftSectionType;
import com.anu.engineeringthoughtrecorder.domainObjects.domainUtils.Thought;
import com.anu.engineeringthoughtrecorder.domainObjects.domainUtils.TranscriptSegment;

public class DesignSessionPrinter {

    /**
     * Prints the complete engineering design session.
     *
     * Intended for integration testing and debugging:
     * - Session metadata
     * - Timestamped transcript
     * - Current design draft
     * - Chronological thought histories
     */
    /**
     * Prints session metadata and all transcript segments.
     *
     * Later, we will extend this method to print the design
     * draft and each item's chronological thought history.
     */
    public static void printSession(DesignSession session) {

        // 1. Reject invalid input early.
        if (session == null) {
            throw new IllegalArgumentException(
                    "Design session cannot be null"
            );
        }

        // 2. Print the session's identifying information.
        System.out.println("==================================================");
        System.out.println("ENGINEERING DESIGN SESSION");
        System.out.println("==================================================");

        System.out.println("Session ID: " + session.getId());
        System.out.println("Title: " + session.getTitle());
        System.out.println("Design Type: " + session.getDesignType());
        System.out.println("State: " + session.getState());
        System.out.println("Created At: " + session.getCreatedAt());
        System.out.println("Finalized At: " + session.getFinalizedAt());

        // 3. Print every transcript segment, including segments
        //    appended during later recording or transcript sessions.
        System.out.println();
        System.out.println("TRANSCRIPT");
        System.out.println("--------------------------------------------------");

        for (TranscriptSegment segment : session.getTranscript().getSegments()) {

            // Print the complete timestamp range and spoken text.
            System.out.println(
                    segment.getTimestampRange().getStartTimestamp()
                            + " - "
                            + segment.getTimestampRange().getEndTimestamp()
                            + " | "
                            + segment.getText()
            );
        }

        // 4. Print the current design draft and its thought histories.
        System.out.println();
        System.out.println("FINAL DESIGN DRAFT");
        System.out.println("--------------------------------------------------");

        for (DraftSectionType sectionType : DraftSectionType.values()) {

            System.out.println();
            System.out.println(sectionType);

            // Read the items belonging to this design section.
            for (DraftItem item : session.getDesignDraft()
                    .getDraftSection(sectionType)
                    .getItems()) {

                System.out.println("  Item ID: " + item.getId());
                System.out.println("  Current: " + item.getContent());
                System.out.println("  Thought History:");

                // Preserve the stored chronological thought sequence.
                for (Thought thought : item.getThoughtFlow()) {
                    System.out.println(
                            "    " + thought.getTimestampRange().getStartTimestamp()
                                    + " - "
                                    + thought.getTimestampRange().getEndTimestamp()
                                    + " | "
                                    + thought.getDescription()
                    );
                }
            }
        }
    }
}