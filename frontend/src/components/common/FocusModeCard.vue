<template>
  <div
    class="mx-5 w-full max-w-[340px] overflow-hidden rounded-[28px] bg-white"
    style="box-shadow: 0 8px 40px rgba(0,0,0,0.22);"
    @click.stop
  >
    <div class="flex flex-col items-center justify-center gap-2 px-8 pb-8 pt-10" style="background: #FFBC00;">
      <span class="font-black leading-none text-[#111827]" style="font-size: 76px;">{{ pat.num }}</span>
      <span class="text-center font-black text-[#111827]" style="font-size: 22px; margin-top: 4px;">{{ pat.label }}</span>
      <span v-if="person" class="text-[15px] font-bold" style="color: rgba(0,0,0,0.55);">
        {{ person.emoji }} {{ person.name }} · {{ person.relation }}
      </span>
    </div>

    <div class="space-y-4 p-5">
      <div>
        <p class="mb-2 text-[13px] font-bold uppercase tracking-wide text-[#9CA3AF]">업무 안내 · 자동 TTS</p>
        <div class="flex items-center gap-3">
          <button
            type="button"
            class="flex h-12 w-12 flex-shrink-0 items-center justify-center rounded-full text-[14px] font-bold disabled:opacity-50"
            style="background: #FFF3CC; color: #92650A;"
            :disabled="loading"
            :aria-label="playing ? '업무 안내 일시 정지' : '업무 안내 재생'"
            @click="toggle"
          >
            {{ loading ? '…' : playing ? '⏸' : '▶' }}
          </button>
          <div class="flex-1">
            <p class="mb-1.5 text-[14px] font-bold text-[#111827]">“{{ quote }}”</p>
            <div class="flex h-5 items-end gap-px" aria-hidden="true">
              <div
                v-for="(_, index) in 28"
                :key="index"
                class="flex-1 rounded-sm"
                :style="{
                  height: `${Math.max(3, Math.abs(Math.sin(index * 0.9)) * 12 + 3)}px`,
                  backgroundColor: playing ? '#FFBC00' : '#F3F4F6',
                }"
              />
            </div>
          </div>
        </div>
        <p v-if="familyFallback" class="mt-2 text-[13px] text-[#6B7280]" role="status">
          시작 안내용 가족 음성이 없어 자동 음성으로 안내해요.
        </p>
        <p v-if="error" class="mt-2 text-[13px] text-[#B91C1C]" role="alert">
          {{ error }} 화면의 업무 설명은 계속 확인할 수 있어요.
        </p>
      </div>

      <div class="flex gap-2 pt-1">
        <button
          type="button"
          class="h-14 flex-1 rounded-[14px] border-2 border-[#E5E7EB] font-bold text-[#374151]"
          style="font-size: 17px;"
          @click="$emit('cancel')"
        >취소하기</button>
        <button
          type="button"
          class="h-14 flex-1 rounded-[14px] font-black text-[#111827]"
          style="font-size: 17px; background: #FFBC00;"
          @click="$emit('start')"
        >시작하기</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { useTtsAudio } from '../../composables/useTtsAudio.js';
import { generatePatternDesc } from '../../constants/data.js';
import { useAppStore } from '../../stores/appStore.js';

const props = defineProps({ pat: { type: Object, required: true } });
defineEmits(['cancel', 'start']);
const store = useAppStore();

const person = computed(() => {
  if (!props.pat.personId) return null;
  return store.people.find((item) => item.id === props.pat.personId) ?? {
    name: props.pat.linkedAccount?.registeredPersonName,
    relation: props.pat.linkedAccount?.relationship,
    emoji: props.pat.linkedAccount?.relationship === '아들' ? '👨' : '👩',
  };
});
const quote = computed(() => (
  props.pat.description || generatePatternDesc(props.pat, store.people, store.accountsByPerson)
));
const speed = computed(() => store.currentUser?.settings?.voiceSpeed ?? 'NORMAL');
const familyFallback = computed(() => store.currentUser?.settings?.guideVoiceType === 'FAMILY');
const { playing, loading, error, toggle } = useTtsAudio(quote, { speed, autoplay: true });
</script>
