<template>
  <button
    @click="$emit('click', $event)"
    className="w-full text-left px-2 py-4 rounded-xl hover:bg-[#F9FAFB] transition-colors"
  >
    <span className="text-[17px] font-semibold block" :style="{ color }">{{ label }}</span>
    <span v-if="sub" className="text-[13px] text-[#9CA3AF]">{{ sub }}</span>
  </button>
</template>

<script setup>
defineProps({
  label: { type: String, required: true },
  sub: { type: String, default: null },
  color: { type: String, default: '#111827' }
});

defineEmits(['click']);
</script>
