<template>
  <div v-if="step === 2" class="flex flex-col h-full" style="background: #FAFAF8;">
    <SafeArea />
    <TopBar title="회원가입" :onBack="() => step = 1" />
    <div class="flex-1 flex flex-col px-6 pt-6 pb-6 space-y-6">
      <div>
        <p class="font-bold text-[#111827]" style="font-size: 28px;">비밀번호를 만들어주세요</p>
        <p class="text-[#6B7280] mt-2" style="font-size: 16px;">숫자와 영문을 포함해 6자 이상 입력해주세요.</p>
      </div>
      <div class="space-y-2">
        <p class="font-bold text-[#374151] px-1" style="font-size: 17px;">비밀번호</p>
        <div class="relative">
          <input
            :type="pwVisible ? 'text' : 'password'"
            v-model="pw"
            placeholder="6자 이상 입력"
            class="w-full rounded-[18px] border-2 border-[#E5E7EB] focus:border-[#FFBC00] outline-none px-5 pr-16 text-[#111827] placeholder:text-[#D1D5DB] placeholder:font-normal"
            style="min-height: 62px; font-size: 20px; font-weight: 700;"
          />
          <button
            @click="pwVisible = !pwVisible"
            class="absolute right-4 top-1/2 -translate-y-1/2 font-bold text-[#9CA3AF] px-2 py-1"
            style="font-size: 14px;"
          >
            {{ pwVisible ? '숨기기' : '보기' }}
          </button>
        </div>
      </div>
      <Btn :disabled="pw.length < 6" @click="store.navigate('home')">가입 완료</Btn>
    </div>
  </div>

  <div v-else class="flex flex-col h-full" style="background: #FAFAF8;">
    <SafeArea />
    <TopBar title="회원가입" :onBack="store.goBack" />
    <div class="flex-1 flex flex-col px-6 pt-6 pb-6 space-y-6">
      <div>
        <p class="font-bold text-[#111827]" style="font-size: 28px;">반갑습니다!</p>
        <p class="text-[#6B7280] mt-2" style="font-size: 16px;">이름과 전화번호를 입력해주세요.</p>
      </div>
      <div class="space-y-5">
        <div class="space-y-2">
          <p class="font-bold text-[#374151] px-1" style="font-size: 17px;">이름</p>
          <input
            v-model="name"
            placeholder="예: 김순자"
            class="w-full rounded-[18px] border-2 border-[#E5E7EB] focus:border-[#FFBC00] outline-none px-5 text-[#111827] placeholder:text-[#D1D5DB] placeholder:font-normal"
            style="min-height: 62px; font-size: 20px; font-weight: 700;"
          />
        </div>
        <div class="space-y-2">
          <p class="font-bold text-[#374151] px-1" style="font-size: 17px;">전화번호</p>
          <input
            type="tel"
            v-model="phone"
            @input="phone = formatPhone($event.target.value)"
            placeholder="010-0000-0000"
            inputMode="tel"
            class="w-full rounded-[18px] border-2 border-[#E5E7EB] focus:border-[#FFBC00] outline-none px-5 text-[#111827] placeholder:text-[#D1D5DB] placeholder:font-normal"
            style="min-height: 62px; font-size: 20px; font-weight: 700;"
          />
        </div>
      </div>
      <Btn :disabled="!name.trim() || phone.length < 12" @click="step = 2">다음</Btn>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useAppStore } from '../stores/appStore';
import SafeArea from '../components/common/SafeArea.vue';
import TopBar from '../components/common/TopBar.vue';
import Btn from '../components/common/Btn.vue';

const store = useAppStore();
const step = ref(1);
const name = ref('');
const phone = ref('');
const pw = ref('');
const pwVisible = ref(false);

function formatPhone(v) {
  const d = v.replace(/\D/g, "").slice(0, 11);
  if (d.length <= 3) return d;
  if (d.length <= 7) return `${d.slice(0, 3)}-${d.slice(3)}`;
  return `${d.slice(0, 3)}-${d.slice(3, 7)}-${d.slice(7)}`;
}
</script>
