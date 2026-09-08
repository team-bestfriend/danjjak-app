package com.bestfriend.danjjak.transfer.mapper;

import static org.junit.jupiter.api.Assertions.*;

import com.bestfriend.danjjak.transfer.model.AnomalyCommand;
import com.bestfriend.danjjak.transfer.model.TransactionCommand;
import com.bestfriend.danjjak.transfer.service.FdsEvaluator;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.LocalCacheScope;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/** 실제 MySQL에서 집계·저장 계약만 검증합니다. 빈 송금 서비스의 HTTP 흐름을 대신 구현하지 않습니다. */
@EnabledIfEnvironmentVariable(named = "DANJJAK_DB_INTEGRATION_TEST", matches = "true")
class FdsPersistenceIntegrationTest {
    private static final long USER = 160001;
    private static final long OTHER = 160002;
    private static final long ACCOUNT_A = 160001;
    private static final long ACCOUNT_B = 160002;
    private static final long OTHER_ACCOUNT = 160003;
    private static final long PATTERN = 160001;
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 9, 8, 12, 0, 0);
    private static SqlSessionFactory factory;
    private SqlSession session;
    private TransferMapper mapper;
    private final FdsEvaluator evaluator = new FdsEvaluator();

    @BeforeAll static void configure() throws Exception {
        var dataSource = new UnpooledDataSource("com.mysql.cj.jdbc.Driver",
                System.getenv("DANJJAK_DB_URL"), System.getenv("DANJJAK_DB_USERNAME"),
                System.getenv("DANJJAK_DB_PASSWORD"));
        var configuration = new Configuration(new Environment("fds-verification", new JdbcTransactionFactory(), dataSource));
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.setLocalCacheScope(LocalCacheScope.STATEMENT);
        String resource = "mapper/transfer/TransferMapper.xml";
        try (var stream = FdsPersistenceIntegrationTest.class.getClassLoader().getResourceAsStream(resource)) {
            assertNotNull(stream);
            new XMLMapperBuilder(stream, configuration, resource, configuration.getSqlFragments()).parse();
        }
        factory = new SqlSessionFactoryBuilder().build(configuration);
    }

    @BeforeEach void prepare() throws SQLException {
        session = factory.openSession(false);
        mapper = session.getMapper(TransferMapper.class);
        sql("INSERT INTO users (user_id, name, consent_completed, usage_log_agreed) VALUES (?, 'FDS 검증', TRUE, FALSE), (?, '다른 FDS 검증', TRUE, FALSE)", USER, OTHER);
        for (long account : List.of(ACCOUNT_A, ACCOUNT_B, OTHER_ACCOUNT)) {
            sql("INSERT INTO bank_accounts (bank_account_id, user_id, bank_code, bank_name, account_number, account_pin_hash, balance) VALUES (?, ?, '004', '검증 은행', ?, '검증용 해시', 50000000)",
                    account, account == OTHER_ACCOUNT ? OTHER : USER, "000000" + account);
        }
        sql("INSERT INTO financial_patterns (financial_pattern_id, user_id, shortcut_number, pattern_type, title, description) VALUES (?, ?, 1, 'TRANSFER', '검증 송금', '검증 전용')", PATTERN, USER);
    }

    @AfterEach void rollback() {
        if (session != null) {
            try { session.rollback(true); } finally { session.close(); }
        }
    }

    @ParameterizedTest
    @CsvSource({"-601,0", "-600,1", "-599,1", "0,1", "1,0"})
    void includesBothTenMinuteEndpointsAndExcludesOutside(int seconds, int expected) {
        completed(USER, ACCOUNT_A, null, NOW.plusSeconds(seconds));
        assertEquals(expected, count());
    }

    @Test void combinesDirectAndPatternTransfersAcrossOwnedAccountsOnlyForSameUser() throws SQLException {
        execution(160001, ACCOUNT_B, "COMPLETED");
        completed(USER, ACCOUNT_A, null, NOW.minusMinutes(2));
        assertEquals(1, count());
        assertEquals("NORMAL", evaluator.evaluate(new BigDecimal("9999999"), count()).riskLevel());
        completed(USER, ACCOUNT_B, 160001L, NOW.minusMinutes(1));
        completed(OTHER, OTHER_ACCOUNT, null, NOW);
        for (String type : List.of("DEPOSIT", "WITHDRAWAL", "PAYMENT")) {
            sql("INSERT INTO transactions (user_id, bank_account_id, transaction_type, amount, description, balance_after, transaction_at) VALUES (?, ?, ?, 1000, '송금 외 거래', 50000000, ?)", USER, ACCOUNT_A, type, NOW);
        }
        assertEquals(2, count());
        assertEquals("MEDIUM", evaluator.evaluate(new BigDecimal("9999999"), count()).riskLevel());
        assertEquals("HIGH", evaluator.evaluate(new BigDecimal("10000000"), count()).riskLevel());
    }

    @Test void failedCancelledPendingAndCurrentAttemptRecordsDoNotCountAsCompleted() throws SQLException {
        execution(160001, ACCOUNT_A, "FAILED");
        execution(160002, ACCOUNT_A, "CANCELLED");
        execution(160003, ACCOUNT_B, "STARTED");
        AnomalyCommand waiting = anomaly(ACCOUNT_A, null);
        mapper.insertAnomaly(waiting);
        AnomalyCommand cancelled = anomaly(ACCOUNT_B, 160002L);
        mapper.insertAnomaly(cancelled);
        assertEquals(1, mapper.resolveAnomalyAsCancelled(USER, cancelled.getAnomalyEventId(), false, NOW));
        assertEquals(0, count());
        assertEquals(0, scalar("SELECT COUNT(*) FROM transactions WHERE user_id = ?", USER));
        // 현재 시도의 판정 시점에는 거래가 아직 없다는 저장 계약만 확인합니다.
        assertFalse(evaluator.evaluate(new BigDecimal("9999999"), count()).anomalous());
    }

    @Test void usageConsentAndUnrelatedAccountChangesDoNotChangeTheTwoRules() throws SQLException {
        completed(USER, ACCOUNT_A, null, NOW.minusSeconds(2));
        completed(USER, ACCOUNT_B, null, NOW.minusSeconds(1));
        var before = evaluator.evaluate(new BigDecimal("10000000"), count());
        sql("UPDATE users SET usage_log_agreed = TRUE WHERE user_id = ?", USER);
        sql("UPDATE bank_accounts SET account_number = '999999999999' WHERE bank_account_id = ?", ACCOUNT_B);
        assertEquals(before, evaluator.evaluate(new BigDecimal("10000000"), count()));
        assertEquals(List.of("HIGH_AMOUNT", "REPEATED_TRANSFER"), before.reasons());
    }

    @Test void persistsBothReasonsInOneRecordAndReadsGeneratedIdentityWithoutDebiting() throws SQLException {
        completed(USER, ACCOUNT_A, null, NOW.minusSeconds(2));
        completed(USER, ACCOUNT_B, null, NOW.minusSeconds(1));
        var result = evaluator.evaluate(new BigDecimal("10000000"), count());
        AnomalyCommand command = anomaly(ACCOUNT_A, null);
        command.setRecentTransferCount(result.recentTransferCount());
        assertEquals(1, mapper.insertAnomaly(command));
        assertNotNull(command.getAnomalyEventId());
        var saved = mapper.findAnomalyForUpdate(USER, command.getAnomalyEventId());
        assertEquals("HIGH", saved.getRiskLevel());
        assertTrue(saved.isHighAmountDetected());
        assertTrue(saved.isRepeatedTransferDetected());
        assertEquals(2, saved.getRecentTransferCount());
        assertNull(saved.getFinalAction());
        assertNull(saved.getTransactionId());
        assertEquals(1, scalar("SELECT COUNT(*) FROM anomaly_events WHERE user_id = ?", USER));
        assertEquals(2, count());
        assertEquals(50000000, scalar("SELECT balance FROM bank_accounts WHERE bank_account_id = ?", ACCOUNT_A));
        assertNull(mapper.findAnomalyForUpdate(OTHER, command.getAnomalyEventId()));
    }

    @Test void pendingLookupReusesMatchingIdentityAndSeparatesChangedOrResolvedAttempts() {
        AnomalyCommand command = anomaly(ACCOUNT_A, null);
        mapper.insertAnomaly(command);
        assertEquals(command.getAnomalyEventId(), mapper.findPendingMatchingAnomalyForUpdate(command).getAnomalyEventId());
        command.setAmount(new BigDecimal("10000001"));
        assertNull(mapper.findPendingMatchingAnomalyForUpdate(command));
        command.setAmount(new BigDecimal("10000000"));
        command.setSourceAccountId(ACCOUNT_B);
        assertNull(mapper.findPendingMatchingAnomalyForUpdate(command));
        command.setSourceAccountId(ACCOUNT_A);
        command.setUserId(OTHER);
        assertNull(mapper.findPendingMatchingAnomalyForUpdate(command));
        command.setUserId(USER);
        mapper.resolveAnomalyAsCancelled(USER, command.getAnomalyEventId(), true, NOW);
        assertNull(mapper.findPendingMatchingAnomalyForUpdate(command));
    }

    @Test void patternExecutionCannotPersistTwoAnomalyRows() throws SQLException {
        execution(160001, ACCOUNT_A, "STARTED");
        mapper.insertAnomaly(anomaly(ACCOUNT_A, 160001L));
        assertThrows(PersistenceException.class, () -> mapper.insertAnomaly(anomaly(ACCOUNT_A, 160001L)));
        assertEquals(1, scalar("SELECT COUNT(*) FROM anomaly_events WHERE user_id = ?", USER));
    }

    @Test void normalDecisionIsReadOnlyAndDatabaseRejectsNormalAnomalyRecord() throws SQLException {
        assertFalse(evaluator.evaluate(new BigDecimal("9999999"), count()).anomalous());
        assertEquals(0, scalar("SELECT COUNT(*) FROM anomaly_events WHERE user_id = ?", USER));
        AnomalyCommand normal = anomaly(ACCOUNT_A, null);
        normal.setRiskLevel("NORMAL");
        normal.setHighAmountDetected(false);
        normal.setRepeatedTransferDetected(false);
        assertThrows(PersistenceException.class, () -> mapper.insertAnomaly(normal));
        assertEquals(0, scalar("SELECT COUNT(*) FROM anomaly_events WHERE user_id = ?", USER));
    }

    @Test void rolledBackTransferDoesNotContributeAndOtherSessionCannotSeeUncommittedTransfer() throws SQLException {
        var point = session.getConnection().setSavepoint();
        completed(USER, ACCOUNT_A, null, NOW);
        assertEquals(1, count());
        try (SqlSession reader = factory.openSession()) {
            assertEquals(0, reader.getMapper(TransferMapper.class).countRecentTransfers(USER, NOW.minusMinutes(10), NOW));
        }
        session.getConnection().rollback(point);
        assertEquals(0, count());
    }

    private int count() { return mapper.countRecentTransfers(USER, NOW.minusMinutes(10), NOW); }

    private void completed(long user, long account, Long execution, LocalDateTime at) {
        TransactionCommand command = new TransactionCommand();
        command.setUserId(user);
        command.setSourceAccountId(account);
        command.setPatternExecutionId(execution);
        command.setAmount(new BigDecimal("1000"));
        command.setRecipientName("검증 수취인");
        command.setRecipientBankCode("004");
        command.setRecipientBankName("검증 은행");
        command.setRecipientAccountNumber("000000000000");
        command.setDescription("완료 송금 검증");
        command.setBalanceAfter(new BigDecimal("49999000"));
        command.setTransactionAt(at);
        assertEquals(1, mapper.insertTransaction(command));
    }

    private AnomalyCommand anomaly(long source, Long execution) {
        AnomalyCommand command = new AnomalyCommand();
        command.setUserId(USER);
        command.setSourceAccountId(source);
        command.setPatternExecutionId(execution);
        command.setRecipientName("검증 수취인");
        command.setRecipientBankCode("004");
        command.setRecipientBankName("검증 은행");
        command.setRecipientAccountNumber("000000000000");
        command.setAmount(new BigDecimal("10000000"));
        command.setRiskLevel("HIGH");
        command.setHighAmountDetected(true);
        command.setRepeatedTransferDetected(true);
        command.setRecentTransferCount(2);
        command.setDetectedAt(NOW);
        return command;
    }

    private void execution(long id, long account, String status) throws SQLException {
        sql("INSERT INTO pattern_executions (pattern_execution_id, financial_pattern_id, source_bank_account_id, status, started_at, ended_at) VALUES (?, ?, ?, ?, ?, ?)",
                id, PATTERN, account, status, NOW.minusMinutes(5), status.equals("STARTED") ? null : NOW.minusMinutes(1));
    }

    private void sql(String query, Object... arguments) throws SQLException {
        try (var statement = session.getConnection().prepareStatement(query)) {
            for (int i = 0; i < arguments.length; i++) statement.setObject(i + 1, arguments[i]);
            statement.executeUpdate();
        }
    }

    private long scalar(String query, Object... arguments) throws SQLException {
        try (var statement = session.getConnection().prepareStatement(query)) {
            for (int i = 0; i < arguments.length; i++) statement.setObject(i + 1, arguments[i]);
            try (var result = statement.executeQuery()) { assertTrue(result.next()); return result.getLong(1); }
        }
    }
}
