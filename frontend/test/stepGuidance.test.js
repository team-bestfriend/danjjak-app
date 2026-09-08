import assert from 'node:assert/strict';
import test from 'node:test';
import { stepInstruction, isWrongGuidanceControl } from '../src/composables/useStepGuidance.js';
import { createPinia, setActivePinia } from 'pinia';
import { useAppStore } from '../src/stores/appStore.js';

test('저장 문구를 사용하고 빈 문구는 금융 결과가 없는 안전한 안내로 대체한다', () => {
  assert.equal(stepInstruction({ instructionText: '  아들 계좌를 눌러 주세요.  ' }), '아들 계좌를 눌러 주세요.');
  for (const step of [null, {}, { instructionText: '' }, { instructionText: '  ' }]) {
    assert.equal(stepInstruction(step), '화면의 안내를 확인하고 원하는 작업을 선택해 주세요.');
  }
});

test('복합 키패드 내부 조작과 뒤로가기·취소·비활성 조작은 잘못된 터치가 아니다', () => {
  const control = { matches: () => false, closest: () => null };
  assert.equal(isWrongGuidanceControl(control, control), false);
  assert.equal(isWrongGuidanceControl(control, { contains: () => true }), false);
  assert.equal(isWrongGuidanceControl({ ...control, closest: () => ({}) }, { contains: () => false }), false);
  assert.equal(isWrongGuidanceControl({ ...control, matches: () => true }, { contains: () => false }), false);
  assert.equal(isWrongGuidanceControl(control, { contains: () => false }), true);
  assert.equal(isWrongGuidanceControl(control, null), false);
  assert.equal(isWrongGuidanceControl(null, control), false);
});

test('잘못된 터치는 현재 방문에 횟수만 저장하고 동의 거부·방문 없음은 수집하지 않는다', async (t) => {
  const originalFetch = globalThis.fetch;
  t.after(() => { globalThis.fetch = originalFetch; });
  setActivePinia(createPinia());
  const store = useAppStore();
  const requests = [];
  globalThis.fetch = async (url, options) => {
    requests.push({ url, body: JSON.parse(options.body) });
    return new Response('{}', { headers: { 'Content-Type': 'application/json' } });
  };
  store.patternExecutionId = 91;
  store.currentStepVisit = { visitId: 12, stepId: 2 };
  store.executionLoggingEnabled = false;
  store.recordPatternAction('wrongTouch');
  assert.equal(requests.length, 0);
  store.executionLoggingEnabled = true;
  store.recordPatternAction('wrongTouch');
  store.recordPatternAction('wrongTouch');
  await new Promise((resolve) => setImmediate(resolve));
  assert.equal(requests.length, 2);
  assert.equal(requests[1].url, '/api/pattern-executions/91/visits/12');
  assert.deepEqual(requests[1].body, { retryCount: 0, backCount: 0, wrongTouchCount: 2, routeDeviation: false });
  store.currentStepVisit = null;
  store.recordPatternAction('wrongTouch');
  await new Promise((resolve) => setImmediate(resolve));
  assert.equal(requests.length, 2);
});
