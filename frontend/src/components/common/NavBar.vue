<template>
  <nav className="bg-white border-t border-[#EBEBEA] flex flex-shrink-0">
    <button
      v-for="t in tabs"
      :key="t.id"
      @click="onSelect(t.id)"
      :class="[
        'flex-1 flex flex-col items-center py-2.5 gap-0.5 transition-colors',
        active === t.id ? 'text-[#D97706]' : 'text-[#9CA3AF]'
      ]"
    >
      <Ic :name="t.icon" />
      <span :class="['text-[10px] font-bold mt-0.5', active === t.id ? 'text-[#D97706]' : 'text-[#9CA3AF]']">
        {{ t.label }}
      </span>
    </button>
  </nav>
</template>

<script setup>
import Ic from './Ic.vue';

defineProps({
  active: { type: String, required: true },
  onSelect: { type: Function, required: true }
});

const tabs = [
  { id: "home", icon: "Home", label: "홈" },
  { id: "patterns", icon: "List", label: "패턴" },
  { id: "analysis", icon: "Chart", label: "이용 분석" },
  { id: "settings", icon: "Gear", label: "설정" },
];
</script>
