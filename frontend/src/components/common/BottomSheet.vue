<template>
  <div
    v-if="open"
    ref="dialogRoot"
    class="absolute inset-0 z-50 flex flex-col justify-end"
    style="background: rgba(0, 0, 0, 0.18)"
    role="dialog"
    aria-modal="true"
    :aria-labelledby="title ? titleId : undefined"
    aria-label="title ? undefined : '선택 창'"
    tabindex="-1"
    @click.self="emitClose"
    @keydown.esc.stop.prevent="emitClose"
    @keydown.tab="trapFocus"
  >
    <div class="rounded-t-[28px] bg-white px-4 pb-10 pt-3">
      <div class="mx-auto mb-3 h-1 w-9 rounded-full bg-[#E5E7EB]" aria-hidden="true" />
      <div class="flex items-center gap-3 px-1 pb-2">
        <p v-if="title" :id="titleId" class="flex-1 text-[20px] font-black text-[#111827]">{{ title }}</p>
        <button
          type="button"
          class="flex h-12 w-12 flex-shrink-0 items-center justify-center rounded-full bg-[#F3F4F6] text-[24px] font-bold text-[#4B5563]"
          aria-label="선택 창 닫기"
          @click="emitClose"
        >×</button>
      </div>
      <slot />
    </div>
  </div>
</template>

<script setup>
import { nextTick, onBeforeUnmount, ref, watch } from 'vue';

const props = defineProps({
  open: { type: Boolean, required: true },
  title: { type: String, default: null },
});
const emit = defineEmits(['close']);
const dialogRoot = ref(null);
const titleId = `bottom-sheet-title-${Math.random().toString(36).slice(2)}`;
let previouslyFocused = null;

function focusableElements() {
  if (!dialogRoot.value) return [];
  return [...dialogRoot.value.querySelectorAll(
    'button:not([disabled]), a[href], input:not([disabled]), select:not([disabled]), textarea:not([disabled]), [tabindex]:not([tabindex="-1"])',
  )];
}

function trapFocus(event) {
  const elements = focusableElements();
  if (elements.length === 0) {
    event.preventDefault();
    dialogRoot.value?.focus();
    return;
  }
  const first = elements[0];
  const last = elements[elements.length - 1];
  if (event.shiftKey && document.activeElement === first) {
    event.preventDefault();
    last.focus();
  } else if (!event.shiftKey && document.activeElement === last) {
    event.preventDefault();
    first.focus();
  }
}

function emitClose() {
  emit('close');
}

watch(
  () => props.open,
  async (open) => {
    if (open) {
      previouslyFocused = document.activeElement;
      await nextTick();
      (focusableElements()[0] ?? dialogRoot.value)?.focus();
      return;
    }
    previouslyFocused?.focus?.();
    previouslyFocused = null;
  },
);

onBeforeUnmount(() => previouslyFocused?.focus?.());
</script>
