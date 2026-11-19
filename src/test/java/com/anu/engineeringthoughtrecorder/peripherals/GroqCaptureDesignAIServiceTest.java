
package com.anu.engineeringthoughtrecorder.peripherals;

public class GroqCaptureDesignAIServiceTest {

    public static void main(String[] args) {

        // Verify that Groq updates an existing functional requirement.
        GroqCaptureDesignAITestMethods.testExistingRequirementUpdate();
        System.out.println("PASS: Existing requirement update");

        // Verify that Groq creates a new requirement when the draft is empty.
        GroqCaptureDesignAITestMethods.testNewRequirementCreation();
        System.out.println("PASS: New requirement creation");

        System.out.println("All GroqCaptureDesignAIService tests passed!");
    }
}
