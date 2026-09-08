<template>
  <div data-guide-exempt className="flex items-center justify-between px-4 bg-white border-b border-[#EEEEED] flex-shrink-0" style="min-height: 56px; padding-top: 10px; padding-bottom: 10px;">
    <button
      v-if="onBack"
      :disabled="backDisabled"
      @click="onBack"
      className="w-12 h-12 flex items-center justify-center text-[#374151] rounded-full active:bg-[#F3F4F6] disabled:opacity-50"
      aria-label="뒤로 가기"
    >
      <Ic name="Back" />
    </button>
    <div v-else className="w-12" />

    <span className="font-bold text-[#111827]" style="font-size: 19px;">{{ title }}</span>

    <button
      v-if="rightLabel"
      :disabled="rightDisabled"
      @click="onRight"
      className="font-medium text-[#6B7280] px-2 min-h-12 flex items-center disabled:opacity-50"
      style="font-size: 16px;"
    >
      {{ rightLabel }}
    </button>
    <div v-else className="w-12" />
  </div>
</template>

<script setup>
import Ic from './Ic.vue';

defineProps({
  title: { type: String, required: true },
  onBack: { type: Function, default: null },
  backDisabled: { type: Boolean, default: false },
  rightLabel: { type: String, default: null },
  onRight: { type: Function, default: null },
  rightDisabled: { type: Boolean, default: false },
});
</script>
