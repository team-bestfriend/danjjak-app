package com.bestfriend.danjjak.support.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.io.IOException;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public final class SupportDtos {

    private SupportDtos() {}

    public static final String PHONE_NUMBER_PATTERN = "^[0-9]+(?:-[0-9]+)*$";

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record GuardianContactResponse(String phoneNumber) {}

    public record GuardianContactRequest(
            @NotNull @Size(min = 5, max = 30)
            @Pattern(regexp = PHONE_NUMBER_PATTERN,
                    message = "전화번호는 숫자 또는 숫자 묶음 사이 하이픈만 사용할 수 있습니다.")
            @JsonDeserialize(using = StrictStringDeserializer.class)
            String phoneNumber) {
        @JsonAnySetter
        public void rejectUnknownField(String name, Object value) {
            throw new IllegalArgumentException("전화번호만 수정할 수 있습니다.");
        }
    }

    public record CustomerCenterResponse(String name, String phoneNumber) {}

    public record SupportResponse(
            GuardianContactResponse guardian,
            CustomerCenterResponse customerCenter,
            String notificationRecipient) {}

    public record NotificationRequest(
            @NotNull @AssertTrue
            @JsonDeserialize(using = StrictBooleanDeserializer.class)
            Boolean confirmedSelfDemo) {
        @JsonAnySetter
        public void rejectUnknownField(String name, Object value) {
            throw new IllegalArgumentException("본인 전송 시연 확인만 제출할 수 있습니다.");
        }
    }

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record NotificationResponse(
            String mode, String message, String sentAt, String recipient) {}

    /** 숫자나 불리언을 전화번호 문자열로 강제 변환하지 않습니다. */
    public static final class StrictStringDeserializer extends JsonDeserializer<String> {
        @Override
        public String deserialize(JsonParser parser, DeserializationContext context)
                throws IOException {
            if (!parser.hasToken(JsonToken.VALUE_STRING)) {
                return (String) context.handleUnexpectedToken(String.class, parser);
            }
            return parser.getText();
        }
    }

    /** 문자열이나 숫자를 본인 전송 동의로 강제 변환하지 않습니다. */
    public static final class StrictBooleanDeserializer extends JsonDeserializer<Boolean> {
        @Override
        public Boolean deserialize(JsonParser parser, DeserializationContext context)
                throws IOException {
            if (!parser.hasToken(JsonToken.VALUE_TRUE) && !parser.hasToken(JsonToken.VALUE_FALSE)) {
                return (Boolean) context.handleUnexpectedToken(Boolean.class, parser);
            }
            return parser.getBooleanValue();
        }
    }
}
