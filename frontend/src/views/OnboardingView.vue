<template>
  <div class="flex h-full flex-col bg-[#FAFAF8]">
    <SafeArea />

    <main class="flex min-h-0 flex-1 flex-col items-center px-6 text-center">
      <div class="flex flex-1 flex-col items-center justify-center">
        <!-- 앱 로고 -->
        <img
          src="../assets/danjjak-logo.png"
          alt="단짝"
          class="h-[142px] w-[142px] object-contain drop-shadow-[0_12px_18px_rgba(17,24,39,0.12)]"
        />

        <!-- 서비스 설명 -->
        <p class="mt-10 text-[20px] font-medium leading-relaxed text-[#737B8C]">
          어르신을 위한 쉬운 금융 도우미
        </p>

        <!-- 핵심 소개 문구 -->
        <div
          class="mt-8 rounded-[22px] border border-[#FFBC00] bg-[#FFF2CC] px-8 py-4"
        >
          <p class="text-[17px] font-semibold leading-relaxed text-[#9A5B00]">
            단축번호로 시작하는<br />
            안심 금융 서비스
          </p>
        </div>
      </div>
    </main>

    <!-- 하단 버튼 -->
    <div class="space-y-3 px-6 pb-10">
      <button
        type="button"
        class="h-[66px] w-full rounded-[20px] bg-[#FFBC00] text-[21px] font-bold text-[#111827] transition active:scale-[0.98]"
        @click="goToIntro"
      >
        처음 시작하기
      </button>

      <button
        type="button"
        class="h-[62px] w-full rounded-[20px] border border-[#D1D5DB] bg-white text-[20px] font-medium text-[#374151] transition active:scale-[0.98]"
        @click="goToLogin"
      >
        다시 이용하기
      </button>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from "vue-router";
import SafeArea from "../components/common/SafeArea.vue";

const router = useRouter();

function goToIntro() {
  router.push({ name: "feature-intro" });
}

function goToLogin() {
  router.push({ name: "login" });
}
</script>
