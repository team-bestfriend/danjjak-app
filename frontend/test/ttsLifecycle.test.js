import assert from 'node:assert/strict';
import test from 'node:test';
import { createRenderer, nextTick, ref } from 'vue';
import { useTtsAudio } from '../src/composables/useTtsAudio.js';

test('단계 변경은 이전 요청·재생을 취소하고 늦은 실패가 새 안내를 덮지 않는다', async (t) => {
  const originalFetch = globalThis.fetch;
  const originalAudio = globalThis.Audio;
  const requests = [];
  const audios = [];
  globalThis.fetch = (url, options) => new Promise((resolve, reject) => requests.push({ options, resolve, reject }));
  globalThis.Audio = class extends EventTarget {
    constructor() { super(); this.paused = true; audios.push(this); }
    async play() { this.paused = false; this.dispatchEvent(new Event('play')); }
    pause() { this.paused = true; this.dispatchEvent(new Event('pause')); }
  };
  const renderer = createRenderer({
    createComment: () => ({}), insert() {}, remove() {}, parentNode: () => null, nextSibling: () => null,
  });
  const instruction = ref('첫 단계 안내');
  let audio;
  const app = renderer.createApp({ setup() { audio = useTtsAudio(instruction); return () => null; } });
  t.after(() => {
    app.unmount();
    globalThis.fetch = originalFetch;
    globalThis.Audio = originalAudio;
  });
  app.mount({});
  assert.equal(requests.length, 1);
  instruction.value = '두 번째 단계 안내';
  await nextTick();
  assert.equal(requests[0].options.signal.aborted, true);
  assert.deepEqual(JSON.parse(requests[1].options.body), { text: '두 번째 단계 안내', speed: 'NORMAL' });
  requests[1].resolve(new Response(new Blob(['audio'], { type: 'audio/mpeg' })));
  await new Promise((resolve) => setImmediate(resolve));
  assert.equal(audio.playing.value, true);
  requests[0].reject(new Error('이전 요청의 늦은 실패'));
  await new Promise((resolve) => setImmediate(resolve));
  assert.equal(audio.error.value, '');
  assert.equal(audio.playing.value, true);
  instruction.value = '세 번째 단계 안내';
  await nextTick();
  assert.equal(audios[0].paused, true);
  assert.equal(audio.playing.value, false);
});
