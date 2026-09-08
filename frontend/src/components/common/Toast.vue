<template>
  <div className="absolute bottom-24 left-1/2 -translate-x-1/2 z-50 bg-[#111827] text-white text-[14px] font-bold px-4 py-3 rounded-2xl shadow-lg whitespace-nowrap fade-in-up flex items-center gap-3">
    <span>{{ message }}</span>
    <button v-if="action" @click="handleAction" className="text-[#F5B800] font-bold ml-1">
      {{ action }}
    </button>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted } from 'vue';

const props = defineProps({
  message: { type: String, required: true },
  action: { type: String, default: null },
  onAction: { type: Function, default: null }
});

const emit = defineEmits(['done']);

let timer = null;

function handleAction() {
  if (props.onAction) props.onAction();
  emit('done');
}

onMounted(() => {
  timer = setTimeout(() => {
    emit('done');
  }, props.action ? 3500 : 2200);
});

onUnmounted(() => {
  if (timer) clearTimeout(timer);
});
</script>
