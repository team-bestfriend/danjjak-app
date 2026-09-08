<template>
  <div class="flex h-full flex-col bg-[#FAFAF8]">
    <SafeArea />
    <TopBar :title="perStep ? '단계별 음성 안내 수정' : '패턴 설명 음성 수정'" :on-back="back" :back-disabled="saving" />
    <main class="flex-1 space-y-5 overflow-y-auto px-5 py-6">
      <p v-if="loading" class="text-[#6B7280]" role="status">안내 정보를 불러오고 있어요…</p>
      <div v-else-if="loadError" class="space-y-4">
        <p class="text-[#B91C1C]" role="alert">{{ loadError }}</p>
        <Btn variant="secondary" @click="initialize">다시 시도</Btn>
        <Btn variant="secondary" @click="store.navigate('patterns', { replace: true })">패턴 목록으로</Btn>
      </div>
      <template v-else-if="detail">
        <template v-if="perStep && !selectedStep">
          <h1 class="text-[25px] font-bold">단계별 음성 안내 설정</h1>
          <p class="text-[16px] text-[#6B7280]">각 단계의 문구와 음성을 확인해 주세요.</p>
          <button v-for="step in orderedSteps" :key="step.stepId" type="button" class="flex min-h-[100px] w-full items-center gap-4 rounded-[20px] border border-[#E5E7EB] bg-white p-4 text-left" @click="openStep(step)">
            <span class="flex h-12 w-12 shrink-0 items-center justify-center rounded-full bg-[#FFBC00] text-[20px] font-bold">{{ step.stepOrder }}</span>
            <span class="min-w-0 flex-1"><strong class="block text-[18px]">{{ step.stepName }}</strong><span class="mt-1 block break-words text-[15px] text-[#6B7280]">“{{ step.instructionText }}”</span><span v-if="step.voiceFilePath" class="mt-1 block text-[13px] text-[#92650A]">저장된 가족 음성 있음</span></span>
            <span aria-hidden="true">›</span>
          </button>
          <p v-if="orderedSteps.length === 0" class="text-[#6B7280]">설정할 단계가 없어요. 패턴 목록에서 내용을 확인해 주세요.</p>
          <Btn variant="secondary" @click="back">패턴 상세로</Btn>
        </template>
        <PatternVoiceEditor
          v-else
          :key="String(detail.patternId) + '-' + (selectedStep?.stepId ?? 'description') + '-' + revision"
          :title="selectedStep?.stepName ?? '패턴 시작 안내 음성 설정'"
          :text="selectedStep?.instructionText ?? detail.description"
          :default-text="defaultText"
          :speed="store.currentUser?.settings?.voiceSpeed"
          :has-recording="Boolean(selectedStep?.voiceFilePath)"
          :saving="saving"
          @dirty="dirty = $event"
          @confirm="save"
          @cancel="dirty = false; back()"
        />
        <p v-if="saveError" class="rounded-[16px] bg-[#FEF2F2] p-4 text-[#B91C1C]" role="alert">{{ saveError }}</p>
      </template>
    </main>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue';
import { onBeforeRouteLeave, onBeforeRouteUpdate, useRoute } from 'vue-router';
import { patternApi } from '../api/patternApi.js';
import { useAppStore } from '../stores/appStore.js';
import PatternVoiceEditor from '../components/common/PatternVoiceEditor.vue';
import Btn from '../components/common/Btn.vue';
import SafeArea from '../components/common/SafeArea.vue';
import TopBar from '../components/common/TopBar.vue';

const props = defineProps({ perStep: Boolean });
const store = useAppStore();
const route = useRoute();
const detail = ref(null);
const template = ref(null);
const loading = ref(false);
const loadError = ref('');
const saveError = ref('');
const saving = ref(false);
const dirty = ref(false);
const revision = ref(0);
let loadSequence = 0;
const orderedSteps = computed(() => [...(detail.value?.steps ?? [])].sort((a, b) => a.stepOrder - b.stepOrder));
const selectedStep = computed(() => props.perStep && route.params.stepOrder
  ? orderedSteps.value.find((step) => step.stepOrder === Number(route.params.stepOrder))
  : null);
const defaultText = computed(() => selectedStep.value
  ? template.value?.steps.find((step) => step.stepCode === selectedStep.value.stepCode)?.instructionText ?? ''
  : template.value?.defaultDescription ?? '');

async function initialize() {
  const sequence = ++loadSequence;
  loading.value = true;
  loadError.value = '';
  saveError.value = '';
  detail.value = null;
  dirty.value = false;
  try {
    const patternId = Number(route.params.patternId);
    if (!Number.isInteger(patternId) || patternId < 1) throw new Error('올바른 패턴을 선택해 주세요.');
    const [pattern, templates] = await Promise.all([patternApi.getPattern(patternId), patternApi.getTemplates()]);
    if (sequence !== loadSequence) return;
    if (props.perStep && route.params.stepOrder && !pattern.steps.some((step) => step.stepOrder === Number(route.params.stepOrder))) {
      throw new Error('선택한 안내 단계를 찾을 수 없어요. 패턴 목록에서 다시 선택해 주세요.');
    }
    detail.value = pattern;
    template.value = templates.find((item) => item.patternType === pattern.patternType) ?? null;
    revision.value++;
  } catch (error) {
    if (sequence === loadSequence) loadError.value = error?.message ?? '안내 정보를 불러오지 못했습니다.';
  } finally {
    if (sequence === loadSequence) loading.value = false;
  }
}

function canLeave() {
  return !saving.value && (!dirty.value || window.confirm('저장하지 않은 문구를 버리고 이동할까요?'));
}

function openStep(step) {
  store.navigate('step-voice-edit', { params: { patternId: detail.value.patternId, stepOrder: step.stepOrder } });
}

function back() {
  if (saving.value) return;
  store.navigate(selectedStep.value ? 'step-voice-list' : 'pattern-detail', {
    params: { patternId: route.params.patternId },
    replace: true,
  });
}

async function save(text) {
  if (saving.value || !detail.value) return;
  saving.value = true;
  saveError.value = '';
  try {
    // 변경한 대상만 보내 다른 단계나 시작 설명을 덮어쓰지 않는다.
    const payload = selectedStep.value
      ? { stepInstructions: [{ stepCode: selectedStep.value.stepCode, instructionText: text }] }
      : { description: text };
    detail.value = await store.updatePattern(detail.value.patternId, payload);
    dirty.value = false;
    revision.value++;
    store.showToast('안내 문구를 저장했어요.');
    saving.value = false;
    back();
  } catch (error) {
    saveError.value = error?.message ?? '문구를 저장하지 못했습니다. 다시 시도해 주세요.';
  } finally {
    saving.value = false;
  }
}

watch(() => [route.params.patternId, route.params.stepOrder], initialize, { immediate: true });
onBeforeRouteLeave(canLeave);
onBeforeRouteUpdate(canLeave);
</script>
