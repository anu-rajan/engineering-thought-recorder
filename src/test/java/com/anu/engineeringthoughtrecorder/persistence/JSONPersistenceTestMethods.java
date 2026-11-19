package com.anu.engineeringthoughtrecorder.persistence;

import com.anu.engineeringthoughtrecorder.domainObjects.DesignSession;
import com.anu.engineeringthoughtrecorder.domainObjects.SystemSpecification;
import com.anu.engineeringthoughtrecorder.domainObjects.domainUtils.*;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class JSONPersistenceTestMethods {

    public static final class Fixture {
        private final JSONPersistence persistence;
        private final DesignSession original;
        private final DesignSession loaded;

        private Fixture(JSONPersistence persistence, DesignSession original, DesignSession loaded) {
            this.persistence = persistence;
            this.original = original;
            this.loaded = loaded;
        }
    }

    // Build one representative finalized session and load it back from JSON.
    public static Fixture createAndSaveSession() {
        JSONPersistence persistence = new JSONPersistence(Path.of("data", "sessions"));
        DesignSession session = createSampleSession();
        session.markFinal();
        persistence.save(session);
        return new Fixture(persistence, session, persistence.load(session.getId()));
    }

    private static DesignSession createSampleSession() {
        DesignSession session = new DesignSession("Persistence Test", null);
        TimestampRange thoughtRange = range("2026-10-28T10:00:00Z", "2026-10-28T10:00:10Z");
        String requirementText = "Users should be able to upload audio recordings.";
        session.getDesignDraft().updateDesignDraft(
                DraftSectionType.FUNCTIONAL_REQUIREMENTS,
                new DraftItem(requirementText),
                new Thought(requirementText, thoughtRange)
        );
        session.getTranscript().addSegment(new TranscriptSegment(
                range("2026-10-28T10:01:00Z", "2026-10-28T10:01:10Z"),
                "The system should support audio uploads."
        ));
        session.getDesignDraft().updateGoal("Design a system for recording engineering discussions.");
        session.setSystemSpecification(new SystemSpecification(
                List.of(new DesignComponent("Audio Processing Service", "Process uploaded audio recordings", "Java")),
                List.of(new API("Upload Audio", "POST", "/api/audio", "Accept audio recordings")),
                new PerformanceCapacity(List.of(new PerformanceMetric("Response time", "200", "ms"))),
                List.of(new ExternalDependency("Groq API", "Classify engineering requirements", "Valid API key"))
        ));
        return session;
    }

    private static TimestampRange range(String start, String end) {
        return new TimestampRange(Instant.parse(start), Instant.parse(end));
    }

    public static void testFinalizationTimestamp(Fixture f) {
        assertEqual("Finalization timestamp", f.original.getFinalizedAt(), f.loaded.getFinalizedAt());
    }

    public static void testDesignGoal(Fixture f) {
        assertEqual("Design draft goal", f.original.getDesignDraft().getGoal(), f.loaded.getDesignDraft().getGoal());
    }

    public static void testSessionId(Fixture f) {
        assertEqual("Session ID", f.original.getId(), f.loaded.getId());
    }

    public static void testDraftItemId(Fixture f) {
        assertEqual("Draft item ID", originalItem(f).getId(), loadedItem(f).getId());
    }

    public static void testThoughtHistory(Fixture f) {
        List<Thought> original = originalItem(f).getThoughtFlow();
        List<Thought> loaded = loadedItem(f).getThoughtFlow();
        assertEqual("Thought history size", original.size(), loaded.size());
        assertTrue("Expected at least one original thought", !original.isEmpty());
        Thought a = original.get(0);
        Thought b = loaded.get(0);
        assertEqual("Thought description", a.getDescription(), b.getDescription());
        assertEqual("Thought start timestamp", a.getTimestampRange().getStartTimestamp(), b.getTimestampRange().getStartTimestamp());
        assertEqual("Thought end timestamp", a.getTimestampRange().getEndTimestamp(), b.getTimestampRange().getEndTimestamp());
    }

    public static void testSessionMetadata(Fixture f) {
        assertEqual("Session title", f.original.getTitle(), f.loaded.getTitle());
        assertEqual("Session state", f.original.getState(), f.loaded.getState());
        assertEqual("Session creation timestamp", f.original.getCreatedAt(), f.loaded.getCreatedAt());
    }

    public static void testTranscriptSegment(Fixture f) {
        TranscriptSegment a = f.original.getTranscript().getSegments().get(0);
        TranscriptSegment b = f.loaded.getTranscript().getSegments().get(0);
        assertEqual("Transcript segment text", a.getText(), b.getText());
        assertEqual("Transcript start timestamp", a.getTimestampRange().getStartTimestamp(), b.getTimestampRange().getStartTimestamp());
        assertEqual("Transcript end timestamp", a.getTimestampRange().getEndTimestamp(), b.getTimestampRange().getEndTimestamp());
    }

    public static void testDesignComponent(Fixture f) {
        DesignComponent a = originalSpecification(f).getComponents().get(0);
        DesignComponent b = loadedSpecification(f).getComponents().get(0);
        assertEqual("Design component name", a.getName(), b.getName());
        assertEqual("Design component responsibility", a.getResponsibility(), b.getResponsibility());
        assertEqual("Design component technology", a.getTechnology(), b.getTechnology());
    }

    public static void testApiDefinition(Fixture f) {
        API a = originalSpecification(f).getApis().get(0);
        API b = loadedSpecification(f).getApis().get(0);
        assertEqual("API name", a.getName(), b.getName());
        assertEqual("API method", a.getMethod(), b.getMethod());
        assertEqual("API path", a.getPath(), b.getPath());
        assertEqual("API purpose", a.getPurpose(), b.getPurpose());
    }

    public static void testPerformanceMetric(Fixture f) {
        PerformanceMetric a = originalSpecification(f).getPerformanceCapacity().getMetrics().get(0);
        PerformanceMetric b = loadedSpecification(f).getPerformanceCapacity().getMetrics().get(0);
        assertEqual("Performance metric name", a.getMetric(), b.getMetric());
        assertEqual("Performance metric value", a.getValue(), b.getValue());
        assertEqual("Performance metric unit", a.getUnit(), b.getUnit());
    }

    public static void testExternalDependency(Fixture f) {
        ExternalDependency a = originalSpecification(f).getExternalDependencies().get(0);
        ExternalDependency b = loadedSpecification(f).getExternalDependencies().get(0);
        assertEqual("External dependency name", a.getName(), b.getName());
        assertEqual("External dependency purpose", a.getPurpose(), b.getPurpose());
        assertEqual("External dependency requirement", a.getRequirement(), b.getRequirement());
    }

    public static void testUpdatedGoalPersists(Fixture f) {
        f.original.getDesignDraft().updateGoal("Updated goal after the initial save.");
        f.persistence.save(f.original);
        DesignSession reloaded = f.persistence.load(f.original.getId());
        assertEqual("Updated design draft goal", f.original.getDesignDraft().getGoal(), reloaded.getDesignDraft().getGoal());
    }

    public static void testNonexistentSession(Fixture f) {
        assertThrows(IllegalStateException.class, "Loading a nonexistent session",
                () -> f.persistence.load(UUID.randomUUID()));
    }

    public static void testNullSession(Fixture f) {
        assertThrows(IllegalArgumentException.class, "Saving a null session",
                () -> f.persistence.save(null));
    }

    private static DraftItem originalItem(Fixture f) {
        return f.original.getDesignDraft().getDraftSection(DraftSectionType.FUNCTIONAL_REQUIREMENTS).getItems().get(0);
    }

    private static DraftItem loadedItem(Fixture f) {
        return f.loaded.getDesignDraft().getDraftSection(DraftSectionType.FUNCTIONAL_REQUIREMENTS).getItems().get(0);
    }

    private static SystemSpecification originalSpecification(Fixture f) {
        return f.original.getSystemSpecification();
    }

    private static SystemSpecification loadedSpecification(Fixture f) {
        SystemSpecification specification = f.loaded.getSystemSpecification();
        assertTrue("System specification was not restored", specification != null);
        return specification;
    }

    private static void assertEqual(String description, Object expected, Object actual) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError(description + " was not preserved. Expected: " + expected + ", actual: " + actual);
        }
    }

    private static void assertTrue(String message, boolean condition) {
        if (!condition) throw new AssertionError(message);
    }

    private static void assertThrows(Class<? extends Throwable> expected, String description, Runnable action) {
        try {
            action.run();
        } catch (Throwable actual) {
            if (expected.isInstance(actual)) return;
            throw new AssertionError(description + " threw " + actual.getClass().getSimpleName()
                    + " instead of " + expected.getSimpleName(), actual);
        }
        throw new AssertionError(description + " should throw " + expected.getSimpleName());
    }
}
