const { test } = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const YAML = require('yaml');
const Ajv = require('ajv-draft-04');
const addFormats = require('ajv-formats');

const root = path.resolve(__dirname, '..');
const api = YAML.parse(fs.readFileSync(path.join(root, 'openapi.yaml'), 'utf8'));
const ajv = new Ajv({ strict: false, allErrors: true });
addFormats(ajv);
ajv.addFormat('binary', true);
ajv.addSchema({ id: 'danjjak', components: api.components }, 'danjjak');
const validators = new Map();
function check(schema, value, expected = true) {
  if (!validators.has(schema)) {
    validators.set(schema, ajv.compile({ $ref: `danjjak#/components/schemas/${schema}` }));
  }
  const validator = validators.get(schema);
  assert.equal(validator(value), expected, `${schema}: ${JSON.stringify(validator.errors)}`);
}
function example(file) {
  return JSON.parse(fs.readFileSync(path.join(root, 'examples', file), 'utf8')).value;
}
const id = '101';
const uuid = '00000000-0000-4000-8000-000000000001';
const instant = '2026-09-08T03:00:00.000Z';
const counts = { retryCount: 0, backCount: 0, wrongTouchCount: 0, routeDeviation: false };
const lastVisit = { visitId: uuid, leftAt: instant, counts };
const operationList = Object.entries(api.paths).flatMap(([url, methods]) =>
  Object.entries(methods).map(([method, operation]) => ({ url, method, ...operation })));

test('모든 스키마를 실제 검증기로 컴파일한다', () => {
  for (const name of Object.keys(api.components.schemas)) {
    assert.doesNotThrow(() => ajv.compile({ $ref: `danjjak#/components/schemas/${name}` }), name);
  }
});

test('모든 동작에 고유 ID·요구사항·실패 응답과 인증 경계가 있다', () => {
  const seen = new Set();
  const publicIds = new Set(['getHealth', 'startKakaoLogin', 'completeKakaoLogin', 'getSession']);
  const redirects = new Set(['startKakaoLogin', 'completeKakaoLogin']);
  for (const op of operationList) {
    assert.ok(!seen.has(op.operationId), op.operationId);
    seen.add(op.operationId);
    assert.ok(op['x-requirements'].length > 0, op.operationId);
    assert.ok(op.responses['400'] && op.responses['500'], op.operationId);
    if (redirects.has(op.operationId)) {
      assert.ok(op.responses['302'].headers.Location);
    } else {
      assert.ok(Object.keys(op.responses).some((status) => /^2\d\d$/.test(status)), op.operationId);
    }
    if (publicIds.has(op.operationId)) assert.deepEqual(op.security, []);
    else {
      assert.ok(op.responses['401'] && op.responses['403'], op.operationId);
      if (op.url.includes('{')) assert.ok(op.responses['404'], op.operationId);
      if (op.method !== 'get') {
        assert.deepEqual(op.security, [{ SessionCookie: [], CsrfToken: [] }]);
      }
    }
    for (const [status, response] of Object.entries(op.responses)) {
      if (!/^[45]/.test(status)) continue;
      const resolved = api.components.responses[response.$ref.split('/').at(-1)];
      assert.equal(resolved.content['application/json'].schema.$ref, '#/components/schemas/ApiError');
    }
  }
});

test('확정 FR 전체가 서버 동작 또는 명시적 FE 전용 범위에 연결된다', () => {
  const covered = new Set(operationList.flatMap((op) => op['x-requirements']));
  const frontendOnly = new Set(['FR-018', 'FR-027', 'FR-060']);
  for (let number = 1; number <= 60; number += 1) {
    const requirement = `FR-${String(number).padStart(3, '0')}`;
    assert.ok(covered.has(requirement) || frontendOnly.has(requirement), requirement);
  }
  assert.ok(!covered.has('FR-061'), '선택 사전 생성 API를 필수 범위에 넣지 않는다');
  assert.ok(!Object.keys(api.paths).some((url) => /recognition|transcription|pre-generation/.test(url)));
});

test('독립 동의 네 조합과 완료 여부를 구별한다', () => {
  for (const usageRecording of [false, true]) {
    for (const guardianSharing of [false, true]) {
      check('ConsentUpdate', { usageRecording, guardianSharing });
      check('Consents', { completed: true, usageRecording, guardianSharing });
    }
  }
  check('ConsentUpdate', { usageRecording: false }, false);
  check('ConsentUpdate', { usageRecording: false, guardianSharing: false, completed: false }, false);
});

test('세션 실패와 익명 200을 구별하고 모순된 사용자 응답을 거절한다', () => {
  const anonymous = example('session-anonymous.json');
  check('Session', anonymous);
  check('Session', { ...anonymous, authenticated: true }, false);
  check('Session', { ...anonymous, user: {} }, false);
});

test('ID를 숫자로 전달하거나 0으로 전달할 수 없다', () => {
  check('Id', id);
  check('Id', '9007199254740992');
  for (const invalid of [101, '0', '-1', '', '1.1']) check('Id', invalid, false);
});

test('계좌번호의 숫자 수와 하이픈 경계를 검증한다', () => {
  for (const valid of ['12345678', '12-345-678', '12345678901234567890']) check('AccountNumberInput', valid);
  for (const invalid of ['1234567', '123456789012345678901', '-12345678', '12345678-', '1234--5678', '1234 5678', 'abcd12345678']) {
    check('AccountNumberInput', invalid, false);
  }
});

test('사람 생성에 최초 계좌가 필수이며 다른 계좌를 사람 수정에 섞지 않는다', () => {
  const person = { requestId: uuid, name: '예시', relationship: '가족', firstAccount: { bankCode: '004', accountNumber: '00000000' } };
  check('PersonCreate', person);
  const { firstAccount, ...withoutAccount } = person;
  check('PersonCreate', withoutAccount, false);
  check('PersonUpdate', { expectedRevision: 1, name: '예시', relationship: '가족', firstAccount }, false);
});

test('받는 계좌 응답에 잔액·PIN·전체 번호를 허용하지 않는다', () => {
  const account = { recipientAccountId: id, registeredPersonId: '201', bankCode: '004', bankName: '예시 은행', accountAlias: null, maskedAccountNumber: '000-****-0000', revision: 1 };
  check('RecipientAccount', account);
  for (const extra of [{ balance: 0 }, { pin: '0000' }, { accountNumber: '00000000' }]) check('RecipientAccount', { ...account, ...extra }, false);
});

test('패턴 번호·개수의 경계와 수정 책임을 제한한다', () => {
  const pattern = { requestId: uuid, templateId: id, shortcutNumber: 12, title: '예시 업무' };
  check('PatternCreate', pattern);
  for (const number of [0, 13]) check('PatternCreate', { ...pattern, shortcutNumber: number }, false);
  check('PatternUpdate', { expectedRevision: 1 }, false);
  check('PatternUpdate', { expectedRevision: 1, description: '다른 경로의 문구' }, false);
  const items = Array.from({ length: 12 }, (_, index) => ({ patternId: String(index + 1), shortcutNumber: index + 1 }));
  check('OrderUpdate', { expectedRevision: 1, swapConfirmed: true, items });
  check('OrderUpdate', { expectedRevision: 1, swapConfirmed: true, items: [...items, items[0]] }, false);
});

test('안내 생략·기본값 복원·상속을 구별하고 공백 문구를 거절한다', () => {
  check('GuidanceUpdate', { expectedRevision: 1, text: null });
  check('GuidanceUpdate', { expectedRevision: 1, voiceMode: null });
  check('GuidanceUpdate', { expectedRevision: 1, text: '가'.repeat(500) });
  for (const text of ['', '   ', '가'.repeat(501)]) check('GuidanceUpdate', { expectedRevision: 1, text }, false);
  check('GuidanceUpdate', { expectedRevision: 1 }, false);
});

test('START 안내에 단계 ID를, STEP 안내에 빈 단계 ID를 넣지 못한다', () => {
  const base = { targetId: id, patternId: '201', text: '안내', defaultText: '안내', customText: null, voiceMode: null, effectiveVoiceMode: 'TTS', revision: 1, textRevision: 1, recordingMismatch: false, audio: null };
  check('GuidanceTarget', { ...base, kind: 'START', stepId: null });
  check('GuidanceTarget', { ...base, kind: 'STEP', stepId: '301' });
  check('GuidanceTarget', { ...base, kind: 'START', stepId: '301' }, false);
  check('GuidanceTarget', { ...base, kind: 'STEP', stepId: null }, false);
});

test('녹음 교체 요청은 업로드 ID·비교 버전·대본을 포함한다', () => {
  const request = { uploadId: uuid, expectedRevision: 1, recordedText: '안내', file: '검증용 바이너리 자리표시자' };
  check('AudioUpload', request);
  for (const field of ['uploadId', 'expectedRevision', 'recordedText']) {
    const incomplete = { ...request }; delete incomplete[field];
    check('AudioUpload', incomplete, false);
  }
  const audio = { audioId: uuid, audioUrl: `/api/audio/${uuid}`, mimeType: 'audio/webm', sizeBytes: 10485760, durationSeconds: 90, recordedTextRevision: 1 };
  check('FamilyAudio', audio);
  for (const extra of [{ sizeBytes: 10485761 }, { sizeBytes: 0 }, { durationSeconds: 90.1 }, { mimeType: 'text/plain' }]) check('FamilyAudio', { ...audio, ...extra }, false);
});

test('직접·비기록 패턴의 금융 요청에 행동/실행 기록을 섞지 못한다', () => {
  const request = example('transfer-request.json');
  check('TransferRequest', request);
  check('TransferRequest', { ...request, recipient: { type: 'REGISTERED', recipientAccountId: '301' } });
  check('TransferContext', { type: 'PATTERN_UNRECORDED', patternId: '201' });
  check('TransferContext', { type: 'PATTERN_RECORDED', patternId: '201', executionId: '401', lastVisit });
  check('TransferContext', { type: 'DIRECT', executionId: '401' }, false);
  check('TransferContext', { type: 'PATTERN_UNRECORDED', patternId: '201', lastVisit }, false);
  check('TransferRecipient', { type: 'REGISTERED', recipientAccountId: '301', accountNumber: '00000000' }, false);
  check('TransferRequest', { ...request, context: { type: 'PATTERN_UNRECORDED', patternId: '201' } }, false);
  check('TransferRequest', { ...request, recipient: { type: 'REGISTERED', recipientAccountId: '301' }, context: { type: 'PATTERN_UNRECORDED', patternId: '201' } });
});

test('송금 금액·PIN 형식을 검증하고 명령 원문을 받지 않는다', () => {
  const request = example('transfer-request.json');
  for (const amount of [0, -1, 0.5, '50000', 9007199254740992]) check('TransferRequest', { ...request, amount }, false);
  for (const pin of ['123', '12345', 1234, 'abcd']) check('TransferRequest', { ...request, pin }, false);
  check('TransferRequest', { ...request, transcript: '일 번' }, false);
  check('SpeechRequest', { text: '안내', speed: 'NORMAL' });
  check('SpeechRequest', { text: '안내', speed: 'NORMAL', pin: '0000' }, false);
});

test('방문은 원시 입력 없이 누적 합계만 받으며 임의 완료가 불가능하다', () => {
  check('VisitUpdate', { counts });
  check('VisitUpdate', { counts: { ...counts, retryCount: -1 } }, false);
  check('VisitUpdate', { counts: { ...counts, accountNumber: '00000000' } }, false);
  check('VisitUpdate', { counts, leftAt: instant }, false);
  check('FinishRequest', { status: 'CANCELLED', lastVisit: null, occurredAt: instant });
  check('FinishRequest', { status: 'COMPLETED', lastVisit, occurredAt: instant }, false);
  check('VisitStart', { visitId: uuid, stepId: id, enteredAt: instant, previousVisit: null });
});

test('경고 대기에 거래/차감 결과가 없고 사유 수와 위험 정도가 일치한다', () => {
  const review = example('transfer-review.json');
  check('TransferResult', review);
  check('TransferResult', { ...review, riskLevel: 'MEDIUM', reasons: ['HIGH_AMOUNT'] });
  for (const extra of [{ transactionId: '501' }, { balanceAfter: 0 }, { riskLevel: 'NORMAL' }, { reasons: ['HIGH_AMOUNT'] }, { reasons: ['HIGH_AMOUNT', 'HIGH_AMOUNT'] }]) {
    check('TransferResult', { ...review, ...extra }, false);
  }
});

test('완료에는 거래가 필요하고 취소는 거래를 반환하지 않는다', () => {
  const review = example('transfer-review.json');
  const { riskLevel, reasons, recentTransferCount, assessedAt, ...common } = review;
  const completed = { ...common, status: 'COMPLETED', transactionId: '501', balanceAfter: 50000, completedAt: instant };
  check('TransferResult', completed);
  const { transactionId, ...missingTransaction } = completed;
  check('TransferResult', missingTransaction, false);
  const cancelled = { ...common, status: 'CANCELLED', cancelledAt: instant };
  check('TransferResult', cancelled);
  check('TransferResult', { ...cancelled, transactionId }, false);
});

test('모의 카카오 결과에는 실제 전송 시각이 없고 명시적 선택이 필요하다', () => {
  const result = { mode: 'MOCK_NO_CREDENTIALS', message: '실제 전송 없는 시연', sentAt: null, recipient: 'SELF' };
  check('NotificationResult', result);
  check('NotificationResult', { ...result, sentAt: instant }, false);
  check('NotificationResult', { ...result, mode: 'ACTUAL' }, false);
  check('NotificationRequest', { confirmedSelfDemo: true });
  check('NotificationRequest', { confirmedSelfDemo: false }, false);
});

test('문구 적용은 비교 기간·현재/제안 문구·버전을 모두 요구한다', () => {
  const apply = { from: '2026-09-02', to: '2026-09-08', expectedText: '현재 안내', suggestedText: '제안 안내', expectedRevision: 1 };
  check('SuggestionApply', apply);
  for (const field of Object.keys(apply)) {
    const incomplete = { ...apply }; delete incomplete[field];
    check('SuggestionApply', incomplete, false);
  }
});
