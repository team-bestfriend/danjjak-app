<template>
  <div class="flex flex-col h-full bg-[#FAFAF8]">
    <SafeArea />
    <TopBar title="사람 및 계좌 관리" :onBack="store.goBack" />
    <div class="flex-1 overflow-y-auto px-4 pt-4 pb-6 space-y-4">
      <p class="text-[#6B7280] text-[16px]">송금할 사람과 수취 계좌 한 개를 관리해요.</p>

      <p v-if="store.financeLoading" class="rounded-2xl bg-white p-5 text-[#6B7280]">등록 정보를 불러오고 있어요…</p>
      <div v-else-if="store.financeError" class="rounded-2xl border border-[#FCA5A5] bg-[#FEF2F2] p-5 space-y-3">
        <p class="text-[#991B1B]">{{ store.financeError }}</p>
        <Btn variant="secondary" @click="store.loadFinancialData(true)">다시 시도</Btn>
      </div>
      <div v-else-if="formattedPeople.length === 0" class="rounded-2xl bg-white p-5 text-center space-y-3">
        <p class="font-bold text-[#111827] text-[19px]">등록된 사람이 없어요.</p>
        <p class="text-[#6B7280]">자주 송금하는 사람과 계좌를 등록해 보세요.</p>
        <Btn @click="openCreate">새로운 사람 등록</Btn>
      </div>
      <template v-else>
        <Card v-for="person in formattedPeople" :key="person.id" class="overflow-hidden">
          <div class="p-5 flex items-center gap-4">
            <div class="w-14 h-14 rounded-full bg-[#FFF3CC] border border-[#FFBC00] flex items-center justify-center text-[28px]">
              {{ person.emoji }}
            </div>
            <div class="flex-1">
              <p class="font-bold text-[#111827] text-[20px]">{{ person.name }}</p>
              <p class="text-[#6B7280] text-[14px]">{{ person.relation }}</p>
            </div>
            <button
              @click="openEdit(person.id)"
              class="min-h-[48px] rounded-xl border border-[#D1D5DB] px-4 font-bold text-[#374151]"
            >수정</button>
          </div>
          <div v-if="person.account" class="border-t border-[#F3F4F6] px-5 py-4 flex items-center gap-3">
            <div class="w-10 h-10 rounded-[10px] flex items-center justify-center font-black bg-[#FFBC00] text-[#111827] text-[11px]">
              {{ person.account.bankName.slice(0, 2) }}
            </div>
            <div class="flex-1 min-w-0">
              <p class="font-bold text-[#374151] text-[15px]">{{ person.account.bankName }}</p>
              <p class="font-mono text-[#9CA3AF] text-[13px]">{{ person.account.masked }}</p>
            </div>
          </div>
          <p v-else class="border-t border-[#F3F4F6] px-5 py-4 text-[#6B7280]">등록된 수취 계좌가 없어요.</p>
        </Card>

        <button
          @click="openCreate"
          class="w-full min-h-[60px] border-2 border-dashed border-[#FFBC00] rounded-[20px] font-bold text-[#92650A] flex items-center justify-center gap-2"
        >
          <Ic name="Plus" />새로운 사람 등록
        </button>
      </template>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue';
import { useAppStore } from '../stores/appStore';
import SafeArea from '../components/common/SafeArea.vue';
import TopBar from '../components/common/TopBar.vue';
import Card from '../components/common/Card.vue';
import Btn from '../components/common/Btn.vue';
import Ic from '../components/common/Ic.vue';

const store = useAppStore();
const formattedPeople = computed(() => store.people.map((person) => ({
  ...person,
  account: store.accountsByPerson[person.id]?.[0] ?? null,
})));

onMounted(() => store.loadFinancialData());

function openCreate() {
  store.editingPersonId = null;
  store.navigate('add-person');
}

function openEdit(personId) {
  store.editingPersonId = personId;
  store.navigate('add-person');
}
</script>
