import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import test from 'node:test';
import { compileScript, parse } from '@vue/compiler-sfc';
import { createRenderer, nextTick } from 'vue';
import { createPinia } from 'pinia';
import { useAppStore } from '../src/stores/appStore.js';
import { bindRouter } from '../src/router/navigation.js';
import { csrfHeaders, setCsrfToken } from '../src/api/httpClient.js';
import { isPhoneNumber, telephoneHref } from '../src/features/support/contact.js';
import { hasGuardianSharingConsent } from '../src/features/support/consent.js';

const originalFetch = globalThis.fetch;
const originalDocument = globalThis.Document;
const originalShadowRoot = globalThis.ShadowRoot;
globalThis.Document = class {};
globalThis.ShadowRoot = class {};
test.after(() => {
  if (originalDocument === undefined) delete globalThis.Document;
  else globalThis.Document = originalDocument;
  if (originalShadowRoot === undefined) delete globalThis.ShadowRoot;
  else globalThis.ShadowRoot = originalShadowRoot;
});
const apps = [];
const response = (body, status = 200) => new Response(JSON.stringify(body), { status });
const support = (phoneNumber = '010-1234-5678') => ({ guardian: { phoneNumber }, customerCenter: { name: '연습 고객센터', phoneNumber: '1588-1234' }, notificationRecipient: 'SELF' });
const user = (userId = '1', agreed = true) => ({ userId, name: '단짝', consents: { guardianShareAgreed: agreed } });
function storeFor() {
  const store = useAppStore(createPinia());
  store.applyCurrentUser(user());
  // 연락처 외 화면 영역은 이 테스트의 연동 범위가 아니다.
  store.loadFinancialData = async () => {};
  store.loadOwnedAccounts = async () => {};
  return store;
}
function high(store) { store.anomaly = { anomalyEventId: '9007199254740993', riskLevel: 'HIGH', reasons: ['HIGH_AMOUNT', 'REPEATED_TRANSFER'], recentTransferCount: 2, amount: 10000000, recipient: { name: '친구', masked: '123-**-456' } }; }
function deferred() { let resolve; const promise = new Promise((done) => { resolve = done; }); return { promise, resolve }; }
async function settle() { await new Promise((resolve) => setImmediate(resolve)); await nextTick(); }
function resolveImports(code, base) {
  return code.replace(/from (['"])([^'"]+)\1/g, (_, quote, path) => 'from ' + JSON.stringify(path.startsWith('.')
    ? new URL(path + (path.endsWith('.js') ? '' : '.js'), base).href : import.meta.resolve(path)));
}
async function loadView(name) {
  const url = new URL('../src/views/' + name + '.vue', import.meta.url);
  const compiled = compileScript(parse(await readFile(url, 'utf8')).descriptor, { id: name, inlineTemplate: true });
  const code = ("import { h } from 'vue';\n" + compiled.content).replace(/import (\w+) from (['"])[^'"]+\.vue\2;/g,
    (_, component) => `const ${component} = { setup: (_, { slots }) => () => h('${component === 'Btn' ? 'button' : 'div'}', null, slots.default?.()) };`);
  return (await import('data:text/javascript;base64,' + Buffer.from(resolveImports(code, url)).toString('base64'))).default;
}
const SettingsView = await loadView('SettingsView');
const TaskView = await loadView('TaskView');
const TransferView = await loadView('TransferFlowView');

const node = (type, text = '') => ({ type, tagName: type.toUpperCase(), text, children: [], props: {}, parent: null, listeners: {},
  getRootNode() { return this.parent?.getRootNode() ?? this; },
  focus() { this.getRootNode().activeElement = this; },
  addEventListener(event, fn) { this.listeners[event] = fn; }, removeEventListener(event) { delete this.listeners[event]; } });
const renderer = createRenderer({
  createElement: (type) => node(type), createText: (text) => node('text', text), createComment: () => node('comment'),
  setText: (target, text) => { target.text = text; }, setElementText: (target, text) => { target.text = text; target.children = []; },
  patchProp: (target, key, previous, value) => { target.props[key] = value; if (key === 'value') { target.value = value; target._value = value; } },
  insert(child, parent, anchor = null) { if (child.parent) this.remove(child); const index = anchor ? parent.children.indexOf(anchor) : -1; parent.children.splice(index < 0 ? parent.children.length : index, 0, child); child.parent = parent; },
  remove(child) { child.parent?.children.splice(child.parent.children.indexOf(child), 1); child.parent = null; },
  parentNode: (target) => target.parent, nextSibling: (target) => target.parent?.children[target.parent.children.indexOf(target) + 1],
});
const flatten = (target) => [target, ...target.children.flatMap(flatten)];
const content = (root) => flatten(root).map((item) => item.text).join(' ');
const action = (root, label) => flatten(root).find((item) => item.type === 'button' && content(item).trim() === label);
async function mount(view, store, props = {}) {
  const root = node('root');
  const app = renderer.createApp(view, props);
  app.use(store._p);
  apps.push(app);
  app.mount(root);
  await settle();
  return root;
}

const phoneInput = (root) => flatten(root).find((item) => item.type === 'input' && item.props['aria-label'] === '보호자 전화번호');
async function typePhone(root, value) {
  const input = phoneInput(root);
  input.value = value;
  input.listeners.input({ target: input });
  input.props.onInput();
  await nextTick();
}
const telLinks = (root) => flatten(root).filter((item) => item.type === 'a' && item.props.href?.startsWith('tel:'));
test.afterEach(() => { for (const app of apps.splice(0)) app.unmount(); globalThis.fetch = originalFetch; bindRouter(null); setCsrfToken(null); });

for (const [label, consents, allowed] of [
  ['정식 동의', { guardianSharing: true }, true],
  ['정식 거절', { guardianSharing: false }, false],
  ['정식 동의와 이전 거절', { guardianSharing: true, guardianShareAgreed: false }, true],
  ['정식 거절과 이전 동의', { guardianSharing: false, guardianShareAgreed: true }, false],
  ['이전 동의만 존재', { guardianShareAgreed: true }, true],
  ['이전 거절만 존재', { guardianShareAgreed: false }, false],
  ['동의 없음', undefined, false],
  ['정식 null과 이전 동의', { guardianSharing: null, guardianShareAgreed: true }, false],
  ['정식 undefined와 이전 동의', { guardianSharing: undefined, guardianShareAgreed: true }, false],
  ['정식 문자열과 이전 동의', { guardianSharing: 'true', guardianShareAgreed: true }, false],
]) {
  test(`보호자 공유 ${label}: 화면과 저장소가 같은 동의 우선순위를 적용한다`, async () => {
    const store = storeFor(); high(store);
    store.currentUser.consents = consents;
    let sends = 0;
    globalThis.fetch = async (url) => {
      if (url === '/api/support') return response(support());
      assert.ok(url.endsWith('/guardian-notification'));
      sends += 1;
      return response({ mode: 'MOCK_NO_CREDENTIALS', message: '모의 시연', sentAt: null, recipient: 'SELF' });
    };
    assert.equal(hasGuardianSharingConsent(consents), allowed);
    const root = await mount(TransferView, store, { flowStep: 'fraud-warning' });
    assert.equal(Boolean(action(root, '보호자에게 카톡 보내기')), allowed);
    if (allowed) {
      await action(root, '보호자에게 카톡 보내기').props.onClick(); await nextTick();
      await action(root, '본인 전송 시연 확인').props.onClick();
      assert.equal(store.notificationResult.mode, 'MOCK_NO_CREDENTIALS');
      assert.equal(sends, 1);
    } else {
      assert.ok(action(root, '동의 설정 변경하기'));
      await assert.rejects(store.sendGuardianNotification(true), { code: 'GUARDIAN_SHARE_CONSENT_REQUIRED' });
      assert.equal(sends, 0);
    }
  });
}

test('열린 알림 확인 중 정식 거절로 바뀌면 이전 동의가 있어도 패널을 닫고 전송을 거부한다', async () => {
  const store = storeFor(); high(store);
  store.currentUser.consents = { guardianSharing: true, guardianShareAgreed: true };
  globalThis.fetch = async (url) => { assert.equal(url, '/api/support'); return response(support()); };
  const root = await mount(TransferView, store, { flowStep: 'fraud-warning' });
  await action(root, '보호자에게 카톡 보내기').props.onClick(); await nextTick();
  assert.ok(action(root, '본인 전송 시연 확인'));
  store.currentUser.consents.guardianSharing = false;
  await nextTick();
  assert.equal(action(root, '본인 전송 시연 확인'), undefined);
  assert.equal(action(root, '보호자에게 카톡 보내기'), undefined);
  await assert.rejects(store.sendGuardianNotification(true), { code: 'GUARDIAN_SHARE_CONSENT_REQUIRED' });
});

for (const decision of ['CONTINUE', 'CANCEL']) {
  test(`알림 대기 중 재확인·계속·취소를 화면/저장소에서 잠그고 실패 후 ${decision} 결정을 허용한다`, async () => {
    const store = storeFor(); high(store);
    store.currentUser.consents = { guardianSharing: true };
    await store.navigate('fraud-warning');
    const pending = deferred();
    let sends = 0;
    const decisions = [];
    globalThis.fetch = async (url, options) => {
      if (url === '/api/support') return response(support());
      if (url.endsWith('/guardian-notification')) { sends += 1; return pending.promise; }
      assert.ok(url.endsWith('/resolve'));
      decisions.push(JSON.parse(options.body));
      return response({ anomalyEventId: store.anomaly.anomalyEventId, action: decision,
        transactionId: decision === 'CONTINUE' ? 11 : null, balanceAfter: decision === 'CONTINUE' ? 100 : null });
    };
    const root = await mount(TransferView, store, { flowStep: 'fraud-warning' });
    const sending = store.sendGuardianNotification(true);
    const rejected = assert.rejects(sending, { code: 'NOTIFICATION_RESULT_UNKNOWN' });
    await nextTick();
    const topBar = flatten(root).find((item) => item.props.rightLabel === '취소');
    assert.equal(topBar.props.rightDisabled, true);
    for (const label of ['거래 정보 다시 확인', '확인 후 계속 송금', '송금 취소하기']) {
      assert.equal(action(root, label).props.disabled, true);
      // DOM의 disabled를 우회해 호출해도 저장소의 잠금이 상태 변경을 막는다.
      await action(root, label).props.onClick();
    }
    await topBar.props.onRight();
    assert.equal(store.recheckAnomaly(), false);
    assert.equal(await store.resolveAnomaly('CONTINUE'), null);
    assert.equal(await store.resolveAnomaly('CANCEL'), null);
    assert.equal(store.anomalyRechecked, false);
    assert.equal(store.screen, 'fraud-warning');
    assert.equal(store.transferResult, null);
    assert.equal(store.transferCancelled, false);
    assert.deepEqual(decisions, []);
    assert.notEqual(action(root, '보호자에게 전화하기').props.disabled, true);
    await action(root, '보호자에게 전화하기').props.onClick(); await nextTick();
    assert.equal(telLinks(root)[0].props.href, 'tel:01012345678');
    pending.resolve(response({ code: 'NOTIFICATION_RESULT_UNKNOWN' }, 502));
    await rejected; await settle();
    assert.equal(store.notificationSending, false);
    assert.equal(topBar.props.rightDisabled, false);
    for (const label of ['거래 정보 다시 확인', '확인 후 계속 송금', '송금 취소하기']) {
      assert.equal(action(root, label).props.disabled, false);
    }
    await action(root, '거래 정보 다시 확인').props.onClick();
    assert.equal(store.anomalyRechecked, true);
    assert.equal(store.screen, 'final-confirm');
    await action(root, decision === 'CONTINUE' ? '확인 후 계속 송금' : '송금 취소하기').props.onClick();
    assert.deepEqual(decisions, [{ action: decision, rechecked: true }]);
    assert.equal(store.screen, decision === 'CONTINUE' ? 'complete' : 'cancelled');
    assert.equal(sends, 1);
  });
}

test('전화번호 계약 경계와 전화 링크: 공백·연속/앞뒤 하이픈·URI·개행은 허용하지 않는다', async () => {
  const store = storeFor();
  globalThis.fetch = () => { throw new Error('유효하지 않은 번호는 요청하면 안 된다'); };
  for (const value of ['', null, '1234', '1'.repeat(31), '-12345', '12345-', '12--345', ' 12345', '12345 ', '+82101234', 'tel:12345', '12345\n', '１２３４５']) {
    assert.equal(isPhoneNumber(value), false, String(value));
    assert.equal(telephoneHref(value), null);
    await assert.rejects(store.saveGuardian(value));
  }
  for (const value of ['12345', '1'.repeat(30), '12-34', '010-0012-0034']) assert.equal(isPhoneNumber(value), true);
  assert.equal(telephoneHref('010-0012-0034'), 'tel:01000120034');
});

test('설정: 늦은 조회와 조회 실패/재시도에도 입력 초안을 보존한다', async () => {
  const store = storeFor();
  const first = deferred();
  let reads = 0;
  globalThis.fetch = async () => ++reads === 1 ? first.promise : response(support());
  const root = await mount(SettingsView, store);
  await typePhone(root, '02-123-4567');
  first.resolve(response({}, 503));
  await settle();
  assert.equal(phoneInput(root).value, '02-123-4567');
  assert.match(content(root), /연락처 서비스를 준비 중/);
  await action(root, '번호 다시 불러오기').props.onClick();
  await settle();
  assert.equal(phoneInput(root).value, '02-123-4567');
  assert.match(content(root), /저장된 번호: 010-1234-5678/);
  assert.doesNotMatch(content(root), /저장한 번호를 보호자 전화 연결에 반영/);
});

test('설정: 저장 중 잠금·중복 방지, 실패 초안 유지, 서버 응답과 재조회 번호 일치', async () => {
  const store = storeFor();
  let saved = '010-1234-5678';
  let pending = deferred();
  let writes = 0;
  setCsrfToken('guardian-csrf');
  globalThis.fetch = async (url, options) => {
    if (url === '/api/support') return response(support(saved));
    assert.equal(url, '/api/support/guardian');
    assert.equal(options.method, 'PUT');
    assert.equal(options.credentials, 'include');
    assert.equal(options.headers['X-CSRF-Token'], 'guardian-csrf');
    assert.deepEqual(JSON.parse(options.body), { phoneNumber: '010-0012-0034' });
    writes += 1;
    return pending.promise;
  };
  const root = await mount(SettingsView, store);
  await typePhone(root, '010-0012-0034');
  const button = action(root, '저장');
  button.props.onClick();
  button.props.onClick();
  await nextTick();
  assert.equal(writes, 1);
  assert.equal(action(root, '저장 중…').props.disabled, true);
  assert.equal(phoneInput(root).props.disabled, true);
  pending.resolve(response({}, 500));
  await settle();
  assert.equal(phoneInput(root).value, '010-0012-0034');
  assert.equal(store.support.guardian.phoneNumber, saved);
  assert.match(content(root), /저장하지 못했어요/);
  assert.doesNotMatch(content(root), /저장한 번호를 보호자 전화 연결에 반영/);
  pending = deferred();
  action(root, '저장').props.onClick();
  saved = '010-0012-0034';
  pending.resolve(response({ phoneNumber: saved }));
  await settle();
  assert.match(content(root), /저장한 번호를 보호자 전화 연결에 반영/);
  assert.equal(store.support.customerCenter.name, '연습 고객센터');
  await store.loadSupport(true);
  assert.equal(store.support.guardian.phoneNumber, saved);
  await typePhone(root, '010-9999-0000');
  assert.doesNotMatch(content(root), /저장한 번호를 보호자 전화 연결에 반영/);
});

test('저장 성공 뒤 끝난 이전 GET은 저장 번호를 되돌리지 않고 조회 중복은 같은 완료를 기다린다', async () => {
  const store = storeFor();
  const read = deferred();
  let reads = 0;
  globalThis.fetch = async (url) => {
    if (url === '/api/support') { reads += 1; return read.promise; }
    return response({ phoneNumber: '02-000-1234' });
  };
  const first = store.loadSupport();
  const second = store.loadSupport();
  let secondDone = false;
  second.then(() => { secondDone = true; });
  await settle();
  assert.equal(secondDone, false);
  await store.saveGuardian('02-000-1234');
  read.resolve(response(support('010-9999-9999')));
  assert.deepEqual(await Promise.all([first, second]), [true, true]);
  assert.equal(reads, 1);
  assert.equal(store.support.guardian.phoneNumber, '02-000-1234');
  assert.equal(store.support.customerCenter.phoneNumber, '1588-1234');
});

for (const status of [200, 500]) {
  test(`세션 변경: 이전 조회/저장 ${status} 응답은 새 사용자 번호·오류·잠금을 변경하지 않는다`, async () => {
    const store = storeFor();
    const oldRead = deferred(); const oldSave = deferred(); const newRead = deferred(); const newSave = deferred();
    let reads = 0; let saves = 0;
    globalThis.fetch = async (url) => url === '/api/support'
      ? (++reads === 1 ? oldRead.promise : newRead.promise)
      : (++saves === 1 ? oldSave.promise : newSave.promise);
    const root = await mount(SettingsView, store);
    await typePhone(root, '010-1111-1111');
    action(root, '저장').props.onClick();
    store.clearSession();
    store.applyCurrentUser(user('2'));
    assert.equal(store.support, null);
    await nextTick();
    assert.equal(phoneInput(root).value, '');
    const freshRead = store.loadSupport();
    await typePhone(root, '010-2222-2222');
    action(root, '저장').props.onClick();
    oldRead.resolve(response(support('010-1111-1111'), status));
    oldSave.resolve(response({ phoneNumber: '010-1111-1111' }, status));
    await settle();
    assert.equal(store.support, null);
    assert.equal(store.supportLoading, true);
    assert.equal(store.guardianSaving, true);
    assert.equal(store.supportError, '');
    assert.equal(store.guardianSaveError, '');
    assert.equal(phoneInput(root).value, '010-2222-2222');
    assert.doesNotMatch(content(root), /1111|저장한 번호를 보호자 전화 연결에 반영/);
    newSave.resolve(response({ phoneNumber: '010-2222-2222' }));
    newRead.resolve(response(support('010-2222-2222')));
    await freshRead;
    await settle();
    assert.equal(store.support.guardian.phoneNumber, '010-2222-2222');
  });
}

test('잘못된 저장 응답은 성공 상태를 만들지 않는다', async () => {
  const store = storeFor();
  store.support = support();
  for (const body of [null, {}, { phoneNumber: null }, { phoneNumber: '123--456' }]) {
    globalThis.fetch = async () => response(body);
    await assert.rejects(store.saveGuardian('010-4444-4444'));
    assert.equal(store.support.guardian.phoneNumber, '010-1234-5678');
    assert.equal(store.guardianSaving, false);
  }
});

test('경고 전화: 저장 번호를 읽고 확인 패널을 선택해야 숫자 tel 링크가 나타나며 취소·세션 변경 시 닫힌다', async () => {
  const store = storeFor(); high(store);
  let requests = 0;
  globalThis.fetch = async (url) => { requests += 1; assert.equal(url, '/api/support'); return response(support('010-0012-0034')); };
  const root = await mount(TransferView, store, { flowStep: 'fraud-warning' });
  assert.match(content(root), /저장된 번호: 010-0012-0034/);
  assert.match(content(root), /통화를 지원하지 않는 기기/);
  assert.equal(telLinks(root).length, 0);
  await action(root, '보호자에게 전화하기').props.onClick(); await nextTick();
  assert.match(content(root), /010-0012-0034 번호가 맞나요/);
  const link = telLinks(root)[0];
  assert.equal(link.props.href, 'tel:01000120034');
  assert.equal(link.props.onClick, undefined); // 링크를 실행하지 않고 외부 앱과 부작용이 없는 구조만 검증한다.
  assert.equal(store.transferResult, null);
  assert.equal(store.transferCancelled, false);
  assert.equal(store.anomaly.decision, undefined);
  assert.equal(requests, 1);
  await action(root, '전화 취소').props.onClick(); await nextTick();
  assert.equal(telLinks(root).length, 0);
  await action(root, '보호자에게 전화하기').props.onClick(); await nextTick();
  store.clearSession(); await nextTick();
  assert.equal(telLinks(root).length, 0);
  assert.doesNotMatch(content(root), /0012-0034/);
});

test('보호자 미등록은 설정으로 연결하고 고객센터는 중첩된 이름/번호와 읽기 대안을 표시한다', async () => {
  const store = storeFor(); high(store);
  globalThis.fetch = async () => response(support(null));
  const warning = await mount(TransferView, store, { flowStep: 'fraud-warning' });
  assert.match(content(warning), /등록된 보호자 번호가 없어요/);
  assert.equal(telLinks(warning).length, 0);
  await action(warning, '보호자 연락처 등록하기').props.onClick();
  assert.equal(store.screen, 'settings');
  let completions = 0;
  store.finishPatternExecution = async () => { completions += 1; };
  const task = await mount(TaskView, store, { taskName: 'task-6' });
  assert.match(content(task), /연습 고객센터/);
  assert.match(content(task), /1588-1234/);
  assert.match(content(task), /휴대전화로 걸어 주세요/);
  assert.equal(telLinks(task)[0].props.href, 'tel:15881234');
  assert.equal(completions, 0);
});

test('고객센터 설정 누락 503은 번호/전화 링크 없이 다시 조회 안내를 제공한다', async () => {
  const store = storeFor();
  globalThis.fetch = async () => response({}, 503);
  const root = await mount(TaskView, store, { taskName: 'task-6' });
  assert.match(content(root), /잠시 후 다시 조회/);
  assert.equal(telLinks(root).length, 0);
  assert.ok(action(root, '다시 시도'));
});

for (const mode of ['ACTUAL', 'MOCK_NO_CREDENTIALS', 'MOCK_AFTER_FAILURE']) {
  test(`알림 ${mode}: 두 번의 명시 선택·SELF·CSRF 계약과 실제/모의 표시`, async () => {
    const store = storeFor(); high(store);
    const pending = deferred();
    let sends = 0;
    setCsrfToken('notify-csrf');
    globalThis.fetch = async (url, options) => {
      if (url === '/api/support') return response(support());
      assert.equal(url, '/api/anomaly-events/9007199254740993/guardian-notification');
      assert.equal(options.method, 'POST');
      assert.equal(options.credentials, 'include');
      assert.equal(options.headers['X-CSRF-Token'], 'notify-csrf');
      assert.deepEqual(JSON.parse(options.body), { confirmedSelfDemo: true });
      sends += 1;
      return pending.promise;
    };
    const root = await mount(TransferView, store, { flowStep: 'fraud-warning' });
    await action(root, '보호자에게 카톡 보내기').props.onClick(); await nextTick();
    assert.match(content(root), /실제 수신자는 로그인한 본인/);
    assert.equal(sends, 0);
    await action(root, '알림 보내기 취소').props.onClick(); await nextTick();
    assert.equal(sends, 0);
    await action(root, '보호자에게 카톡 보내기').props.onClick(); await nextTick();
    action(root, '본인 전송 시연 확인').props.onClick();
    assert.equal(await store.sendGuardianNotification(true), null);
    await nextTick();
    assert.equal(action(root, '알림 요청 중…').props.disabled, true);
    const sentAt = mode === 'ACTUAL' ? '2026-09-08T10:20:00Z' : null;
    pending.resolve(response({ mode, message: '서버에서 확인한 시연 결과', sentAt, recipient: 'SELF' }));
    await settle();
    assert.equal(sends, 1);
    assert.match(content(root), /서버에서 확인한 시연 결과/);
    if (mode === 'ACTUAL') {
      assert.match(content(root), /발송 시각/);
      assert.equal(action(root, '본인 전송 시연 완료').props.disabled, true);
      assert.equal((await store.sendGuardianNotification(true)).mode, 'ACTUAL');
      assert.equal(sends, 1);
    } else {
      assert.doesNotMatch(content(root), /발송 시각/);
      assert.match(content(root), /실제 전송 없이|실패해 모의 알림/);
    }
    assert.equal(store.transferResult, null);
    assert.equal(store.transferCancelled, false);
  });
}

test('동의/명시 선택/HIGH/미결정 조건 누락은 알림 요청을 만들지 않는다', async () => {
  globalThis.fetch = () => { throw new Error('알림을 전송하면 안 된다'); };
  for (const agreed of [false, undefined]) {
    const store = storeFor(); store.currentUser.consents.guardianShareAgreed = agreed; high(store);
    await assert.rejects(store.sendGuardianNotification(true), { code: 'GUARDIAN_SHARE_CONSENT_REQUIRED' });
  }
  const store = storeFor(); high(store);
  assert.equal(await store.sendGuardianNotification(), null);
  for (const patch of [{ riskLevel: 'MEDIUM' }, { decision: 'CANCEL' }, { resolvedAt: '2026-09-08T10:20:00Z' }]) {
    high(store); Object.assign(store.anomaly, patch);
    assert.equal(await store.sendGuardianNotification(true), null);
  }
  high(store); store.transferCancelled = true;
  assert.equal(await store.sendGuardianNotification(true), null);
  store.transferCancelled = false; store.transferResult = { action: 'CONTINUE' };
  assert.equal(await store.sendGuardianNotification(true), null);
});

test('결과 불명 502는 자동 재시도/성공 표시 없이 같은 이상거래의 전화·결정을 허용한다', async () => {
  const store = storeFor(); high(store);
  let sends = 0;
  globalThis.fetch = async (url) => {
    if (url === '/api/support') return response(support());
    if (url.endsWith('/guardian-notification')) { sends += 1; return response({ code: 'NOTIFICATION_RESULT_UNKNOWN' }, 502); }
    assert.ok(url.endsWith('/resolve'));
    return response({ anomalyEventId: store.anomaly.anomalyEventId, action: 'CANCEL', transactionId: null, balanceAfter: null });
  };
  const root = await mount(TransferView, store, { flowStep: 'fraud-warning' });
  await assert.rejects(store.sendGuardianNotification(true)); await settle();
  assert.equal(store.notificationResult, null);
  assert.match(content(root), /자동으로 다시 보내지 않아요/);
  await action(root, '보호자에게 전화하기').props.onClick(); await nextTick();
  assert.equal(telLinks(root).length, 1);
  assert.equal(action(root, '확인 후 계속 송금').props.disabled, false);
  assert.equal(action(root, '송금 취소하기').props.disabled, false);
  assert.equal((await store.resolveAnomaly('CANCEL')).action, 'CANCEL');
  assert.equal(sends, 1);
});

test('다른 사용자/새 시도/해결된 시도에 도착한 알림 응답은 결과를 남기지 않는다', async () => {
  for (const change of ['session', 'attempt', 'resolved']) {
    const store = storeFor(); high(store);
    const pending = deferred();
    globalThis.fetch = async () => pending.promise;
    const sending = store.sendGuardianNotification(true);
    if (change === 'session') store.applyCurrentUser(user('2'));
    if (change === 'attempt') store.startTransfer();
    if (change === 'resolved') store.transferCancelled = true;
    pending.resolve(response({ mode: 'ACTUAL', message: '이전 결과', sentAt: '2026-09-08T10:20:00Z', recipient: 'SELF' }));
    assert.equal(await sending, null);
    assert.equal(store.notificationResult, null);
    assert.equal(store.notificationSending, false);
    assert.equal(store.transferError, '');
  }
});

test('모순된 알림 응답은 실제 성공/모의 결과로 표시하지 않는다', async () => {
  const store = storeFor(); high(store);
  for (const body of [
    { mode: 'ACTUAL', message: '성공', sentAt: null, recipient: 'SELF' },
    { mode: 'ACTUAL', message: '성공', sentAt: 'not-a-date', recipient: 'SELF' },
    { mode: 'MOCK_AFTER_FAILURE', message: '모의', sentAt: '2026-09-08T10:20:00Z', recipient: 'SELF' },
    { mode: 'MOCK_NO_CREDENTIALS', message: '모의', sentAt: null, recipient: 'GUARDIAN' },
    { mode: 'OTHER', message: '모의', sentAt: null, recipient: 'SELF' },
  ]) {
    globalThis.fetch = async () => response(body);
    await assert.rejects(store.sendGuardianNotification(true), { code: 'INVALID_RESPONSE' });
    assert.equal(store.notificationResult, null);
  }
});

test('이전 세션의 늦은 401은 새 세션 CSRF 토큰을 제거하지 않는다', async () => {
  const store = storeFor();
  const pending = deferred();
  setCsrfToken('previous-session');
  globalThis.fetch = async () => pending.promise;
  const loading = store.loadSupport();
  store.clearSession();
  store.applyCurrentUser(user('2'));
  setCsrfToken('current-session');
  pending.resolve(response({ code: 'SESSION_REQUIRED' }, 401));
  assert.equal(await loading, false);
  assert.equal(store.currentUser.userId, '2');
  assert.equal(store.supportError, '');
  assert.deepEqual(csrfHeaders(), { 'X-CSRF-Token': 'current-session' });
  globalThis.fetch = async () => response({ code: 'SESSION_REQUIRED' }, 401);
  await store.loadSupport();
  assert.deepEqual(csrfHeaders(), {});
});

for (const status of [200, 502]) {
  test(`이전 세션 알림 ${status} 응답은 새 알림의 진행 잠금·결과·오류를 덮지 않는다`, async () => {
    const store = storeFor(); high(store);
    const previous = deferred(); const current = deferred();
    let requests = 0;
    globalThis.fetch = async () => ++requests === 1 ? previous.promise : current.promise;
    const oldSend = store.sendGuardianNotification(true);
    store.clearSession(); store.applyCurrentUser(user('2')); high(store);
    const newSend = store.sendGuardianNotification(true);
    previous.resolve(response(status === 200
      ? { mode: 'ACTUAL', message: '이전 발송', sentAt: '2026-09-08T10:20:00Z', recipient: 'SELF' }
      : { code: 'NOTIFICATION_RESULT_UNKNOWN' }, status));
    assert.equal(await oldSend, null);
    assert.equal(store.notificationSending, true);
    assert.equal(store.notificationResult, null);
    assert.equal(store.transferError, '');
    current.resolve(response({ mode: 'MOCK_NO_CREDENTIALS', message: '현재 시연', sentAt: null, recipient: 'SELF' }));
    assert.equal((await newSend).message, '현재 시연');
    assert.equal(store.notificationSending, false);
  });
}
