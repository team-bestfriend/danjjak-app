<template>
  <div className="flex items-end gap-2 fade-in-up">
    <div className="w-9 h-9 rounded-full bg-[#F5B800] flex items-center justify-center text-[14px] flex-shrink-0 font-black text-[#111827]">
      안
    </div>
    <div className="bg-white border-2 border-[#F5B800] rounded-2xl rounded-bl-sm px-4 py-2.5 shadow-sm">
      <p className="text-[15px] font-bold text-[#111827]">{{ text }}</p>
    </div>
  </div>
</template>

<script setup>
defineProps({
  text: { type: String, required: true }
});
</script>
