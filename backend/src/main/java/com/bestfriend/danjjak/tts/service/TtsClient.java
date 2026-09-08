package com.bestfriend.danjjak.tts.service;

public interface TtsClient {

    byte[] createSpeech(String text, double speed);
}
