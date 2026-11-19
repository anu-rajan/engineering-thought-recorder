package com.anu.engineeringthoughtrecorder.documentGeneration;

public class MarkdownDesignDocumentGeneratorTest {
    public static void main(String[] args) {
        // Verify the title, goal, requirement, and system component appear in Markdown.
        MarkdownDesignDocumentTestMethods.testBasicMarkdownContent();
        System.out.println("PASS: Basic Markdown content");

        // Verify exported file content matches generated Markdown exactly.
        MarkdownDesignDocumentTestMethods.testMarkdownExport();
        System.out.println("PASS: Markdown export content");

        // Verify exporting to an existing file replaces its contents.
        MarkdownDesignDocumentTestMethods.testMarkdownOverwrite();
        System.out.println("PASS: Markdown overwrite");

        // Verify the exporter rejects null document content.
        MarkdownDesignDocumentTestMethods.testNullExportContent();
        System.out.println("PASS: Null export content");

        // Verify the exporter rejects a null output path.
        MarkdownDesignDocumentTestMethods.testNullExportPath();
        System.out.println("PASS: Null export path");

        // Verify writing to a directory reports a file-writing failure.
        MarkdownDesignDocumentTestMethods.testExportWritingFailure();
        System.out.println("PASS: Export writing failure");

        // Verify the generator rejects a null design session.
        MarkdownDesignDocumentTestMethods.testNullDesignSession();
        System.out.println("PASS: Null design session");

        // Verify a new, empty session generates a title and goal placeholder.
        MarkdownDesignDocumentTestMethods.testEmptyDesignSession();
        System.out.println("PASS: Empty design session");

        // Verify all functional requirements are rendered.
        MarkdownDesignDocumentTestMethods.testMultipleFunctionalRequirements();
        System.out.println("PASS: Multiple functional requirements");

        // Verify functional requirements retain their insertion order.
        MarkdownDesignDocumentTestMethods.testFunctionalRequirementOrder();
        System.out.println("PASS: Functional requirement order");

        // Verify all system components are rendered.
        MarkdownDesignDocumentTestMethods.testMultipleSystemComponents();
        System.out.println("PASS: Multiple system components");

        // Verify system components retain their insertion order.
        MarkdownDesignDocumentTestMethods.testSystemComponentOrder();
        System.out.println("PASS: System component order");

        // Verify all API definitions are rendered.
        MarkdownDesignDocumentTestMethods.testMultipleApis();
        System.out.println("PASS: Multiple APIs");

        // Verify API definitions retain their insertion order.
        MarkdownDesignDocumentTestMethods.testApiOrder();
        System.out.println("PASS: API order");

        // Verify the first API's method, path, and purpose are rendered.
        MarkdownDesignDocumentTestMethods.testUploadApiDetails();
        System.out.println("PASS: Upload API details");

        // Verify the second API's method, path, and purpose are rendered.
        MarkdownDesignDocumentTestMethods.testTranscriptApiDetails();
        System.out.println("PASS: Transcript API details");

        // Verify performance metrics include their values and units.
        MarkdownDesignDocumentTestMethods.testPerformanceMetrics();
        System.out.println("PASS: Performance metrics");

        // Verify an external dependency includes its name, purpose, and requirement.
        MarkdownDesignDocumentTestMethods.testExternalDependency();
        System.out.println("PASS: External dependency");

        // Verify architecture headings are omitted when the specification is null.
        MarkdownDesignDocumentTestMethods.testMissingSpecification();
        System.out.println("PASS: Missing specification");

        // Verify an empty specification does not prevent document generation.
        MarkdownDesignDocumentTestMethods.testEmptySpecification();
        System.out.println("PASS: Empty specification");

        // Verify null specification collections do not prevent document generation.
        MarkdownDesignDocumentTestMethods.testNullSpecificationCollections();
        System.out.println("PASS: Null specification collections");

        // Verify an empty functional requirements section has no invented items.
        MarkdownDesignDocumentTestMethods.testEmptyRequirements();
        System.out.println("PASS: Empty functional requirements");

        // Verify all five draft section headings are included.
        MarkdownDesignDocumentTestMethods.testDraftSectionHeadings();
        System.out.println("PASS: Draft section headings");

        // Verify a non-functional requirement is rendered.
        MarkdownDesignDocumentTestMethods.testNonFunctionalRequirement();
        System.out.println("PASS: Non-functional requirement");

        // Verify assumptions, decisions, and open questions are rendered.
        MarkdownDesignDocumentTestMethods.testRemainingDraftSections();
        System.out.println("PASS: Remaining draft sections");

        System.out.println("All Markdown document tests passed!");
    }
}
