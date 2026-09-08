-- 시작 안내와 단계 안내는 서로 독립적으로 음성 선택과 녹음을 유지한다.
ALTER TABLE financial_patterns
    ADD COLUMN voice_mode VARCHAR(10) NULL,
    ADD COLUMN voice_file_path VARCHAR(500) NULL,
    ADD COLUMN voice_content_type VARCHAR(100) NULL,
    ADD COLUMN voice_script_outdated BOOLEAN NOT NULL DEFAULT FALSE,
    ADD CONSTRAINT chk_patterns_voice_mode CHECK (voice_mode IN ('TTS', 'FAMILY')),
    ADD CONSTRAINT chk_patterns_voice_file CHECK (
        (voice_file_path IS NULL AND voice_content_type IS NULL)
        OR (voice_file_path IS NOT NULL AND voice_content_type IS NOT NULL)
    );

ALTER TABLE pattern_steps
    ADD COLUMN voice_mode VARCHAR(10) NULL,
    ADD COLUMN voice_script_outdated BOOLEAN NOT NULL DEFAULT FALSE,
    ADD CONSTRAINT chk_steps_voice_mode CHECK (voice_mode IN ('TTS', 'FAMILY'));
