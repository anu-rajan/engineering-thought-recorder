package com.anu.engineeringthoughtrecorder.peripherals;


import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.ThinkingConfig;

public class GeminiConnectionTest {

    public static void main(String[] args) {

        try (Client client = Client.builder()
                .apiKey(System.getenv("GEMINI_API_KEY"))
                .build()) {

            System.out.println("Calling Gemini...");
            long start = System.currentTimeMillis();

            GenerateContentConfig config = GenerateContentConfig.builder()
                    .thinkingConfig(ThinkingConfig.builder()
                            .thinkingLevel("low")
                            .build())
                    .build();

            GenerateContentResponse response =
                    client.models.generateContent(
                            "gemini-3.8-flash",
                            "Say hello from EngineeringThoughtRecorder.",
                            null);
            System.out.println("Gemini responded in " +
                    (System.currentTimeMillis() - start) + " ms");
            System.out.println(response.text());
        }
    }
}