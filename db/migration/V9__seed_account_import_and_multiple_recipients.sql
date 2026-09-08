-- 기본 계좌는 불러오기 완료 상태로 두고 나머지 본인 계좌는 선택 후보로 남긴다.
UPDATE bank_accounts
SET imported_at = CURRENT_TIMESTAMP
WHERE registered_person_id IS NULL
  AND is_primary = TRUE;

-- 한 사람에게 여러 받는 계좌가 연결되는 시연 자료를 추가한다.
INSERT INTO bank_accounts (
    user_id,
    registered_person_id,
    bank_code,
    bank_name,
    account_number,
    account_alias,
    account_pin_hash,
    balance,
    is_primary,
    imported_at
)
SELECT rp.user_id,
       rp.registered_person_id,
       '004',
       '국민은행',
       '123-000-100002',
       '민수 용돈 계좌',
       NULL,
       NULL,
       FALSE,
       NULL
FROM registered_persons rp
JOIN users u ON u.user_id = rp.user_id
WHERE u.name = '김단짝'
  AND rp.name = '김민수'
  AND NOT EXISTS (
      SELECT 1
      FROM bank_accounts ba
      WHERE ba.user_id = rp.user_id
        AND ba.bank_code = '004'
        AND ba.account_number = '123-000-100002'
  );
