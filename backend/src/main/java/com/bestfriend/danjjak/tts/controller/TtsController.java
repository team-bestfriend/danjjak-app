package com.bestfriend.danjjak.tts.controller;

import com.bestfriend.danjjak.common.session.DemoSessionUserResolver;
import com.bestfriend.danjjak.tts.dto.TtsDtos.TtsRequest;
import com.bestfriend.danjjak.tts.service.TtsService;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tts")
public class TtsController {

    private final TtsService ttsService;
    private final DemoSessionUserResolver userResolver;

    public TtsController(TtsService ttsService, DemoSessionUserResolver userResolver) {
        this.ttsService = ttsService;
        this.userResolver = userResolver;
    }

    @PostMapping(produces = "audio/mpeg")
    public ResponseEntity<byte[]> createSpeech(
        @Valid @RequestBody TtsRequest request, HttpSession session) {
        userResolver.resolveUserId(session);
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType("audio/mpeg"))
            .cacheControl(CacheControl.noStore())
            .body(ttsService.createSpeech(request));
    }
}
