package com.anu.engineeringthoughtrecorder.peripherals;

import com.anu.engineeringthoughtrecorder.domainObjects.Transcript;

public interface SpeechToTextService {

    Transcript transcribe(AudioInput audioInput);

}
