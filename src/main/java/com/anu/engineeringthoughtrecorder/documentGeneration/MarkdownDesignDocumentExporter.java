package com.anu.engineeringthoughtrecorder.documentGeneration;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class MarkdownDesignDocumentExporter
        implements DesignDocumentExporter {

    @Override
    public void export(String content, Path outputPath) {
        if (content == null || outputPath == null) {
            throw new IllegalArgumentException(
                    "Document content and output path cannot be null"
            );
        }

        try {
            Path parentDirectory = outputPath.getParent();

            if (parentDirectory != null) {
                Files.createDirectories(parentDirectory);
            }

            Files.writeString(
                    outputPath,
                    content,
                    StandardCharsets.UTF_8
            );

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to export Markdown document: " + outputPath,
                    e
            );
        }
    }
}