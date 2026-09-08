-- 두 동의를 모두 거부한 상태와 아직 선택하지 않은 상태를 구분한다.
ALTER TABLE users
    ADD COLUMN usage_log_agreed BOOLEAN NOT NULL DEFAULT FALSE
        AFTER guide_voice_type,
    ADD COLUMN guardian_share_agreed BOOLEAN NOT NULL DEFAULT FALSE
        AFTER usage_log_agreed,
    ADD COLUMN consent_completed BOOLEAN NOT NULL DEFAULT FALSE
        AFTER guardian_share_agreed;
