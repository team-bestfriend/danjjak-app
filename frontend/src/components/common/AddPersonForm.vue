<template>
  <form class="space-y-5" @submit.prevent="handleSave">
    <p class="text-[21px] font-black text-[#111827]">{{ existingPerson ? '등록 정보 수정' : '새로운 사람 등록' }}</p>

    <label class="block space-y-2">
      <span class="text-[14px] font-bold text-[#374151]">이름</span>
      <input
        id="registered-person-name"
        v-model.trim="name"
        :aria-invalid="Boolean(fieldErrors.name)"
        aria-describedby="registered-person-name-error"
        @blur="touched.name = true"
        maxlength="50"
        placeholder="예: 김민준"
        class="w-full min-h-[52px] rounded-[14px] border-2 border-[#E5E7EB] focus:border-[#F5B800] outline-none px-4 text-[17px] font-bold"
      />
      <p v-if="fieldErrors.name" id="registered-person-name-error" class="text-[13px] text-[#B91C1C]" role="alert">
        {{ fieldErrors.name }}
      </p>
    </label>

    <label class="block space-y-2">
      <span class="text-[14px] font-bold text-[#374151]">관계</span>
      <input
        id="registered-person-relationship"
        v-model.trim="relationship"
        :aria-invalid="Boolean(fieldErrors.relationship)"
        aria-describedby="registered-person-relationship-error"
        @blur="touched.relationship = true"
        maxlength="30"
        placeholder="예: 아들, 딸"
        class="w-full min-h-[52px] rounded-[14px] border-2 border-[#E5E7EB] focus:border-[#F5B800] outline-none px-4 text-[17px] font-bold"
      />
      <p v-if="fieldErrors.relationship" id="registered-person-relationship-error" class="text-[13px] text-[#B91C1C]" role="alert">
        {{ fieldErrors.relationship }}
      </p>
    </label>

    <div class="space-y-2">
      <p class="text-[14px] font-bold text-[#374151]">은행</p>
      <button
        type="button"
        :aria-invalid="Boolean(fieldErrors.bank)"
        aria-describedby="registered-person-bank-error"
        @click="touched.bank = true; showBanks = !showBanks"
        :class="[
          'w-full min-h-[52px] rounded-[14px] border-2 px-4 text-left text-[16px] font-bold flex items-center justify-between',
          selectedBank ? 'border-[#F5B800] text-[#111827]' : 'border-[#E5E7EB] text-[#9CA3AF]'
        ]"
      >
        <span>{{ selectedBank?.name || '은행 선택' }}</span>
        <span>▾</span>
      </button>
      <p v-if="fieldErrors.bank" id="registered-person-bank-error" class="text-[13px] text-[#B91C1C]" role="alert">
        {{ fieldErrors.bank }}
      </p>
      <div v-if="showBanks" class="grid grid-cols-2 gap-2">
        <button
          v-for="bank in BANKS"
          :key="bank.code"
          type="button"
          @click="bankCode = bank.code; showBanks = false"
          :class="[
            'min-h-[48px] rounded-[12px] border-2 font-bold',
            bankCode === bank.code ? 'border-[#F5B800] bg-[#FFFBEB] text-[#92650A]' : 'border-[#E5E7EB] text-[#374151]'
          ]"
        >{{ bank.name }}</button>
      </div>
    </div>

    <label class="block space-y-2">
      <span class="text-[14px] font-bold text-[#374151]">계좌 번호</span>
      <input
        id="registered-person-account"
        type="tel"
        :value="accountNumber"
        :aria-invalid="Boolean(fieldErrors.account)"
        aria-describedby="registered-person-account-help registered-person-account-error"
        @input="accountNumber = $event.target.value.replace(/[^0-9-]/g, '')"
        @blur="touched.account = true"
        maxlength="50"
        placeholder="000-00-000000"
        inputmode="numeric"
        class="w-full min-h-[52px] rounded-[14px] border-2 border-[#E5E7EB] focus:border-[#F5B800] outline-none px-4 text-[17px] font-bold"
      />
      <p id="registered-person-account-help" class="text-[13px] text-[#6B7280]">숫자와 하이픈을 포함해 8자 이상 입력해 주세요.</p>
      <p v-if="fieldErrors.account" id="registered-person-account-error" class="text-[13px] text-[#B91C1C]" role="alert">
        {{ fieldErrors.account }}
      </p>
    </label>

    <label class="block space-y-2">
      <span class="text-[14px] font-bold text-[#374151]">계좌 별칭 <span class="font-normal text-[#9CA3AF]">(선택)</span></span>
      <input
        v-model.trim="accountAlias"
        maxlength="50"
        placeholder="예: 민수 계좌"
        class="w-full min-h-[52px] rounded-[14px] border-2 border-[#E5E7EB] focus:border-[#F5B800] outline-none px-4 text-[16px] font-bold"
      />
    </label>

    <p v-if="formError" class="rounded-xl bg-[#FEF2F2] p-3 text-[#991B1B]" role="alert">{{ formError }}</p>

    <div class="flex gap-3 pt-2">
      <button
        type="button"
        :disabled="saving"
        @click="emit('cancel')"
        class="flex-1 min-h-[56px] rounded-[18px] border-2 border-[#E5E7EB] text-[16px] font-bold text-[#6B7280] disabled:opacity-50"
      >취소</button>
      <button
        type="submit"
        :disabled="!canSave || saving"
        :class="[
          'flex-1 min-h-[56px] rounded-[18px] text-[16px] font-bold',
          canSave && !saving ? 'bg-[#F5B800] text-[#111827]' : 'bg-[#E5E7EB] text-[#9CA3AF]'
        ]"
      >{{ saving ? '저장 중…' : '저장' }}</button>
    </div>
  </form>
</template>

<script setup>
import { computed, ref } from 'vue';
import { ApiError } from '../../api/httpClient';
import { BANKS, findBankByName } from '../../constants/banks';
import { useAppStore } from '../../stores/appStore';

const props = defineProps({
  existingPerson: { type: Object, default: null },
});
const emit = defineEmits(['saved', 'cancel']);
const store = useAppStore();
const existingAccount = props.existingPerson?.account ?? null;
const matchedBank = BANKS.find((bank) => bank.code === existingAccount?.bankCode)
  ?? findBankByName(existingAccount?.bankName);

const name = ref(props.existingPerson?.name ?? '');
const relationship = ref(props.existingPerson?.relation ?? '');
const bankCode = ref(matchedBank?.code ?? '');
const accountNumber = ref(existingAccount?.accountNumber ?? '');
const accountAlias = ref(existingAccount?.accountAlias ?? '');
const showBanks = ref(false);
const saving = ref(false);
const formError = ref('');
const touched = ref({ name: false, relationship: false, bank: false, account: false });
const selectedBank = computed(() => BANKS.find((bank) => bank.code === bankCode.value) ?? null);
const canSave = computed(() => (
  name.value.length > 0
  && relationship.value.length > 0
  && Boolean(selectedBank.value)
  && /^[0-9-]{8,50}$/.test(accountNumber.value)
));
const fieldErrors = computed(() => ({
  name: touched.value.name && name.value.length === 0 ? '이름을 입력해 주세요.' : '',
  relationship: touched.value.relationship && relationship.value.length === 0
    ? '관계를 입력해 주세요.'
    : '',
  bank: touched.value.bank && !selectedBank.value ? '은행을 선택해 주세요.' : '',
  account: touched.value.account && !/^[0-9-]{8,50}$/.test(accountNumber.value)
    ? '계좌번호는 숫자와 하이픈을 포함해 8~50자로 입력해 주세요.'
    : '',
}));

async function handleSave() {
  if (!canSave.value || saving.value) {
    formError.value = '이름, 관계, 은행, 계좌번호를 모두 확인해 주세요.';
    return;
  }
  saving.value = true;
  formError.value = '';
  try {
    const saved = await store.saveRegisteredPerson({
      name: name.value,
      relationship: relationship.value,
      bankCode: selectedBank.value.code,
      bankName: selectedBank.value.name,
      accountNumber: accountNumber.value,
      accountAlias: accountAlias.value || null,
    }, props.existingPerson?.id ?? null);
    emit('saved', saved.registeredPersonId);
  } catch (error) {
    formError.value = error instanceof ApiError
      ? error.message
      : '등록 정보를 저장하지 못했습니다. 다시 시도해 주세요.';
  } finally {
    saving.value = false;
  }
}
</script>
