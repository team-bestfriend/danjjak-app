import assert from 'node:assert/strict';
import test from 'node:test';
import { effectScope } from 'vue';
import { matchShortcutCommand, voiceCommandExamples } from '../src/features/voice/shortcutCommands.js';
import { useShortcutSpeech } from '../src/composables/useShortcutSpeech.js';

const patterns = [
  { patternId: 101, num: 9, label: '생활비', patternType: 'TRANSFER', linkedAccount: { relationship: '아들' } },
  { patternId: 102, num: 2, label: '딸 송금', patternType: 'TRANSFER', linkedAccount: { relationship: '딸' } },
  ...['PENSION_CHECK', 'MANAGEMENT_FEE_CHECK', 'BALANCE_CHECK', 'TRANSACTION_HISTORY', 'CUSTOMER_CENTER', 'UTILITY_BILL_CHECK']
    .map((patternType, index) => ({ patternId: 103 + index, num: 3 + index, label: patternType, patternType })),
];

test('대표 문장 8개는 번호·제목과 무관하게 서버 업무와 연결 수취인을 찾는다', () => {
  voiceCommandExamples.forEach((phrase, index) => {
    assert.deepEqual(matchShortcutCommand(phrase, patterns).map((pattern) => pattern.patternId), [101 + index]);
  });
  for (const phrase of ['우리 아들한테 돈을 보내주세요!', '아 들 에 게 송 금 해 줘.', '아들에게 이체해 줄래요?']) {
    assert.equal(matchShortcutCommand(phrase, patterns)[0]?.patternId, 101);
  }
  assert.equal(matchShortcutCommand('통장 잔액을 알려주세요.', patterns)[0]?.patternType, 'BALANCE_CHECK');
  assert.equal(matchShortcutCommand('입출금 내역 조회해 줘', patterns)[0]?.patternType, 'TRANSACTION_HISTORY');
});

test('없음·비활성·미지원·부정·복합 문장은 임의의 첫 단축번호로 대체하지 않는다', () => {
  for (const phrase of ['', '날씨 알려줘', '아들에게 돈 보내지 마', '아들에게 돈 보내 줘 그리고 잔액 알려줘', '카드 내역 보여줘']) {
    assert.deepEqual(matchShortcutCommand(phrase, patterns), []);
  }
  assert.deepEqual(matchShortcutCommand('아들에게 돈 보내 줘', []), []);
  for (const extra of [{ num: null }, { num: 13 }, { active: false }, { isActive: false }, { linkedAccount: { relationship: '친구' } }]) {
    assert.deepEqual(matchShortcutCommand('아들에게 돈 보내 줘', [{ ...patterns[0], ...extra }]), []);
  }
});

test('같은 수취인 또는 조회 업무가 여러 개면 모든 후보를 반환한다', () => {
  const duplicate = { ...patterns[0], patternId: 201, num: 10 };
  assert.equal(matchShortcutCommand('아들 송금', [...patterns, duplicate]).length, 2);
  const balance = patterns.find((pattern) => pattern.patternType === 'BALANCE_CHECK');
  assert.equal(matchShortcutCommand('잔액 확인', [...patterns, { ...balance, patternId: 202, num: 11 }]).length, 2);
});

function setup(t, availablePatterns = patterns, { supported = true, prefixed = false } = {}) {
  const instances = [];
  class Recognition {
    constructor() { instances.push(this); }
    start() { this.starts = (this.starts ?? 0) + 1; }
    stop() { this.stops = (this.stops ?? 0) + 1; }
    abort() { this.aborts = (this.aborts ?? 0) + 1; }
    result(text, isFinal = true) {
      const result = Object.assign([{ transcript: text }], { isFinal });
      this.onresult?.({ results: [result] });
    }
  }
  const original = globalThis.SpeechRecognition;
  const originalPrefixed = globalThis.webkitSpeechRecognition;
  globalThis.SpeechRecognition = supported && !prefixed ? Recognition : undefined;
  globalThis.webkitSpeechRecognition = supported && prefixed ? Recognition : undefined;
  const matches = [];
  const scope = effectScope();
  const speech = scope.run(() => useShortcutSpeech(() => availablePatterns, (pattern) => matches.push(pattern)));
  t.after(() => {
    scope.stop();
    globalThis.SpeechRecognition = original;
    globalThis.webkitSpeechRecognition = originalPrefixed;
  });
  return { speech, instances, matches, scope };
}

test('클릭으로만 시작하고 최종 결과 하나만 확인 대상으로 전달한다', (t) => {
  const { speech, instances, matches } = setup(t);
  assert.equal(instances.length, 0);
  speech.start();
  speech.start();
  assert.equal(instances.length, 1);
  const recognition = instances[0];
  assert.equal(speech.state.value, 'starting');
  assert.equal(recognition.lang, 'ko-KR');
  assert.equal(recognition.interimResults, false);
  recognition.onstart();
  assert.equal(speech.state.value, 'listening');
  recognition.result('아들 송금', false);
  assert.equal(matches.length, 0);
  const lateResult = recognition.onresult;
  recognition.result('아들 송금');
  assert.deepEqual(matches, [patterns[0]]);
  assert.equal(speech.state.value, 'idle');
  assert.equal(recognition.aborts, 1);
  assert.match(speech.message.value, /9번 생활비/);
  assert.ok(!speech.message.value.includes('아들 송금'));
  lateResult({ results: [Object.assign([{ transcript: '딸 송금' }], { isFinal: true })] });
  assert.equal(matches.length, 1);
});

test('말하기 완료는 결과를 기다리고 취소는 늦은 결과를 무시한다', (t) => {
  const { speech, instances, matches } = setup(t, patterns, { prefixed: true });
  speech.start();
  instances[0].onstart();
  speech.stop();
  assert.equal(instances[0].stops, 1);
  assert.equal(speech.state.value, 'processing');
  const lateResult = instances[0].onresult;
  speech.cancel();
  lateResult({ results: [Object.assign([{ transcript: '잔액 알려줘' }], { isFinal: true })] });
  assert.equal(matches.length, 0);
  speech.start();
  instances[1].result('잔액 알려줘');
  assert.equal(matches.length, 1);
});

test('없음·복수 일치는 확인 대상을 만들지 않고 수동 선택을 안내한다', (t) => {
  const { speech, instances, matches } = setup(t, [...patterns, { ...patterns[0], patternId: 201, num: 10 }]);
  speech.start();
  instances[0].result('아들 송금');
  assert.match(speech.message.value, /여러 개/);
  speech.start();
  instances[1].result('알 수 없는 문장');
  assert.match(speech.message.value, /찾지 못했어요/);
  assert.equal(matches.length, 0);
});

test('권한 거부·마이크 없음·네트워크 실패 후 재시도할 수 있다', (t) => {
  const { speech, instances, matches } = setup(t);
  for (const error of ['not-allowed', 'audio-capture', 'network', 'service-not-allowed', 'language-not-supported', 'no-speech']) {
    speech.start();
    instances.at(-1).onerror({ error, message: '저장하면 안 되는 원문' });
    assert.equal(speech.state.value, 'idle');
    assert.match(speech.message.value, /화면에서/);
    assert.ok(!speech.message.value.includes('저장하면'));
  }
  speech.start();
  instances.at(-1).result('잔액 알려줘');
  assert.equal(matches.length, 1);
});

test('미지원·결과 없는 종료·화면 이탈은 인식과 후보를 남기지 않는다', (t) => {
  const unavailable = setup(t, patterns, { supported: false });
  unavailable.speech.start();
  assert.equal(unavailable.instances.length, 0);
  assert.match(unavailable.speech.message.value, /지원하지 않아요/);
  const { speech, instances, scope, matches } = setup(t);
  speech.start();
  instances[0].onend();
  assert.equal(speech.state.value, 'idle');
  speech.start();
  const onResult = instances[1].onresult;
  scope.stop();
  onResult({ results: [] });
  assert.equal(instances[1].aborts, 1);
  assert.equal(matches.length, 0);
});
