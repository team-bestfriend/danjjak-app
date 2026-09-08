<template>
  <svg :width="size" :height="size" viewBox="0 0 40 40" fill="none">
    <rect width="40" height="40" rx="10" fill="#F5B800"/>
    <circle cx="13" cy="11" r="3.5" fill="white"/>
    <path d="M6.5 21 Q13 15.5 19.5 21" stroke="white" stroke-width="2.5" stroke-linecap="round" fill="none"/>
    <path d="M20 19.5c0-2-2.5-3.8-2.5-1.2 0 2 2.5 3.5 2.5 3.5s2.5-1.5 2.5-3.5c0-2.6-2.5-0.8-2.5 1.2z" fill="white"/>
    <circle cx="27" cy="11" r="3.5" stroke="#FFFBEB" stroke-width="2" fill="none"/>
    <path d="M20.5 21 Q27 15.5 33.5 21" stroke="#FFFBEB" stroke-width="2.5" stroke-linecap="round" fill="none"/>
  </svg>
</template>

<script setup>
defineProps({
  size: { type: Number, default: 32 }
});
</script>
