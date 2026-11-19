package com.anu.engineeringthoughtrecorder.documentGeneration;

import java.nio.file.Path;

public interface DesignDocumentExporter {

    void export(String content, Path outputPath);

}