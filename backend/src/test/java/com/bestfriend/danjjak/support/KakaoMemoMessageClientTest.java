package com.bestfriend.danjjak.support;

import static org.junit.jupiter.api.Assertions.*;

import com.bestfriend.danjjak.common.error.ApiException;
import com.bestfriend.danjjak.support.mapper.SupportMapper;
import com.bestfriend.danjjak.support.service.KakaoMemoMessageClient;
import com.bestfriend.danjjak.support.service.KakaoMessageClient;
import com.bestfriend.danjjak.support.service.SupportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.env.MockEnvironment;

/** 로컬 HTTP 서버만 사용하여 카카오 요청 형식과 확정 실패·불명확 결과를 구분합니다. */
class KakaoMemoMessageClientTest {
    private HttpServer server;
    private String endpoint;
    private final AtomicInteger calls = new AtomicInteger();
    private final AtomicInteger status = new AtomicInteger(200);
    private final AtomicReference<String> response = new AtomicReference<>("{\"result_code\":0}");
    private final AtomicReference<String> body = new AtomicReference<>();
    private final AtomicReference<String> authorization = new AtomicReference<>();

    @BeforeEach void start() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/memo", exchange -> {
            calls.incrementAndGet();
            authorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
            body.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            byte[] bytes = response.get().getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(status.get(), bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });
        server.start();
        endpoint = "http://127.0.0.1:" + server.getAddress().getPort() + "/memo";
    }
    @AfterEach void stop() { server.stop(0); }

    private KakaoMemoMessageClient client() {
        return new KakaoMemoMessageClient(new MockEnvironment()
                .withProperty("kakao.message-url", endpoint)
                .withProperty("kakao.message-link-url", "https://example.invalid/demo"));
    }

    @Test void encodesOnlySelfMemoTemplateAndAcceptsExplicitSuccess() throws Exception {
        var result = client().sendToMe("fake-test-token", "본인 전송 시연 & 확인\n금액: 10,000,000원");
        assertTrue(result.success());
        assertEquals(1, calls.get());
        assertEquals("Bearer fake-test-token", authorization.get());
        assertTrue(body.get().startsWith("template_object="));
        var template = new ObjectMapper().readTree(URLDecoder.decode(body.get().substring(16), StandardCharsets.UTF_8));
        assertEquals("text", template.path("object_type").asText());
        assertEquals("본인 전송 시연 & 확인\n금액: 10,000,000원", template.path("text").asText());
        assertFalse(template.has("receiver_uuids"));
        assertFalse(template.has("phoneNumber"));
    }

    @Test void rejectsDefiniteHttpAndApiFailuresWithoutRetry() {
        status.set(401);
        var rejected = client().sendToMe("fake-test-token", "시연");
        assertFalse(rejected.success());
        assertEquals(401, rejected.httpStatus());
        assertEquals("KAKAO_API_REJECTED", rejected.detail());
        status.set(200);
        response.set("{\"result_code\":-1}");
        assertEquals("KAKAO_API_REJECTED", client().sendToMe("fake-test-token", "시연").detail());
        assertEquals(2, calls.get());
    }

    @Test void malformedSuccessCannotBecomeDefiniteFailureOrSuccess() {
        for (String payload : new String[] {"{}", "null", "not-json", "{\"result_code\":\"0\"}", "{\"result_code\":0.0}"}) {
            response.set(payload);
            var result = client().sendToMe("fake-test-token", "시연");
            assertFalse(result.success());
            assertEquals(KakaoMessageClient.RESULT_UNKNOWN, result.detail());
        }
        assertEquals(5, calls.get());
    }

    @Test void missingConfigurationNeverCallsExternalService() {
        assertEquals(KakaoMessageClient.NOT_CONFIGURED,
                new KakaoMemoMessageClient(new MockEnvironment()).sendToMe("fake-test-token", "시연").detail());
        assertEquals(KakaoMessageClient.NOT_CONFIGURED, client().sendToMe("", "시연").detail());
        assertEquals(0, calls.get());
    }

    @Test void timeoutAfterRequestArrivesIsUnknownAndNeverAutomaticallyRetried() {
        CountDownLatch release = new CountDownLatch(1);
        server.createContext("/slow", exchange -> {
            calls.incrementAndGet();
            try { release.await(10, TimeUnit.SECONDS); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            finally { exchange.close(); }
        });
        endpoint = endpoint.replace("/memo", "/slow");
        try {
            var result = client().sendToMe("fake-test-token", "시연");
            assertFalse(result.success());
            assertEquals(KakaoMessageClient.RESULT_UNKNOWN, result.detail());
            assertEquals(1, calls.get());
        } finally { release.countDown(); }
    }

    @Test void supportWithoutCustomerPhoneReturns503InsteadOfInventingNumber() {
        SupportMapper mapper = (SupportMapper) Proxy.newProxyInstance(
                SupportMapper.class.getClassLoader(), new Class<?>[] {SupportMapper.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("findConsentCompleted")) return Boolean.TRUE;
                    throw new AssertionError("설정 누락 뒤에는 연락처를 조회하지 않습니다.");
                });
        var service = new SupportService(mapper, new MockEnvironment());
        ApiException failure = assertThrows(ApiException.class, () -> service.getSupport(1));
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, failure.getStatus());
        assertEquals("SUPPORT_NOT_CONFIGURED", failure.getCode());
    }
}
