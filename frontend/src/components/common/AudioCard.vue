<template>
  <Card className="p-4">
    <p className="text-[11px] font-bold text-[#9CA3AF] uppercase tracking-wide mb-2">가족 음성 안내</p>
    <div className="flex items-center gap-3">
      <div className="w-10 h-10 rounded-full bg-[#DBEAFE] flex items-center justify-center text-[#2563EB] font-bold flex-shrink-0">
        {{ name }}
      </div>
      <div className="flex-1">
        <p className="text-[15px] font-bold text-[#111827] mb-2">"{{ quote }}"</p>
        <div className="flex items-center gap-2">
          <button
            @click="togglePlay"
            className="w-8 h-8 rounded-full bg-[#DBEAFE] text-[#2563EB] flex items-center justify-center flex-shrink-0"
          >
            {{ playing ? '⏸' : '▶' }}
          </button>
          <div className="flex-1 flex items-end gap-px h-5">
            <div
              v-for="(_, i) in 30"
              :key="i"
              :class="[
                'flex-1 rounded-sm transition-colors',
                (i / 30) < (prog / 100) ? 'bg-[#3B82F6]' : 'bg-[#DBEAFE]'
              ]"
              :style="{ height: `${Math.max(3, Math.abs(Math.sin(i * 0.9)) * 14 + 3)}px` }"
            />
          </div>
        </div>
      </div>
    </div>
  </Card>
</template>

<script setup>
import { ref, watch, onUnmounted } from 'vue';
import Card from './Card.vue';

defineProps({
  quote: { type: String, required: true },
  name: { type: String, default: '민' }
});

const playing = ref(false);
const prog = ref(0);
let timer = null;

function togglePlay() {
  playing.value = !playing.value;
}

watch([playing, prog], ([newPlaying, newProg]) => {
  if (timer) clearTimeout(timer);
  if (newPlaying && newProg < 100) {
    timer = setTimeout(() => {
      prog.value = Math.min(prog.value + 3, 100);
    }, 150);
  } else if (newProg >= 100) {
    playing.value = false;
    prog.value = 0;
  }
});

onUnmounted(() => {
  if (timer) clearTimeout(timer);
});
</script>
