<template>
  <!-- 등록 수취인 송금 시작 확인 -->
  <div v-if="taskName === 'task-transfer'" class="flex flex-col h-full bg-[#FAFAF8]">
    <SafeArea />
    <TopBar :title="(person?.relation || '가족') + '에게 송금'" :onBack="leaveTask" />
    <div class="flex-1 flex flex-col items-center justify-center px-5 gap-6">
      <div class="w-24 h-24 rounded-full flex items-center justify-center bg-[#FFBC00]"><Ic name="Transfer" /></div>
      <div v-if="store.financeLoading" class="text-center text-[#6B7280]">등록 정보를 불러오고 있어요…</div>
      <div v-else-if="store.financeError" class="w-full rounded-2xl border border-[#FCA5A5] bg-[#FEF2F2] p-5 text-center space-y-3">
        <p class="text-[#991B1B]" role="alert">{{ store.financeError }}</p>
        <Btn variant="secondary" @click="store.loadFinancialData(true)">다시 시도</Btn>
      </div>
      <div v-else-if="person" class="text-center">
        <p class="font-bold text-[#111827] text-[28px]">{{ person.emoji }} {{ person.name }}</p>
        <p class="text-[#6B7280] mt-1 text-[16px]">{{ account?.bankName }} · {{ account?.masked }}</p>
      </div>
      <div v-else class="w-full rounded-2xl bg-white p-5 text-center">
        <p class="font-bold text-[#111827]">연결된 수취인을 찾을 수 없어요.</p>
        <p class="text-[#6B7280] mt-2">등록 정보를 확인한 뒤 다시 시작해 주세요.</p>
      </div>
      <div class="w-full space-y-3">
        <Btn :disabled="!person || !account" @click="beginPatternTransfer">시작하기</Btn>
        <Btn variant="secondary" @click="store.goBack">돌아가기</Btn>
      </div>
    </div>
  </div>

  <!-- 잔액·거래·분류 조회 -->
  <div v-else-if="isInquiryTask" class="flex flex-col h-full bg-[#FAFAF8]">
    <SafeArea />
    <TopBar :title="inquiryTitle" :onBack="leaveTask" />
    <div class="flex-1 overflow-y-auto px-4 pt-4 pb-6 space-y-4">
      <label v-if="store.ownedAccounts.length > 0" class="block space-y-2">
        <span class="font-bold text-[#374151]">조회할 본인 계좌</span>
        <select
          :value="store.selectedInquiryAccountId || ''"
          @change="changeAccount"
          class="w-full min-h-[52px] rounded-[14px] border-2 border-[#E5E7EB] bg-white px-4 text-[17px] font-bold text-[#111827]"
        >
          <option v-for="owned in store.ownedAccounts" :key="owned.accountId" :value="owned.accountId">
            {{ owned.accountAlias || owned.bankName }} · {{ owned.masked }}
          </option>
        </select>
      </label>

      <p v-if="store.financeWarning" class="rounded-xl border border-[#FDE68A] bg-[#FFFBEB] p-3 text-[#92400E]" role="status">{{ store.financeWarning }}</p>

      <p v-if="store.financeLoading || store.inquiryLoading" class="rounded-2xl bg-white p-5 text-[#6B7280]">금융 정보를 불러오고 있어요…</p>
      <div v-else-if="store.financeError || store.inquiryError" class="rounded-2xl border border-[#FCA5A5] bg-[#FEF2F2] p-5 space-y-3">
        <p class="text-[#991B1B]" role="alert">{{ store.financeError || store.inquiryError }}</p>
        <Btn variant="secondary" @click="prepareInquiry(true)">다시 시도</Btn>
      </div>
      <div v-else-if="store.ownedAccounts.length === 0" class="rounded-2xl bg-white p-5 text-center space-y-2">
        <p class="font-bold text-[#111827] text-[19px]">조회할 본인 계좌가 없어요.</p>
        <p class="text-[#6B7280]">수취 계좌는 조회 계좌로 사용할 수 없어요.</p>
      </div>
      <template v-else>
        <Card v-if="showAccountBalance" class="p-5">
          <p class="text-[#6B7280]">{{ selectedInquiryAccount?.bankName }} · {{ selectedInquiryAccount?.masked }}</p>
          <div class="mt-4 flex items-center gap-3">
            <p class="font-black text-[#111827] text-[32px]">
              {{ balanceVisible ? formatWon(store.inquiryBalance?.balance) : '• • • • • •' }}
            </p>
            <button
              @click="balanceVisible = !balanceVisible"
              class="ml-auto min-h-[48px] rounded-xl bg-[#F3F4F6] px-4 font-bold text-[#374151]"
            >{{ balanceVisible ? '숨기기' : '잔액 보기' }}</button>
          </div>
        </Card>

        <div v-if="taskName === 'task-5'" class="flex gap-2">
          <button
            v-for="filter in historyFilters"
            :key="filter.key"
            @click="historyFilter = filter.key"
            :class="[
              'flex-1 min-h-[48px] rounded-xl border font-bold',
              historyFilter === filter.key ? 'bg-[#FFBC00] border-[#FFBC00] text-[#111827]' : 'bg-white border-[#E5E7EB] text-[#374151]'
            ]"
          >{{ filter.label }}</button>
        </div>

        <div v-if="filteredTransactions.length === 0" class="rounded-2xl bg-white p-5 text-center space-y-2">
          <p class="font-bold text-[#111827]">{{ emptyTitle }}</p>
          <p class="text-[#6B7280]">{{ emptyDescription }}</p>
        </div>
        <Card v-else class="overflow-hidden">
          <article
            v-for="(transaction, index) in filteredTransactions"
            :key="transaction.transactionId"
            :class="['px-5 py-4 space-y-2', index < filteredTransactions.length - 1 ? 'border-b border-[#F3F4F6]' : '']"
          >
            <div class="flex items-start justify-between gap-4">
              <div>
                <p class="font-bold text-[#111827] text-[18px]">{{ transaction.counterpartyName || transaction.description }}</p>
                <p class="text-[#6B7280] text-[14px]">{{ typeLabel(transaction.transactionType) }} · {{ formatDate(transaction.transactionAt) }}</p>
              </div>
              <p :class="['font-black text-[18px]', isDeposit(transaction) ? 'text-[#2563EB]' : 'text-[#374151]']">
                {{ isDeposit(transaction) ? '+' : '-' }}{{ formatWon(transaction.amount) }}
              </p>
            </div>
            <div class="flex justify-between gap-4 text-[14px] text-[#6B7280]">
              <span>{{ transaction.description }}</span>
              <span>거래 후 {{ formatWon(transaction.balanceAfter) }}</span>
            </div>
          </article>
        </Card>
      </template>
    </div>
  </div>

  <!-- 고객센터 -->
  <div v-else-if="taskName === 'task-6'" class="flex flex-col h-full bg-[#FAFAF8]">
    <SafeArea />
    <TopBar title="고객센터 연결" :onBack="leaveTask" />
    <div class="flex-1 overflow-y-auto px-4 pt-5 pb-6 space-y-4">
      <p class="font-bold text-[#111827] text-[25px]">도움이 필요하신가요?</p>
      <p v-if="store.supportLoading" class="rounded-2xl bg-white p-5 text-[#6B7280]">고객센터 번호를 불러오고 있어요…</p>
      <div v-else-if="store.supportError" class="rounded-2xl border border-[#FCA5A5] bg-[#FEF2F2] p-5 space-y-3">
        <p class="text-[#991B1B]">{{ store.supportError }}</p>
        <Btn variant="secondary" @click="store.loadSupport(true)">다시 시도</Btn>
      </div>
      <Card v-else-if="store.support?.customerCenterPhone" class="p-6 text-center space-y-4">
        <div class="w-16 h-16 mx-auto rounded-full bg-[#DBEAFE] flex items-center justify-center text-[#2563EB]"><Ic name="Phone" /></div>
        <div>
          <p class="text-[#6B7280]">단짝 고객센터</p>
          <p class="font-black text-[#111827] text-[28px] mt-1">{{ store.support.customerCenterPhone }}</p>
        </div>
        <a
          :href="'tel:' + store.support.customerCenterPhone"
          @click="completeSupportTask"
          class="flex min-h-[58px] w-full items-center justify-center rounded-[18px] bg-[#2563EB] px-5 text-[18px] font-bold text-white"
        >전화 연결하기</a>
        <p class="text-[#6B7280] text-[14px]">컴퓨터에서는 전화번호를 확인한 뒤 휴대전화로 걸어 주세요.</p>
      </Card>
      <div v-else class="rounded-2xl bg-white p-5 text-center space-y-3">
        <p class="font-bold text-[#111827]">고객센터 번호가 없어요.</p>
        <p class="text-[#6B7280] mt-2">잠시 후 다시 조회해 주세요.</p>
        <Btn variant="secondary" @click="store.loadSupport(true)">번호 다시 불러오기</Btn>
      </div>
    </div>
  </div>

  <!-- MVP 제외 기능 -->
  <div v-else class="flex flex-col h-full bg-[#FAFAF8]">
    <SafeArea />
    <TopBar title="준비 중인 기능" :onBack="leaveTask" />
    <div class="flex-1 flex flex-col items-center justify-center px-6 text-center gap-4">
      <div class="w-20 h-20 rounded-full bg-[#E5E7EB] flex items-center justify-center"><Ic name="Gear" /></div>
      <p class="font-bold text-[#111827] text-[24px]">이 기능은 아직 준비 중이에요.</p>
      <p class="text-[#6B7280] text-[17px]">현재 시연에서는 잔액, 거래내역, 연금, 관리비, 공과금 조회를 이용할 수 있어요.</p>
      <Btn variant="secondary" @click="store.goBack">돌아가기</Btn>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue';
import { useAppStore } from '../stores/appStore';
import SafeArea from '../components/common/SafeArea.vue';
import TopBar from '../components/common/TopBar.vue';
import Card from '../components/common/Card.vue';
import Btn from '../components/common/Btn.vue';
import Ic from '../components/common/Ic.vue';

const props = defineProps({
  taskName: { type: String, required: true },
});

const store = useAppStore();
const balanceVisible = ref(false);
const historyFilter = ref('ALL');
const historyFilters = [
  { key: 'ALL', label: '전체' },
  { key: 'DEPOSIT', label: '입금' },
  { key: 'OUTGOING', label: '출금' },
];

const taskConfig = computed(() => ({
  'task-2': { title: '연금 입금 내역', category: 'PENSION' },
  'pension-history': { title: '연금 입금 내역', category: 'PENSION' },
  'task-3': { title: '관리비 내역', category: 'MANAGEMENT_FEE' },
  'task-4': { title: '내 계좌 잔액', category: null },
  'task-5': { title: '거래내역', category: null },
  'task-8': { title: '공과금 내역', category: 'UTILITY_BILL' },
}[props.taskName] ?? null));
const isInquiryTask = computed(() => Boolean(taskConfig.value));
const inquiryTitle = computed(() => taskConfig.value?.title ?? '금융 조회');
const showAccountBalance = computed(() => ['task-4', 'task-5'].includes(props.taskName));
const selectedInquiryAccount = computed(() => (
  store.ownedAccounts.find((owned) => owned.accountId === store.selectedInquiryAccountId) ?? null
));
const filteredTransactions = computed(() => {
  if (historyFilter.value === 'DEPOSIT') {
    return store.inquiryTransactions.filter((transaction) => transaction.transactionType === 'DEPOSIT');
  }
  if (historyFilter.value === 'OUTGOING') {
    return store.inquiryTransactions.filter((transaction) => transaction.transactionType !== 'DEPOSIT');
  }
  return store.inquiryTransactions;
});
const emptyTitle = computed(() => {
  if (taskConfig.value?.category) return '이 분류의 거래가 없어요.';
  if (historyFilter.value !== 'ALL') return '선택한 입출금 내역이 없어요.';
  return '거래내역이 없어요.';
});
const emptyDescription = computed(() => (
  taskConfig.value?.category
    ? '선택한 계좌의 ' + inquiryTitle.value + '을 확인했어요.'
    : '선택한 계좌의 전체 기간을 확인했어요.'
));
const person = computed(() => (
  store.people.find((item) => item.id === (store.activePattern?.personId ?? store.selectedPersonId))
    ?? null
));
const account = computed(() => store.accountsByPerson[person.value?.id]?.[0] ?? null);

onMounted(async () => {
  if (props.taskName === 'task-6') {
    await store.loadSupport();
    return;
  }
  if (props.taskName === 'task-transfer') {
    await store.loadFinancialData();
    return;
  }
  if (isInquiryTask.value) await prepareInquiry();
});

watch(() => props.taskName, async () => {
  historyFilter.value = 'ALL';
  if (isInquiryTask.value) await prepareInquiry();
});

async function prepareInquiry(force = false) {
  const loaded = await store.loadFinancialData(force);
  if (!loaded) return;
  const accountId = store.selectedInquiryAccountId ?? store.defaultOwnedAccount?.accountId;
  if (accountId) {
    await store.loadInquiry(accountId, taskConfig.value?.category ?? null);
    if (!store.inquiryError && store.activePatternDetail) {
      await store.finishPatternExecution('COMPLETED');
    }
  }
}

async function changeAccount(event) {
  const accountId = Number(event.target.value);
  await store.loadInquiry(accountId, taskConfig.value?.category ?? null);
}

async function beginPatternTransfer() {
  await store.loadFinancialData();
  if (!person.value || !account.value) return;
  store.startTransfer({ pattern: true, personId: person.value.id });
  store.selectPerson(person.value.id);
  store.navigate('transfer-source');
}

async function completeSupportTask() {
  if (store.activePatternDetail) await store.finishPatternExecution('COMPLETED');
}

async function leaveTask() {
  if (store.activePatternDetail) {
    store.recordPatternAction('back');
    await store.finishPatternExecution('CANCELLED');
    await store.navigate('home', { replace: true });
    return;
  }
  store.goBack();
}

function isDeposit(transaction) {
  return transaction.transactionType === 'DEPOSIT';
}

function formatWon(value) {
  return Number(value ?? 0).toLocaleString('ko-KR') + '원';
}

function formatDate(value) {
  if (!value) return '-';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return '시각 확인 불가';
  return new Intl.DateTimeFormat('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  }).format(date);
}

function typeLabel(type) {
  const labels = {
    DEPOSIT: '입금',
    WITHDRAWAL: '출금',
    TRANSFER_OUT: '송금',
    PAYMENT: '납부',
  };
  return labels[type] ?? type;
}
</script>
