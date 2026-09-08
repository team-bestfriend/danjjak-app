package com.bestfriend.danjjak.support;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import com.bestfriend.danjjak.config.RootConfig;
import com.bestfriend.danjjak.config.WebConfig;
import com.bestfriend.danjjak.support.service.KakaoMessageClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

/** 실제 MySQL과 HTTP 경계에서 연락처 보존과 본인 전송 시연을 검증합니다. 외부 메시지는 보내지 않습니다. */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {RootConfig.class, WebConfig.class, SupportIntegrationTest.ClientConfig.class})
@WebAppConfiguration
@TestPropertySource(properties = "support.customer-center-phone=00000")
@EnabledIfEnvironmentVariable(named = "DANJJAK_DB_INTEGRATION_TEST", matches = "true")
@Transactional
class SupportIntegrationTest {
    @Autowired private WebApplicationContext context;
    @Autowired private DataSource dataSource;
    @Autowired private FakeClient client;
    @Autowired private org.apache.ibatis.session.SqlSessionFactory sqlSessionFactory;
    private final ObjectMapper json = new ObjectMapper();
    private JdbcTemplate jdbc;
    private MockMvc mvc;
    private MockHttpSession session;

    @Configuration
    static class ClientConfig {
        @Bean @Primary FakeClient fakeKakaoClient() { return new FakeClient(); }
    }

    static class FakeClient implements KakaoMessageClient {
        final AtomicInteger calls = new AtomicInteger();
        volatile KakaoSendResult result;
        volatile String message;
        volatile CountDownLatch entered;
        volatile CountDownLatch release;
        void reset() {
            calls.set(0);
            result = new KakaoSendResult(true, 200, "OK");
            message = null;
            entered = null;
            release = null;
        }
        @Override public KakaoSendResult sendToMe(String token, String text) {
            assertEquals("fake-test-token", token);
            calls.incrementAndGet();
            message = text;
            if (entered != null) entered.countDown();
            if (release != null) {
                try { assertTrue(release.await(10, TimeUnit.SECONDS)); }
                catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new AssertionError(e); }
            }
            return result;
        }
    }

    @BeforeEach void prepare() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
        jdbc = new JdbcTemplate(dataSource);
        client.reset();
        session = sessionFor(1);
        if (TestTransaction.isActive()) {
            jdbc.update("UPDATE users SET consent_completed = TRUE, guardian_share_agreed = FALSE, usage_log_agreed = FALSE WHERE user_id = 1");
        }
    }

    @Test void savesExactPhoneAndKeepsOneContactWithoutFinancialChanges() throws Exception {
        var balances = jdbc.queryForList("SELECT * FROM bank_accounts ORDER BY bank_account_id");
        var transactions = jdbc.queryForList("SELECT * FROM transactions ORDER BY transaction_id");
        var patterns = jdbc.queryForList("SELECT * FROM financial_patterns ORDER BY financial_pattern_id");
        for (String phone : List.of("00000", "010-0000-1004", "0".repeat(30))) {
            JsonNode saved = perform(guardian(json.writeValueAsString(phone)), 200);
            assertEquals(phone, saved.path("phoneNumber").asText());
            assertEquals(1, saved.size());
            session = sessionFor(1);
            JsonNode support = perform(get("/api/support"), 200);
            assertEquals(saved, support.path("guardian"));
            assertEquals("SELF", support.path("notificationRecipient").asText());
            assertEquals("00000", support.path("customerCenter").path("phoneNumber").asText());
            assertEquals(3, support.size());
        }
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM guardian_contacts WHERE user_id = 1", Integer.class));
        assertEquals(balances, jdbc.queryForList("SELECT * FROM bank_accounts ORDER BY bank_account_id"));
        assertEquals(transactions, jdbc.queryForList("SELECT * FROM transactions ORDER BY transaction_id"));
        assertEquals(patterns, jdbc.queryForList("SELECT * FROM financial_patterns ORDER BY financial_pattern_id"));
    }

    @Test void rejectsMalformedAndCoercedPhonesWithoutSaving() throws Exception {
        var before = jdbc.queryForList("SELECT * FROM guardian_contacts ORDER BY guardian_contact_id");
        for (String value : List.of("\"0000\"", "\"" + "0".repeat(31) + "\"", "\"-00000\"", "\"00000-\"", "\"000--00\"", "\" 00000\"", "\"tel:00000\"", "\"０００００\"", "12345", "true", "null", "[]", "{}")) {
            assertEquals("INVALID_REQUEST", perform(guardian(value), 400).path("code").asText());
        }
        perform(put("/api/support/guardian").contentType(MediaType.APPLICATION_JSON).content("{\"phoneNumber\":\"00000\",\"userId\":2}"), 400);
        assertEquals(before, jdbc.queryForList("SELECT * FROM guardian_contacts ORDER BY guardian_contact_id"));
    }

    @Test void absentContactIsExplicitNullAndNeverAnotherUsersContact() throws Exception {
        String originalPhone = jdbc.queryForObject("SELECT phone_number FROM guardian_contacts WHERE user_id = 1", String.class);
        jdbc.update("INSERT INTO users (user_id, name, consent_completed) VALUES (20001, '연락처 검증', TRUE)");
        session = sessionFor(20001);
        JsonNode guardian = perform(get("/api/support"), 200).path("guardian");
        assertTrue(guardian.has("phoneNumber"));
        assertTrue(guardian.path("phoneNumber").isNull());
        perform(guardian("\"00000\""), 200);
        assertEquals(originalPhone, jdbc.queryForObject("SELECT phone_number FROM guardian_contacts WHERE user_id = 1", String.class));
    }

    @Test void requiresSessionCsrfAndCompletedChoiceButNotOptionalConsent() throws Exception {
        assertEquals(401, mvc.perform(get("/api/support")).andReturn().getResponse().getStatus());
        assertEquals(403, mvc.perform(guardian("\"00000\"").session(session)).andReturn().getResponse().getStatus());
        jdbc.update("UPDATE users SET consent_completed = FALSE WHERE user_id = 1");
        perform(get("/api/support"), 409);
        perform(guardian("\"00000\""), 409);
        jdbc.update("UPDATE users SET consent_completed = TRUE WHERE user_id = 1");
        perform(guardian("\"00000\""), 200);
    }

    @Test void mockWithoutCredentialsDoesNotPersistOrCallClient() throws Exception {
        long id = eligible();
        JsonNode result = perform(notification(id), 200);
        assertEquals("MOCK_NO_CREDENTIALS", result.path("mode").asText());
        assertTrue(result.path("sentAt").isNull());
        assertEquals("SELF", result.path("recipient").asText());
        assertEquals(4, result.size());
        assertEquals(0, client.calls.get());
        assertUnchanged(id);
    }

    @Test void actualSuccessReusesTimestampAndSendsOnlyMaskedSelfDemo() throws Exception {
        long id = eligible();
        session.setAttribute("kakaoAccessToken", "fake-test-token");
        var balances = jdbc.queryForList("SELECT bank_account_id, balance FROM bank_accounts ORDER BY bank_account_id");
        JsonNode first = perform(notification(id), 200);
        assertEquals("ACTUAL", first.path("mode").asText());
        assertDoesNotThrow(() -> java.time.Instant.parse(first.path("sentAt").asText()));
        assertEquals(first, perform(notification(id), 200));
        assertEquals(1, client.calls.get());
        assertTrue(client.message.contains("검증 수취인"));
        assertTrue(client.message.contains("10,000,000"));
        assertTrue(client.message.contains("1,000만원 이상"));
        assertTrue(client.message.contains("최근 10분"));
        assertTrue(client.message.contains("본인"));
        assertTrue(client.message.contains("*"));
        assertFalse(client.message.contains("12345678901234"));
        assertFalse(client.message.contains("010-0000-1004"));
        assertEquals(balances, jdbc.queryForList("SELECT bank_account_id, balance FROM bank_accounts ORDER BY bank_account_id"));
        assertNull(jdbc.queryForObject("SELECT final_action FROM anomaly_events WHERE anomaly_event_id = ?", String.class, id));
        assertNull(jdbc.queryForObject("SELECT transaction_id FROM anomaly_events WHERE anomaly_event_id = ?", Long.class, id));
    }

    @Test void definiteFailureIsMockWithoutPersistedSuccess() throws Exception {
        long id = eligible();
        session.setAttribute("kakaoAccessToken", "fake-test-token");
        client.result = new KakaoMessageClient.KakaoSendResult(false, 401, "KAKAO_API_REJECTED");
        JsonNode result = perform(notification(id), 200);
        assertEquals("MOCK_AFTER_FAILURE", result.path("mode").asText());
        assertTrue(result.path("sentAt").isNull());
        assertEquals(1, client.calls.get());
        assertUnchanged(id);
    }

    @Test void ambiguousFailureNeverClaimsMockOrRetriesAutomatically() throws Exception {
        long id = eligible();
        session.setAttribute("kakaoAccessToken", "fake-test-token");
        client.result = new KakaoMessageClient.KakaoSendResult(false, null, "KAKAO_REQUEST_FAILED");
        JsonNode result = perform(notification(id), 502);
        assertEquals("NOTIFICATION_RESULT_UNKNOWN", result.path("code").asText());
        assertFalse(result.path("retryable").asBoolean());
        assertEquals(1, client.calls.get());
        assertUnchanged(id);
    }

    @Test void rejectsMissingConfirmationConsentAndIneligibleAnomaliesBeforeSending() throws Exception {
        long id = eligible();
        session.setAttribute("kakaoAccessToken", "fake-test-token");
        for (String body : List.of("{}", "{\"confirmedSelfDemo\":false}", "{\"confirmedSelfDemo\":\"true\"}", "{\"confirmedSelfDemo\":true,\"phoneNumber\":\"00000\"}")) {
            perform(post(notificationUrl(id)).contentType(MediaType.APPLICATION_JSON).content(body), 400);
        }
        jdbc.update("UPDATE users SET guardian_share_agreed = FALSE WHERE user_id = 1");
        perform(notification(id), 403);
        jdbc.update("UPDATE users SET guardian_share_agreed = TRUE WHERE user_id = 1");
        perform(notification(999999999), 404);
        jdbc.update("UPDATE anomaly_events SET risk_level = 'MEDIUM', repeated_transfer_detected = FALSE WHERE anomaly_event_id = ?", id);
        perform(notification(id), 409);
        jdbc.update("UPDATE anomaly_events SET risk_level = 'HIGH', repeated_transfer_detected = TRUE, final_action = 'CANCEL', resolved_at = CURRENT_TIMESTAMP WHERE anomaly_event_id = ?", id);
        perform(notification(id), 409);
        jdbc.update("INSERT INTO users (user_id, name, consent_completed, guardian_share_agreed) VALUES (20002, '다른 검증 사용자', TRUE, TRUE)");
        session = sessionFor(20002);
        perform(notification(id), 404);
        assertEquals(0, client.calls.get());
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void concurrentActualRequestsSendOnceAndReuseCommittedResult() throws Exception {
        long user = 20020;
        var pool = Executors.newFixedThreadPool(2);
        long id = -1;
        try {
            jdbc.update("INSERT INTO users (user_id, name, consent_completed, guardian_share_agreed) VALUES (?, '알림 동시성 검증', TRUE, TRUE)", user);
            jdbc.update("INSERT INTO bank_accounts (bank_account_id, user_id, bank_code, bank_name, account_number, account_pin_hash, balance) VALUES (?, ?, '004', '검증 은행', '000000000', '검증용 해시', 123)", user, user);
            id = anomaly(user, user);
            long event = id;
            client.entered = new CountDownLatch(1);
            client.release = new CountDownLatch(1);
            var first = pool.submit(() -> invokeConcurrent(user, event));
            assertTrue(client.entered.await(5, TimeUnit.SECONDS));
            var second = pool.submit(() -> invokeConcurrent(user, event));
            client.release.countDown();
            JsonNode a = first.get(15, TimeUnit.SECONDS);
            JsonNode b = second.get(15, TimeUnit.SECONDS);
            assertEquals("ACTUAL", a.path("mode").asText());
            assertEquals(a, b);
            assertEquals(1, client.calls.get());
        } finally {
            if (client.release != null) client.release.countDown();
            pool.shutdownNow();
            assertTrue(pool.awaitTermination(20, TimeUnit.SECONDS));
            if (id > 0) jdbc.update("DELETE FROM anomaly_events WHERE anomaly_event_id = ? AND user_id = ?", id, user);
            jdbc.update("DELETE FROM bank_accounts WHERE bank_account_id = ? AND user_id = ?", user, user);
            jdbc.update("DELETE FROM users WHERE user_id = ? AND name = '알림 동시성 검증'", user);
        }
    }

    private JsonNode invokeConcurrent(long user, long id) throws Exception {
        MockHttpSession requestSession = sessionFor(user);
        requestSession.setAttribute("kakaoAccessToken", "fake-test-token");
        var response = mvc.perform(notification(id).session(requestSession).header("X-CSRF-Token", "issue20-test-csrf")).andReturn().getResponse();
        assertEquals(200, response.getStatus(), response.getContentAsString());
        return json.readTree(response.getContentAsByteArray());
    }

    private long eligible() {
        jdbc.update("UPDATE users SET guardian_share_agreed = TRUE WHERE user_id = 1");
        return anomaly(1, 1);
    }

    private long anomaly(long user, long account) {
        jdbc.update("INSERT INTO anomaly_events (user_id, source_bank_account_id, recipient_name, recipient_bank_code, recipient_bank_name, recipient_account_number, amount, high_amount_detected, repeated_transfer_detected, recent_transfer_count, risk_level) VALUES (?, ?, '검증 수취인', '004', '검증 은행', '12345678901234', 10000000, TRUE, TRUE, 3, 'HIGH')", user, account);
        return jdbc.queryForObject("SELECT MAX(anomaly_event_id) FROM anomaly_events WHERE user_id = ?", Long.class, user);
    }

    private void assertUnchanged(long id) {
        assertNull(jdbc.queryForObject("SELECT guardian_notified_at FROM anomaly_events WHERE anomaly_event_id = ?", java.sql.Timestamp.class, id));
        assertNull(jdbc.queryForObject("SELECT final_action FROM anomaly_events WHERE anomaly_event_id = ?", String.class, id));
        assertNull(jdbc.queryForObject("SELECT transaction_id FROM anomaly_events WHERE anomaly_event_id = ?", Long.class, id));
    }

    private MockHttpSession sessionFor(long user) {
        MockHttpSession value = new MockHttpSession();
        value.setAttribute("userId", user);
        value.setAttribute("csrfToken", "issue20-test-csrf");
        return value;
    }
    private String notificationUrl(long id) { return "/api/anomaly-events/" + id + "/guardian-notification"; }
    private MockHttpServletRequestBuilder notification(long id) {
        return post(notificationUrl(id)).contentType(MediaType.APPLICATION_JSON).content("{\"confirmedSelfDemo\":true}");
    }
    private MockHttpServletRequestBuilder guardian(String phoneJson) {
        return put("/api/support/guardian").contentType(MediaType.APPLICATION_JSON).content("{\"phoneNumber\":" + phoneJson + "}");
    }
    private JsonNode perform(MockHttpServletRequestBuilder request, int expected) throws Exception {
        // 테스트만 공유하는 트랜잭션의 MyBatis 캐시를 비워 실제 별도 HTTP 요청의 조회 경계를 재현합니다.
        var sqlSession = org.mybatis.spring.SqlSessionUtils.getSqlSession(sqlSessionFactory);
        try { sqlSession.clearCache(); }
        finally { org.mybatis.spring.SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory); }
        var response = mvc.perform(request.session(session).header("X-CSRF-Token", "issue20-test-csrf")).andReturn().getResponse();
        assertEquals(expected, response.getStatus(), response.getContentAsString());
        assertEquals("no-store", response.getHeader("Cache-Control"));
        return json.readTree(response.getContentAsByteArray());
    }
}
