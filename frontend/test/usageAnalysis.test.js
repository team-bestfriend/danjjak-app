import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import test from 'node:test';
import { parse, compileScript } from '@vue/compiler-sfc';
import { createRenderer, nextTick } from 'vue';
import { createPinia } from 'pinia';
import { usageAnalysisApi } from '../src/api/usageAnalysisApi.js';
import { bindRouter } from '../src/router/navigation.js';

// 브라우저 의존성 없이 실제 화면의 렌더링과 버튼 동작을 검증한다.
const source = await readFile(new URL('../src/views/AnalysisView.vue', import.meta.url), 'utf8');
const compiled = compileScript(parse(source).descriptor, { id: 'analysis-test', inlineTemplate: true });
const code = ("import { h } from 'vue';\n" + compiled.content).replace(/import (\w+) from '([^']+\.vue)';/g,
  (_, name) => "const " + name + " = { setup: (_, { slots }) => () => h('div', null, slots.default?.()) };")
  .replace(/from (['"])([^'"]+)\1/g, (_, quote, path) => {
    const resolved = path.startsWith('.') ? new URL('../src/views/' + path + (path.endsWith('.js') ? '' : '.js'), import.meta.url).href : import.meta.resolve(path);
    return 'from ' + JSON.stringify(resolved);
  });
const { default: AnalysisView } = await import('data:text/javascript;base64,' + Buffer.from(code).toString('base64'));
const node = (type, text = '') => ({ type, text, children: [], props: {}, parent: null });
const renderer = createRenderer({
  createElement: (type) => node(type),
  createText: (text) => node('text', text),
  createComment: () => node('comment'),
  setText: (target, text) => { target.text = text; },
  setElementText: (target, text) => { target.text = text; target.children = []; },
  patchProp: (target, key, previous, value) => { target.props[key] = value; },
  insert(child, parent, anchor = null) {
    if (child.parent) this.remove(child);
    const index = anchor ? parent.children.indexOf(anchor) : -1;
    parent.children.splice(index < 0 ? parent.children.length : index, 0, child);
    child.parent = parent;
  },
  remove(child) {
    child.parent?.children.splice(child.parent.children.indexOf(child), 1);
    child.parent = null;
  },
  parentNode: (target) => target.parent,
  nextSibling: (target) => target.parent?.children[target.parent.children.indexOf(target) + 1],
});
const flatten = (target) => [target, ...target.children.flatMap(flatten)];
const content = (root) => flatten(root).map((item) => item.text).join(' ');
const originalFetch = globalThis.fetch;
let app;
test.afterEach(() => { app?.unmount(); globalThis.fetch = originalFetch; bindRouter(null); });
const response = (body, status = 200) => new Response(JSON.stringify(body), { status });
const empty = (status) => ({ status, from: '2026-09-01', to: '2026-09-07', patterns: [], steps: [], difficultStep: null });
async function settle() {
  await new Promise((resolve) => setImmediate(resolve));
  await nextTick();
}
async function mount() {
  const root = node('root');
  app = renderer.createApp(AnalysisView);
  app.use(createPinia());
  app.mount(root);
  await settle();
  return root;
}

test('API는 기간과 세션 쿠키를 전달하고 응답을 보존한다', async () => {
  const body = empty('NO_DATA');
  globalThis.fetch = async (url, options) => {
    assert.equal(url, '/api/usage-analysis?from=2026-09-01&to=2026-09-07');
    assert.equal(options.credentials, 'include');
    return response(body);
  };
  assert.deepEqual(await usageAnalysisApi.getUsageAnalysis(body.from, body.to), body);
});

for (const [status, message, button, destination] of [
  ['CONSENT_REQUIRED', '아직 동의 선택이 완료되지 않았어요', '동의 선택하러 가기', 'consent'],
  ['CONSENT_DECLINED', '이용 기록을 수집하지 않고 있어요', '설정으로 가기', 'settings'],
  ['NO_DATA', '이 기간에는 이용 기록이 없어요', '홈으로 가기', 'home'],
]) {
  test(status + ' 안내와 이동을 제공하고 분석 수치를 숨긴다', async () => {
    globalThis.fetch = async () => response(empty(status));
    let target;
    bindRouter({ push: (value) => { target = value; } });
    const root = await mount();
    assert.ok(content(root).includes(message));
    assert.ok(!content(root).includes('금융 업무 실행'));
    // 공통 버튼을 스텁으로 렌더링해도 화면에서 전달한 클릭 핸들러는 유지된다.
    const action = flatten(root).find((item) => item.props.onClick && content(item).includes(button));
    action.props.onClick();
    assert.equal(target.name, destination);
    if (status === 'CONSENT_REQUIRED') assert.deepEqual(target.query, { edit: '1' });
  });
}

for (const duration of [null, 0, 12.5]) {
  test('서버의 어려운 단계와 평균 시간 ' + duration + '을 그대로 표시한다', async () => {
    const selected = { patternId: 71, stepId: 8, stepCode: 'AMOUNT', stepName: '금액 입력', stepOrder: 2, visitCount: 3, errorScore: 2, averageDurationSeconds: duration };
    globalThis.fetch = async () => response({
      ...empty('AVAILABLE'),
      patterns: [{ patternId: 71, title: '시험 송금', completedCount: 4 }, { patternId: 72, title: '시험 잔액', completedCount: 0 }],
      steps: [{ ...selected, stepName: '클라이언트가 선택하면 안 되는 단계', errorScore: 999 }],
      difficultStep: selected,
    });
    const text = content(await mount());
    for (const expected of ['2026-09-01 ~ 2026-09-07', '시험 송금', '4회', '시험 잔액', '0회', '금액 입력', '3회', '2점', duration === null ? '측정 기록 없음' : duration + '초']) {
      assert.ok(text.includes(expected), expected);
    }
    assert.ok(!text.includes('클라이언트가 선택하면 안 되는 단계'));
    assert.match(text, /금융 업무 실행\s+4\s+회/);
  });
}

test('완료 횟수가 모두 0이고 어려운 단계가 null이어도 결과를 대체하지 않는다', async () => {
  globalThis.fetch = async () => response({ ...empty('AVAILABLE'), patterns: [{ patternId: 1, title: '취소된 업무', completedCount: 0 }] });
  const root = await mount();
  assert.ok(content(root).includes('분석할 단계 방문 기록이 없어'));
  assert.ok(content(root).includes('0회'));
  assert.ok(!JSON.stringify(flatten(root).map((item) => item.props.style)).includes('NaN'));
});

test('로딩, 서버 실패, 재시도 중 중복 요청 방지 및 성공 복구', async () => {
  let finish;
  let calls = 0;
  globalThis.fetch = () => { calls += 1; return new Promise((resolve) => { finish = resolve; }); };
  const root = await mount();
  assert.ok(content(root).includes('불러오는 중'));
  finish(response({ code: 'SERVER_ERROR' }, 500));
  await settle();
  assert.ok(content(root).includes('불러오지 못했어요'));
  const retry = flatten(root).find((item) => item.props.onClick && content(item).includes('다시 시도하기')).props.onClick;
  retry();
  retry();
  await nextTick();
  assert.equal(calls, 2);
  assert.ok(content(root).includes('불러오는 중'));
  finish(response(empty('NO_DATA')));
  await settle();
  assert.ok(content(root).includes('이 기간에는 이용 기록이 없어요'));
  assert.ok(!content(root).includes('다시 시도하기'));
});
