-- 모의 본인 계좌 후보와 사용자가 불러온 계좌를 같은 자료에서 구분한다.

ALTER TABLE bank_accounts
    ADD COLUMN imported_at DATETIME NULL AFTER is_primary,
    ADD KEY idx_bank_accounts_user_imported (user_id, imported_at),
    ADD CONSTRAINT chk_bank_accounts_recipient_import
        CHECK (registered_person_id IS NULL OR imported_at IS NULL);
