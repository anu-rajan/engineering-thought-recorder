
package com.anu.engineeringthoughtrecorder.domainObjects.domainUtils;

public class DraftItemEditingTest {

    public static void main(String[] args) {

        // Verify that editing a draft item updates its content.
        DraftItemEditingTestMethods.testContentUpdate();
        System.out.println("PASS: Draft item content updated");

        // Verify that editing a draft item preserves its UUID.
        DraftItemEditingTestMethods.testIdPreservation();
        System.out.println("PASS: Draft item ID preserved");

        // Verify that blank content is rejected.
        DraftItemEditingTestMethods.testBlankContentRejection();
        System.out.println("PASS: Blank content rejected");

        // Verify that a rejected update does not modify existing content.
        DraftItemEditingTestMethods.testContentPreservationAfterInvalidUpdate();
        System.out.println("PASS: Content preserved after invalid update");

        // Verify that editing a draft item preserves its thought history.
        DraftItemEditingTestMethods.testThoughtHistoryPreservation();
        System.out.println("PASS: Thought history preserved after editing");

        System.out.println("All DraftItem editing tests passed!");
    }
}
