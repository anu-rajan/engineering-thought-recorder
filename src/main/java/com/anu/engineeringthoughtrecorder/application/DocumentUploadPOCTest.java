package com.anu.engineeringthoughtrecorder.application;

import com.anu.engineeringthoughtrecorder.domainObjects.Transcript;
import com.anu.engineeringthoughtrecorder.domainObjects.domainUtils.TranscriptSegment;
import com.anu.engineeringthoughtrecorder.domainObjects.domainUtils.TimestampRange;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;

import com.anu.engineeringthoughtrecorder.domainObjects.DesignSession;
import com.anu.engineeringthoughtrecorder.domainObjects.domainUtils.DesignType;
import com.anu.engineeringthoughtrecorder.peripherals.GroqCaptureDesignAIService;
import com.anu.engineeringthoughtrecorder.peripherals.GroqSpeechToTextService;

/**
 * End-to-end proof of concept for EngineeringThoughtRecorder.
 *
 * Demonstrates:
 * - Processing a timestamped engineering transcript using Groq AI.
 * - Generating a structured engineering design draft.
 * - Updating existing design items while preserving their UUIDs.
 * - Retaining timestamped thought history across revisions.
 *
 * Known limitation:
 * AI may occasionally merge related design decisions or
 * misclassify unresolved questions.
 *
 * This test makes real Groq API calls and should not be run
 * unnecessarily.
 */
public class DocumentUploadPOCTest {

    public static void main(String[] args) throws IOException {

        // Create the transcript for our engineering design meeting.
        Transcript transcript = new Transcript();

// Segment 1: Functional requirement — document upload.
        transcript.addSegment(new TranscriptSegment(
                new TimestampRange(
                        Instant.parse("2026-11-18T10:00:00Z"),
                        Instant.parse("2026-11-18T10:00:20Z")
                ),
                "Users should be able to upload PDF and TXT documents through the application."
        ));

// Segment 2: Functional requirement — upload status.
        transcript.addSegment(new TranscriptSegment(
                new TimestampRange(
                        Instant.parse("2026-11-18T10:00:20Z"),
                        Instant.parse("2026-11-18T10:00:40Z")
                ),
                "Users should be able to check the processing status of their uploaded documents."
        ));

// Segment 3: Initial non-functional requirement — file size.
        transcript.addSegment(new TranscriptSegment(
                new TimestampRange(
                        Instant.parse("2026-11-18T10:00:40Z"),
                        Instant.parse("2026-11-18T10:01:00Z")
                ),
                "The maximum permitted document upload size should be 10 MB."
        ));

        // Segment 4: Initial architectural decision — synchronous processing.
//
// This establishes the original design choice.
// A later segment will explicitly revise this decision,
// allowing us to test whether the AI preserves the item's
// original UUID and chronological thought history.
        transcript.addSegment(new TranscriptSegment(
                new TimestampRange(
                        Instant.parse("2026-11-18T10:01:00Z"),
                        Instant.parse("2026-11-18T10:01:20Z")
                ),
                "We have decided that uploaded documents will initially be processed synchronously, within the upload request."
        ));

        // Segment 5: Assumption — document storage.
//
// This is an assumption rather than a confirmed decision.
// We want Groq to classify it under ASSUMPTIONS,
// not DECISIONS or FUNCTIONAL_REQUIREMENTS.
        transcript.addSegment(new TranscriptSegment(
                new TimestampRange(
                        Instant.parse("2026-11-18T10:01:20Z"),
                        Instant.parse("2026-11-18T10:01:40Z")
                ),
                "For now, we are assuming that the application will have access to an existing cloud object storage service for storing uploaded documents."
        ));

        // Segment 6: Open question — document retention policy.
//
// The team has not decided how long uploaded documents
// should remain in storage.
//
// We expect Groq to classify this under OPEN_QUESTIONS
// rather than treating it as an established requirement.
        transcript.addSegment(new TranscriptSegment(
                new TimestampRange(
                        Instant.parse("2026-11-18T10:01:40Z"),
                        Instant.parse("2026-11-18T10:02:00Z")
                ),
                "We still need to determine how long uploaded documents should be retained before they are automatically deleted."
        ));

        // Segment 7: Revision — increase maximum upload size.
//
// Segment 3 originally established a 10 MB limit.
// This segment explicitly changes that SAME requirement
// to 50 MB.
//
// Expected AI behavior:
// 1. Identify the existing file-size requirement.
// 2. Preserve its original UUID.
// 3. Update its current content to 50 MB.
// 4. Append this segment to its thought history.
        transcript.addSegment(new TranscriptSegment(
                new TimestampRange(
                        Instant.parse("2026-11-18T10:02:00Z"),
                        Instant.parse("2026-11-18T10:02:20Z")
                ),
                "After reviewing the expected document sizes, we are revising the earlier 10 MB upload limit. The maximum permitted document upload size should now be 50 MB instead."
        ));

        // Segment 8: Revision — switch to asynchronous processing.
//
// Segment 4 originally established synchronous processing.
// This segment explicitly reverses that SAME decision.
//
// Expected AI behavior:
// 1. Find the existing synchronous processing decision.
// 2. Preserve its original UUID.
// 3. Update the current decision to asynchronous processing.
// 4. Retain both discussions in the thought history.
        transcript.addSegment(new TranscriptSegment(
                new TimestampRange(
                        Instant.parse("2026-11-18T10:02:20Z"),
                        Instant.parse("2026-11-18T10:02:40Z")
                ),
                "We are changing our earlier decision to process documents synchronously. Document processing will now be asynchronous, using background workers instead of processing within the upload request."
        ));

        // Segment 9: Architectural decision — message queue.
//
// This is a NEW decision, separate from the earlier
// synchronous-to-asynchronous processing change.
//
// Expected AI behavior:
// 1. Classify it under DECISIONS.
// 2. Create a new DraftItem with its own UUID.
// 3. Preserve Segment 8's existing processing decision.
        transcript.addSegment(new TranscriptSegment(
                new TimestampRange(
                        Instant.parse("2026-11-18T10:02:40Z"),
                        Instant.parse("2026-11-18T10:03:00Z")
                ),
                "We have decided to use a message queue to distribute document processing jobs to background workers."
        ));

        // Segment 10: Revision — define document processing statuses.
//
// Segment 2 introduced the ability to check processing status.
// This segment adds detail to that SAME functional requirement.
//
// Expected AI behavior:
// 1. Find the existing processing-status requirement.
// 2. Preserve its original UUID.
// 3. Update its content to include the three statuses.
// 4. Retain both discussions in its thought history.
        transcript.addSegment(new TranscriptSegment(
                new TimestampRange(
                        Instant.parse("2026-11-18T10:03:00Z"),
                        Instant.parse("2026-11-18T10:03:20Z")
                ),
                "To clarify our earlier processing-status requirement, users should be able to check whether their uploaded document is PENDING, PROCESSING, or COMPLETED."
        ));


        // Segment 11: Non-functional requirement — access security.
//
// Uploaded documents may contain sensitive information.
// This requirement establishes an access-control constraint.
//
// Expected AI behavior:
// 1. Classify under NON_FUNCTIONAL_REQUIREMENTS.
// 2. Create a new DraftItem with its own UUID.
// 3. Keep the existing file-size requirement unchanged.
        transcript.addSegment(new TranscriptSegment(
                new TimestampRange(
                        Instant.parse("2026-11-18T10:03:20Z"),
                        Instant.parse("2026-11-18T10:03:40Z")
                ),
                "Uploaded documents must only be accessible to their owners or explicitly authorized users."
        ));

        // Segment 12: Non-functional requirement — upload response time.
//
// Because processing happens asynchronously, the upload API
// should acknowledge the request quickly without waiting
// for document processing to finish.
//
// Expected AI behavior:
// 1. Classify under NON_FUNCTIONAL_REQUIREMENTS.
// 2. Create a new DraftItem with its own UUID.
// 3. Keep existing size and security requirements unchanged.
        transcript.addSegment(new TranscriptSegment(
                new TimestampRange(
                        Instant.parse("2026-11-18T10:03:40Z"),
                        Instant.parse("2026-11-18T10:04:00Z")
                ),
                "The document upload API should acknowledge accepted uploads within two seconds under normal operating conditions, without waiting for background processing to complete."
        ));

        // Segment 13: Functional requirement — failed processing status.
//
// This introduces an additional behavior for the existing
// processing-status requirement.
//
// Expected AI behavior:
// 1. Find the existing processing-status requirement.
// 2. Preserve its original UUID.
// 3. Extend the supported statuses to include FAILED.
// 4. Retain the earlier status discussions in thought history.
        transcript.addSegment(new TranscriptSegment(
                new TimestampRange(
                        Instant.parse("2026-11-18T10:04:00Z"),
                        Instant.parse("2026-11-18T10:04:20Z")
                ),
                "We need to extend our earlier document processing-status requirement. In addition to PENDING, PROCESSING, and COMPLETED, users should also see FAILED when background processing cannot complete successfully."
        ));

        // Segment 14: Architectural decision — retry failed jobs.
//
// This is a NEW decision about background worker behavior.
// It is separate from the functional requirement that users
// should see FAILED when processing cannot complete.
//
// Expected AI behavior:
// 1. Classify under DECISIONS.
// 2. Create a new DraftItem with its own UUID.
// 3. Preserve the existing processing and queue decisions.
        transcript.addSegment(new TranscriptSegment(
                new TimestampRange(
                        Instant.parse("2026-11-18T10:04:20Z"),
                        Instant.parse("2026-11-18T10:04:40Z")
                ),
                "We have decided that background workers should retry a document processing job up to three times when a transient failure occurs."
        ));

        // Segment 15: Open question — duplicate document handling.
//
// The team has not yet decided how duplicate uploads
// should be handled.
//
// Expected AI behavior:
// 1. Classify under OPEN_QUESTIONS.
// 2. Create a new DraftItem with its own UUID.
// 3. Preserve the earlier document-retention question.
        transcript.addSegment(new TranscriptSegment(
                new TimestampRange(
                        Instant.parse("2026-11-18T10:04:40Z"),
                        Instant.parse("2026-11-18T10:05:00Z")
                ),
                "We still need to decide whether uploading the same document multiple times should create separate records or whether the system should detect and reject duplicate uploads."
        ));


        // Segment 16: Assumption — existing authentication service.
//
// We assume authentication is already handled by another
// system, rather than building it into this service.
//
// Expected AI behavior:
// 1. Classify under ASSUMPTIONS.
// 2. Create a new DraftItem with its own UUID.
// 3. Preserve the earlier cloud storage assumption.
        transcript.addSegment(new TranscriptSegment(
                new TimestampRange(
                        Instant.parse("2026-11-18T10:05:00Z"),
                        Instant.parse("2026-11-18T10:05:20Z")
                ),
                "We are assuming that an existing authentication service will verify user identities before requests reach the document upload service."
        ));

        // Segment 17: Functional requirement — document retrieval.
//
// This introduces a NEW functional requirement, separate
// from document upload and processing-status tracking.
//
// Expected AI behavior:
// 1. Classify under FUNCTIONAL_REQUIREMENTS.
// 2. Create a new DraftItem with its own UUID.
// 3. Preserve the existing upload and status requirements.
        transcript.addSegment(new TranscriptSegment(
                new TimestampRange(
                        Instant.parse("2026-11-18T10:05:20Z"),
                        Instant.parse("2026-11-18T10:05:40Z")
                ),
                "Users should be able to download documents they have previously uploaded, provided they are authorized to access those documents."
        ));

        // Segment 18: Functional requirement — document deletion.
//
// This introduces a NEW functional requirement.
// It is separate from document retrieval and the unresolved
// question about automatic document retention.
//
// Expected AI behavior:
// 1. Classify under FUNCTIONAL_REQUIREMENTS.
// 2. Create a new DraftItem with its own UUID.
// 3. Preserve the existing upload, status, and retrieval items.
        transcript.addSegment(new TranscriptSegment(
                new TimestampRange(
                        Instant.parse("2026-11-18T10:05:40Z"),
                        Instant.parse("2026-11-18T10:06:00Z")
                ),
                "Users should be able to permanently delete documents they previously uploaded, provided they are authorized to delete those documents."
        ));

        // Segment 19: Architectural decision — metadata database.
//
// Document files will be stored in cloud object storage,
// while their metadata will be stored separately.
//
// Expected AI behavior:
// 1. Classify under DECISIONS.
// 2. Create a new DraftItem with its own UUID.
// 3. Preserve the existing processing, queue, and retry decisions.
        transcript.addSegment(new TranscriptSegment(
                new TimestampRange(
                        Instant.parse("2026-11-18T10:06:00Z"),
                        Instant.parse("2026-11-18T10:06:20Z")
                ),
                "We have decided to use PostgreSQL to store document metadata, including document identifiers, owners, filenames, storage locations, and processing statuses."
        ));

        // Segment 20: Non-functional requirement — audit logging.
//
// This introduces a NEW requirement for traceability.
//
// Expected AI behavior:
// 1. Classify under NON_FUNCTIONAL_REQUIREMENTS.
// 2. Create a new DraftItem with its own UUID.
// 3. Preserve the existing size, security, and performance requirements.
        transcript.addSegment(new TranscriptSegment(
                new TimestampRange(
                        Instant.parse("2026-11-18T10:06:20Z"),
                        Instant.parse("2026-11-18T10:06:40Z")
                ),
                "The system must maintain audit logs recording which user uploaded, downloaded, or deleted a document, along with the operation timestamp."
        ));


        // Create the orchestrator using the real Groq AI service.
// The speech-to-text service is provided as a dependency,
// but it will not be called because we supply a transcript.
        DesignCaptureOrchestrator orchestrator =
                new DesignCaptureOrchestrator(
                        new GroqSpeechToTextService(),
                        new GroqCaptureDesignAIService()
                );

// Process the complete timestamped transcript.
        DesignSession session =
                orchestrator.captureDesignSessionFromTranscript(
                        "Document Upload Service",
                        DesignType.EXPLORATORY,
                        transcript
                );

// Display the resulting draft and thought histories.
        DesignSessionPrinter.printSession(session);

        // Persist the complete design session as JSON.
        DesignSessionJsonWriter.writeSession(
                session,
                Path.of("data/output/document-upload-design.json")
        );
        System.out.println(
                "Document Upload Service - End-to-End POC"
        );
        // Next step: construct the timestamped transcript.
    }
}