package com.anu.engineeringthoughtrecorder.documentGeneration;

import com.anu.engineeringthoughtrecorder.domainObjects.DesignSession;
import com.anu.engineeringthoughtrecorder.domainObjects.SystemSpecification;
import com.anu.engineeringthoughtrecorder.domainObjects.domainUtils.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class MarkdownDesignDocumentTestMethods {
    private static final DesignDocumentGenerator GENERATOR = new MarkdownDesignDocumentGenerator();
    private static final DesignDocumentExporter EXPORTER = new MarkdownDesignDocumentExporter();
    private static final Path OUTPUT_PATH = Path.of("data", "documents", "audio-recording-system.md");

    private static DesignSession createSession(String title) {
        return new DesignSession(title, null);
    }

    private static void addDraftItem(DesignSession session, DraftSectionType section, String content) {
        session.getDesignDraft().updateDesignDraft(section, new DraftItem(content));
    }

    private static SystemSpecification createSpecification(
            List<DesignComponent> components, List<API> apis,
            PerformanceCapacity capacity, List<ExternalDependency> dependencies) {
        return new SystemSpecification(components, apis, capacity, dependencies);
    }

    private static DesignSession createBasicSession() {
        DesignSession session = createSession("Audio Recording System");
        session.getDesignDraft().updateGoal("Design a system that records engineering discussions.");
        addDraftItem(session, DraftSectionType.FUNCTIONAL_REQUIREMENTS,
                "Users should be able to upload audio recordings.");
        session.setSystemSpecification(createSpecification(
                List.of(new DesignComponent("Audio Processing Service",
                        "Process uploaded audio recordings", "Java")), List.of(), null, List.of()));
        return session;
    }

    private static DesignSession createMultipleRequirementsSession() {
        DesignSession session = createSession("Multiple Requirements Test");
        addDraftItem(session, DraftSectionType.FUNCTIONAL_REQUIREMENTS, "Users can upload audio recordings.");
        addDraftItem(session, DraftSectionType.FUNCTIONAL_REQUIREMENTS, "Users can download transcripts.");
        return session;
    }

    private static DesignSession createMultipleComponentsSession() {
        DesignSession session = createSession("Multiple Components Test");
        session.setSystemSpecification(createSpecification(List.of(
                new DesignComponent("Audio Processing Service", "Process uploaded audio recordings", "Java"),
                new DesignComponent("Transcription Service", "Convert audio recordings into text", "Python")
        ), List.of(), null, List.of()));
        return session;
    }

    private static DesignSession createMultipleApisSession() {
        DesignSession session = createSession("Multiple APIs Test");
        session.setSystemSpecification(createSpecification(List.of(), List.of(
                new API("Upload Audio", "POST", "/api/audio", "Upload an audio recording"),
                new API("Get Transcript", "GET", "/api/transcripts/{id}", "Retrieve a generated transcript")
        ), null, List.of()));
        return session;
    }

    private static String generate(DesignSession session) {
        return GENERATOR.generate(session);
    }

    private static void assertContains(String content, String expected, String description) {
        if (!content.contains(expected)) {
            throw new AssertionError(description + " is missing: " + expected);
        }
    }

    private static void assertNotContains(String content, String unexpected, String description) {
        if (content.contains(unexpected)) {
            throw new AssertionError(description + " should not appear: " + unexpected);
        }
    }

    private static void assertBefore(String content, String first, String second, String description) {
        int firstPosition = content.indexOf(first);
        int secondPosition = content.indexOf(second);
        if (firstPosition < 0 || secondPosition < 0 || firstPosition >= secondPosition) {
            throw new AssertionError(description + " are missing or out of order");
        }
    }

    private static void assertEquals(String expected, String actual, String description) {
        if (!expected.equals(actual)) {
            throw new AssertionError(description + " does not match expected content");
        }
    }

    private static String readFile(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read exported Markdown file: " + path, e);
        }
    }

    private static void assertThrows(Class<? extends Throwable> expected, Runnable action, String description) {
        try {
            action.run();
        } catch (Throwable actual) {
            if (expected.isInstance(actual)) {
                return;
            }
            throw new AssertionError(description + ": expected " + expected.getSimpleName()
                    + " but got " + actual.getClass().getSimpleName(), actual);
        }
        throw new AssertionError(description + ": expected " + expected.getSimpleName());
    }

    public static void testBasicMarkdownContent() {
        String markdown = generate(createBasicSession());
        assertContains(markdown, "# Audio Recording System", "Document title");
        assertContains(markdown, "Design a system that records engineering discussions.", "Design goal");
        assertContains(markdown, "- Users should be able to upload audio recordings.", "Functional requirement");
        assertContains(markdown, "### Audio Processing Service", "System component");
    }

    public static void testMarkdownExport() {
        String markdown = generate(createBasicSession());
        EXPORTER.export(markdown, OUTPUT_PATH);
        assertEquals(markdown, readFile(OUTPUT_PATH), "Exported Markdown");
    }

    public static void testMarkdownOverwrite() {
        String markdown = generate(createBasicSession());
        EXPORTER.export(markdown, OUTPUT_PATH);
        String updatedMarkdown = markdown + "\n## Revision Notes\n\nUpdated design.\n";
        EXPORTER.export(updatedMarkdown, OUTPUT_PATH);
        assertEquals(updatedMarkdown, readFile(OUTPUT_PATH), "Overwritten Markdown");
    }

    public static void testNullExportContent() {
        assertThrows(IllegalArgumentException.class, () -> EXPORTER.export(null, OUTPUT_PATH),
                "Null document content");
    }

    public static void testNullExportPath() {
        assertThrows(IllegalArgumentException.class, () -> EXPORTER.export("# Test", null),
                "Null output path");
    }

    public static void testExportWritingFailure() {
        Path directory = OUTPUT_PATH.getParent();
        try {
            Files.createDirectories(directory);
        } catch (IOException e) {
            throw new IllegalStateException("Could not prepare export failure test directory", e);
        }
        assertThrows(IllegalStateException.class, () -> EXPORTER.export("# Test", directory),
                "Exporting to a directory");
    }

    public static void testNullDesignSession() {
        assertThrows(IllegalArgumentException.class, () -> GENERATOR.generate(null),
                "Null design session");
    }

    public static void testEmptyDesignSession() {
        String markdown = generate(createSession("Empty Design Session"));
        assertContains(markdown, "# Empty Design Session", "Empty session title");
        assertContains(markdown, "## Design Goal", "Design goal heading");
        assertContains(markdown, "Not specified.", "Missing goal placeholder");
    }

    public static void testMultipleFunctionalRequirements() {
        String markdown = generate(createMultipleRequirementsSession());
        assertContains(markdown, "- Users can upload audio recordings.", "First functional requirement");
        assertContains(markdown, "- Users can download transcripts.", "Second functional requirement");
    }

    public static void testFunctionalRequirementOrder() {
        assertBefore(generate(createMultipleRequirementsSession()),
                "- Users can upload audio recordings.", "- Users can download transcripts.",
                "Functional requirements");
    }

    public static void testMultipleSystemComponents() {
        String markdown = generate(createMultipleComponentsSession());
        assertContains(markdown, "### Audio Processing Service", "First system component");
        assertContains(markdown, "### Transcription Service", "Second system component");
    }

    public static void testSystemComponentOrder() {
        assertBefore(generate(createMultipleComponentsSession()),
                "### Audio Processing Service", "### Transcription Service", "System components");
    }

    public static void testMultipleApis() {
        String markdown = generate(createMultipleApisSession());
        assertContains(markdown, "### Upload Audio", "First API");
        assertContains(markdown, "### Get Transcript", "Second API");
    }

    public static void testApiOrder() {
        assertBefore(generate(createMultipleApisSession()), "### Upload Audio", "### Get Transcript", "API definitions");
    }

    public static void testUploadApiDetails() {
        String markdown = generate(createMultipleApisSession());
        assertContains(markdown, "**Method:** POST", "Upload API method");
        assertContains(markdown, "**Path:** `/api/audio`", "Upload API path");
        assertContains(markdown, "**Purpose:** Upload an audio recording", "Upload API purpose");
    }

    public static void testTranscriptApiDetails() {
        String markdown = generate(createMultipleApisSession());
        assertContains(markdown, "**Method:** GET", "Transcript API method");
        assertContains(markdown, "**Path:** `/api/transcripts/{id}`", "Transcript API path");
        assertContains(markdown, "**Purpose:** Retrieve a generated transcript", "Transcript API purpose");
    }

    public static void testPerformanceMetrics() {
        DesignSession session = createSession("Performance Metrics Test");
        PerformanceCapacity capacity = new PerformanceCapacity(List.of(
                new PerformanceMetric("Response Time", "200", "ms"),
                new PerformanceMetric("Concurrent Users", "1000", "users")
        ));
        session.setSystemSpecification(createSpecification(List.of(), List.of(), capacity, List.of()));
        String markdown = generate(session);
        assertContains(markdown, "## Performance and Capacity", "Performance heading");
        assertContains(markdown, "- **Response Time:** 200 ms", "Response time metric");
        assertContains(markdown, "- **Concurrent Users:** 1000 users", "Concurrent users metric");
    }

    public static void testExternalDependency() {
        DesignSession session = createSession("External Dependencies Test");
        session.setSystemSpecification(createSpecification(List.of(), List.of(), null, List.of(
                new ExternalDependency("Speech Recognition API", "Convert audio recordings into text",
                        "Internet connectivity")
        )));
        String markdown = generate(session);
        assertContains(markdown, "## External Dependencies", "Dependencies heading");
        assertContains(markdown, "### Speech Recognition API", "Dependency name");
        assertContains(markdown, "**Purpose:** Convert audio recordings into text", "Dependency purpose");
        assertContains(markdown, "**Requirement:** Internet connectivity", "Dependency requirement");
    }

    public static void testMissingSpecification() {
        String markdown = generate(createSession("No Specification Test"));
        assertNotContains(markdown, "## System Components", "System components heading");
        assertNotContains(markdown, "## API Definitions", "API heading");
        assertNotContains(markdown, "## Performance and Capacity", "Performance heading");
        assertNotContains(markdown, "## External Dependencies", "Dependencies heading");
    }

    public static void testEmptySpecification() {
        DesignSession session = createSession("Empty Specification Test");
        session.setSystemSpecification(createSpecification(List.of(), List.of(), null, List.of()));
        assertContains(generate(session), "# Empty Specification Test", "Empty specification document title");
    }

    public static void testNullSpecificationCollections() {
        DesignSession session = createSession("Null Collections Test");
        session.setSystemSpecification(createSpecification(null, null, null, null));
        assertContains(generate(session), "# Null Collections Test", "Null collections document title");
    }

    public static void testEmptyRequirements() {
        String markdown = generate(createSession("Empty Requirements Test"));
        String firstHeading = "## Functional Requirements";
        String nextHeading = "## Non-Functional Requirements";
        assertBefore(markdown, firstHeading, nextHeading, "Requirement section headings");
        String section = markdown.substring(markdown.indexOf(firstHeading), markdown.indexOf(nextHeading));
        assertNotContains(section, "- ", "Empty functional requirements items");
    }

    public static void testDraftSectionHeadings() {
        String markdown = generate(createSession("Empty Requirements Test"));
        for (String heading : List.of("## Functional Requirements", "## Non-Functional Requirements",
                "## Assumptions", "## Design Decisions", "## Open Questions")) {
            assertContains(markdown, heading, "Draft section heading");
        }
    }

    public static void testNonFunctionalRequirement() {
        DesignSession session = createSession("Non-Functional Requirements Test");
        addDraftItem(session, DraftSectionType.NON_FUNCTIONAL_REQUIREMENTS,
                "The system should respond within 200 milliseconds.");
        assertContains(generate(session), "- The system should respond within 200 milliseconds.",
                "Non-functional requirement");
    }

    public static void testRemainingDraftSections() {
        DesignSession session = createSession("Remaining Draft Sections Test");
        addDraftItem(session, DraftSectionType.ASSUMPTIONS, "Users have a stable internet connection.");
        addDraftItem(session, DraftSectionType.DECISIONS, "Use PostgreSQL for data storage.");
        addDraftItem(session, DraftSectionType.OPEN_QUESTIONS, "Should audio files be retained permanently?");
        String markdown = generate(session);
        assertContains(markdown, "- Users have a stable internet connection.", "Assumption");
        assertContains(markdown, "- Use PostgreSQL for data storage.", "Design decision");
        assertContains(markdown, "- Should audio files be retained permanently?", "Open question");
    }
}
