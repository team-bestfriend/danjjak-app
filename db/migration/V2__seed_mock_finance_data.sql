-- 송금·조회 시연에서 공통으로 사용하는 가상 사용자와 금융 데이터

INSERT INTO users (name, font_size, voice_speed, guide_voice_type)
VALUES ('김단짝', 'LARGE', 'SLOW', 'TTS');

SET @demo_user_id = LAST_INSERT_ID();

INSERT INTO guardian_contacts (user_id, phone_number)
VALUES (@demo_user_id, '010-0000-1004');

INSERT INTO bank_accounts (
    user_id,
    bank_code,
    bank_name,
    account_number,
    account_alias,
    account_pin_hash,
    balance,
    is_primary
) VALUES
    (
        @demo_user_id,
        '088',
        '신한은행',
        '110-000-000001',
        '생활비 통장',
        '$2a$10$sjht3pDQhuydNc41VUyyQe9FTAXeG5Xup8z1b41I9LmnLyrVVvB.6',
        50000000,
        TRUE
    ),
    (
        @demo_user_id,
        '004',
        '국민은행',
        '123-000-000002',
        '저축 통장',
        '$2a$10$sjht3pDQhuydNc41VUyyQe9FTAXeG5Xup8z1b41I9LmnLyrVVvB.6',
        30000000,
        FALSE
    );

SET @primary_account_id = (
    SELECT bank_account_id
    FROM bank_accounts
    WHERE user_id = @demo_user_id AND is_primary = TRUE
);

INSERT INTO registered_persons (user_id, name, relationship)
VALUES (@demo_user_id, '김민수', '아들');

SET @son_person_id = LAST_INSERT_ID();

INSERT INTO bank_accounts (
    user_id,
    registered_person_id,
    bank_code,
    bank_name,
    account_number,
    account_alias,
    account_pin_hash,
    balance,
    is_primary
) VALUES (
    @demo_user_id,
    @son_person_id,
    '020',
    '우리은행',
    '1002-000-000001',
    '민수 계좌',
    NULL,
    NULL,
    FALSE
);

INSERT INTO registered_persons (user_id, name, relationship)
VALUES (@demo_user_id, '김지영', '딸');

SET @daughter_person_id = LAST_INSERT_ID();

INSERT INTO bank_accounts (
    user_id,
    registered_person_id,
    bank_code,
    bank_name,
    account_number,
    account_alias,
    account_pin_hash,
    balance,
    is_primary
) VALUES (
    @demo_user_id,
    @daughter_person_id,
    '081',
    '하나은행',
    '355-000-000002',
    '지영 계좌',
    NULL,
    NULL,
    FALSE
);

INSERT INTO transactions (
    user_id,
    bank_account_id,
    transaction_type,
    transaction_category,
    amount,
    counterparty_name,
    description,
    balance_after,
    transaction_at
) VALUES
    (
        @demo_user_id,
        @primary_account_id,
        'PAYMENT',
        'UTILITY_BILL',
        100000,
        '한국전력',
        '전기요금',
        49150000,
        DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 30 DAY)
    ),
    (
        @demo_user_id,
        @primary_account_id,
        'PAYMENT',
        'MANAGEMENT_FEE',
        250000,
        '단짝아파트 관리사무소',
        '아파트 관리비',
        48900000,
        DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 20 DAY)
    ),
    (
        @demo_user_id,
        @primary_account_id,
        'DEPOSIT',
        'PENSION',
        1100000,
        '국민연금공단',
        '국민연금',
        50000000,
        DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 10 DAY)
    );
