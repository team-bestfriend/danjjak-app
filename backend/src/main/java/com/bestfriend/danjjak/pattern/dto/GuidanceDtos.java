package com.bestfriend.danjjak.pattern.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public final class GuidanceDtos {
    private GuidanceDtos() {}

    public enum VoiceMode { TTS, FAMILY }

    public record GuidanceUpdateRequest(
        @NotBlank @Size(max = 500) String text, VoiceMode voiceMode) {}

    public record GuidanceResponse(
        String target, String text, String voiceMode, String audioUrl,
        String voiceContentType, boolean voiceScriptOutdated) {}
}
