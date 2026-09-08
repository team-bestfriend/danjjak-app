package com.bestfriend.danjjak.tts.service;

import com.bestfriend.danjjak.tts.dto.TtsDtos.TtsRequest;
import org.springframework.stereotype.Service;

@Service
public class TtsService {

    private final TtsClient ttsClient;

    public TtsService(TtsClient ttsClient) {
        this.ttsClient = ttsClient;
    }

    public byte[] createSpeech(TtsRequest request) {
        return ttsClient.createSpeech(request.text().trim(), request.speed().getValue());
    }
}
