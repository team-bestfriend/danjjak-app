-- 송금 완료 화면은 금융 업무 성공 뒤의 결과 화면이므로 실행 단계와 방문 분석 대상에서 제외한다.
-- 과거 방문 기록이 연결된 행은 로그 보존을 위해 남기고 조회 계층에서 실행 대상에서 제외한다.
DELETE ps
FROM pattern_steps ps
JOIN financial_patterns fp
  ON fp.financial_pattern_id = ps.financial_pattern_id
WHERE fp.pattern_type = 'TRANSFER'
  AND ps.step_code = 'TRANSFER_COMPLETE'
  AND NOT EXISTS (
      SELECT 1
      FROM step_execution_logs sel
      WHERE sel.pattern_step_id = ps.pattern_step_id
  );
