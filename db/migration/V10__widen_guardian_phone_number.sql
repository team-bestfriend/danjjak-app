-- 현재 전화번호 계약의 최대 30자를 원문 그대로 저장한다.
ALTER TABLE guardian_contacts
    MODIFY COLUMN phone_number VARCHAR(30) NOT NULL;
