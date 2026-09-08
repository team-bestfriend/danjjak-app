<template>
  <div
    class="z-20 max-h-[40%] flex-shrink-0 overflow-y-auto bg-[#FAFAF8] px-4 py-3"
    aria-label="현재 단계 안내"
  >
    <div
      class="overflow-hidden rounded-[22px] bg-white"
      style="box-shadow: 0 -2px 24px rgba(0,0,0,0.08), 0 4px 16px rgba(0,0,0,0.07); border: 1px solid #EEEEED;"
    >
      <p v-if="guided" class="px-4 pt-3 text-[14px] font-bold text-[#92650A]">지금 할 일</p>
      <div class="flex items-start gap-3 px-4 pb-2 pt-3">
        <button
          type="button"
          class="flex h-12 w-12 flex-shrink-0 items-center justify-center rounded-full transition-transform active:scale-95 disabled:opacity-50"
          style="background: #FFBC00;"
          :disabled="loading"
          :aria-label="playing ? '음성 안내 일시 정지' : '음성 안내 재생'"
          @click="togglePlay"
        >
          <span style="font-size: 17px; line-height: 1; color: #111827;">{{ loading ? '…' : playing ? '⏸' : '▶' }}</span>
        </button>
        <p
          id="step-guidance-caption"
          role="status"
          aria-live="polite"
          aria-atomic="true"
          class="flex-1 font-bold leading-snug text-[#111827]"
          style="font-size: 19px; word-break: keep-all; overflow-wrap: anywhere;"
        >“{{ text }}”</p>
      </div>

      <div class="flex items-center gap-3 px-4 pb-3">
        <div class="flex h-[26px] flex-1 items-end gap-[2.5px]" aria-hidden="true">
          <div
            v-for="(height, index) in WAVE"
            :key="index"
            class="flex-1 rounded-full"
            :style="{
              height: playing ? `${height}px` : '3px',
              background: playing ? '#FFBC00' : '#E5E7EB',
              transformOrigin: 'bottom center',
              animation: playing ? `voice-wave 0.75s ease-in-out ${(index * 0.048).toFixed(3)}s infinite alternate` : 'none',
              transition: 'background 0.4s, height 0.3s',
            }"
          />
        </div>
        <button
          type="button"
          class="flex min-h-12 flex-shrink-0 items-center gap-1 rounded-full px-3 font-semibold disabled:opacity-50"
          style="font-size: 13px; color: #92650A; background: #FFF3CC; border: 1px solid #FFBC00;"
          :disabled="loading"
          @click="replay"
        >
          <span aria-hidden="true" style="font-size: 15px;">↺</span> 다시 듣기
        </button>
      </div>

      <p v-if="fallbackNotice" class="px-4 pb-3 text-[13px] text-[#6B7280]" role="status">
        {{ fallbackNotice }}
      </p>
      <p v-if="errorText" class="px-4 pb-3 text-[13px] text-[#B91C1C]" role="alert">
        {{ errorText }} 화면 안내와 금융 기능은 계속 사용할 수 있어요.
      </p>
      <p v-if="notice" class="px-4 pb-3 text-[15px] text-[#92650A]" role="status">{{ notice }}</p>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref, watch } from 'vue';
import { useTtsAudio } from '../../composables/useTtsAudio.js';

const props = defineProps({
  text: { type: String, required: true },
  speed: { type: String, default: 'NORMAL' },
  voiceMode: { type: String, default: 'TTS' },
  familyAudioUrl: { type: String, default: '' },
  notice: { type: String, default: '' },
  guided: { type: Boolean, default: false },
});

const WAVE = [6, 12, 22, 8, 18, 26, 10, 5, 23, 26, 14, 19, 26, 5, 16, 24, 9, 21, 12, 26, 7, 18, 26, 10, 22, 14, 25, 15, 8, 20];
const familyFailed = ref(false);
const familyPlaying = ref(false);
const familyLoading = ref(false);
let familyAudio = null;

const shouldUseTts = computed(() => (
  props.voiceMode !== 'FAMILY' || !props.familyAudioUrl || familyFailed.value
));
const tts = useTtsAudio(
  () => props.text,
  {
    speed: () => props.speed,
    autoplay: true,
    enabled: shouldUseTts,
  },
);
const playing = computed(() => shouldUseTts.value ? tts.playing.value : familyPlaying.value);
const loading = computed(() => shouldUseTts.value ? tts.loading.value : familyLoading.value);
const errorText = computed(() => shouldUseTts.value ? tts.error.value : '');
const fallbackNotice = computed(() => {
  if (props.voiceMode !== 'FAMILY') return '';
  if (!props.familyAudioUrl) return '저장된 가족 음성이 없어 자동 음성으로 안내해요.';
  if (familyFailed.value) return '가족 음성을 재생하지 못해 자동 음성으로 안내해요.';
  return '저장된 가족 음성으로 안내해요.';
});

function releaseFamilyAudio() {
  if (!familyAudio) return;
  familyAudio.pause();
  familyAudio.src = '';
  familyAudio = null;
  familyPlaying.value = false;
  familyLoading.value = false;
}

async function loadFamilyAudio(shouldAutoplay = true) {
  releaseFamilyAudio();
  familyFailed.value = false;
  if (props.voiceMode !== 'FAMILY' || !props.familyAudioUrl) return;

  familyLoading.value = true;
  const audio = new Audio(props.familyAudioUrl);
  familyAudio = audio;
  audio.addEventListener('playing', () => {
    familyLoading.value = false;
    familyPlaying.value = true;
  });
  audio.addEventListener('pause', () => { familyPlaying.value = false; });
  audio.addEventListener('ended', () => { familyPlaying.value = false; });
  audio.addEventListener('canplay', () => { familyLoading.value = false; });
  audio.addEventListener('error', () => {
    releaseFamilyAudio();
    familyFailed.value = true;
  });

  if (!shouldAutoplay) return;
  try {
    await audio.play();
  } catch (error) {
    familyLoading.value = false;
    // 브라우저 자동재생 차단은 사용자가 재생 버튼으로 이어갈 수 있다.
    if (error?.name !== 'NotAllowedError') familyFailed.value = true;
  }
}

async function togglePlay() {
  if (shouldUseTts.value) {
    await tts.toggle();
    return;
  }
  if (!familyAudio) {
    await loadFamilyAudio(true);
    return;
  }
  if (familyAudio.paused) await familyAudio.play();
  else familyAudio.pause();
}

async function replay() {
  if (shouldUseTts.value) {
    await tts.replay();
    return;
  }
  if (!familyAudio) {
    await loadFamilyAudio(true);
    return;
  }
  familyAudio.currentTime = 0;
  await familyAudio.play();
}

watch(
  () => [props.text, props.voiceMode, props.familyAudioUrl],
  () => { void loadFamilyAudio(true); },
);
onMounted(() => { void loadFamilyAudio(true); });
onUnmounted(releaseFamilyAudio);
</script>
