<template>
  <div
    @click="$emit('click', $event)"
    :class="[
      'bg-white rounded-[20px] border transition-all',
      glowing ? 'border-[#F5B800] glow-highlight' : highlighted ? 'border-[#F5B800] border-2' : 'border-[#EBEBEA]',
      $attrs.onClick || hasClick ? 'cursor-pointer active:scale-[0.98]' : '',
      customClass
    ]"
  >
    <slot />
  </div>
</template>

<script setup>
import { useAttrs, computed } from 'vue';

const props = defineProps({
  className: { type: String, default: '' },
  highlighted: { type: Boolean, default: false },
  glowing: { type: Boolean, default: false }
});

defineEmits(['click']);

const attrs = useAttrs();
const hasClick = computed(() => !!attrs.onClick);
const customClass = computed(() => props.className);
</script>
