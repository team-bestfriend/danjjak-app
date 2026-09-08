<template>
  <div class="flex h-full flex-col bg-[#FAFAF8]">
    <SafeArea />
    <TopBar title="내 계좌 불러오기" :onBack="() => store.navigate('settings')" />
    <main class="min-h-0 flex-1 overflow-y-auto px-5 py-6 space-y-5">
      <h1 class="text-[28px] font-bold text-[#111827]">내 계좌를 불러올까요?</h1>
      <p class="text-[20px] leading-relaxed">실제 은행에 연결하지 않는 연습용 계좌예요.</p>
      <p v-if="!store.ownedAccountsLoaded || !store.ownedAccounts.length" class="text-[16px] text-[#6B7280]">
        계좌를 준비하면 송금과 잔액 조회를 이용할 수 있어요. 설정과 이용방법은 지금도 이용할 수 있어요.
      </p>
      <p v-if="store.ownedAccountsError" role="alert">{{ store.ownedAccountsError }}</p>
      <p v-if="store.candidatesLoading" role="status">불러올 계좌를 확인하고 있어요.</p>
      <div v-else-if="store.candidatesError" class="space-y-3">
        <p role="alert" class="text-[#B91C1C]">{{ store.candidatesError }}</p>
        <Btn variant="secondary" @click="reload">다시 시도</Btn>
      </div>
      <template v-else-if="store.candidatesLoaded">
        <div v-if="!store.importCandidates.length" class="rounded-[20px] bg-white p-5 space-y-3">
          <p class="text-[20px] font-bold">불러올 계좌가 없어요.</p>
          <p>이미 가져온 계좌는 설정에서 확인할 수 있어요. 준비된 계좌가 없으면 나중에 다시 확인해 주세요.</p>
          <Btn variant="secondary" @click="reload">다시 확인</Btn>
        </div>
        <fieldset v-else :disabled="store.accountSaving" class="space-y-3">
          <legend class="mb-3 text-[20px] font-bold">불러올 계좌를 선택해 주세요.</legend>
          <label v-for="candidate in store.importCandidates" :key="candidate.accountId"
            class="flex min-h-[112px] items-start gap-3 rounded-[20px] border-2 bg-white p-4"
            :class="store.selectedImportAccountIds.includes(candidate.accountId) ? 'border-[#FFBC00]' : 'border-[#E5E7EB]'">
            <input v-model="store.selectedImportAccountIds" type="checkbox" :value="candidate.accountId"
              :disabled="!candidate.available || store.accountSaving" class="mt-1 h-6 w-6 shrink-0" />
            <span class="min-w-0 space-y-1 break-words text-[16px]">
              <span class="block text-[20px] font-bold">{{ candidate.bankName }}</span>
              <span v-if="candidate.accountAlias" class="block">{{ candidate.accountAlias }}</span>
              <span class="block">{{ candidate.maskedAccountNumber }}</span>
              <span class="block">{{ candidate.available ? '불러오기 가능' : (candidate.unavailableReason || '아직 준비되지 않았어요.') }}</span>
              <span v-if="store.selectedImportAccountIds.includes(candidate.accountId)" class="block font-bold">✓ 선택됨</span>
            </span>
          </label>
        </fieldset>
      </template>
      <p v-if="store.accountSaveError" role="alert" class="text-[#B91C1C]">{{ store.accountSaveError }}</p>
      <Btn :disabled="!canImport" @click="save">{{ store.accountSaving ? '계좌를 저장하고 있어요…' : '계좌 불러오기' }}</Btn>
      <Btn v-if="store.ownedAccountsLoaded && store.ownedAccounts.length" variant="secondary" @click="store.navigate('home')">홈으로 가기</Btn>
      <Btn variant="secondary" @click="store.navigate('settings')">설정으로 가기</Btn>
      <Btn variant="ghost" @click="store.navigate('service-help')">서비스 이용방법</Btn>
      <Btn variant="ghost" :disabled="store.logoutPending" @click="store.logout">{{ store.logoutPending ? '로그아웃 중…' : '로그아웃' }}</Btn>
      <p v-if="store.authError" role="alert" class="text-[#B91C1C]">{{ store.authError }}</p>
    </main>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue';
import { useAppStore } from '../stores/appStore.js';
import SafeArea from '../components/common/SafeArea.vue';
import TopBar from '../components/common/TopBar.vue';
import Btn from '../components/common/Btn.vue';

const store = useAppStore();
const canImport = computed(() => !store.accountSaving && !store.candidatesLoading && store.candidatesLoaded
  && store.selectedImportAccountIds.length > 0 && store.selectedImportAccountIds.every((id) =>
    store.importCandidates.some((candidate) => candidate.accountId === id && candidate.available)));

async function reload() {
  await Promise.all([store.loadOwnedAccounts(true), store.loadImportCandidates()]);
}

async function save() {
  if (await store.importOwnedAccounts()) {
    store.showToast('내 계좌를 불러왔어요.');
    await store.navigate('home', { replace: true });
  }
}

onMounted(reload);
</script>
