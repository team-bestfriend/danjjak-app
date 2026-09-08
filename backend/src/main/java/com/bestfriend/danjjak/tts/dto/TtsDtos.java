package com.bestfriend.danjjak.tts.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public final class TtsDtos {

    private TtsDtos() {}

    public record TtsRequest(
        @NotBlank @Size(max = 1000) String text,
        @NotNull TtsSpeed speed) {}

    public enum TtsSpeed {
        SLOW(0.8),
        NORMAL(1.0),
        FAST(1.2);

        private final double value;

        TtsSpeed(double value) {
            this.value = value;
        }

        public double getValue() {
            return value;
        }
    }
}
