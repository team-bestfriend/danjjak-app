<template>
  <div class="flex h-full flex-col bg-[#FAFAF8]">
    <SafeArea />
    <main class="flex flex-1 flex-col items-center justify-center px-8 text-center">
      <div class="mb-6 flex h-24 w-24 items-center justify-center rounded-[24px] bg-[#FFBC00] shadow-lg">
        <DanjjakMark :size="52" />
      </div>
      <h1 class="text-[38px] font-bold text-[#111827]">단짝</h1>
      <p class="mt-2 text-[18px] leading-relaxed text-[#6B7280]">
        번호 하나로 시작하는<br />쉽고 안전한 금융 도우미
      </p>

      <div class="mt-10 w-full space-y-3 text-left">
        <div v-for="item in FEATURES" :key="item.text" class="flex items-center gap-4 rounded-[18px] bg-white p-4 shadow-sm">
          <span class="text-[28px]" aria-hidden="true">{{ item.icon }}</span>
          <p class="text-[17px] font-semibold text-[#374151]">{{ item.text }}</p>
        </div>
      </div>
    </main>

    <div class="px-6 pb-12">
      <button
        type="button"
        class="h-[66px] w-full rounded-[18px] bg-[#FFBC00] text-[20px] font-semibold text-[#111827]"
        @click="store.navigate('login')"
      >카카오로 시작하기</button>
      <button
        type="button"
        class="mt-3 h-12 w-full rounded-[14px] border border-dashed border-[#9CA3AF] bg-white text-[15px] font-semibold text-[#4B5563] disabled:opacity-50"
        :disabled="store.devSessionStarting"
        @click="store.startDevSession"
      >
        {{ store.devSessionStarting ? '홈으로 이동 중...' : '개발용: 바로 홈으로' }}
      </button>
    </div>
  </div>
</template>

<script setup>
import DanjjakMark from '../components/common/DanjjakMark.vue';
import SafeArea from '../components/common/SafeArea.vue';
import { useAppStore } from '../stores/appStore';

const store = useAppStore();
const FEATURES = [
  { icon: '🔢', text: '자주 쓰는 금융 업무를 단축번호로 실행해요.' },
  { icon: '🔊', text: '화면과 음성 안내를 천천히 따라가요.' },
  { icon: '🛡️', text: '이상 거래는 한 번 더 확인하고 보호자에게 알려요.' },
];
</script>
