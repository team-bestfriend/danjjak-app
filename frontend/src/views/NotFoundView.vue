<template>
  <div class="flex h-full flex-col items-center justify-center bg-[#FAFAF8] px-7 text-center">
    <div class="text-[64px]" aria-hidden="true">🧭</div>
    <h1 class="mt-5 text-[28px] font-bold text-[#111827]">화면을 찾을 수 없어요</h1>
    <p class="mt-3 text-[16px] text-[#6B7280]">주소를 다시 확인하거나 처음 화면으로 이동해 주세요.</p>
    <button
      type="button"
      class="mt-8 h-[62px] w-full rounded-[18px] bg-[#FFBC00] text-[18px] font-semibold text-[#111827]"
      @click="router.replace({ name: store.authStatus === 'authenticated' ? 'home' : 'onboarding' })"
    >처음 화면으로</button>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router';
import { useAppStore } from '../stores/appStore';

const router = useRouter();
const store = useAppStore();
</script>
