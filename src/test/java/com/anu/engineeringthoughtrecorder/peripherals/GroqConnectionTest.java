package com.anu.engineeringthoughtrecorder.peripherals;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class GroqConnectionTest {

    public static void main(String[] args) throws Exception {

        String apiKey = System.getenv("GROQ_API_KEY");

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("GROQ_API_KEY is not configured");
        }

        String requestBody = """
                {
                  "model": "openai/gpt-oss-20b",
                  "messages": [
                    {
                      "role": "user",
                      "content": "Say hello from EngineeringThoughtRecorder."
                    }
                  ],
                  "max_completion_tokens": 500,
                  "reasoning_effort": "low"
                }
                """;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.groq.com/openai/v1/chat/completions"))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(60))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        try (HttpClient client = HttpClient.newHttpClient()) {

            System.out.println("Calling Groq...");
            long start = System.currentTimeMillis();

            HttpResponse<String> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            System.out.println("Groq responded in "
                    + (System.currentTimeMillis() - start) + " ms");

            System.out.println("HTTP status: " + response.statusCode());
            System.out.println("Response: " + response.body());
        }
    }
}