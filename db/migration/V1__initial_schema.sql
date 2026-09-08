-- 단짝 해커톤 MVP 초기 스키마
-- MySQL 8.4 기준이며, 실제 금융 거래가 아닌 시연용 Mock 데이터를 저장한다.

CREATE TABLE users (
    user_id BIGINT NOT NULL AUTO_INCREMENT,
    kakao_user_id BIGINT NULL,
    name VARCHAR(50) NOT NULL,
    font_size VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
    voice_speed VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
    guide_voice_type VARCHAR(20) NOT NULL DEFAULT 'TTS',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id),
    CONSTRAINT uq_users_kakao_user_id UNIQUE (kakao_user_id),
    CONSTRAINT chk_users_font_size
        CHECK (font_size IN ('SMALL', 'NORMAL', 'LARGE')),
    CONSTRAINT chk_users_voice_speed
        CHECK (voice_speed IN ('SLOW', 'NORMAL', 'FAST')),
    CONSTRAINT chk_users_guide_voice_type
        CHECK (guide_voice_type IN ('TTS', 'FAMILY'))
) COMMENT = '카카오 로그인 사용자와 접근성 설정';

CREATE TABLE guardian_contacts (
    guardian_contact_id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (guardian_contact_id),
    CONSTRAINT uq_guardian_contacts_user_id UNIQUE (user_id),
    CONSTRAINT fk_guardian_contacts_user
        FOREIGN KEY (user_id) REFERENCES users (user_id)
        ON DELETE RESTRICT
) COMMENT = '사용자당 한 명의 보호자 연락처';

CREATE TABLE registered_persons (
    registered_person_id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL,
    relationship VARCHAR(30) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (registered_person_id),
    KEY idx_registered_persons_user_id (user_id),
    CONSTRAINT fk_registered_persons_user
        FOREIGN KEY (user_id) REFERENCES users (user_id)
        ON DELETE RESTRICT
) COMMENT = '아들, 딸 등 등록된 송금 대상';

CREATE TABLE bank_accounts (
    bank_account_id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    registered_person_id BIGINT NULL,
    bank_code VARCHAR(20) NOT NULL,
    bank_name VARCHAR(50) NOT NULL,
    account_number VARCHAR(50) NOT NULL,
    account_alias VARCHAR(50) NULL,
    account_pin_hash VARCHAR(255) NULL,
    balance DECIMAL(15, 0) NULL,
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (bank_account_id),
    CONSTRAINT uq_bank_accounts_user_bank_number
        UNIQUE (user_id, bank_code, account_number),
    KEY idx_bank_accounts_registered_person_id (registered_person_id),
    CONSTRAINT chk_bank_accounts_balance
        CHECK (balance IS NULL OR balance >= 0),
    CONSTRAINT chk_bank_accounts_owner_data
        CHECK (
            (
                registered_person_id IS NULL
                AND balance IS NOT NULL
                AND account_pin_hash IS NOT NULL
            )
            OR (
                registered_person_id IS NOT NULL
                AND balance IS NULL
                AND account_pin_hash IS NULL
            )
        ),
    CONSTRAINT fk_bank_accounts_user
        FOREIGN KEY (user_id) REFERENCES users (user_id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_bank_accounts_registered_person
        FOREIGN KEY (registered_person_id) REFERENCES registered_persons (registered_person_id)
        ON DELETE CASCADE
) COMMENT = '사용자 본인 계좌와 등록된 사람의 수취 계좌';

CREATE TABLE financial_patterns (
    financial_pattern_id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    shortcut_number TINYINT UNSIGNED NULL,
    pattern_type VARCHAR(40) NOT NULL,
    title VARCHAR(100) NOT NULL,
    description VARCHAR(500) NOT NULL,
    linked_bank_account_id BIGINT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (financial_pattern_id),
    CONSTRAINT uq_financial_patterns_user_shortcut
        UNIQUE (user_id, shortcut_number),
    KEY idx_financial_patterns_linked_bank_account_id (linked_bank_account_id),
    CONSTRAINT chk_financial_patterns_shortcut_number
        CHECK (shortcut_number IS NULL OR shortcut_number BETWEEN 1 AND 12),
    CONSTRAINT chk_financial_patterns_active_shortcut
        CHECK (
            (is_active = TRUE AND shortcut_number IS NOT NULL)
            OR (is_active = FALSE AND shortcut_number IS NULL)
        ),
    CONSTRAINT chk_financial_patterns_pattern_type
        CHECK (
            pattern_type IN (
                'TRANSFER',
                'PENSION_CHECK',
                'MANAGEMENT_FEE_CHECK',
                'BALANCE_CHECK',
                'TRANSACTION_HISTORY',
                'CUSTOMER_CENTER',
                'UTILITY_BILL_CHECK',
                'AUTO_TRANSFER_CHECK',
                'CARD_HISTORY',
                'DEPOSIT_MATURITY_CHECK'
            )
        ),
    CONSTRAINT fk_financial_patterns_user
        FOREIGN KEY (user_id) REFERENCES users (user_id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_financial_patterns_linked_bank_account
        FOREIGN KEY (linked_bank_account_id) REFERENCES bank_accounts (bank_account_id)
        ON DELETE RESTRICT
) COMMENT = '단축번호와 연결된 금융 업무 패턴';

CREATE TABLE pattern_steps (
    pattern_step_id BIGINT NOT NULL AUTO_INCREMENT,
    financial_pattern_id BIGINT NOT NULL,
    step_order SMALLINT UNSIGNED NOT NULL,
    step_code VARCHAR(50) NOT NULL,
    step_name VARCHAR(100) NOT NULL,
    instruction_text VARCHAR(500) NOT NULL,
    screen_code VARCHAR(100) NOT NULL,
    target_element_id VARCHAR(100) NULL,
    voice_file_path VARCHAR(500) NULL,
    voice_content_type VARCHAR(100) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (pattern_step_id),
    CONSTRAINT uq_pattern_steps_pattern_order
        UNIQUE (financial_pattern_id, step_order),
    CONSTRAINT uq_pattern_steps_pattern_code
        UNIQUE (financial_pattern_id, step_code),
    CONSTRAINT chk_pattern_steps_step_order
        CHECK (step_order >= 1),
    CONSTRAINT chk_pattern_steps_voice_file
        CHECK (
            (voice_file_path IS NULL AND voice_content_type IS NULL)
            OR (voice_file_path IS NOT NULL AND voice_content_type IS NOT NULL)
        ),
    CONSTRAINT fk_pattern_steps_financial_pattern
        FOREIGN KEY (financial_pattern_id) REFERENCES financial_patterns (financial_pattern_id)
        ON DELETE CASCADE
) COMMENT = '금융 패턴의 단계별 화면 안내와 가족 음성 경로';

CREATE TABLE pattern_executions (
    pattern_execution_id BIGINT NOT NULL AUTO_INCREMENT,
    financial_pattern_id BIGINT NOT NULL,
    source_bank_account_id BIGINT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'STARTED',
    started_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ended_at DATETIME NULL,
    PRIMARY KEY (pattern_execution_id),
    KEY idx_pattern_executions_pattern_started_at
        (financial_pattern_id, started_at),
    KEY idx_pattern_executions_source_bank_account_id
        (source_bank_account_id),
    CONSTRAINT chk_pattern_executions_status
        CHECK (status IN ('STARTED', 'COMPLETED', 'CANCELLED', 'FAILED')),
    CONSTRAINT chk_pattern_executions_end_state
        CHECK (
            (status = 'STARTED' AND ended_at IS NULL)
            OR (status IN ('COMPLETED', 'CANCELLED', 'FAILED') AND ended_at IS NOT NULL)
        ),
    CONSTRAINT chk_pattern_executions_time_order
        CHECK (ended_at IS NULL OR ended_at >= started_at),
    CONSTRAINT fk_pattern_executions_financial_pattern
        FOREIGN KEY (financial_pattern_id) REFERENCES financial_patterns (financial_pattern_id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_pattern_executions_source_bank_account
        FOREIGN KEY (source_bank_account_id) REFERENCES bank_accounts (bank_account_id)
        ON DELETE RESTRICT
) COMMENT = '단축번호 금융 패턴의 실행 기록';

CREATE TABLE step_execution_logs (
    step_execution_log_id BIGINT NOT NULL AUTO_INCREMENT,
    pattern_execution_id BIGINT NOT NULL,
    pattern_step_id BIGINT NOT NULL,
    visit_number SMALLINT UNSIGNED NOT NULL DEFAULT 1,
    retry_count INT UNSIGNED NOT NULL DEFAULT 0,
    back_count INT UNSIGNED NOT NULL DEFAULT 0,
    wrong_touch_count INT UNSIGNED NOT NULL DEFAULT 0,
    route_deviation BOOLEAN NOT NULL DEFAULT FALSE,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    started_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ended_at DATETIME NULL,
    PRIMARY KEY (step_execution_log_id),
    CONSTRAINT uq_step_execution_logs_execution_step_visit
        UNIQUE (pattern_execution_id, pattern_step_id, visit_number),
    KEY idx_step_execution_logs_pattern_step_id (pattern_step_id),
    CONSTRAINT chk_step_execution_logs_visit_number
        CHECK (visit_number >= 1),
    CONSTRAINT chk_step_execution_logs_time_order
        CHECK (ended_at IS NULL OR ended_at >= started_at),
    CONSTRAINT chk_step_execution_logs_completed_end
        CHECK (completed = FALSE OR ended_at IS NOT NULL),
    CONSTRAINT fk_step_execution_logs_pattern_execution
        FOREIGN KEY (pattern_execution_id) REFERENCES pattern_executions (pattern_execution_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_step_execution_logs_pattern_step
        FOREIGN KEY (pattern_step_id) REFERENCES pattern_steps (pattern_step_id)
        ON DELETE RESTRICT
) COMMENT = '한 번의 단계 방문에서 발생한 이용 행동 집계';

CREATE TABLE transactions (
    transaction_id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    bank_account_id BIGINT NOT NULL,
    counterparty_bank_account_id BIGINT NULL,
    pattern_execution_id BIGINT NULL,
    transaction_type VARCHAR(20) NOT NULL,
    transaction_category VARCHAR(30) NOT NULL DEFAULT 'GENERAL',
    amount DECIMAL(15, 0) NOT NULL,
    counterparty_name VARCHAR(100) NULL,
    counterparty_bank_code VARCHAR(20) NULL,
    counterparty_bank_name VARCHAR(50) NULL,
    counterparty_account_number VARCHAR(50) NULL,
    description VARCHAR(200) NOT NULL,
    balance_after DECIMAL(15, 0) NOT NULL,
    transaction_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (transaction_id),
    CONSTRAINT uq_transactions_pattern_execution_id UNIQUE (pattern_execution_id),
    KEY idx_transactions_user_type_time
        (user_id, transaction_type, transaction_at),
    KEY idx_transactions_account_time
        (bank_account_id, transaction_at),
    KEY idx_transactions_user_category_time
        (user_id, transaction_category, transaction_at),
    KEY idx_transactions_counterparty_bank_account_id
        (counterparty_bank_account_id),
    CONSTRAINT chk_transactions_transaction_type
        CHECK (transaction_type IN ('DEPOSIT', 'WITHDRAWAL', 'TRANSFER_OUT', 'PAYMENT')),
    CONSTRAINT chk_transactions_transaction_category
        CHECK (
            transaction_category IN (
                'GENERAL',
                'TRANSFER',
                'PENSION',
                'MANAGEMENT_FEE',
                'UTILITY_BILL',
                'AUTO_TRANSFER',
                'CARD'
            )
        ),
    CONSTRAINT chk_transactions_amount
        CHECK (amount > 0),
    CONSTRAINT chk_transactions_balance_after
        CHECK (balance_after >= 0),
    CONSTRAINT chk_transactions_transfer_category
        CHECK (transaction_type <> 'TRANSFER_OUT' OR transaction_category = 'TRANSFER'),
    CONSTRAINT chk_transactions_transfer_counterparty
        CHECK (
            transaction_type <> 'TRANSFER_OUT'
            OR (
                counterparty_bank_code IS NOT NULL
                AND counterparty_bank_name IS NOT NULL
                AND counterparty_account_number IS NOT NULL
            )
        ),
    CONSTRAINT fk_transactions_user
        FOREIGN KEY (user_id) REFERENCES users (user_id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_transactions_bank_account
        FOREIGN KEY (bank_account_id) REFERENCES bank_accounts (bank_account_id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_transactions_counterparty_bank_account
        FOREIGN KEY (counterparty_bank_account_id) REFERENCES bank_accounts (bank_account_id)
        ON DELETE SET NULL,
    CONSTRAINT fk_transactions_pattern_execution
        FOREIGN KEY (pattern_execution_id) REFERENCES pattern_executions (pattern_execution_id)
        ON DELETE SET NULL
) COMMENT = '사용자 계좌의 Mock 거래내역과 완료된 송금';

CREATE TABLE anomaly_events (
    anomaly_event_id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    pattern_execution_id BIGINT NULL,
    transaction_id BIGINT NULL,
    source_bank_account_id BIGINT NOT NULL,
    recipient_bank_account_id BIGINT NULL,
    recipient_name VARCHAR(100) NULL,
    recipient_bank_code VARCHAR(20) NOT NULL,
    recipient_bank_name VARCHAR(50) NOT NULL,
    recipient_account_number VARCHAR(50) NOT NULL,
    amount DECIMAL(15, 0) NOT NULL,
    high_amount_detected BOOLEAN NOT NULL DEFAULT FALSE,
    repeated_transfer_detected BOOLEAN NOT NULL DEFAULT FALSE,
    recent_transfer_count SMALLINT UNSIGNED NOT NULL DEFAULT 0,
    risk_level VARCHAR(10) NOT NULL,
    rechecked BOOLEAN NOT NULL DEFAULT FALSE,
    guardian_notified_at DATETIME NULL,
    final_action VARCHAR(20) NULL,
    detected_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at DATETIME NULL,
    PRIMARY KEY (anomaly_event_id),
    CONSTRAINT uq_anomaly_events_pattern_execution_id UNIQUE (pattern_execution_id),
    CONSTRAINT uq_anomaly_events_transaction_id UNIQUE (transaction_id),
    KEY idx_anomaly_events_user_detected_at (user_id, detected_at),
    KEY idx_anomaly_events_source_bank_account_id (source_bank_account_id),
    KEY idx_anomaly_events_recipient_bank_account_id (recipient_bank_account_id),
    CONSTRAINT chk_anomaly_events_amount
        CHECK (amount > 0),
    CONSTRAINT chk_anomaly_events_detected_rule
        CHECK (high_amount_detected = TRUE OR repeated_transfer_detected = TRUE),
    CONSTRAINT chk_anomaly_events_risk_level
        CHECK (
            (
                risk_level = 'MEDIUM'
                AND (
                    (high_amount_detected = TRUE AND repeated_transfer_detected = FALSE)
                    OR (high_amount_detected = FALSE AND repeated_transfer_detected = TRUE)
                )
            )
            OR (
                risk_level = 'HIGH'
                AND high_amount_detected = TRUE
                AND repeated_transfer_detected = TRUE
            )
        ),
    CONSTRAINT chk_anomaly_events_final_action
        CHECK (final_action IS NULL OR final_action IN ('CONTINUE', 'CANCEL')),
    CONSTRAINT chk_anomaly_events_resolution
        CHECK (
            (final_action IS NULL AND resolved_at IS NULL)
            OR (final_action IS NOT NULL AND resolved_at IS NOT NULL)
        ),
    CONSTRAINT chk_anomaly_events_continue_transaction
        CHECK (final_action <> 'CONTINUE' OR transaction_id IS NOT NULL),
    CONSTRAINT chk_anomaly_events_cancel_transaction
        CHECK (final_action <> 'CANCEL' OR transaction_id IS NULL),
    CONSTRAINT chk_anomaly_events_guardian_notification
        CHECK (guardian_notified_at IS NULL OR risk_level = 'HIGH'),
    CONSTRAINT chk_anomaly_events_time_order
        CHECK (resolved_at IS NULL OR resolved_at >= detected_at),
    CONSTRAINT fk_anomaly_events_user
        FOREIGN KEY (user_id) REFERENCES users (user_id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_anomaly_events_pattern_execution
        FOREIGN KEY (pattern_execution_id) REFERENCES pattern_executions (pattern_execution_id)
        ON DELETE SET NULL,
    CONSTRAINT fk_anomaly_events_transaction
        FOREIGN KEY (transaction_id) REFERENCES transactions (transaction_id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_anomaly_events_source_bank_account
        FOREIGN KEY (source_bank_account_id) REFERENCES bank_accounts (bank_account_id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_anomaly_events_recipient_bank_account
        FOREIGN KEY (recipient_bank_account_id) REFERENCES bank_accounts (bank_account_id)
        ON DELETE SET NULL
) COMMENT = '고액 또는 반복 송금 탐지와 사용자 처리 결과';
