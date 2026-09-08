<template>
  <div ref="containerRef" className="flex flex-col h-full" style="background: #FAFAF8;">
    <SafeArea />

    <!-- Header -->
    <div className="px-5 bg-white border-b border-[#EEEEED] flex-shrink-0" style="padding-top: 18px; padding-bottom: 16px;">
      <div className="flex items-center gap-2 mb-2">
        <DanjjakMark :size="20" />
        <span className="font-semibold" style="color: #B8860B; font-size: 13px; letter-spacing: 0.02em;">단짝</span>
      </div>
      <p className="font-bold text-[#111827] leading-tight" style="font-size: 26px;">안녕하세요, {{ store.userName }}님 👵</p>
      <p className="font-normal text-[#6B7280] mt-1" style="font-size: 15px;">오늘도 안전한 금융 생활 되세요.</p>
    </div>

    <div
      className="flex-1 flex flex-col px-4 overflow-y-auto"
      style="padding-top: 12px; padding-bottom: 10px; gap: 10px;"
      @touchstart="onTouchStart"
      @touchend="onTouchEnd"
    >
      <!-- 직접 송금하기 — Primary CTA -->
      <button
        @click="beginDirectTransfer"
        className="w-full flex items-center justify-center gap-2.5 active:scale-[0.985] transition-transform flex-shrink-0"
        style="height: 64px; border-radius: 18px; background: #FFBC00;"
      >
        <span style="font-size: 18px; color: #111827;">→</span>
        <span style="font-size: 20px; font-weight: 600; color: #111827;">직접 송금하기</span>
      </button>

      <!-- 음성으로 말하기 — Secondary CTA -->
      <div className="flex-shrink-0">
        <button
          @click="toggleStt"
          type="button"
          :disabled="!speechSupported || store.patternLoading || store.patternOrderSaving"
          aria-describedby="voice-command-status"
          :class="['w-full flex items-center justify-center gap-2.5 font-bold transition-all']"
          :style="
            sttState === 'listening'
              ? 'height:56px;border-radius:18px;background:#FEF2F2;border:1px solid #FCA5A5;color:#B91C1C;font-size:17px;font-weight:500;'
              : sttState === 'processing'
              ? 'height:56px;border-radius:18px;background:#FFFBEB;border:1px solid #FFBC00;color:#92650A;font-size:17px;font-weight:500;'
              : 'height:56px;border-radius:18px;background:#FFFFFF;border:1px solid #D1D5DB;color:#374151;font-size:17px;font-weight:500;'"
        >
          <template v-if="sttState === 'listening'">
            <div className="w-3 h-3 rounded-full bg-[#EF4444] animate-pulse" />
            <span>말하기 완료</span>
          </template>
          <template v-else-if="sttState === 'processing'">
            <span className="animate-spin inline-block" aria-hidden="true">⟳</span>
            <span>확인 중 · 취소하기</span>
          </template>
          <template v-else-if="sttState === 'starting'">
            <Ic name="Mic" />
            <span>마이크 준비 중 · 취소하기</span>
          </template>
          <template v-else>
            <Ic name="Mic" />
            <span>{{ speechSupported ? '음성으로 말하기' : '음성 인식 지원 안 됨' }}</span>
          </template>
        </button>

        <p id="voice-command-status" class="mt-2 text-[15px] leading-relaxed text-[#374151]" role="status" aria-live="polite" aria-atomic="true">{{ sttMessage }}</p>
        <button v-if="highlightedPattern" type="button" class="mt-2 min-h-12 w-full rounded-[14px] border-2 border-[#B8860B] bg-[#FFFBEB] px-3 py-2 font-bold text-[#111827]" :disabled="store.patternLoading" @click="handleCardClick(highlightedPattern.num, highlightedPattern)">
          {{ highlightedPattern.num }}번 업무 확인하기
        </button>
        <details class="mt-1 text-[14px] leading-relaxed text-[#6B7280]">
          <summary class="min-h-12 cursor-pointer py-3">지원 문장과 마이크 사용 안내</summary>
          <p>원하는 단축번호를 찾을 때만 마이크를 사용해요. 단짝은 인식한 문장과 음성을 저장하지 않아요. 브라우저의 음성 인식 서비스로 음성이 전송될 수 있어요.</p>
          <ul class="mt-2 list-inside list-disc">
            <li v-for="example in voiceCommandExamples" :key="example">{{ example }}</li>
          </ul>
        </details>
      </div>

      <!-- 내 단축번호 + 그리드 + 페이지 도트 -->
      <div className="flex-1 flex flex-col" style="min-height: 360px; gap: 8px;">
        <div className="flex items-center flex-shrink-0">
          <p className="font-semibold text-[#111827]" style="font-size: 16px;">내 단축번호</p>
        </div>

        <div className="flex-1" style="min-height: 0;">
          <div v-if="store.patternLoading && store.patterns.length === 0" className="flex h-full items-center justify-center rounded-[18px] bg-white text-[#6B7280]">
            단축번호를 불러오고 있어요…
          </div>
          <div v-else-if="store.patternError && store.patterns.length === 0" className="flex h-full flex-col items-center justify-center gap-3 rounded-[18px] bg-white px-5 text-center">
            <p className="text-[#B91C1C]" role="alert">{{ store.patternError }}</p>
            <Btn variant="secondary" @click="store.loadPatterns(true)">다시 시도</Btn>
          </div>
          <PatternGrid
            v-else
            :pageNums="pageNums"
            :patterns="store.patterns"
            :dragState="drag"
            :disabled="store.patternOrderSaving"
            :highlightedPatternId="highlightedPattern?.patternId"
            @pointer-down="handlePointerDown"
            @pointer-move="handlePointerMove"
            @pointer-up="handlePointerUp"
            @pointer-cancel="handlePointerCancel"
            @card-click="handleCardClick"
          />
        </div>

        <!-- 페이지 도트 -->
        <div className="flex flex-shrink-0 items-center justify-center gap-1">
          <button
            v-for="p in [1, 2, 3]"
            :key="p"
            @click="store.homePage = p"
            class="flex h-12 w-12 items-center justify-center"
            :aria-label="`단축번호 ${p}페이지`"
            :aria-current="store.homePage === p ? 'page' : undefined"
          >
            <span
              class="block transition-all"
              :style="store.homePage === p
                ? 'width:28px;height:10px;border-radius:5px;background:#FFBC00;'
                : 'width:10px;height:10px;border-radius:50%;background:#D1D5DB;'"
            />
          </button>
        </div>
      </div>
    </div>

    <NavBar active="home" :onSelect="store.navTo" />

    <!-- Focus mode overlay -->
    <div
      v-if="focusedPat"
      ref="focusDialog"
      className="absolute inset-0 z-40 flex items-center justify-center"
      style="background: rgba(0, 0, 0, 0.60)"
      role="dialog"
      aria-modal="true"
      :aria-label="`${focusedPat.label} 실행 전 확인`"
      tabindex="-1"
      @click="focusedPat = null"
      @keydown.esc.stop.prevent="focusedPat = null"
      @keydown.tab="trapDialogFocus"
    >
      <FocusModeCard
        :pat="focusedPat"
        @cancel="focusedPat = null"
        @start="startFocusedPattern"
      />
    </div>

    <!-- Drag ghost -->
    <div
      v-if="drag && ghostPat"
      className="pointer-events-none z-[9999] rounded-[22px] flex flex-col items-center justify-center gap-1.5 shadow-2xl"
      :style="{
        position: 'fixed',
        left: `${drag.ghostX - 60}px`,
        top: `${drag.ghostY - 68}px`,
        width: '120px',
        height: '136px',
        backgroundColor: ghostPat.color,
        transform: 'scale(1.08)',
        opacity: 0.93
      }"
    >
      <span className="text-[32px] font-black text-white leading-none">{{ ghostPat.num }}</span>
      <span className="text-[11px] font-bold text-white text-center px-2 leading-tight">{{ ghostPat.label }}</span>
    </div>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from 'vue';
import { useAppStore } from '../stores/appStore';
import SafeArea from '../components/common/SafeArea.vue';
import DanjjakMark from '../components/common/DanjjakMark.vue';
import Card from '../components/common/Card.vue';
import Btn from '../components/common/Btn.vue';
import Ic from '../components/common/Ic.vue';
import NavBar from '../components/common/NavBar.vue';
import PatternGrid from '../components/common/PatternGrid.vue';
import FocusModeCard from '../components/common/FocusModeCard.vue';
import { useShortcutSpeech } from '../composables/useShortcutSpeech.js';
import { voiceCommandExamples } from '../features/voice/shortcutCommands.js';

const store = useAppStore();

const highlightedPatternId = ref(null);
const highlightedPattern = computed(() => store.patterns.find((pattern) => pattern.patternId === highlightedPatternId.value));
const { supported: speechSupported, state: sttState, message: sttMessage, start: startSpeech, stop: stopSpeech, cancel: cancelSpeech } = useShortcutSpeech(
  () => store.patterns,
  async (pattern) => {
    highlightedPatternId.value = pattern.patternId;
    store.homePage = Math.ceil(pattern.num / 4);
    await nextTick();
    if (highlightedPatternId.value === pattern.patternId) {
      containerRef.value?.querySelector(`[data-slot-num="${pattern.num}"]`)?.scrollIntoView({ block: 'nearest' });
    }
  },
);
const focusedPat = ref(null);
const focusDialog = ref(null);
let previouslyFocused = null;

const drag = ref(null);
const justDropped = ref(false);
let longPressTimer = null;
let edgeTimer = null;
const startPos = ref({ x: 0, y: 0 });
const containerRef = ref(null);

const LONG_PRESS_MS = 500;
const MOVE_THRESHOLD = 8;
const EDGE_PX = 50;

const pageNums = computed(() => {
  if (store.homePage === 1) return [1, 2, 3, 4];
  if (store.homePage === 2) return [5, 6, 7, 8];
  return [9, 10, 11, 12];
});
const ghostPat = computed(() => (drag.value ? store.patterns.find((p) => p.num === drag.value.sourceNum) : null));

watch(() => store.patterns, () => {
  if (sttState.value !== 'idle' || highlightedPatternId.value !== null) resetSpeech();
});

watch(focusedPat, async (pattern) => {
  if (pattern) {
    previouslyFocused = document.activeElement;
    await nextTick();
    focusDialog.value?.querySelector('button')?.focus();
    return;
  }
  previouslyFocused?.focus?.();
  previouslyFocused = null;
});

function trapDialogFocus(event) {
  const buttons = [...(focusDialog.value?.querySelectorAll('button:not([disabled])') ?? [])];
  if (buttons.length === 0) {
    event.preventDefault();
    focusDialog.value?.focus();
    return;
  }
  const first = buttons[0];
  const last = buttons[buttons.length - 1];
  if (event.shiftKey && document.activeElement === first) {
    event.preventDefault();
    last.focus();
  } else if (!event.shiftKey && document.activeElement === last) {
    event.preventDefault();
    first.focus();
  }
}

function toggleStt() {
  if (sttState.value === 'listening') stopSpeech();
  else if (sttState.value !== 'idle') resetSpeech();
  else {
    highlightedPatternId.value = null;
    startSpeech();
  }
}

function resetSpeech() {
  highlightedPatternId.value = null;
  cancelSpeech();
}

function findSlotAtPoint(x, y) {
  const els = document.elementsFromPoint(x, y);
  for (const el of els) {
    const s = el.dataset?.slotNum;
    if (s) {
      const n = parseInt(s);
      if (n >= 1 && n <= 12) return n;
    }
  }
  return null;
}

function checkEdge(clientX) {
  const r = containerRef.value?.getBoundingClientRect();
  if (!r) return;
  if (clientX > r.right - EDGE_PX && store.homePage < 3) {
    if (!edgeTimer) edgeTimer = setTimeout(() => { store.homePage = Math.min(store.homePage + 1, 3); edgeTimer = null; }, 500);
  } else if (clientX < r.left + EDGE_PX && store.homePage > 1) {
    if (!edgeTimer) edgeTimer = setTimeout(() => { store.homePage = Math.max(store.homePage - 1, 1); edgeTimer = null; }, 500);
  } else {
    if (edgeTimer) { clearTimeout(edgeTimer); edgeTimer = null; }
  }
}

// Document-level drag handlers — reliable across the whole screen regardless of pointer position
function onDocPointerMove(e) {
  if (!drag.value) return;
  const x = e.clientX, y = e.clientY;
  drag.value = { ...drag.value, ghostX: x, ghostY: y, targetNum: findSlotAtPoint(x, y) };
  checkEdge(x);
}

function onDocPointerUp() {
  removeDocListeners();
  if (edgeTimer) { clearTimeout(edgeTimer); edgeTimer = null; }
  if (drag.value) {
    const { sourceNum, targetNum } = drag.value;
    const didReorder = targetNum !== null && targetNum !== sourceNum;
    if (didReorder) store.reorder(sourceNum, targetNum);
    drag.value = null;
    justDropped.value = didReorder;
  }
}

function onDocPointerCancel() {
  removeDocListeners();
  if (edgeTimer) { clearTimeout(edgeTimer); edgeTimer = null; }
  drag.value = null;
  justDropped.value = false;
}

function removeDocListeners() {
  document.removeEventListener('pointermove', onDocPointerMove);
  document.removeEventListener('pointerup', onDocPointerUp);
  document.removeEventListener('pointercancel', onDocPointerCancel);
}

function handlePointerDown(num, e) {
  if (store.patternOrderSaving) return;
  if (!store.patterns.find((p) => p.num === num)) return;
  e.preventDefault();
  const x = e.clientX, y = e.clientY;
  startPos.value = { x, y };

  longPressTimer = setTimeout(() => {
    longPressTimer = null;
    resetSpeech();
    drag.value = { sourceNum: num, ghostX: x, ghostY: y, targetNum: null };
    // Track drag at document level so events fire regardless of pointer position
    document.addEventListener('pointermove', onDocPointerMove, { passive: true });
    document.addEventListener('pointerup', onDocPointerUp);
    document.addEventListener('pointercancel', onDocPointerCancel);
  }, LONG_PRESS_MS);
}

function handlePointerMove(e) {
  // Only used before drag starts: cancel long press if finger drifted
  if (drag.value || longPressTimer === null) return;
  const dx = e.clientX - startPos.value.x;
  const dy = e.clientY - startPos.value.y;
  if (Math.hypot(dx, dy) > MOVE_THRESHOLD) {
    clearTimeout(longPressTimer);
    longPressTimer = null;
  }
}

function handlePointerUp() {
  if (longPressTimer) { clearTimeout(longPressTimer); longPressTimer = null; }
  // Drag cleanup handled by document-level listener
}

function handlePointerCancel() {
  if (longPressTimer) { clearTimeout(longPressTimer); longPressTimer = null; }
  // Drag cleanup handled by document-level listener
}

async function handleCardClick(num, pat) {
  if (justDropped.value) { justDropped.value = false; return; }
  if (drag.value) return;
  if (store.patternLoading) return;
  resetSpeech();
  if (pat) {
    try {
      await store.loadPatternDetail(pat.patternId);
      focusedPat.value = store.activePattern;
    } catch {
      // 저장소의 오류 문구와 새 목록을 유지해 다시 시도할 수 있게 한다.
      sttMessage.value = store.patternError;
    }
  } else {
    store.navigate("pattern-register");
  }
}

function beginDirectTransfer() {
  resetSpeech();
  store.startTransfer();
  store.navigate('transfer-source');
}

async function startFocusedPattern() {
  if (!focusedPat.value) return;
  const pat = focusedPat.value;
  focusedPat.value = null;
  try {
    await store.startPatternExecution(pat);
  } catch {
    focusedPat.value = pat;
  }
}

onMounted(() => { void store.loadPatterns(); });

// Swipe page navigation
const swipeStartX = ref(null);
function onTouchStart(e) {
  swipeStartX.value = e.touches[0].clientX;
}
function onTouchEnd(e) {
  if (swipeStartX.value === null) return;
  const delta = e.changedTouches[0].clientX - swipeStartX.value;
  if (Math.abs(delta) > 60) {
    if (delta < 0) store.homePage = Math.min(store.homePage + 1, 3);
    else store.homePage = Math.max(store.homePage - 1, 1);
  }
  swipeStartX.value = null;
}

onUnmounted(() => {
  if (longPressTimer) clearTimeout(longPressTimer);
  if (edgeTimer) clearTimeout(edgeTimer);
  removeDocListeners();
  previouslyFocused?.focus?.();
});
</script>
