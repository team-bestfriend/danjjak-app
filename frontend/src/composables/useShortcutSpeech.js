import { onScopeDispose, ref } from 'vue';
import { matchShortcutCommand } from '../features/voice/shortcutCommands.js';

const errorMessages = {
  'not-allowed': '마이크 사용이 허용되지 않았어요. 브라우저에서 권한을 허용하거나 화면에서 단축번호를 눌러 주세요.',
  'service-not-allowed': '이 브라우저에서 음성 인식을 사용할 수 없어요. 화면에서 단축번호를 눌러 주세요.',
  'audio-capture': '마이크를 찾을 수 없어요. 연결을 확인하거나 화면에서 단축번호를 눌러 주세요.',
  'no-speech': '말소리를 듣지 못했어요. 다시 말하거나 화면에서 단축번호를 눌러 주세요.',
  network: '음성 인식에 연결하지 못했어요. 다시 시도하거나 화면에서 단축번호를 눌러 주세요.',
  'language-not-supported': '한국어 음성 인식을 지원하지 않아요. 화면에서 단축번호를 눌러 주세요.',
};
const noSpeechMessage = errorMessages['no-speech'];

export function useShortcutSpeech(getPatterns, onMatch) {
  const Recognition = globalThis.SpeechRecognition ?? globalThis.webkitSpeechRecognition;
  const supported = typeof Recognition === 'function';
  const state = ref('idle');
  const message = ref(supported
    ? '마이크로 원하는 단축번호를 찾아요. 예: “아들에게 돈 보내 줘”'
    : '이 브라우저는 음성 인식을 지원하지 않아요. 화면에서 단축번호를 눌러 주세요.');
  let recognition = null;
  let timer = null;

  function release() {
    const previous = recognition;
    recognition = null;
    clearTimeout(timer);
    if (!previous) return;
    previous.onstart = previous.onresult = previous.onerror = previous.onend = null;
    previous.onnomatch = previous.onspeechend = previous.onaudioend = null;
    try { previous.abort(); } catch { /* 이미 종료된 인식기는 정리만 한다. */ }
  }

  function finish(text) {
    release();
    state.value = 'idle';
    message.value = text;
  }

  function cancel() {
    finish('음성 인식을 멈췄어요. 다시 말하거나 화면에서 단축번호를 눌러 주세요.');
  }

  function start() {
    if (!supported || recognition) return;
    state.value = 'starting';
    message.value = '마이크 사용을 준비하고 있어요. 권한 요청이 나오면 허용해 주세요.';
    try {
      const current = new Recognition();
      recognition = current;
      current.lang = 'ko-KR';
      current.continuous = false;
      current.interimResults = false;
      current.maxAlternatives = 1;
      current.onstart = () => {
        if (recognition !== current) return;
        state.value = 'listening';
        message.value = '듣고 있어요. 원하는 업무를 말한 뒤 ‘말하기 완료’를 눌러 주세요.';
      };
      current.onresult = (event) => {
        if (recognition !== current) return;
        const results = Array.from(event.results);
        if (!results.length || results.some((result) => !result.isFinal)) return;
        // 인식 문장은 이번 매칭에서만 사용하고 상태·저장소·로그에 남기지 않는다.
        const matches = matchShortcutCommand(results.map((result) => result[0]?.transcript ?? '').join(' '), getPatterns());
        if (matches.length === 1) {
          const pattern = matches[0];
          finish(`${pattern.num}번 ${pattern.label}, 찾으시는 업무가 맞나요? 업무 내용을 확인해 주세요.`);
          onMatch(pattern);
        } else {
          finish(matches.length === 0
            ? '맞는 단축번호를 찾지 못했어요. 지원 문장으로 다시 말하거나 화면에서 골라 주세요.'
            : '같은 업무의 단축번호가 여러 개예요. 화면에서 원하는 번호를 고르거나 다른 지원 문장으로 다시 말해 주세요.');
        }
      };
      current.onerror = (event) => {
        if (recognition !== current) return;
        finish(errorMessages[event.error] ?? '음성을 인식하지 못했어요. 다시 시도하거나 화면에서 단축번호를 눌러 주세요.');
      };
      current.onnomatch = current.onend = () => {
        if (recognition === current) finish(noSpeechMessage);
      };
      current.onspeechend = () => {
        if (recognition === current) stop();
      };
      current.onaudioend = () => {
        if (recognition !== current) return;
        state.value = 'processing';
        message.value = '말씀하신 업무를 확인하고 있어요.';
      };
      timer = setTimeout(() => {
        if (recognition === current) finish('음성 인식 시간이 지났어요. 다시 말하거나 화면에서 단축번호를 눌러 주세요.');
      }, 20000);
      current.start();
    } catch (error) {
      finish(error?.name === 'NotAllowedError'
        ? errorMessages['not-allowed']
        : '마이크를 시작하지 못했어요. 다시 시도하거나 화면에서 단축번호를 눌러 주세요.');
    }
  }

  function stop() {
    if (!recognition || state.value !== 'listening') return;
    state.value = 'processing';
    message.value = '말씀하신 업무를 확인하고 있어요.';
    try { recognition.stop(); } catch { finish(noSpeechMessage); }
  }

  onScopeDispose(release);
  return { supported, state, message, start, stop, cancel };
}
