import { onMounted, onUnmounted, ref, toValue, watch } from 'vue';
import { createSpeech } from '../api/ttsApi.js';

export function useTtsAudio(
  text,
  { speed = 'NORMAL', autoplay = true, enabled = true } = {},
) {
  const playing = ref(false);
  const loading = ref(false);
  const error = ref('');
  const progress = ref(0);
  let audio = null;
  let audioUrl = null;
  let requestController = null;

  function handlePlay() {
    playing.value = true;
  }

  function handlePause() {
    playing.value = false;
  }

  function handleEnded() {
    playing.value = false;
    progress.value = 100;
  }

  function handleTimeUpdate() {
    if (!audio?.duration) return;
    progress.value = Math.min((audio.currentTime / audio.duration) * 100, 100);
  }

  function handleAudioError() {
    playing.value = false;
    error.value = '음성 안내를 재생하지 못했습니다.';
  }

  function releaseAudio() {
    if (audio) {
      audio.pause();
      audio.removeEventListener('play', handlePlay);
      audio.removeEventListener('pause', handlePause);
      audio.removeEventListener('ended', handleEnded);
      audio.removeEventListener('timeupdate', handleTimeUpdate);
      audio.removeEventListener('error', handleAudioError);
      audio = null;
    }
    if (audioUrl) {
      URL.revokeObjectURL(audioUrl);
      audioUrl = null;
    }
    playing.value = false;
    progress.value = 0;
  }

  function cancelRequest() {
    requestController?.abort();
    requestController = null;
  }

  function cleanup() {
    cancelRequest();
    releaseAudio();
    loading.value = false;
  }

  async function load(shouldAutoplay = toValue(autoplay)) {
    cleanup();
    if (!toValue(enabled)) return;
    loading.value = true;
    error.value = '';
    const controller = new AbortController();
    requestController = controller;

    try {
      const blob = await createSpeech(toValue(text), toValue(speed), controller.signal);
      if (controller.signal.aborted) return;

      audioUrl = URL.createObjectURL(blob);
      audio = new Audio(audioUrl);
      audio.addEventListener('play', handlePlay);
      audio.addEventListener('pause', handlePause);
      audio.addEventListener('ended', handleEnded);
      audio.addEventListener('timeupdate', handleTimeUpdate);
      audio.addEventListener('error', handleAudioError);

      if (shouldAutoplay) {
        try {
          await audio.play();
        } catch (playError) {
          // 자동재생 차단은 사용자가 재생 버튼으로 이어갈 수 있는 정상 fallback이다.
          if (!controller.signal.aborted && playError?.name !== 'NotAllowedError') handleAudioError();
        }
      }
    } catch (requestError) {
      if (!controller.signal.aborted && requestError?.name !== 'AbortError') {
        error.value = '음성 안내를 불러오지 못했습니다.';
      }
    } finally {
      if (requestController === controller) {
        requestController = null;
        loading.value = false;
      }
    }
  }

  async function toggle() {
    if (!audio) {
      await load(true);
      return;
    }
    if (audio.paused) {
      if (audio.ended) audio.currentTime = 0;
      error.value = '';
      try {
        await audio.play();
      } catch {
        handleAudioError();
      }
    } else {
      audio.pause();
    }
  }

  async function replay() {
    if (!audio) {
      await load(true);
      return;
    }
    audio.currentTime = 0;
    error.value = '';
    try {
      await audio.play();
    } catch {
      handleAudioError();
    }
  }

  watch(
    [() => toValue(text), () => toValue(speed), () => toValue(enabled)],
    () => {
      if (toValue(enabled)) void load();
      else cleanup();
    },
  );
  onMounted(() => {
    if (toValue(enabled)) void load();
  });
  onUnmounted(cleanup);

  return { playing, loading, error, progress, toggle, replay, cleanup };
}
