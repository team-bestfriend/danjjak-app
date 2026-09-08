import assert from 'node:assert/strict';
import test from 'node:test';
import { createSpeech } from '../src/api/ttsApi.js';

test('선택한 안내 속도를 TTS 요청 본문에 전달한다', async () => {
  let requestUrl;
  let requestOptions;
  globalThis.fetch = async (url, options) => {
    requestUrl = url;
    requestOptions = options;
    return new Response(new Blob(['mock-audio'], { type: 'audio/mpeg' }), {
      status: 200,
      headers: { 'Content-Type': 'audio/mpeg' },
    });
  };

  const audio = await createSpeech('천천히 확인해 주세요.', 'SLOW');

  assert.equal(requestUrl, '/api/tts');
  assert.equal(requestOptions.credentials, 'include');
  assert.deepEqual(JSON.parse(requestOptions.body), {
    text: '천천히 확인해 주세요.',
    speed: 'SLOW',
  });
  assert.equal(audio.type, 'audio/mpeg');
});
