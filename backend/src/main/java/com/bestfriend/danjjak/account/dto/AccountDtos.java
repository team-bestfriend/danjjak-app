package com.bestfriend.danjjak.account.dto;

import com.bestfriend.danjjak.common.error.ApiException;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import org.springframework.http.HttpStatus;

public final class AccountDtos {

    private AccountDtos() {}

    public record OwnedAccountResponse(
            String accountId,
            String bankCode,
            String bankName,
            String maskedAccountNumber,
            String accountAlias,
            BigDecimal balance,
            boolean primary) {}

    public record ImportCandidateResponse(
            String accountId,
            String bankCode,
            String bankName,
            String maskedAccountNumber,
            String accountAlias,
            boolean available,
            String unavailableReason) {}

    public record AccountImportRequest(
            @NotEmpty
            @JsonDeserialize(contentUsing = StrictIdDeserializer.class)
            List<@NotBlank @Pattern(regexp = "^[1-9][0-9]{0,18}$") String> accountIds) {
        @JsonAnySetter
        public void rejectUnknownField(String name, Object value) {
            throw new IllegalArgumentException("허용되지 않은 요청 필드입니다.");
        }
    }

    public record DefaultAccountRequest(
            @NotBlank @Pattern(regexp = "^[1-9][0-9]{0,18}$")
            @JsonDeserialize(using = StrictIdDeserializer.class) String accountId) {
        @JsonAnySetter
        public void rejectUnknownField(String name, Object value) {
            throw new IllegalArgumentException("허용되지 않은 요청 필드입니다.");
        }
    }

    /** 숫자 JSON 값을 문자열 식별자로 강제 변환하지 않습니다. */
    public static final class StrictIdDeserializer extends JsonDeserializer<String> {
        @Override
        public String deserialize(JsonParser parser, DeserializationContext context)
                throws IOException {
            if (!parser.hasToken(JsonToken.VALUE_STRING)) {
                return (String) context.handleUnexpectedToken(String.class, parser);
            }
            return parser.getText();
        }
    }

    public record RecipientAccountResponse(
            String accountId,
            String bankCode,
            String bankName,
            String maskedAccountNumber,
            String accountAlias) {}

    public record RegisteredPersonResponse(
            String registeredPersonId,
            String name,
            String relationship,
            List<RecipientAccountResponse> accounts) {}

    public record RegisteredPersonUpdateRequest(
            @NotBlank @Size(max = 50) String name,
            @NotBlank @Size(max = 30) String relationship) {
        @JsonAnySetter
        public void rejectUnknownField(String name, Object value) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_REQUEST",
                    "이름과 관계만 수정할 수 있어요. 계좌 변경은 해당 계좌에서 따로 진행해 주세요.");
        }
    }

    public record RegisteredPersonRequest(
            @NotBlank @Size(max = 50) String name,
            @NotBlank @Size(max = 30) String relationship,
            @NotBlank @Size(max = 20) String bankCode,
            @NotBlank @Size(max = 50) String bankName,
            @NotBlank
                    @Size(min = 8, max = 50)
                    @Pattern(regexp = "^[0-9-]+$")
                    String accountNumber,
            @Size(max = 50) String accountAlias) {}

    public record BalanceResponse(String accountId, BigDecimal balance) {}

    public record TransactionResponse(
            long transactionId,
            String transactionType,
            String category,
            BigDecimal amount,
            String counterpartyName,
            String description,
            BigDecimal balanceAfter,
            LocalDateTime transactionAt) {}
}
