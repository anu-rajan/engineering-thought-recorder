
package com.anu.engineeringthoughtrecorder.application;

import com.anu.engineeringthoughtrecorder.domainObjects.DesignSession;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class DesignSessionJsonWriter {

    private static final ObjectMapper OBJECT_MAPPER =
            new ObjectMapper()
                    .registerModule(new JavaTimeModule())
                    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                    .enable(SerializationFeature.INDENT_OUTPUT);

    public static void writeSession(
            DesignSession session,
            Path outputPath
    ) throws IOException {

        if (session == null) {
            throw new IllegalArgumentException(
                    "Design session cannot be null"
            );
        }

        if (outputPath == null) {
            throw new IllegalArgumentException(
                    "Output path cannot be null"
            );
        }

        Path parent = outputPath.getParent();

        if (parent != null) {
            Files.createDirectories(parent);
        }

        OBJECT_MAPPER.writeValue(
                outputPath.toFile(),
                session
        );
    }

}
