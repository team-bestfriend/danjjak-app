<template>
  <div class="flex h-full flex-col items-center justify-center bg-[#FAFAF8] px-7 text-center">
    <div v-if="processing" class="flex flex-col items-center" role="status">
      <div class="h-16 w-16 animate-spin rounded-full border-8 border-[#FFF3CC] border-t-[#FFBC00]" />
      <h1 class="mt-7 text-[26px] font-bold text-[#111827]">로그인을 확인하고 있어요</h1>
      <p class="mt-2 text-[16px] text-[#6B7280]">잠시만 기다려 주세요.</p>
    </div>

    <div v-else>
      <div class="text-[64px]" aria-hidden="true">{{ cancelled ? '↩️' : '⚠️' }}</div>
      <h1 class="mt-5 text-[26px] font-bold text-[#111827]">
        {{ cancelled ? '카카오 로그인을 취소했어요' : '로그인을 완료하지 못했어요' }}
      </h1>
      <p class="mt-3 text-[16px] leading-relaxed text-[#6B7280]">{{ errorMessage }}</p>
      <button
        type="button"
        class="mt-8 h-[62px] w-full rounded-[18px] bg-[#FEE500] text-[18px] font-semibold text-[#111827]"
        @click="store.startKakaoLogin"
      >다시 카카오로 시작하기</button>
      <button type="button" class="mt-3 h-[52px] w-full text-[16px] text-[#6B7280]" @click="router.replace({ name: 'onboarding' })">
        처음 화면으로
      </button>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useAppStore } from '../stores/appStore';

const route = useRoute();
const router = useRouter();
const store = useAppStore();
const processing = ref(route.query.status === 'success');
const cancelled = computed(() => route.query.status === 'cancelled');

const ERROR_MESSAGES = {
  OAUTH_STATE_INVALID: '로그인 확인 정보가 만료되었습니다. 처음부터 다시 시도해 주세요.',
  OAUTH_CODE_MISSING: '카카오에서 인증 정보를 받지 못했습니다.',
  KAKAO_NOT_CONFIGURED: '카카오 앱 설정을 확인해 주세요.',
  KAKAO_AUTHORIZATION_FAILED: '카카오 인증 과정에서 문제가 발생했습니다.',
  KAKAO_TOKEN_EXCHANGE_FAILED: '카카오 로그인 정보를 확인하지 못했습니다.',
  KAKAO_USER_INFO_FAILED: '카카오 사용자 정보를 확인하지 못했습니다.',
  DEMO_USER_CAPACITY_EXCEEDED: '연결할 수 있는 시연 사용자가 없습니다.',
};

const errorMessage = computed(() => (
  cancelled.value
    ? '원할 때 다시 로그인할 수 있습니다.'
    : ERROR_MESSAGES[String(route.query.code)] ?? '잠시 후 다시 시도해 주세요.'
));

onMounted(async () => {
  if (route.query.status !== 'success') {
    processing.value = false;
    return;
  }

  const authenticated = await store.checkSession(true);
  if (!authenticated) {
    processing.value = false;
    return;
  }
  const nextRoute = store.currentUser?.consents?.completed ? 'home' : 'consent';
  await router.replace({ name: nextRoute });
});
</script>
