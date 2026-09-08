<template>
  <div class="flex h-full flex-col bg-white">
    <SafeArea />

    <!-- 상단 로고 -->
    <header class="flex items-center gap-3 px-5 pt-2">
      <div
        class="flex h-9 w-9 items-center justify-center rounded-[10px] bg-[#FFBC00]"
      >
        <DanjjakMark :size="25" />
      </div>

      <span class="text-[20px] font-extrabold text-[#111827]"> 단짝 </span>
    </header>

    <!-- 슬라이드 내용 -->
    <main
      class="flex min-h-0 flex-1 flex-col items-center justify-center px-6 text-center"
    >
      <!-- 첫 번째 화면 -->
      <template v-if="currentPage === 0">
        <div class="flex items-center justify-center gap-3">
          <div
            class="flex h-[92px] w-[92px] items-center justify-center rounded-[24px] bg-[#FF5D62] text-[43px] font-extrabold text-white shadow-[0_10px_16px_rgba(17,24,39,0.12)]"
          >
            1
          </div>

          <div
            class="flex h-[92px] w-[92px] items-center justify-center rounded-[24px] bg-[#FF9945] text-[43px] font-extrabold text-white shadow-[0_10px_16px_rgba(17,24,39,0.12)]"
          >
            2
          </div>

          <div
            class="flex h-[92px] w-[92px] items-center justify-center rounded-[24px] bg-[#20C768] text-[43px] font-extrabold text-white shadow-[0_10px_16px_rgba(17,24,39,0.12)]"
          >
            3
          </div>
        </div>

        <h1
          class="mt-11 text-[34px] font-extrabold leading-[1.3] tracking-[-0.04em] text-[#111827]"
        >
          금융 업무,<br />
          이제 번호 하나로!
        </h1>
      </template>

      <!-- 두 번째 화면 -->
      <template v-else-if="currentPage === 1">
        <div
          class="w-[260px] rounded-[26px] border border-[#E5E7EB] bg-white p-5 shadow-[0_6px_15px_rgba(17,24,39,0.10)]"
        >
          <div class="h-[46px] rounded-[14px] bg-[#F3F4F6]" />
          <div class="mt-4 h-[46px] rounded-[14px] bg-[#F3F4F6]" />

          <div
            class="mt-4 flex h-[58px] items-center gap-3 rounded-[16px] border-[4px] border-[#FFBC00] bg-[#FFF9E8] px-4 shadow-[0_0_0_3px_rgba(255,188,0,0.25)]"
          >
            <span
              class="flex h-7 w-7 items-center justify-center rounded-full bg-[#FFBC00] text-[14px] font-bold text-[#111827]"
            >
              ✓
            </span>

            <span class="text-[16px] font-bold text-[#111827]">
              👨 김민수 (아들)
            </span>
          </div>
        </div>

        <h1
          class="mt-11 text-[34px] font-extrabold leading-[1.3] tracking-[-0.04em] text-[#111827]"
        >
          반짝이는 곳만<br />
          따라 눌러요
        </h1>
      </template>

      <!-- 세 번째 화면 -->
      <template v-else-if="currentPage === 2">
        <div
          class="flex h-[150px] w-[150px] items-center justify-center rounded-full border-[4px] border-[#B7D7FF] bg-[#EFF6FF]"
        >
          <span class="text-[72px]" aria-hidden="true">🎙️</span>
        </div>

        <h1
          class="mt-11 text-[34px] font-extrabold leading-[1.3] tracking-[-0.04em] text-[#111827]"
        >
          가족 목소리가<br />
          함께 안내해요
        </h1>
      </template>

      <!-- 네 번째 화면 -->
      <template v-else>
        <div
          class="relative flex h-[150px] w-[150px] items-center justify-center rounded-full border-[4px] border-[#FFC7CA] bg-[#FFF1F2]"
        >
          <span class="text-[72px]" aria-hidden="true">🥷</span>

          <span
            class="absolute -bottom-1 -right-1 flex h-12 w-12 items-center justify-center rounded-full border-[3px] border-white bg-[#20C768] text-[24px]"
            aria-hidden="true"
          >
            🛡️
          </span>
        </div>

        <h1
          class="mt-11 text-[34px] font-extrabold leading-[1.3] tracking-[-0.04em] text-[#111827]"
        >
          이상한 거래는<br />
          다시 확인해요
        </h1>
      </template>
    </main>

    <!-- 페이지 표시와 버튼 -->
    <div class="px-5 pb-8">
      <div
        class="mb-6 flex items-center justify-center gap-3"
        aria-label="소개 화면 진행 상태"
      >
        <span
          v-for="(_, index) in TOTAL_PAGES"
          :key="index"
          :class="[
            'h-3 rounded-full transition-all duration-300',
            currentPage === index ? 'w-10 bg-[#FFBC00]' : 'w-3 bg-[#E5E7EB]',
          ]"
        />
      </div>

      <button
        type="button"
        class="h-[66px] w-full rounded-[20px] bg-[#FFBC00] text-[21px] font-bold text-[#111827] transition active:scale-[0.98]"
        @click="goNext"
      >
        {{ currentPage === TOTAL_PAGES - 1 ? "시작하기" : "다음" }}
      </button>

      <button
        type="button"
        class="mt-3 h-[58px] w-full rounded-[20px] border border-[#D1D5DB] bg-white text-[19px] font-medium text-[#737B8C] transition active:scale-[0.98]"
        @click="goToLogin"
      >
        건너뛰기
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";
import DanjjakMark from "../components/common/DanjjakMark.vue";
import SafeArea from "../components/common/SafeArea.vue";

const router = useRouter();

const TOTAL_PAGES = 4;
const currentPage = ref(0);

function goNext() {
  if (currentPage.value < TOTAL_PAGES - 1) {
    currentPage.value += 1;
    return;
  }

  goToLogin();
}

function goToLogin() {
  router.push({ name: "login" });
}
</script>
