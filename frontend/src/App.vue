<template>
  <div
    class="fixed inset-0 flex items-start justify-center overflow-hidden bg-[#E5E7EB]"
  >
    <div
      class="relative flex flex-col flex-shrink-0 overflow-hidden bg-white"
      :style="appShellStyle"
    >
      <SplashScreen v-if="showSplash" @finished="showSplash = false" />

      <div
        ref="routeArea"
        class="min-h-0 flex-1 overflow-hidden"
        @click.capture="handleGuidanceClick"
      >
        <RouterView />
      </div>

      <VoiceGuideBar
        v-if="voiceText"
        :key="`${route.name}:${activeStep?.stepId ?? ''}`"
        :text="voiceText"
        :speed="voiceSpeed"
        :voice-mode="voiceMode"
        :family-audio-url="activeStep?.voiceFilePath ?? ''"
        :notice="guidanceNotice"
        :guided="Boolean(activeStep)"
      />

      <Toast
        v-if="store.toast"
        :key="store.toast.key"
        :message="store.toast.msg"
        :action="store.toast.action"
        :on-action="store.toast.cb"
        @done="store.clearToast"
      />
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from "vue";
import { RouterView, useRoute, useRouter } from "vue-router";
import Toast from "./components/common/Toast.vue";
import VoiceGuideBar from "./components/common/VoiceGuideBar.vue";
import { useAppStore } from "./stores/appStore";
import {
  stepInstruction,
  useStepGuidance,
} from "./composables/useStepGuidance.js";
import SplashScreen from "./components/common/SplashScreen.vue";

const route = useRoute();
const router = useRouter();
const store = useAppStore();
const routeArea = ref(null);
const showSplash = ref(true);

const VOICE_TEXTS = {
  "transfer-source": "송금할 본인 계좌를 선택해 주세요.",
  "direct-transfer":
    "누구에게 보내실지 선택해 주세요. 등록된 가족에게 보내기를 눌러보세요.",
  "direct-newaccount":
    "보낼 계좌 정보를 입력해 주세요. 은행을 먼저 선택하고 계좌 번호를 입력하세요.",
  "guide-person": "보낼 사람 이름을 눌러 주세요.",
  "guide-account": "보낼 계좌를 눌러 선택해 주세요.",
  "amount-input": "보내실 금액을 입력해 주세요.",
  "pin-entry": "비밀번호를 입력해 주세요.",
  "fraud-warning": "서버가 확인한 이상 거래 사유를 천천히 살펴보세요.",
  "final-confirm": "출금 계좌와 받는 분, 금액이 맞는지 확인해 주세요.",
  complete: "송금이 모두 완료됐어요. 정말 잘 하셨어요!",
  cancelled: "송금을 취소했어요. 잔액과 거래 내역은 바뀌지 않았어요.",
  "task-transfer":
    "아들 김민수님에게 송금하는 업무입니다. 시작하려면 시작하기를 눌러주세요.",
  "task-2": "이번 달 연금 입금 내역이에요. 천천히 확인해 보세요.",
  "task-3": "이번 달 관리비예요. 납부하려면 관리비 납부하기를 눌러 주세요.",
  "task-4": "내 계좌 잔액을 확인하는 화면이에요. 잔액 보기를 눌러 확인하세요.",
  "task-5": "거래 내역 화면이에요. 입금, 출금을 선택해서 확인할 수 있어요.",
  "task-6": "고객센터 화면이에요. 도움이 필요하면 전화 연결하기를 눌러 주세요.",
  "task-8": "공과금 내역이에요. 전기, 수도, 가스 요금을 확인해 보세요.",
  "task-9": "자동이체 내역이에요. 매달 나가는 금액을 확인해 보세요.",
  "task-10": "카드 이용 내역이에요. 이번 달 쓴 금액을 확인해 보세요.",
  "task-11": "예금 만기 일정이에요. 만기일을 꼭 확인해 두세요.",
  "task-12": "오늘의 환율 정보예요. 천천히 살펴보세요.",
  "pension-history": "연금 입금 내역이에요. 매달 들어온 금액을 확인해 보세요.",
};

const activeStep = computed(
  () =>
    store.activePatternDetail?.steps?.find(
      (step) => step.screenCode === String(route.name),
    ) ?? null,
);
const voiceText = computed(() =>
  activeStep.value
    ? stepInstruction(activeStep.value)
    : (VOICE_TEXTS[String(route.name)] ?? null),
);
const guidanceReady = computed(
  () => !store.financeLoading && !store.inquiryLoading && !store.supportLoading,
);
const { notice: guidanceNotice, handleClick: handleGuidanceClick } =
  useStepGuidance(routeArea, activeStep, guidanceReady, () => {
    if (store.currentStepVisit?.stepId === activeStep.value?.stepId)
      store.recordPatternAction("wrongTouch");
  });
const voiceSpeed = computed(
  () => store.currentUser?.settings?.voiceSpeed ?? "NORMAL",
);
const voiceMode = computed(
  () => store.currentUser?.settings?.guideVoiceType ?? "TTS",
);
const uiScale = computed(
  () =>
    ({ SMALL: 0.94, NORMAL: 1, LARGE: 1.08 })[
      store.currentUser?.settings?.fontSize ?? "NORMAL"
    ] ?? 1,
);
const appShellStyle = computed(() => ({
  width: `calc(100vw / ${uiScale.value})`,
  maxWidth: `${390 / uiScale.value}px`,
  height: `${100 / uiScale.value}%`,
  transform: `scale(${uiScale.value})`,
  transformOrigin: "top center",
}));

watch(
  () => route.name,
  (routeName) => {
    void store.syncPatternStep(String(routeName));
  },
  { immediate: true },
);

watch(
  () => store.currentUser?.settings?.fontSize,
  (fontSize) => {
    if (typeof document === "undefined") return;
    document.documentElement.dataset.fontSize = String(
      fontSize ?? "NORMAL",
    ).toLowerCase();
  },
  { immediate: true },
);

function handleSessionExpired() {
  store.clearSession("로그인이 만료되었습니다. 다시 로그인해 주세요.");
  if (route.name !== "login") void router.replace({ name: "login" });
}

onMounted(() =>
  window.addEventListener("danjjak:session-expired", handleSessionExpired),
);
onBeforeUnmount(() =>
  window.removeEventListener("danjjak:session-expired", handleSessionExpired),
);
</script>

<style>
.vbar-enter-active {
  transition:
    opacity 0.3s ease,
    transform 0.3s ease;
}
.vbar-leave-active {
  transition:
    opacity 0.2s ease,
    transform 0.2s ease;
}
.vbar-enter-from {
  opacity: 0;
  transform: translateY(16px);
}
.vbar-leave-to {
  opacity: 0;
  transform: translateY(16px);
}
</style>
