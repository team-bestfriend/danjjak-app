<template>
  <div class="flex h-full flex-col bg-[#FAFAF8]">
    <SafeArea />
    <main class="flex-1 overflow-y-auto px-5 pb-5 pt-8">
      <h1 class="text-[28px] font-bold leading-snug text-[#111827]">선택 동의를<br />확인해 주세요</h1>
      <p class="mt-3 text-[16px] leading-relaxed text-[#6B7280]">
        두 항목은 모두 선택 사항입니다. 동의하지 않아도 단짝의 기본 기능을 이용할 수 있어요.
      </p>

      <div class="mt-8 space-y-4">
        <ConsentCard
          v-model="usageLogAgreed"
          title="이용 기록 저장"
          description="사용한 패턴과 완료 여부를 기록해 이용 분석에 보여드려요. 동의하지 않으면 실행 기록을 만들지 않습니다."
        />
        <ConsentCard
          v-model="guardianShareAgreed"
          title="이상 거래 카카오 알림"
          description="시연에서는 로그인한 내 카카오 계정의 ‘나에게 보내기’로 알림을 확인해요. 보호자 계정으로 직접 보내는 기능은 아닙니다."
        />
      </div>

      <p v-if="errorMessage" class="mt-5 rounded-[14px] bg-[#FEF2F2] p-4 text-[15px] text-[#B91C1C]" role="alert">
        {{ errorMessage }}
      </p>
    </main>

    <div class="border-t border-[#EEEEED] bg-white px-5 pb-9 pt-4">
      <button
        type="button"
        class="h-[64px] w-full rounded-[18px] bg-[#FFBC00] text-[19px] font-semibold text-[#111827] disabled:opacity-50"
        :disabled="saving"
        @click="save"
      >{{ saving ? '저장 중…' : '선택 저장하고 시작하기' }}</button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import ConsentCard from '../components/common/ConsentCard.vue';
import SafeArea from '../components/common/SafeArea.vue';
import { useAppStore } from '../stores/appStore';

const router = useRouter();
const route = useRoute();
const store = useAppStore();
const usageLogAgreed = ref(Boolean(store.currentUser?.consents?.usageLogAgreed));
const guardianShareAgreed = ref(Boolean(store.currentUser?.consents?.guardianShareAgreed));
const saving = ref(false);
const errorMessage = ref('');

async function save() {
  if (saving.value) return;
  saving.value = true;
  errorMessage.value = '';
  try {
    await store.saveConsents({
      usageLogAgreed: usageLogAgreed.value,
      guardianShareAgreed: guardianShareAgreed.value,
    });
    await router.replace({ name: route.query.edit === '1' ? 'settings' : 'home' });
  } catch (error) {
    errorMessage.value = error?.message ?? '동의 내용을 저장하지 못했습니다. 다시 시도해 주세요.';
  } finally {
    saving.value = false;
  }
}
</script>
