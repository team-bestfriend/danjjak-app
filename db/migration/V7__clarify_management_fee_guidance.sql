-- 기본 시드 문구만 실제 조회 범위에 맞춘다. 사용자가 바꾼 문구와 녹음은 유지한다.
UPDATE financial_patterns
SET voice_script_outdated = voice_script_outdated OR voice_file_path IS NOT NULL,
    description = '선택한 계좌의 전체 기간 관리비 거래를 확인하는 업무입니다.'
WHERE pattern_type = 'MANAGEMENT_FEE_CHECK'
  AND BINARY description = BINARY '이번 달 관리비 내역을 확인하는 업무입니다.';

UPDATE pattern_steps ps JOIN financial_patterns fp USING (financial_pattern_id)
SET ps.voice_script_outdated = ps.voice_script_outdated OR ps.voice_file_path IS NOT NULL,
    ps.instruction_text = '선택한 계좌의 전체 기간 관리비 거래 내역을 확인해 주세요.'
WHERE fp.pattern_type = 'MANAGEMENT_FEE_CHECK'
  AND BINARY ps.instruction_text = BINARY '이번 달 관리비 내역을 확인하는 업무입니다.';
