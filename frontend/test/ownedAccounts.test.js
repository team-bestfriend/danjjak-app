import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import test from 'node:test';
import { compileScript, parse } from '@vue/compiler-sfc';
import { createRenderer, nextTick } from 'vue';
import { createPinia } from 'pinia';
import { useAppStore } from '../src/stores/appStore.js';
import { bindRouter } from '../src/router/navigation.js';
import { csrfHeaders, setCsrfToken } from '../src/api/httpClient.js';
import { accountApi } from '../src/api/financeApi.js';

const originalFetch = globalThis.fetch;
const apps = [];
const response = (body, status = 200) => new Response(JSON.stringify(body), { status });
const largeId = '9007199254740993';
const otherId = '9007199254740995';
const owned = (id = largeId, primary = true, balance = 35791) => ({ accountId: id, bankCode: '004', bankName: '국민은행', accountAlias: '생활비', maskedAccountNumber: '123-**-***456', balance, primary });
const candidate = (id = largeId, available = true) => ({ accountId: id, bankCode: '004', bankName: '국민은행', accountAlias: '생활비', maskedAccountNumber: '123-**-***456', available, unavailableReason: available ? null : '계좌 준비 중이에요.' });
const user = (completed = true) => ({ userId: '1', name: '김단짝', consents: { completed, usageLogAgreed: false, guardianShareAgreed: false } });
const storeFor = () => useAppStore(createPinia());
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
const ImportView = await loadView('OwnedAccountImportView');
const SettingsView = await loadView('SettingsView');
const TaskView = await loadView('TaskView');
const TransferView = await loadView('TransferFlowView');
const PersonForm = await loadView('../components/common/AddPersonForm');

const node = (type, text = '') => ({ type, tagName: type.toUpperCase(), text, children: [], props: {}, parent: null, listeners: {},
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
async function choose(root, id) {
  const input = flatten(root).find((item) => item.type === 'input' && item.props.value === id);
  assert.ok(input, '선택할 계좌 입력');
  input.checked = true;
  input.listeners.change({ target: input });
  await nextTick();
}

test.afterEach(() => { for (const app of apps.splice(0)) app.unmount(); globalThis.fetch = originalFetch; bindRouter(null); setCsrfToken(null); });

test('사람 생성·수정은 세션 CSRF 토큰과 쿠키를 전송하고 요청 본문을 유지한다', async () => {
  const store = storeFor();
  const requests = [];
  globalThis.fetch = async (url, options) => {
    if (url === '/api/auth/session') return response({ authenticated: true, user: user(), csrfToken: 'person-session-csrf' });
    requests.push({ url, options });
    return response({ registeredPersonId: '19' });
  };
  assert.equal(await store.checkSession(), true);
  const createPayload = { name: '민수', relationship: '아들', bankCode: '004', bankName: '국민은행', accountNumber: '123-45-678901', accountAlias: '생활비' };
  const updatePayload = { name: '김민수', relationship: '가족' };
  await accountApi.createRegisteredPerson(createPayload);
  await accountApi.updateRegisteredPerson('19', updatePayload);
  assert.deepEqual(requests.map(({ url, options }) => [url, options.method, JSON.parse(options.body)]), [
    ['/api/registered-persons', 'POST', createPayload],
    ['/api/registered-persons/19', 'PUT', updatePayload],
  ]);
  for (const { options } of requests) {
    assert.equal(options.headers['X-CSRF-Token'], 'person-session-csrf');
    assert.equal(options.credentials, 'include');
  }
});

test('가져오기 화면: 선택·실패·재시도·중복 잠금 후 저장된 전체 계좌를 확인하고 홈으로 이동한다', async () => {
  const store = storeFor();
  const requests = [];
  let accounts = [];
  let finish;
  let destination;
  bindRouter({ replace: (to) => { destination = to; } });
  setCsrfToken('session-csrf');
  globalThis.fetch = async (url, options) => {
    requests.push({ url, options });
    if (url === '/api/accounts') return response(accounts);
    if (url === '/api/accounts/import-candidates') return response([candidate(), candidate(otherId, false)]);
    if (url === '/api/accounts/import') return new Promise((resolve) => { finish = resolve; });
    throw new Error(url);
  };
  const root = await mount(ImportView, store);
  assert.match(content(root), /실제 은행에 연결하지 않는 연습용 계좌/);
  assert.match(content(root), /계좌 준비 중/);
  assert.equal(flatten(root).find((n) => n.props.value === otherId).props.disabled, true);
  await choose(root, largeId);
  assert.match(content(root), /✓ 선택됨/);
  action(root, '계좌 불러오기').props.onClick();
  await nextTick();
  assert.equal(action(root, '계좌를 저장하고 있어요…').props.disabled, true);
  assert.equal(await store.importOwnedAccounts(), false);
  assert.equal(requests.filter((r) => r.url === '/api/accounts/import').length, 1);
  finish(response({ code: 'SERVER_ERROR' }, 500));
  await settle();
  assert.match(content(root), /선택한 계좌로 다시 시도/);
  assert.deepEqual(store.selectedImportAccountIds, [largeId]);
  assert.equal(destination, undefined);
  action(root, '계좌 불러오기').props.onClick();
  accounts = [owned()];
  finish(response(accounts));
  await settle();
  assert.equal(destination.name, 'home');
  assert.equal(store.defaultOwnedAccount.accountId, largeId);
  assert.equal(store.defaultOwnedAccount.balance, 35791);
  assert.equal(store.defaultOwnedAccount.masked, '123-**-***456');
  assert.equal('accountNumber' in store.defaultOwnedAccount, false);
  assert.deepEqual(store.selectedImportAccountIds, []);
  for (const r of requests.filter((r) => r.url === '/api/accounts/import')) {
    assert.deepEqual(JSON.parse(r.options.body), { accountIds: [largeId] });
    assert.equal(r.options.method, 'POST');
    assert.equal(r.options.headers['X-CSRF-Token'], 'session-csrf');
  }
});

test('가져오기 화면: 후보 없음·조회 실패·재시도와 설정/도움말/로그아웃 경로를 제공한다', async () => {
  const store = storeFor();
  let fail = true;
  let destination;
  bindRouter({ push: (to) => { destination = to; }, replace: (to) => { destination = to; } });
  globalThis.fetch = async (url) => {
    if (url === '/api/accounts/import-candidates') return fail ? response({}, 500) : response([]);
    if (url === '/api/accounts') return response([]);
    if (url === '/api/auth/logout') return new Response(null, { status: 204 });
    throw new Error(url);
  };
  const root = await mount(ImportView, store);
  assert.match(content(root), /불러올 계좌를 확인하지 못했어요/);
  assert.equal(action(root, '계좌 불러오기').props.disabled, true);
  fail = false;
  await action(root, '다시 시도').props.onClick();
  await settle();
  assert.match(content(root), /불러올 계좌가 없어요/);
  assert.equal(action(root, '계좌 불러오기').props.disabled, true);
  for (const [label, name] of [['설정으로 가기', 'settings'], ['서비스 이용방법', 'service-help'], ['로그아웃', 'login']]) {
    await action(root, label).props.onClick();
    assert.equal(destination.name, name);
  }
});

test('설정: 기본 없음 설명·저장 실패 선택 유지·중복 방지·저장 응답 반영, 이번 송금 선택은 독립적이다', async () => {
  const store = storeFor();
  setCsrfToken('default-csrf');
  let accounts = [owned(largeId, false), owned(otherId, false, 81000)];
  let finish;
  let saves = 0;
  globalThis.fetch = async (url, options) => {
    if (url === '/api/accounts') return response(accounts);
    if (url === '/api/registered-persons') return response([]);
    if (url === '/api/support') return response({ guardian: null });
    if (url === '/api/accounts/default') {
      saves += 1;
      assert.equal(options.headers['X-CSRF-Token'], 'default-csrf');
      assert.equal(options.method, 'PUT');
      assert.deepEqual(JSON.parse(options.body), { accountId: otherId });
      return new Promise((resolve) => { finish = resolve; });
    }
    throw new Error(url);
  };
  const root = await mount(SettingsView, store);
  assert.match(content(root), /기본 계좌가 지정되지 않아 첫 번째/);
  assert.match(content(root), /35,791원/);
  assert.equal(store.selectedSourceAccountId, largeId);
  await choose(root, otherId);
  action(root, '기본 계좌 저장').props.onClick();
  await nextTick();
  assert.equal(action(root, '저장 중…').props.disabled, true);
  assert.equal(await store.saveDefaultAccount(), false);
  assert.equal(saves, 1);
  finish(response({}, 500));
  await settle();
  assert.equal(store.defaultAccountSelection, otherId);
  assert.equal(store.defaultOwnedAccount.accountId, largeId);
  assert.match(content(root), /기본 계좌를 저장하지 못했어요/);
  action(root, '기본 계좌 저장').props.onClick();
  accounts = [owned(largeId, false), owned(otherId, true, 81000)];
  finish(response(accounts[1]));
  await settle();
  assert.match(content(root), /기본 계좌를 저장했어요/);
  assert.equal(store.defaultOwnedAccount.accountId, otherId);
  assert.equal(store.selectedSourceAccountId, largeId);
  store.startTransfer();
  assert.equal(store.selectedSourceAccountId, otherId);
  store.selectedSourceAccountId = largeId;
  await store.loadOwnedAccounts(true);
  assert.equal(store.selectedSourceAccountId, largeId);
  assert.equal(store.defaultOwnedAccount.accountId, otherId);
  assert.equal(saves, 2);
});

test('추가 불러오기와 재로그인은 기존 잔액·기본·패턴을 보존하고 GET 전체 목록으로 준비 상태를 복구한다', async () => {
  const store = storeFor();
  let accounts = [owned()];
  globalThis.fetch = async (url) => {
    if (url === '/api/accounts') return response(accounts);
    if (url === '/api/accounts/import-candidates') return response([candidate(otherId)]);
    if (url === '/api/accounts/import') { accounts = [owned(), owned(otherId, false, 123)]; return response([accounts[1]]); }
    throw new Error(url);
  };
  await store.loadOwnedAccounts();
  store.patterns = [{ patternId: 7, label: '저장된 업무' }];
  await store.loadImportCandidates();
  store.selectedImportAccountIds = [otherId];
  assert.equal(await store.importOwnedAccounts(), true);
  assert.equal(store.ownedAccounts.length, 2);
  assert.equal(store.defaultOwnedAccount.accountId, largeId);
  assert.equal(store.defaultOwnedAccount.balance, 35791);
  assert.equal(store.patterns[0].patternId, 7);
  const relogin = storeFor();
  assert.equal(await relogin.loadOwnedAccounts(), true);
  assert.equal(relogin.ownedAccounts.length, 2);
  assert.equal(relogin.defaultOwnedAccount.balance, 35791);
});

test('세션에서 받은 CSRF는 계좌 변경에만 사용하고 만료·로그아웃 시 지우며 늦은 계좌 응답을 무시한다', async () => {
  const store = storeFor();
  let finish;
  globalThis.fetch = async (url) => {
    if (url === '/api/auth/session') return response({ authenticated: true, user: user(), csrfToken: 'csrf-value' });
    if (url === '/api/accounts') return new Promise((resolve) => { finish = resolve; });
    throw new Error(url);
  };
  assert.equal(await store.checkSession(), true);
  assert.deepEqual(csrfHeaders(), { 'X-CSRF-Token': 'csrf-value' });
  const pending = store.loadOwnedAccounts();
  store.selectedImportAccountIds = [largeId];
  store.clearSession();
  finish(response([owned()]));
  assert.equal(await pending, false);
  assert.deepEqual(store.ownedAccounts, []);
  assert.deepEqual(store.selectedImportAccountIds, []);
  assert.deepEqual(csrfHeaders(), {});
  setCsrfToken('expired');
  globalThis.fetch = async () => response({}, 401);
  assert.equal(await store.loadOwnedAccounts(), false);
  assert.deepEqual(csrfHeaders(), {});
});

let routerCounter = 0;
async function actualRouter(store) {
  const url = new URL('../src/router/index.js', import.meta.url);
  let code = (await readFile(url, 'utf8')).replace(/import (\w+) from "[^"\n]+\.vue";/g, (_, name) => 'const ' + name + ' = {};');
  code = code.replace('createRouter, createWebHistory', 'createRouter, createMemoryHistory');
  code = code.replace('createWebHistory(import.meta.env.BASE_URL)', 'createMemoryHistory()');
  const mod = await import('data:text/javascript;base64,' + Buffer.from(resolveImports(code, url) + '\n//' + ++routerCounter).toString('base64'));
  mod.installRouterGuards(store._p);
  return mod.default;
}

test('실제 라우터/모의 세션: 동의 후 계좌 준비, 금융 직접 진입 차단, 설정·도움말·고객센터 허용', async () => {
  const store = storeFor();
  let completed = false;
  let accounts = [];
  globalThis.fetch = async (url) => {
    if (url === '/api/auth/session') return response({ authenticated: true, user: { ...user(completed), accountReady: true }, csrfToken: 'csrf' });
    if (url === '/api/accounts') return response(accounts);
    throw new Error(url);
  };
  const router = await actualRouter(store);
  await router.push('/login');
  assert.equal(router.currentRoute.value.name, 'consent');
  completed = true;
  await store.checkSession(true);
  await router.push('/home');
  assert.equal(router.currentRoute.value.name, 'owned-account-import');
  for (const route of router.getRoutes().filter((route) => route.meta.requiresAccounts)) {
    await router.push({ name: route.name });
    assert.equal(router.currentRoute.value.name, 'owned-account-import', route.name);
  }
  assert.ok(router.getRoutes().filter((route) => route.meta.requiresAccounts).length > 15);
  for (const name of ['settings', 'service-help', 'task-6', 'analysis', 'patterns']) {
    await router.push({ name });
    assert.equal(router.currentRoute.value.name, name);
  }
  accounts = [owned()];
  await store.loadOwnedAccounts(true);
  await router.push('/login');
  assert.equal(router.currentRoute.value.name, 'home');
  await router.push('/transfer/source');
  assert.equal(router.currentRoute.value.name, 'transfer-source');
});

test('새로고침: 계좌가 있어도 소실된 송금은 출금계좌 확인부터, 조회 실패는 가져오기 복구로 안내한다', async () => {
  const store = storeFor();
  store.applyCurrentUser(user());
  globalThis.fetch = async () => response([owned()]);
  const router = await actualRouter(store);
  await router.push('/transfer/pin');
  assert.equal(router.currentRoute.value.name, 'transfer-source');
  assert.match(store.transferError, /처음부터/);
  store.clearSession();
  store.applyCurrentUser(user());
  globalThis.fetch = async () => response({}, 500);
  await router.push('/tasks/balance');
  assert.equal(router.currentRoute.value.name, 'owned-account-import');
  assert.equal(store.ownedAccountsLoaded, false);
  await router.push('/settings/help');
  assert.equal(router.currentRoute.value.name, 'service-help');
});

test('금융 패턴은 계좌 없을 때 실행 API 전에 가져오기로 보내고 고객센터는 유지한다', async () => {
  const store = storeFor();
  let destination;
  bindRouter({ push: (to) => { destination = to; } });
  const requests = [];
  globalThis.fetch = async (url) => { requests.push(url); return response([]); };
  assert.equal(await store.startPatternExecution({ patternId: 2, patternType: 'BALANCE_CHECK' }), false);
  assert.equal(destination.name, 'owned-account-import');
  assert.deepEqual(requests, ['/api/accounts']);
});

test('조회/송금 화면은 2^53 초과 계좌 ID를 그대로 선택하며 기존 숫자 fixture도 선택 가능하다', async () => {
  for (const id of [otherId, 7]) {
    const store = storeFor();
    store.ownedAccounts = [owned(), owned(id, false)];
    store.ownedAccountsLoaded = true;
    store.financeLoaded = true;
    store.selectedInquiryAccountId = largeId;
    const urls = [];
    globalThis.fetch = async (url) => {
      urls.push(url);
      if (url.endsWith('/balance')) return response({ accountId: id, balance: 35791 });
      if (url.endsWith('/transactions')) return response([]);
      throw new Error(url);
    };
    const root = await mount(TaskView, store, { taskName: 'task-4' });
    const select = flatten(root).find((n) => n.type === 'select');
    await select.props.onChange({ target: { value: String(id) } });
    assert.equal(store.selectedInquiryAccountId, id);
    assert.ok(urls.includes(`/api/accounts/${id}/balance`));
    const transferRoot = await mount(TransferView, store, { flowStep: 'transfer-source' });
    const button = flatten(transferRoot).find((n) => n.type === 'button' && content(n).includes('생활비') && n.props['aria-pressed'] === false);
    // 직접 계좌를 고르는 클릭은 기본값 저장 요청을 만들지 않는다.
    button.props.onClick();
    assert.ok(store.selectedSourceAccount);
    assert.equal(store.defaultOwnedAccount.accountId, largeId);
  }
});

test('복수 받는 계좌를 모두 표시하고 이름·관계 수정은 계좌 필드를 전송하지 않는다', async () => {
  const store = storeFor();
  const person = { registeredPersonId: '19', name: '민수', relationship: '아들', accounts: [
    { accountId: 3, bankCode: '004', bankName: '국민은행', maskedAccountNumber: '123-**-***456', accountAlias: '생활비' },
    { accountId: 4, bankCode: '088', bankName: '신한은행', maskedAccountNumber: '222-**-***789', accountAlias: '저축' },
  ] };
  let body;
  let saved = false;
  globalThis.fetch = async (url, options) => {
    if (url === '/api/accounts') return response([owned()]);
    if (url === '/api/registered-persons') return response([person]);
    if (url === '/api/registered-persons/19') { body = JSON.parse(options.body); return response(person); }
    throw new Error(url);
  };
  await store.loadFinancialData();
  assert.equal(store.people[0].accounts, 2);
  assert.equal(store.accountsByPerson['19'][1].masked, '222-**-***789');
  store.selectPerson('19');
  assert.equal(store.selectedRecipientAccountId, null);
  const root = await mount(PersonForm, store, { existingPerson: store.people[0], onSaved: () => { saved = true; } });
  assert.match(content(root), /기존 받는 계좌의 추가·수정은 아직 준비 중/);
  assert.equal(flatten(root).some((n) => n.props.id === 'registered-person-account'), false);
  await flatten(root).find((n) => n.type === 'form').props.onSubmit({ preventDefault() {} });
  await settle();
  assert.deepEqual(body, { name: '민수', relationship: '아들' });
  assert.equal(saved, true);
  assert.equal(store.accountsByPerson['19'].length, 2);
});

test('후보 새로 조회에서 준비 불가로 바뀐 선택은 설명 후 해제하여 다시 선택할 수 있다', async () => {
  const store = storeFor();
  store.selectedImportAccountIds = [largeId, otherId];
  globalThis.fetch = async () => response([candidate(largeId, false), candidate(otherId)]);
  assert.equal(await store.loadImportCandidates(), true);
  assert.deepEqual(store.selectedImportAccountIds, [otherId]);
  assert.match(store.accountSaveError, /준비 상태를 확인/);
});

for (const outcome of ['success', 'refresh-failed', 'not-primary']) {
  test(`기본 저장 경합: 화면 이동 조회 실패 뒤 PUT 성공은 전체 재조회 확인을 기다린다 (${outcome})`, async () => {
    const store = storeFor();
    const reads = [];
    let finishSave;
    globalThis.fetch = async (url) => {
      if (url === '/api/accounts') return new Promise((resolve) => reads.push(resolve));
      if (url === '/api/accounts/default') return new Promise((resolve) => { finishSave = resolve; });
      throw new Error(url);
    };
    const initialLoad = store.loadOwnedAccounts();
    reads[0](response([owned(), owned(otherId, false)]));
    assert.equal(await initialLoad, true);
    store.defaultAccountSelection = otherId;
    let settled = false;
    const save = store.saveDefaultAccount().then((result) => { settled = true; return result; });

    // 저장 중 가져오기 화면의 재조회가 실패해 로컬 계좌 목록이 비워진다.
    const navigationLoad = store.loadOwnedAccounts(true);
    reads[1](response({}, 500));
    assert.equal(await navigationLoad, false);
    assert.deepEqual(store.ownedAccounts, []);
    assert.equal(store.ownedAccountsLoaded, false);
    finishSave(response(owned(otherId, true)));
    await settle();

    assert.equal(settled, false, 'PUT 성공만으로 저장 완료를 알리지 않는다');
    assert.equal(store.accountSaving, true);
    assert.equal(store.ownedAccountsLoaded, false);
    assert.equal(reads.length, 3, 'PUT 이후 별도의 전체 계좌 조회가 필요하다');
    assert.equal(store.defaultAccountSelection, otherId);
    assert.equal(await store.saveDefaultAccount(), false, '재조회 중에도 중복 저장을 막는다');
    reads[2](outcome === 'refresh-failed'
      ? response({}, 500)
      : response([owned(largeId, outcome !== 'success'), owned(otherId, outcome === 'success', 81000)]));
    assert.equal(await save, outcome === 'success');
    assert.equal(store.accountSaving, false);
    assert.equal(store.defaultAccountSelection, otherId);
    if (outcome === 'success') {
      assert.equal(store.ownedAccountsLoaded, true);
      assert.equal(store.ownedAccounts.length, 2);
      assert.equal(store.defaultOwnedAccount.accountId, otherId);
      assert.equal(store.defaultOwnedAccount.balance, 81000);
      assert.equal(store.accountSaveError, '');
    } else {
      assert.match(store.accountSaveError, /기본 계좌를 저장하지 못했어요/);
      if (outcome === 'refresh-failed') {
        assert.equal(store.ownedAccountsLoaded, false);
        assert.deepEqual(store.ownedAccounts, []);
        assert.match(store.ownedAccountsError, /내 계좌를 불러오지 못했어요/);
      }
    }
  });
}
