package com.anu.engineeringthoughtrecorder.persistence;

public class JSONPersistenceTest {
    public static void main(String[] args) {
        JSONPersistenceTestMethods.Fixture fixture = JSONPersistenceTestMethods.createAndSaveSession();

        // Verify that the finalization timestamp survives saving and loading.
        JSONPersistenceTestMethods.testFinalizationTimestamp(fixture);
        System.out.println("PASS: Finalization timestamp preserved");

        // Verify that the design goal survives a JSON round trip.
        JSONPersistenceTestMethods.testDesignGoal(fixture);
        System.out.println("PASS: Design goal preserved");

        // Verify that the session UUID survives a JSON round trip.
        JSONPersistenceTestMethods.testSessionId(fixture);
        System.out.println("PASS: Session ID preserved");

        // Verify that draft item identity survives a JSON round trip.
        JSONPersistenceTestMethods.testDraftItemId(fixture);
        System.out.println("PASS: Draft item ID preserved");

        // Verify that thought count, description, and timestamps survive loading.
        JSONPersistenceTestMethods.testThoughtHistory(fixture);
        System.out.println("PASS: Thought history preserved");

        // Verify that title, state, and creation timestamp are restored.
        JSONPersistenceTestMethods.testSessionMetadata(fixture);
        System.out.println("PASS: Session metadata preserved");

        // Verify that transcript text and both timestamps are restored.
        JSONPersistenceTestMethods.testTranscriptSegment(fixture);
        System.out.println("PASS: Transcript segment preserved");

        // Verify that component name, responsibility, and technology are restored.
        JSONPersistenceTestMethods.testDesignComponent(fixture);
        System.out.println("PASS: Design component preserved");

        // Verify that API name, method, path, and purpose are restored.
        JSONPersistenceTestMethods.testApiDefinition(fixture);
        System.out.println("PASS: API definition preserved");

        // Verify that performance metric name, value, and unit are restored.
        JSONPersistenceTestMethods.testPerformanceMetric(fixture);
        System.out.println("PASS: Performance metric preserved");

        // Verify that external dependency name, purpose, and requirement are restored.
        JSONPersistenceTestMethods.testExternalDependency(fixture);
        System.out.println("PASS: External dependency preserved");

        // Verify that saving again overwrites the previously stored goal.
        JSONPersistenceTestMethods.testUpdatedGoalPersists(fixture);
        System.out.println("PASS: Session update persisted");

        // Verify that loading an unknown session ID fails appropriately.
        JSONPersistenceTestMethods.testNonexistentSession(fixture);
        System.out.println("PASS: Nonexistent session rejected");

        // Verify that saving a null session fails appropriately.
        JSONPersistenceTestMethods.testNullSession(fixture);
        System.out.println("PASS: Null session rejected");

        System.out.println("All JSONPersistence tests passed!");
    }
}
