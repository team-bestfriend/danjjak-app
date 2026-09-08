<template>
  <section class="rounded-[20px] border-2 border-[#E5E7EB] bg-white p-5">
    <div class="flex items-start justify-between gap-4">
      <div>
        <p class="text-[19px] font-bold text-[#111827]">{{ title }} <span class="text-[14px] font-normal text-[#6B7280]">(선택)</span></p>
        <p class="mt-2 text-[15px] leading-relaxed text-[#6B7280]">{{ description }}</p>
      </div>
      <button
        type="button"
        role="switch"
        :aria-checked="modelValue"
        :aria-label="`${title} ${modelValue ? '동의함' : '동의하지 않음'}`"
        :class="['relative h-12 w-20 flex-shrink-0 rounded-full transition-colors', modelValue ? 'bg-[#FFBC00]' : 'bg-[#D1D5DB]']"
        @click="$emit('update:modelValue', !modelValue)"
      >
        <span :class="['absolute top-1 h-10 w-10 rounded-full bg-white shadow transition-all', modelValue ? 'left-9' : 'left-1']" />
      </button>
    </div>
    <p class="mt-4 text-[14px] font-semibold" :class="modelValue ? 'text-[#15803D]' : 'text-[#6B7280]'">
      {{ modelValue ? '동의함' : '동의하지 않음' }}
    </p>
  </section>
</template>

<script setup>
defineProps({
  modelValue: { type: Boolean, required: true },
  title: { type: String, required: true },
  description: { type: String, required: true },
});
defineEmits(['update:modelValue']);
</script>
