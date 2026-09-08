<template>
  <section class="space-y-5">
    <template v-if="mode === 'select'">
      <h2 class="text-[26px] font-bold text-[#111827]">{{ title }}</h2>
      <p class="text-[16px] text-[#6B7280]">안내 문구와 음성을 확인해 주세요.</p>
      <button type="button" class="flex min-h-[96px] w-full items-center gap-4 rounded-[20px] border-2 border-[#FFBC00] bg-white p-5 text-left" @click="mode = 'tts'">
        <span class="text-[30px]" aria-hidden="true">🤖</span>
        <span><strong class="block text-[20px]">TTS 자동 음성</strong><span class="text-[15px] text-[#6B7280]">안내 문구를 편집하고 미리 들어요.</span></span>
      </button>
      <button type="button" class="flex min-h-[96px] w-full items-center gap-4 rounded-[20px] border border-[#E5E7EB] bg-white p-5 text-left" @click="mode = 'family'">
        <span class="text-[30px]" aria-hidden="true">🎙️</span>
        <span><strong class="block text-[20px]">가족 음성 녹음</strong><span class="text-[15px] text-[#6B7280]">녹음은 준비 중 · 읽을 문구를 편집할 수 있어요.</span></span>
      </button>
      <Btn variant="secondary" @click="cancel">돌아가기</Btn>
    </template>

    <template v-else>
      <div class="flex items-center justify-between gap-3">
        <h2 class="text-[25px] font-bold text-[#111827]">{{ mode === 'tts' ? 'TTS 자동 음성' : '가족 음성 녹음' }}</h2>
        <button type="button" :disabled="saving" class="min-h-12 min-w-16 rounded-[12px] bg-[#FFF3CC] px-3 text-[17px] font-bold disabled:opacity-50" @click="editing = !editing">
          {{ editing ? '완료' : '편집' }}
        </button>
      </div>
      <p class="text-[16px] font-semibold text-[#6B7280]">{{ title }}</p>
      <div v-if="editing" class="space-y-2">
        <label class="block text-[16px] font-bold" :for="inputId">안내 문구</label>
        <textarea :id="inputId" v-model="draft" :disabled="saving" maxlength="500" rows="4" class="w-full resize-none rounded-[18px] border-2 border-[#FFBC00] bg-[#FFFBEB] p-4 text-[18px] leading-relaxed outline-none" :aria-invalid="!valid" />
        <p class="text-right text-[13px] text-[#6B7280]">{{ draft.length }} / 500자</p>
        <button type="button" :disabled="saving || !defaultText" class="min-h-12 w-full text-[15px] text-[#6B7280] disabled:opacity-40" @click="draft = defaultText">초기화 (기본 문구로 되돌리기)</button>
      </div>
      <p v-if="!valid" class="text-[#B91C1C]" role="alert">안내 문구를 1~500자로 입력해 주세요.</p>
      <div class="space-y-4 rounded-[22px] border-2 border-[#FFBC00] bg-white p-5">
        <p class="text-[15px] font-bold text-[#92650A]">{{ mode === 'tts' ? '재생될 문구' : '가족이 읽을 문구' }}</p>
        <p class="whitespace-pre-wrap break-words text-[18px] leading-relaxed text-[#111827]">“{{ draft }}”</p>
        <button type="button" :disabled="!valid || saving || loading" class="flex min-h-12 items-center gap-3 text-[17px] font-bold text-[#2563EB] disabled:opacity-40" @click="preview">
          <span class="flex h-12 w-12 items-center justify-center rounded-full bg-[#DBEAFE]" aria-hidden="true">{{ playing ? '⏸' : '▶' }}</span>
          {{ loading ? '음성을 불러오는 중…' : playing ? '일시 정지' : mode === 'family' ? '대본을 TTS로 미리 듣기' : '미리 듣기' }}
        </button>
        <p v-if="error" class="text-[15px] text-[#B91C1C]" role="alert">{{ error }} 문구는 계속 편집하고 저장할 수 있어요.</p>
      </div>
      <template v-if="mode === 'family'">
        <p class="text-[15px] text-[#6B7280]" role="status">가족 음성 녹음은 준비 중이에요. 읽을 문구를 먼저 저장할 수 있어요.</p>
        <Btn variant="secondary" disabled>녹음 시작 (준비 중)</Btn>
      </template>
      <p v-if="hasRecording && dirty" class="rounded-[16px] bg-[#FFF3CC] p-4 text-[15px] text-[#92650A]">문구를 바꿔도 기존 가족 녹음은 바뀌지 않아요. 새 문구와 녹음 내용이 다를 수 있어요.</p>
      <p class="text-[14px] text-[#6B7280]">현재는 안내 문구를 저장해요. 실행할 때의 음성 방식은 전체 설정을 따라요.</p>
      <p v-if="dirty" class="text-[14px] text-[#92650A]" role="status">아직 저장하지 않은 문구예요.</p>
      <Btn :disabled="!valid || saving" @click="confirm">{{ saving ? '저장 중…' : actionLabel }}</Btn>
      <Btn variant="secondary" :disabled="saving" @click="changeMode">방식 변경</Btn>
      <Btn variant="secondary" :disabled="saving" @click="cancel">취소</Btn>
    </template>
  </section>
</template>

<script setup>
import { computed, ref, useId, watch } from 'vue';
import Btn from './Btn.vue';
import { useTtsAudio } from '../../composables/useTtsAudio.js';

const props = defineProps({
  title: { type: String, required: true },
  text: { type: String, default: '' },
  defaultText: { type: String, default: '' },
  speed: { type: String, default: 'NORMAL' },
  saving: Boolean,
  hasRecording: Boolean,
  actionLabel: { type: String, default: '안내 문구 저장' },
});
const emit = defineEmits(['confirm', 'cancel', 'dirty']);
const inputId = useId();
const mode = ref('select');
const editing = ref(false);
const draft = ref(props.text);
const dirty = computed(() => draft.value !== props.text);
const valid = computed(() => draft.value.trim().length > 0 && draft.value.length <= 500);
const previewEnabled = ref(false);
// 입력할 때마다 TTS를 생성하지 않고 미리듣기를 누른 문구만 요청한다.
const { playing, loading, error, toggle, cleanup } = useTtsAudio(
  () => draft.value.trim(),
  { speed: () => props.speed, autoplay: true, enabled: previewEnabled },
);
watch(dirty, (value) => emit('dirty', value));
watch(draft, () => { previewEnabled.value = false; cleanup(); });

function preview() {
  if (!valid.value) return;
  if (!previewEnabled.value) {
    previewEnabled.value = true;
    // enabled 감시자가 최초 요청을 시작하므로 같은 클릭에서 중복 요청하지 않는다.
    return;
  }
  void toggle();
}

function changeMode() {
  cleanup();
  previewEnabled.value = false;
  mode.value = 'select';
  editing.value = false;
}

function confirm() {
  if (!valid.value || props.saving) return;
  cleanup();
  previewEnabled.value = false;
  emit('confirm', draft.value.trim());
}

function cancel() {
  if (props.saving) return;
  if (dirty.value && !window.confirm('저장하지 않은 문구를 버리고 돌아갈까요?')) return;
  cleanup();
  emit('dirty', false);
  emit('cancel');
}
</script>
