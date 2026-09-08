package com.bestfriend.danjjak.common.session;

import com.bestfriend.danjjak.common.error.ApiException;
import javax.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class DemoSessionUserResolver {

    public static final String USER_ID_ATTRIBUTE = "userId";
    public static final String CSRF_TOKEN_ATTRIBUTE = "csrfToken";
    public static final String KAKAO_ACCESS_TOKEN_ATTRIBUTE = "kakaoAccessToken";
    public static final String KAKAO_REFRESH_TOKEN_ATTRIBUTE = "kakaoRefreshToken";

    public long resolveUserId(HttpSession session) {
        Object value = attribute(session, USER_ID_ATTRIBUTE);
        if (value instanceof Long || value instanceof Integer) {
            long userId = ((Number) value).longValue();
            if (userId > 0) {
                return userId;
            }
        }
        if (value instanceof String text && text.matches("^[1-9][0-9]{0,18}$")) {
            try {
                return Long.parseLong(text);
            } catch (NumberFormatException ignored) {
                // 손상된 세션 식별자는 인증 실패로 처리합니다.
            }
        }
        throw new ApiException(
                HttpStatus.UNAUTHORIZED,
                "SESSION_REQUIRED",
                "로그인이 필요합니다. 카카오 로그인 후 다시 시도해 주세요.");
    }

    public String resolveKakaoAccessToken(HttpSession session) {
        Object value = attribute(session, KAKAO_ACCESS_TOKEN_ATTRIBUTE);
        return value instanceof String text && !text.isBlank() ? text : null;
    }

    public void validateCsrfToken(HttpSession session, String suppliedToken) {
        Object value = attribute(session, CSRF_TOKEN_ATTRIBUTE);
        if (!(value instanceof String expectedToken) || expectedToken.isBlank()
                || suppliedToken == null || suppliedToken.isBlank()
                || !MessageDigest.isEqual(expectedToken.getBytes(StandardCharsets.UTF_8),
                        suppliedToken.getBytes(StandardCharsets.UTF_8))) {
            throw new ApiException(HttpStatus.FORBIDDEN, "CSRF_INVALID",
                    "요청을 확인할 수 없어요. 화면을 새로고침한 뒤 다시 시도해 주세요.");
        }
    }

    private Object attribute(HttpSession session, String name) {
        if (session == null) {
            return null;
        }
        try {
            return session.getAttribute(name);
        } catch (IllegalStateException exception) {
            return null;
        }
    }
}
