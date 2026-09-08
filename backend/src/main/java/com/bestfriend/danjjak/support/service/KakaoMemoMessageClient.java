package com.bestfriend.danjjak.support.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class KakaoMemoMessageClient implements KakaoMessageClient {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final URI messageUri;
    private final String linkUrl;

    public KakaoMemoMessageClient(Environment environment) {
        this.httpClient =
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
        this.objectMapper = new ObjectMapper();
        this.messageUri = httpUri(environment.getProperty("kakao.message-url", ""));
        this.linkUrl = environment.getProperty("kakao.message-link-url", "");
    }

    @Override
    public KakaoSendResult sendToMe(String accessToken, String message) {
        if (accessToken == null || accessToken.isBlank()
                || messageUri == null || httpUri(linkUrl) == null) {
            return new KakaoSendResult(false, null, NOT_CONFIGURED);
        }
        HttpRequest request;
        try {
            String template = createTextTemplate(message);
            String body =
                    "template_object="
                            + URLEncoder.encode(template, StandardCharsets.UTF_8);
            request =
                    HttpRequest.newBuilder(messageUri)
                            .timeout(Duration.ofSeconds(5))
                            .header("Authorization", "Bearer " + accessToken)
                            .header(
                                    "Content-Type",
                                    "application/x-www-form-urlencoded;charset=utf-8")
                            .POST(HttpRequest.BodyPublishers.ofString(body))
                            .build();
        } catch (IOException | RuntimeException exception) {
            // 요청 생성 실패는 외부 호출 이전이므로 미전송이 확실합니다.
            return new KakaoSendResult(false, null, REQUEST_NOT_SENT);
        }
        try {
            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return new KakaoSendResult(false, response.statusCode(), "KAKAO_API_REJECTED");
            }
            JsonNode responseBody = objectMapper.readTree(response.body());
            JsonNode resultCode = responseBody == null ? null : responseBody.get("result_code");
            if (resultCode == null || !resultCode.isIntegralNumber() || !resultCode.canConvertToInt()) {
                return new KakaoSendResult(false, response.statusCode(), RESULT_UNKNOWN);
            }
            boolean success = resultCode.intValue() == 0;
            return new KakaoSendResult(
                    success,
                    response.statusCode(),
                    success ? "KAKAO_SENT" : "KAKAO_API_REJECTED");
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return new KakaoSendResult(false, null, RESULT_UNKNOWN);
        } catch (IOException | RuntimeException exception) {
            // 시간 초과·연결 단절·성공 응답 해석 실패 뒤에는 실제 미전송을 단정할 수 없습니다.
            return new KakaoSendResult(false, null, RESULT_UNKNOWN);
        }
    }

    private URI httpUri(String value) {
        try {
            URI uri = URI.create(value);
            return ("https".equalsIgnoreCase(uri.getScheme()) || "http".equalsIgnoreCase(uri.getScheme()))
                    && uri.getHost() != null ? uri : null;
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private String createTextTemplate(String message) throws IOException {
        ObjectNode template = objectMapper.createObjectNode();
        template.put("object_type", "text");
        template.put("text", message);
        ObjectNode link = template.putObject("link");
        link.put("web_url", linkUrl);
        link.put("mobile_web_url", linkUrl);
        template.put("button_title", "단짝 확인");
        return objectMapper.writeValueAsString(template);
    }
}
