package com.bestfriend.danjjak.account;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import com.bestfriend.danjjak.config.RootConfig;
import com.bestfriend.danjjak.config.WebConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.web.context.WebApplicationContext;

/** SC-016의 계좌 준비·재요청·원자성과 HTTP 소유권 경계를 실제 MySQL에서 확인한다. */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {RootConfig.class, WebConfig.class})
@WebAppConfiguration
@EnabledIfEnvironmentVariable(named = "DANJJAK_DB_INTEGRATION_TEST", matches = "true")
@Transactional
class OwnedAccountImportIntegrationTest {

    @Autowired private WebApplicationContext context;
    @Autowired private DataSource dataSource;

    private final ObjectMapper json = new ObjectMapper();
    private MockMvc mvc;
    private JdbcTemplate jdbc;
    private MockHttpSession session;

    @BeforeEach
    void prepare() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
        jdbc = new JdbcTemplate(dataSource);
        if (TestTransaction.isActive()) {
            jdbc.update("UPDATE users SET consent_completed = TRUE WHERE user_id = 1");
        }
        session = new MockHttpSession();
        session.setAttribute("userId", 1L);
        session.setAttribute("csrfToken", "issue19-test-csrf-token");
    }

    @Test
    void readsOnlyImportedOwnedAccountsAndMasksBothLists() throws Exception {
        JsonNode accounts = perform(get("/api/accounts"), 200);
        JsonNode candidates = perform(get("/api/accounts/import-candidates"), 200);
        assertEquals(1, accounts.size());
        assertEquals("1", accounts.get(0).path("accountId").asText());
        assertEquals(1, candidates.size());
        assertEquals("2", candidates.get(0).path("accountId").asText());
        assertTrue(candidates.get(0).path("available").asBoolean());
        for (JsonNode account : List.of(accounts.get(0), candidates.get(0))) {
            assertTrue(account.path("accountId").isTextual());
            assertTrue(account.path("maskedAccountNumber").asText().contains("*"));
            assertFalse(account.has("accountNumber"));
            assertFalse(account.has("accountPinHash"));
        }
        assertFalse(candidates.get(0).has("balance"));
        assertEquals(7, accounts.get(0).size());
        assertEquals(7, candidates.get(0).size());
    }

    @Test
    void firstImportUsesSelectionOrderWhileListUsesIdOrder() throws Exception {
        jdbc.update("UPDATE bank_accounts SET imported_at = NULL WHERE user_id = 1 AND registered_person_id IS NULL");
        assertEquals(0, perform(get("/api/accounts"), 200).size());
        JsonNode saved = perform(importRequest("[\"2\",\"1\"]"), 200);
        assertEquals("1", saved.get(0).path("accountId").asText());
        assertEquals("2", saved.get(1).path("accountId").asText());
        assertFalse(saved.get(0).path("primary").asBoolean());
        assertTrue(saved.get(1).path("primary").asBoolean());
        assertEquals(saved, perform(get("/api/accounts"), 200));
        assertEquals(0, perform(get("/api/accounts/import-candidates"), 200).size());
    }

    @Test
    void retryAndNewSessionPreserveBalancesHistoryPatternsAndImportTimes() throws Exception {
        List<Map<String, Object>> balances = jdbc.queryForList("SELECT bank_account_id, balance FROM bank_accounts ORDER BY bank_account_id");
        List<Map<String, Object>> transactions = jdbc.queryForList("SELECT * FROM transactions ORDER BY transaction_id");
        List<Map<String, Object>> patterns = jdbc.queryForList("SELECT * FROM financial_patterns ORDER BY financial_pattern_id");
        JsonNode saved = perform(importRequest("[\"2\"]"), 200);
        List<Map<String, Object>> imported = jdbc.queryForList("SELECT bank_account_id, imported_at, is_primary FROM bank_accounts ORDER BY bank_account_id");
        assertEquals(saved, perform(importRequest("[\"2\"]"), 200));
        session = new MockHttpSession();
        session.setAttribute("userId", "1");
        session.setAttribute("csrfToken", "issue19-test-csrf-token");
        assertEquals(saved, perform(get("/api/accounts"), 200));
        assertEquals(balances, jdbc.queryForList("SELECT bank_account_id, balance FROM bank_accounts ORDER BY bank_account_id"));
        assertEquals(transactions, jdbc.queryForList("SELECT * FROM transactions ORDER BY transaction_id"));
        assertEquals(patterns, jdbc.queryForList("SELECT * FROM financial_patterns ORDER BY financial_pattern_id"));
        assertEquals(imported, jdbc.queryForList("SELECT bank_account_id, imported_at, is_primary FROM bank_accounts ORDER BY bank_account_id"));
    }

    @Test
    void mixedInvalidBatchLeavesEveryCandidateUnimported() throws Exception {
        perform(importRequest("[\"2\",\"999999\"]"), 422);
        assertEquals(1, perform(get("/api/accounts"), 200).size());
        assertEquals("2", perform(get("/api/accounts/import-candidates"), 200).get(0).path("accountId").asText());
    }

    @Test
    void rejectsRecipientAndAnotherUsersAccountWithoutMutation() throws Exception {
        perform(importRequest("[\"3\"]"), 422);
        jdbc.update("INSERT INTO users (user_id, name, consent_completed) VALUES (19001, '검증 사용자', TRUE)");
        jdbc.update("INSERT INTO bank_accounts (bank_account_id, user_id, bank_code, bank_name, account_number, account_pin_hash, balance) VALUES (19001, 19001, '004', '검증 은행', '123456789', '검증용 해시', 123)");
        perform(importRequest("[\"2\",\"19001\"]"), 422);
        assertEquals(1, perform(get("/api/accounts"), 200).size());
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM bank_accounts WHERE bank_account_id = 19001 AND imported_at IS NOT NULL", Integer.class));
    }

    @Test
    void defaultChangesOnlyByExplicitSaveAndSurvivesFurtherImports() throws Exception {
        perform(importRequest("[\"2\"]"), 200);
        JsonNode saved = perform(defaultRequest("2"), 200);
        assertEquals("2", saved.path("accountId").asText());
        assertTrue(saved.path("primary").asBoolean());
        perform(get("/api/accounts/1/balance"), 200);
        perform(get("/api/accounts/1/transactions"), 200);
        perform(importRequest("[\"1\",\"2\"]"), 200);
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM bank_accounts WHERE user_id = 1 AND is_primary = TRUE", Integer.class));
        assertEquals(2L, jdbc.queryForObject("SELECT bank_account_id FROM bank_accounts WHERE user_id = 1 AND is_primary = TRUE", Long.class));
    }

    @Test
    void cannotMakeUnimportedOrRecipientAccountDefault() throws Exception {
        perform(defaultRequest("2"), 422);
        perform(defaultRequest("3"), 422);
        assertEquals(1L, jdbc.queryForObject("SELECT bank_account_id FROM bank_accounts WHERE user_id = 1 AND is_primary = TRUE", Long.class));
    }

    @Test
    void unpreparedAccountsCannotExposeBalanceOrTransactions() throws Exception {
        perform(get("/api/accounts/2/balance"), 404);
        perform(get("/api/accounts/2/transactions"), 404);
        perform(get("/api/accounts/3/balance"), 404);
    }

    @Test
    void incompleteConsentCannotReadOrImportAccounts() throws Exception {
        jdbc.update("UPDATE users SET consent_completed = FALSE WHERE user_id = 1");
        perform(get("/api/accounts"), 403);
        perform(get("/api/accounts/import-candidates"), 403);
        perform(importRequest("[\"2\"]"), 403);
    }

    @Test
    void personNameEditPreservesEveryRecipientAccount() throws Exception {
        List<Map<String, Object>> accounts = jdbc.queryForList("SELECT * FROM bank_accounts WHERE registered_person_id = 1 ORDER BY bank_account_id");
        assertEquals(2, accounts.size());
        JsonNode saved = perform(put("/api/registered-persons/1").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"김민수 수정\",\"relationship\":\"아들\"}"), 200);
        assertEquals(2, saved.path("accounts").size());
        assertEquals(accounts, jdbc.queryForList("SELECT * FROM bank_accounts WHERE registered_person_id = 1 ORDER BY bank_account_id"));
        perform(put("/api/registered-persons/1").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"김민수\",\"relationship\":\"아들\",\"accountNumber\":\"12345678\"}"), 400);
        assertEquals(accounts, jdbc.queryForList("SELECT * FROM bank_accounts WHERE registered_person_id = 1 ORDER BY bank_account_id"));
    }

    @Test
    void additionalImportDoesNotInventDefaultWhenExistingAccountHasNone() throws Exception {
        jdbc.update("UPDATE bank_accounts SET is_primary = FALSE WHERE user_id = 1");
        JsonNode saved = perform(importRequest("[\"2\"]"), 200);
        assertEquals(2, saved.size());
        assertFalse(saved.get(0).path("primary").asBoolean());
        assertFalse(saved.get(1).path("primary").asBoolean());
    }

    @Test
    void validatesEmptyDuplicateMalformedAndNonStringIds() throws Exception {
        for (String ids : List.of("[]", "[\"2\",\"2\"]", "[\"0\"]", "[\"-1\"]", "[\"9223372036854775808\"]", "[null]", "[2]")) {
            perform(importRequest(ids), 400);
        }
        perform(post("/api/accounts/import").contentType(MediaType.APPLICATION_JSON).content("{}"), 400);
        assertEquals(1, perform(get("/api/accounts"), 200).size());
    }

    @Test
    void requiresAuthenticatedSessionAndCsrfForMutations() throws Exception {
        MvcResult unauthenticated = mvc.perform(get("/api/accounts")).andReturn();
        assertEquals(401, unauthenticated.getResponse().getStatus());
        MvcResult missingCsrf = mvc.perform(importRequest("[\"2\"]").session(session)).andReturn();
        assertEquals(403, missingCsrf.getResponse().getStatus());
        MvcResult wrongCsrf = mvc.perform(defaultRequest("1").session(session).header("X-CSRF-Token", "wrong")).andReturn();
        assertEquals(403, wrongCsrf.getResponse().getStatus());
        assertEquals(1, perform(get("/api/accounts"), 200).size());
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void concurrentFirstImportsKeepExactlyOneDefaultAndBothAccounts() throws Exception {
        String fixtureName = "issue19-" + UUID.randomUUID();
        jdbc.update("INSERT INTO users (name, consent_completed) VALUES (?, TRUE)", fixtureName);
        long userId = jdbc.queryForObject("SELECT user_id FROM users WHERE name = ?", Long.class, fixtureName);
        var pool = Executors.newFixedThreadPool(2);
        try {
            for (String number : List.of("1912345678", "1923456789")) {
                jdbc.update("INSERT INTO bank_accounts (user_id, bank_code, bank_name, account_number, account_pin_hash, balance) VALUES (?, '004', '검증 은행', ?, '검증용 해시', 123)", userId, number);
            }
            List<Long> ids = jdbc.queryForList("SELECT bank_account_id FROM bank_accounts WHERE user_id = ? ORDER BY bank_account_id", Long.class, userId);
            CountDownLatch ready = new CountDownLatch(2);
            CountDownLatch start = new CountDownLatch(1);
            var futures = ids.stream().map(id -> pool.submit(() -> {
                MockHttpSession concurrentSession = new MockHttpSession();
                concurrentSession.setAttribute("userId", userId);
                concurrentSession.setAttribute("csrfToken", "issue19-test-csrf-token");
                ready.countDown();
                assertTrue(start.await(5, TimeUnit.SECONDS));
                return mvc.perform(importRequest("[\"" + id + "\"]")
                        .session(concurrentSession)
                        .header("X-CSRF-Token", "issue19-test-csrf-token"))
                        .andReturn().getResponse().getStatus();
            })).toList();
            assertTrue(ready.await(5, TimeUnit.SECONDS));
            start.countDown();
            for (var future : futures) assertEquals(200, future.get(15, TimeUnit.SECONDS));
            assertEquals(2, jdbc.queryForObject("SELECT COUNT(*) FROM bank_accounts WHERE user_id = ? AND imported_at IS NOT NULL", Integer.class, userId));
            assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM bank_accounts WHERE user_id = ? AND is_primary = TRUE", Integer.class, userId));
            assertEquals(246, jdbc.queryForObject("SELECT SUM(balance) FROM bank_accounts WHERE user_id = ?", Integer.class, userId));
        } finally {
            pool.shutdownNow();
            assertTrue(pool.awaitTermination(20, TimeUnit.SECONDS));
            jdbc.update("DELETE FROM bank_accounts WHERE user_id = ?", userId);
            jdbc.update("DELETE FROM users WHERE user_id = ? AND name = ?", userId, fixtureName);
        }
    }

    private MockHttpServletRequestBuilder importRequest(String ids) {
        return post("/api/accounts/import").contentType(MediaType.APPLICATION_JSON).content("{\"accountIds\":" + ids + "}");
    }

    private MockHttpServletRequestBuilder defaultRequest(String id) {
        return put("/api/accounts/default").contentType(MediaType.APPLICATION_JSON).content("{\"accountId\":\"" + id + "\"}");
    }

    private JsonNode perform(MockHttpServletRequestBuilder request, int expectedStatus) throws Exception {
        MvcResult result = mvc.perform(request.session(session).header("X-CSRF-Token", "issue19-test-csrf-token")).andReturn();
        assertEquals(expectedStatus, result.getResponse().getStatus(), result.getResponse().getContentAsString());
        if (expectedStatus == 200) assertEquals("no-store", result.getResponse().getHeader("Cache-Control"));
        return json.readTree(result.getResponse().getContentAsByteArray());
    }
}
