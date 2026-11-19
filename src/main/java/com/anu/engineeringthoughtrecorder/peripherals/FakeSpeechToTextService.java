package com.anu.engineeringthoughtrecorder.peripherals;

import com.anu.engineeringthoughtrecorder.domainObjects.Transcript;

public class FakeSpeechToTextService implements SpeechToTextService {

    private final Transcript transcript;

    public FakeSpeechToTextService(Transcript transcript) {
        this.transcript = transcript;
    }

    @Override
    public Transcript transcribe(AudioInput audioInput) {
        return transcript;
    }
}