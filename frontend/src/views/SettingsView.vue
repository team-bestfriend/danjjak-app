<template>
  <div className="flex flex-col h-full" style="background: #FAFAF8;">
    <SafeArea />
    <div className="bg-white px-5 border-b border-[#EEEEED] flex-shrink-0" style="padding-top: 18px; padding-bottom: 16px;">
      <h1 className="font-bold text-[#111827]" style="font-size: 26px;">설정</h1>
    </div>
    <div className="flex-1 overflow-y-auto px-4 pt-4 pb-4 space-y-5">
      <button
        v-if="store.anomaly"
        type="button"
        @click="store.goBack"
        className="w-full min-h-[52px] rounded-[16px] border-2 border-[#FFBC00] bg-[#FFFBEB] font-bold text-[#92650A]"
      >이상거래 확인 화면으로 돌아가기</button>

      <!-- 사용자 프로필 -->
      <Card className="p-5">
        <div className="flex items-center gap-4">
          <div className="w-16 h-16 rounded-full flex items-center justify-center flex-shrink-0" style="background: #FFF3CC; border: 2px solid #FFBC00; font-size: 32px;">👵</div>
          <div className="flex-1">
            <p className="font-bold text-[#111827]" style="font-size: 23px;">{{ store.userName }}</p>
          </div>
        </div>
      </Card>

      <!-- 화면 설정 -->
      <div>
        <p className="font-bold text-[#9CA3AF] uppercase tracking-wide mb-3 px-1" style="font-size: 13px;">화면 설정</p>
        <Card className="p-5 space-y-5">
          <div>
            <p className="font-semibold text-[#111827] mb-3" style="font-size: 19px;">글씨 크기</p>
            <SegControl
              :options="[{ key: 'small', label: '작게' }, { key: 'normal', label: '보통' }, { key: 'large', label: '크게' }]"
              v-model:value="fontSize"
            />
          </div>
        </Card>
      </div>

      <!-- 음성 안내 설정 -->
      <div>
        <p className="font-bold text-[#9CA3AF] uppercase tracking-wide mb-3 px-1" style="font-size: 13px;">음성 안내</p>
        <Card className="p-5 space-y-5">
          <div>
            <p className="font-semibold text-[#111827] mb-3" style="font-size: 19px;">안내 속도</p>
            <SegControl
              :options="[{ key: 'slow', label: '느리게' }, { key: 'normal', label: '보통' }, { key: 'fast', label: '빠르게' }]"
              v-model:value="guideSpeed"
            />
          </div>
          <div className="border-t border-[#F3F4F6] pt-5">
            <p className="font-semibold text-[#111827] mb-3" style="font-size: 19px;">음성 안내 방식</p>
            <SegControl
              :options="[{ key: 'tts', label: '자동 음성(TTS)' }, { key: 'family', label: '가족 음성' }]"
              v-model:value="voiceMode"
            />
            <p className="text-[#9CA3AF] mt-2 px-1" style="font-size: 13px;">
              {{ voiceMode === 'tts' ? '등록 정보를 기반으로 자동 생성된 음성이 재생됩니다.' : '가족이 직접 녹음한 음성이 재생됩니다.' }}
            </p>
          </div>
          <button
            type="button"
            className="w-full rounded-[16px] bg-[#FFBC00] font-semibold text-[#111827] disabled:opacity-50"
            style="min-height: 56px; font-size: 17px;"
            :disabled="settingsSaving"
            @click="saveSettings"
          >{{ settingsSaving ? '저장 중…' : '화면·음성 설정 저장' }}</button>
          <p v-if="settingsError" className="text-[#B91C1C]" role="alert">{{ settingsError }}</p>
          <p v-if="settingsSaved" className="text-[#166534]" role="status">설정을 저장했어요.</p>
        </Card>
      </div>

      <button
        type="button"
        className="w-full rounded-[18px] border-2 border-[#E5E7EB] bg-white p-4 text-left"
        @click="store.navigate('consent', { query: { edit: '1' } })"
      >
        <span className="font-semibold text-[#111827]" style="font-size: 18px;">선택 동의 관리</span>
        <span className="mt-1 block text-[#6B7280]" style="font-size: 14px;">이용 기록과 카카오 나에게 보내기 동의를 변경해요.</span>
      </button>

      <!-- 보호자 설정 -->
      <div>
        <p className="font-bold text-[#9CA3AF] uppercase tracking-wide mb-3 px-1" style="font-size: 13px;">보호자 설정</p>
        <Card className="p-5 space-y-3">
          <div>
            <p className="font-semibold text-[#111827] mb-1" style="font-size: 19px;">보호자 전화번호 등록</p>
            <p className="text-[#6B7280] mb-3" style="font-size: 15px;">이상 거래 화면의 보호자 전화 연결에 사용해요.</p>
            <p v-if="store.supportLoading" className="text-[#6B7280] mb-3">보호자 번호를 불러오고 있어요…</p>
            <p v-else-if="store.supportLoaded && !store.support?.guardian?.phoneNumber" className="text-[#6B7280] mb-3">등록된 보호자 번호가 없어요. 아래에 번호를 입력해 주세요.</p>
            <p v-if="store.support?.guardian?.phoneNumber" class="break-all text-[20px]">저장된 번호: {{ store.support.guardian.phoneNumber }}</p>
            <button v-if="store.supportError" type="button" class="min-h-[48px] underline" :disabled="store.supportLoading" @click="store.loadSupport(true)">번호 다시 불러오기</button>
            <div className="flex gap-2 w-full">
              <input
                v-model="guardianPhone"
                @input="editGuardian"
                :aria-invalid="Boolean(guardianError || store.guardianSaveError)"
                aria-describedby="guardian-phone-help"
                :disabled="store.guardianSaving"
                type="tel"
                aria-label="보호자 전화번호"
                placeholder="010-0000-0000"
                inputMode="tel"
                className="flex-1 min-w-0 rounded-[14px] border-2 border-[#E5E7EB] focus:border-[#FFBC00] outline-none px-4 font-bold text-[#111827] placeholder:text-[#D1D5DB]"
                style="min-height: 54px; font-size: 17px;"
              />
              <button
                :disabled="store.guardianSaving"
                @click="saveGuardian"
                className="rounded-[14px] bg-[#FFBC00] text-[#111827] font-semibold flex-shrink-0 px-5 disabled:opacity-50"
                style="height: 54px; font-size: 17px;"
              >{{ store.guardianSaving ? '저장 중…' : '저장' }}</button>
            </div>
            <p id="guardian-phone-help" class="text-[16px] mt-2">숫자와 숫자 묶음 사이 하이픈으로 5~30자를 입력해 주세요. 카카오톡은 로그인한 본인에게 보내는 시연이며 이 번호를 수신자로 사용하지 않아요.</p>
            <p v-if="guardianError || store.guardianSaveError || store.supportError" className="text-[#B91C1C] mt-2" role="alert">{{ guardianError || store.guardianSaveError || store.supportError }}</p>
            <p v-if="guardianSaved" role="status" className="text-[#166534] mt-2">저장한 번호를 보호자 전화 연결에 반영했어요.</p>
          </div>
        </Card>
      </div>

      <section aria-labelledby="owned-accounts-title" class="space-y-3">
        <h2 id="owned-accounts-title" class="text-[22px] font-bold">내 계좌 관리</h2>
        <p v-if="store.ownedAccountsLoading" role="status">내 계좌를 불러오고 있어요.</p>
        <div v-else-if="store.ownedAccountsError" class="space-y-3">
          <p role="alert" class="text-[#B91C1C]">{{ store.ownedAccountsError }}</p>
          <Btn variant="secondary" @click="store.loadOwnedAccounts(true)">다시 시도</Btn>
        </div>
        <template v-else-if="store.ownedAccountsLoaded">
          <p v-if="!store.ownedAccounts.length">아직 불러온 내 계좌가 없어요. 연습용 계좌를 불러와 주세요.</p>
          <template v-else>
            <p v-if="store.financeWarning" role="status">{{ store.financeWarning }}</p>
            <fieldset :disabled="store.accountSaving" class="space-y-3">
              <legend class="mb-3 text-[20px] font-semibold">기본으로 사용할 내 계좌</legend>
              <label v-for="account in store.ownedAccounts" :key="account.accountId"
                class="flex min-h-[112px] items-start gap-3 rounded-[20px] border-2 bg-white p-4"
                :class="store.defaultAccountSelection === account.accountId ? 'border-[#FFBC00]' : 'border-[#E5E7EB]'">
                <input v-model="store.defaultAccountSelection" type="radio" name="default-account" :value="account.accountId"
                  :disabled="store.accountSaving" class="mt-1 h-6 w-6 shrink-0" @change="defaultSaved = false" />
                <span class="min-w-0 break-words text-[16px]">
                  <span class="block text-[20px] font-bold">{{ account.bankName }} <span v-if="account.primary">· 기본</span></span>
                  <span v-if="account.accountAlias" class="block">{{ account.accountAlias }}</span>
                  <span class="block">{{ account.maskedAccountNumber }}</span>
                  <span class="block">잔액 {{ account.balance.toLocaleString('ko-KR') }}원</span>
                  <span v-if="store.defaultAccountSelection === account.accountId" class="block font-bold">✓ 선택됨</span>
                </span>
              </label>
            </fieldset>
            <p class="text-[16px]">이번 송금에서 고른 계좌는 저장된 기본 계좌를 바꾸지 않아요.</p>
            <Btn :disabled="store.accountSaving || !store.defaultAccountSelection" @click="saveDefault">
              {{ store.accountSaving ? '저장 중…' : '기본 계좌 저장' }}
            </Btn>
            <p v-if="defaultSaved" role="status">기본 계좌를 저장했어요.</p>
          </template>
        </template>
        <p v-if="store.accountSaveError" role="alert" class="text-[#B91C1C]">{{ store.accountSaveError }}</p>
        <Btn variant="secondary" @click="store.navigate('owned-account-import')">계좌 불러오기</Btn>
      </section>

      <!-- 계좌/사람 관리 -->
      <div>
        <p className="font-bold text-[#9CA3AF] uppercase tracking-wide mb-3 px-1" style="font-size: 13px;">계좌 관리</p>
        <Card className="overflow-hidden">
          <button
            type="button"
            className="w-full flex items-center justify-between px-5 py-4 text-left active:bg-[#F9FAFB]"
            @click="store.navigate('contact-manage')"
          >
            <div className="flex items-center gap-3">
              <div className="w-11 h-11 rounded-full bg-[#FFF3CC] border border-[#FFBC00] flex items-center justify-center" style="font-size: 22px;">👨‍👩‍👧</div>
              <div>
                <p className="font-semibold text-[#111827]" style="font-size: 19px;">사람 및 계좌 관리</p>
                <p className="text-[#9CA3AF]" style="font-size: 14px;">가족 {{ store.people.length }}명 · 송금 계좌 관리</p>
              </div>
            </div>
            <Ic name="ChevR" />
          </button>
        </Card>
      </div>

      <!-- 서비스 -->
      <div>
        <p className="font-bold text-[#9CA3AF] uppercase tracking-wide mb-3 px-1" style="font-size: 13px;">서비스</p>
        <Card className="overflow-hidden">
          <button
            v-for="(item, i) in serviceItems"
            :key="item.label"
            :disabled="!item.screen"
            @click="item.screen && store.navigate(item.screen)"
            :class="['w-full flex items-center justify-between px-5 py-4 text-left active:bg-[#F9FAFB] disabled:cursor-not-allowed disabled:opacity-60', i > 0 ? 'border-t border-[#F3F4F6]' : '']"
          >
            <div className="flex items-center gap-3">
              <span style="font-size: 22px;">{{ item.icon }}</span>
              <span className="font-normal text-[#111827]" style="font-size: 18px;">{{ item.label }}{{ item.screen ? '' : ' (준비 중)' }}</span>
            </div>
            <Ic name="ChevR" />
          </button>
        </Card>
      </div>

      <!-- 로그아웃 -->
      <button
        type="button"
        :disabled="store.logoutPending"
        className="w-full rounded-[18px] border-2 border-[#E5E7EB] bg-white font-medium text-[#6B7280] disabled:opacity-50"
        style="padding: 18px; font-size: 17px;"
        @click="store.logout"
      >
        {{ store.logoutPending ? '로그아웃 중…' : '로그아웃' }}
      </button>
      <p v-if="store.authError" role="alert" class="text-[#B91C1C]">{{ store.authError }}</p>
    </div>

    <NavBar active="settings" :onSelect="store.navTo" />
  </div>
</template>

<script setup>
import { onMounted, onBeforeUnmount, ref, watch } from 'vue';
import { isPhoneNumber } from '../features/support/contact.js';
import { useAppStore } from '../stores/appStore';
import SafeArea from '../components/common/SafeArea.vue';
import Card from '../components/common/Card.vue';
import Ic from '../components/common/Ic.vue';
import NavBar from '../components/common/NavBar.vue';
import SegControl from '../components/common/SegControl.vue';
import Btn from '../components/common/Btn.vue';

const store = useAppStore();

const fontSize = ref("normal");
const guideSpeed = ref("normal");
const voiceMode = ref("tts");
const settingsSaving = ref(false);
const settingsError = ref("");
const settingsSaved = ref(false);
const guardianPhone = ref(store.support?.guardian?.phoneNumber ?? '');
const guardianDirty = ref(false);
let active = true;
onBeforeUnmount(() => { active = false; });
watch(() => store.supportSessionVersion, () => {
  guardianPhone.value = '';
  guardianDirty.value = false;
  guardianError.value = '';
  guardianSaved.value = false;
}, { flush: 'sync' });
watch(() => store.support?.guardian?.phoneNumber, (phone) => {
  if (!guardianDirty.value) guardianPhone.value = phone ?? '';
});
function editGuardian() {
  guardianDirty.value = true;
  guardianSaved.value = false;
  guardianError.value = '';
  store.guardianSaveError = '';
}
const guardianError = ref("");
const guardianSaved = ref(false);
const defaultSaved = ref(false);

async function saveDefault() {
  defaultSaved.value = false;
  defaultSaved.value = await store.saveDefaultAccount();
}

const serviceItems = [
  { label: "서비스 이용방법", icon: "📖", screen: 'service-help' },
  { label: "고객센터 연결", icon: "📞", screen: "task-6" },
  { label: "개인정보 처리방침", icon: "🔒", screen: null }
];

onMounted(async () => {
  const settings = store.currentUser?.settings;
  fontSize.value = String(settings?.fontSize ?? 'NORMAL').toLowerCase();
  guideSpeed.value = String(settings?.voiceSpeed ?? 'NORMAL').toLowerCase();
  voiceMode.value = String(settings?.guideVoiceType ?? 'TTS').toLowerCase();
  await Promise.all([store.loadSupport(), store.loadFinancialData(), store.loadOwnedAccounts(true)]);
});

async function saveSettings() {
  if (settingsSaving.value) return;
  settingsSaving.value = true;
  settingsError.value = '';
  settingsSaved.value = false;
  try {
    await store.saveAccessibilitySettings({
      fontSize: fontSize.value.toUpperCase(),
      voiceSpeed: guideSpeed.value.toUpperCase(),
      guideVoiceType: voiceMode.value.toUpperCase(),
    });
    settingsSaved.value = true;
  } catch (error) {
    settingsError.value = error?.message ?? '설정을 저장하지 못했습니다. 다시 시도해 주세요.';
  } finally {
    settingsSaving.value = false;
  }
}

async function saveGuardian() {
  if (store.guardianSaving) return;
  guardianError.value = '';
  guardianSaved.value = false;
  if (!isPhoneNumber(guardianPhone.value)) {
    guardianError.value = '전화번호는 5~30자로, 숫자 묶음 사이에 하이픈 하나만 입력해 주세요.';
    return;
  }
  const version = store.supportSessionVersion;
  const draft = guardianPhone.value;
  try {
    const saved = await store.saveGuardian(draft);
    if (!active || version !== store.supportSessionVersion || !saved) return;
    if (guardianPhone.value === draft) {
      guardianPhone.value = saved.phoneNumber;
      guardianDirty.value = false;
      guardianSaved.value = true;
    }
  } catch {
    // 오류 시 입력 초안을 유지하고 저장소의 오류 문구를 표시한다.
  }
}
</script>
