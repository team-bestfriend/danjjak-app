-- 새 시연 사용자가 처음 로그인했을 때 표시할 기본 단축번호 8개

SET @demo_user_id = (SELECT user_id FROM users ORDER BY user_id LIMIT 1);
SET @primary_account_id = (
    SELECT bank_account_id
    FROM bank_accounts
    WHERE user_id = @demo_user_id
      AND registered_person_id IS NULL
      AND is_primary = TRUE
    LIMIT 1
);
SET @son_account_id = (
    SELECT ba.bank_account_id
    FROM bank_accounts ba
    JOIN registered_persons rp ON rp.registered_person_id = ba.registered_person_id
    WHERE ba.user_id = @demo_user_id AND rp.relationship = '아들'
    LIMIT 1
);
SET @daughter_account_id = (
    SELECT ba.bank_account_id
    FROM bank_accounts ba
    JOIN registered_persons rp ON rp.registered_person_id = ba.registered_person_id
    WHERE ba.user_id = @demo_user_id AND rp.relationship = '딸'
    LIMIT 1
);

INSERT INTO financial_patterns
    (user_id, shortcut_number, pattern_type, title, description, linked_bank_account_id)
VALUES
    (@demo_user_id, 1, 'TRANSFER', '아들에게 송금', '아들 김민수님에게 송금하는 업무입니다.', @son_account_id);
SET @son_transfer_pattern_id = LAST_INSERT_ID();

INSERT INTO pattern_steps
    (financial_pattern_id, step_order, step_code, step_name, instruction_text, screen_code, target_element_id)
VALUES
    (@son_transfer_pattern_id, 1, 'SELECT_SOURCE', '출금 계좌 선택', '송금할 본인 계좌를 선택해 주세요.', 'transfer-source', 'source-account-list'),
    (@son_transfer_pattern_id, 2, 'SELECT_PERSON', '받는 사람 선택', '아들 김민수님을 선택해 주세요.', 'guide-person', 'registered-person-list'),
    (@son_transfer_pattern_id, 3, 'SELECT_ACCOUNT', '받는 계좌 선택', '민수님의 등록 계좌를 선택해 주세요.', 'guide-account', 'recipient-account-list'),
    (@son_transfer_pattern_id, 4, 'INPUT_AMOUNT', '금액 입력', '보낼 금액을 입력해 주세요.', 'amount-input', 'amount-keypad'),
    (@son_transfer_pattern_id, 5, 'CONFIRM_TRANSFER', '송금 내용 확인', '받는 분과 금액이 맞는지 확인해 주세요.', 'final-confirm', 'transfer-summary'),
    (@son_transfer_pattern_id, 6, 'ENTER_PIN', 'PIN 입력', '계좌 비밀번호 네 자리를 입력해 주세요.', 'pin-entry', 'pin-keypad'),
    (@son_transfer_pattern_id, 7, 'TRANSFER_COMPLETE', '송금 완료', '송금이 완료됐어요.', 'complete', NULL);

INSERT INTO financial_patterns
    (user_id, shortcut_number, pattern_type, title, description, linked_bank_account_id)
VALUES
    (@demo_user_id, 2, 'PENSION_CHECK', '연금 입금 확인', '최근 연금 입금 내역을 확인하는 업무입니다.', @primary_account_id),
    (@demo_user_id, 3, 'MANAGEMENT_FEE_CHECK', '관리비 확인', '이번 달 관리비 내역을 확인하는 업무입니다.', @primary_account_id),
    (@demo_user_id, 4, 'BALANCE_CHECK', '잔액 확인', '생활비 통장의 현재 잔액을 확인하는 업무입니다.', @primary_account_id),
    (@demo_user_id, 5, 'TRANSACTION_HISTORY', '거래내역 조회', '생활비 통장의 최근 거래 내역을 확인하는 업무입니다.', @primary_account_id),
    (@demo_user_id, 6, 'CUSTOMER_CENTER', '고객센터 연결', '은행 고객센터 전화번호를 확인하고 연결하는 업무입니다.', NULL);

INSERT INTO pattern_steps
    (financial_pattern_id, step_order, step_code, step_name, instruction_text, screen_code)
SELECT financial_pattern_id, 1, 'CHECK_RESULT', title, description,
       CASE pattern_type
           WHEN 'PENSION_CHECK' THEN 'task-2'
           WHEN 'MANAGEMENT_FEE_CHECK' THEN 'task-3'
           WHEN 'BALANCE_CHECK' THEN 'task-4'
           WHEN 'TRANSACTION_HISTORY' THEN 'task-5'
           WHEN 'CUSTOMER_CENTER' THEN 'task-6'
       END
FROM financial_patterns
WHERE user_id = @demo_user_id AND shortcut_number BETWEEN 2 AND 6;

INSERT INTO financial_patterns
    (user_id, shortcut_number, pattern_type, title, description, linked_bank_account_id)
VALUES
    (@demo_user_id, 7, 'TRANSFER', '딸에게 송금', '딸 김지영님에게 송금하는 업무입니다.', @daughter_account_id);
SET @daughter_transfer_pattern_id = LAST_INSERT_ID();

INSERT INTO pattern_steps
    (financial_pattern_id, step_order, step_code, step_name, instruction_text, screen_code, target_element_id)
VALUES
    (@daughter_transfer_pattern_id, 1, 'SELECT_SOURCE', '출금 계좌 선택', '송금할 본인 계좌를 선택해 주세요.', 'transfer-source', 'source-account-list'),
    (@daughter_transfer_pattern_id, 2, 'SELECT_PERSON', '받는 사람 선택', '딸 김지영님을 선택해 주세요.', 'guide-person', 'registered-person-list'),
    (@daughter_transfer_pattern_id, 3, 'SELECT_ACCOUNT', '받는 계좌 선택', '지영님의 등록 계좌를 선택해 주세요.', 'guide-account', 'recipient-account-list'),
    (@daughter_transfer_pattern_id, 4, 'INPUT_AMOUNT', '금액 입력', '보낼 금액을 입력해 주세요.', 'amount-input', 'amount-keypad'),
    (@daughter_transfer_pattern_id, 5, 'CONFIRM_TRANSFER', '송금 내용 확인', '받는 분과 금액이 맞는지 확인해 주세요.', 'final-confirm', 'transfer-summary'),
    (@daughter_transfer_pattern_id, 6, 'ENTER_PIN', 'PIN 입력', '계좌 비밀번호 네 자리를 입력해 주세요.', 'pin-entry', 'pin-keypad'),
    (@daughter_transfer_pattern_id, 7, 'TRANSFER_COMPLETE', '송금 완료', '송금이 완료됐어요.', 'complete', NULL);

INSERT INTO financial_patterns
    (user_id, shortcut_number, pattern_type, title, description, linked_bank_account_id)
VALUES
    (@demo_user_id, 8, 'UTILITY_BILL_CHECK', '공과금 확인', '최근 공과금 납부 내역을 확인하는 업무입니다.', @primary_account_id);
SET @utility_pattern_id = LAST_INSERT_ID();

INSERT INTO pattern_steps
    (financial_pattern_id, step_order, step_code, step_name, instruction_text, screen_code)
VALUES
    (@utility_pattern_id, 1, 'CHECK_RESULT', '공과금 내역 확인', '최근 공과금 납부 내역을 확인해 주세요.', 'task-8');
