<template>
  <div className="flex bg-[#F3F4F6] p-1 rounded-[14px] gap-1">
    <button
      v-for="o in options"
      :key="o.key"
      @click="$emit('update:value', o.key)"
      :class="[
        'flex-1 min-h-12 rounded-[11px] text-[15px] font-bold transition-all',
        value === o.key ? 'bg-white text-[#111827] shadow-sm' : 'text-[#6B7280]'
      ]"
    >
      {{ o.label }}
    </button>
  </div>
</template>

<script setup>
defineProps({
  options: { type: Array, required: true },
  value: { type: String, required: true }
});

defineEmits(['update:value']);
</script>
