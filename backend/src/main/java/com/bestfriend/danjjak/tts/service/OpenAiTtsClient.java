package com.bestfriend.danjjak.tts.service;

import com.bestfriend.danjjak.common.error.ApiException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class OpenAiTtsClient implements TtsClient {

    private static final Logger log = LogManager.getLogger(OpenAiTtsClient.class);
    private static final URI SPEECH_URI = URI.create("https://api.openai.com/v1/audio/speech");

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;

    public OpenAiTtsClient() {
        this(
            HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build(),
            new ObjectMapper(),
            System.getenv("OPENAI_API_KEY"));
    }

    OpenAiTtsClient(HttpClient httpClient, ObjectMapper objectMapper, String apiKey) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
    }

    @Override
    public byte[] createSpeech(String text, double speed) {
        if (apiKey == null || apiKey.isBlank()) {
            throw providerError();
        }

        try {
            HttpRequest request =
                HttpRequest.newBuilder(SPEECH_URI)
                    .timeout(Duration.ofSeconds(30))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(createRequestBody(text, speed)))
                    .build();
            HttpResponse<byte[]> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                logProviderError(response);
                throw providerError();
            }
            if (response.body().length == 0) {
                throw providerError();
            }
            return response.body();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw providerError();
        } catch (IOException | RuntimeException exception) {
            if (exception instanceof ApiException apiException) {
                throw apiException;
            }
            throw providerError();
        }
    }

    private void logProviderError(HttpResponse<byte[]> response) {
        String code = null;
        String type = null;

        try {
            JsonNode error = objectMapper.readTree(response.body()).path("error");
            code = textValue(error, "code");
            type = textValue(error, "type");
        } catch (IOException | RuntimeException exception) {
            log.warn("OpenAI TTS 오류 응답을 파싱하지 못했습니다: status={}", response.statusCode());
        }

        log.warn(
            "OpenAI TTS 요청 실패: status={}, code={}, type={}",
            response.statusCode(),
            code,
            type);
    }

    private String textValue(JsonNode node, String fieldName) {
        JsonNode value = node.path(fieldName);
        return value.isTextual() ? value.textValue() : null;
    }

    private String createRequestBody(String text, double speed) throws JsonProcessingException {
        ObjectNode body = objectMapper.createObjectNode();
        body.put("model", "gpt-4o-mini-tts");
        body.put("voice", "marin");
        body.put("input", text);
        body.put(
            "instructions",
            "Speak in Korean with a warm, friendly, and calm tone. "
                + "Speak clearly at a natural conversational pace. "
                + "Use natural pauses between sentences. "
                + "Sound like a kind family member giving simple guidance. "
                + "Do not sound robotic or overly cheerful."
        );
        body.put("response_format", "mp3");
        body.put("speed", speed);
        return objectMapper.writeValueAsString(body);
    }

    private ApiException providerError() {
        return new ApiException(
            HttpStatus.BAD_GATEWAY,
            "TTS_PROVIDER_ERROR",
            "음성 안내를 생성하지 못했습니다. 잠시 후 다시 시도해 주세요.");
    }
}
