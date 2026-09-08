<template>
  <button
    :disabled="disabled"
    @click="$emit('click', $event)"
    :class="[
      'w-full rounded-[18px] flex items-center justify-center gap-2 transition-all active:scale-[0.97]',
      variantClasses[variant] || variantClasses.primary,
      disabled ? 'opacity-35' : '',
      customClass
    ]"
    style="min-height: 60px; font-size: 18px; font-weight: 600;"
  >
    <slot />
  </button>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  variant: { type: String, default: 'primary' },
  disabled: { type: Boolean, default: false },
  className: { type: String, default: '' }
});

defineEmits(['click']);

const variantClasses = {
  primary:   'bg-[#FFBC00] text-[#111827]',
  secondary: 'bg-white border border-[#D1D5DB] text-[#374151]',
  danger:    'bg-[#EF4444] text-white',
  info:      'bg-[#EFF6FF] text-[#1D4ED8] border border-[#BFDBFE]',
  ghost:     'text-[#6B7280]',
};

const customClass = computed(() => props.className);
</script>
