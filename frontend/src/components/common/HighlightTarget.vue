<template>
  <div className="relative">
    <div
      v-if="active && !done"
      className="absolute inset-0 -m-3 rounded-[24px] pointer-events-none z-10"
      style="box-shadow: 0 0 0 2000px rgba(0,0,0,0.07)"
    />
    <div
      :class="['relative z-20', active && !done ? 'glow-highlight rounded-[20px] cursor-pointer' : '']"
      @click="handleTap"
    >
      <slot />
    </div>
    <div v-if="active && !done" className="relative z-30 mt-2.5">
      <GuideBubble :text="bubble" />
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import GuideBubble from './GuideBubble.vue';

const props = defineProps({
  active: { type: Boolean, required: true },
  bubble: { type: String, required: true }
});

const emit = defineEmits(['tap']);
const done = ref(false);

function handleTap() {
  if (props.active && !done.value) {
    done.value = true;
    setTimeout(() => {
      emit('tap');
    }, 250);
  }
}
</script>
