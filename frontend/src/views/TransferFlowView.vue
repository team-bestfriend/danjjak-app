<template>
  <!-- 출금 계좌 선택 -->
  <div v-if="flowStep === 'transfer-source'" class="flex flex-col h-full bg-[#FAFAF8]">
    <SafeArea />
    <TopBar title="출금 계좌 선택" :onBack="store.goBack" rightLabel="취소" :onRight="store.cancelTransfer" />
    <StepBar :current="1" :total="6" />
    <div class="flex-1 overflow-y-auto px-4 pt-4 pb-6 space-y-4">
      <p class="font-bold text-[#111827] text-[26px]">어느 계좌에서 보낼까요?</p>
      <p v-if="store.transferError" class="rounded-xl bg-[#FEF2F2] p-3 text-[#991B1B]" role="alert">{{ store.transferError }}</p>
      <p v-if="store.financeWarning" class="rounded-xl border border-[#FDE68A] bg-[#FFFBEB] p-3 text-[#92400E]" role="status">{{ store.financeWarning }}</p>
      <p v-if="store.financeLoading" class="rounded-2xl bg-white p-5 text-[#6B7280]">계좌를 불러오고 있어요…</p>
      <div v-else-if="store.financeError" class="rounded-2xl border border-[#FCA5A5] bg-[#FEF2F2] p-5 space-y-3">
        <p class="text-[#991B1B]">{{ store.financeError }}</p>
        <Btn variant="secondary" @click="store.loadFinancialData(true)">다시 시도</Btn>
      </div>
      <div v-else-if="store.ownedAccounts.length === 0" class="rounded-2xl bg-white p-5 text-center space-y-2">
        <p class="font-bold text-[#111827] text-[19px]">사용할 수 있는 본인 계좌가 없어요.</p>
        <p class="text-[#6B7280]">계좌가 준비된 뒤 송금을 시작해 주세요.</p>
      </div>
      <div v-else class="space-y-3">
        <button
          v-for="account in store.ownedAccounts"
          :key="account.accountId"
          :data-guide-target="store.selectedSourceAccountId === account.accountId ? 'source-account-list' : null"
          data-guide-exempt
          @click="store.selectedSourceAccountId = account.accountId"
          :class="[
            'w-full rounded-[20px] border-2 bg-white p-5 text-left min-h-[112px]',
            store.selectedSourceAccountId === account.accountId ? 'border-[#FFBC00]' : 'border-[#E5E7EB]'
          ]"
        >
          <div class="flex items-center justify-between gap-3">
            <div>
              <p class="font-bold text-[#111827] text-[19px]">{{ account.accountAlias || account.bankName }}</p>
              <p class="text-[#6B7280] mt-1">{{ account.bankName }} · {{ account.masked }}</p>
            </div>
            <span v-if="account.primary" class="rounded-full bg-[#FFF3CC] px-3 py-1 text-sm font-bold text-[#92650A]">기본</span>
          </div>
          <p class="font-black text-[#111827] text-[23px] mt-3">{{ formatWon(account.balance) }}</p>
        </button>
      </div>
      <div v-if="patternTargetMissing" class="rounded-2xl border border-[#FCA5A5] bg-[#FEF2F2] p-4 space-y-3">
        <p class="font-bold text-[#991B1B]">단축번호에 연결된 받는 사람이나 계좌를 찾을 수 없어요.</p>
        <p class="text-[#991B1B]">사람 및 계좌 관리에서 연결 정보를 먼저 확인해 주세요.</p>
        <Btn data-guide-exempt variant="secondary" @click="store.navigate('contact-manage')">사람 및 계좌 관리</Btn>
      </div>
      <Btn
        data-guide-exempt
        :disabled="!store.selectedSourceAccountId || store.financeLoading || Boolean(store.financeError) || patternTargetMissing"
        @click="proceedFromSource"
      >다음</Btn>
    </div>
  </div>

  <!-- 수취 방식 선택 -->
  <div v-else-if="flowStep === 'direct-transfer'" class="flex flex-col h-full bg-[#FAFAF8]">
    <SafeArea />
    <TopBar title="받는 방법 선택" :onBack="store.goBack" rightLabel="취소" :onRight="store.cancelTransfer" />
    <StepBar :current="2" :total="6" />
    <div class="flex-1 flex flex-col overflow-y-auto px-5 pt-8 pb-6 gap-5">
      <p class="font-bold text-[#111827] text-[28px]">누구에게 보내시겠어요?</p>
      <div class="rounded-[28px] p-2 flex flex-col gap-3 bg-white">
        <button
          @click="selectFamily"
          class="w-full rounded-[20px] bg-white border border-[#FFBC00] p-6 flex flex-col items-center gap-3 active:scale-[0.97] transition-all"
        >
          <div class="w-16 h-16 rounded-full bg-[#FFBC00] flex items-center justify-center text-[32px]">👨‍👩‍👧</div>
          <p class="font-bold text-[#111827] text-[21px]">등록된 가족에게 보내기</p>
          <p class="text-[#6B7280] text-center text-[15px]">미리 등록해 둔 가족 계좌로 보내요.</p>
        </button>
        <button
          @click="store.navigate('direct-newaccount')"
          class="w-full rounded-[20px] bg-white border border-[#E5E7EB] p-6 flex flex-col items-center gap-3 active:scale-[0.97] transition-all"
        >
          <div class="w-16 h-16 rounded-full bg-[#374151] flex items-center justify-center text-white"><Ic name="Transfer" /></div>
          <p class="font-bold text-[#111827] text-[21px]">새 계좌로 보내기</p>
          <p class="text-[#6B7280] text-center text-[15px]">계좌 번호를 직접 입력해서 보내요.</p>
        </button>
      </div>
    </div>
  </div>

  <!-- 직접 수취 계좌 입력 -->
  <div v-else-if="flowStep === 'direct-newaccount'" class="flex flex-col h-full bg-[#FAFAF8]">
    <SafeArea />
    <TopBar title="새 계좌로 송금" :onBack="store.goBack" rightLabel="취소" :onRight="store.cancelTransfer" />
    <StepBar :current="3" :total="6" />
    <div class="flex-1 overflow-y-auto px-4 pt-5 pb-6 space-y-5">
      <p class="font-bold text-[#111827] text-[26px]">받는 계좌를 입력해 주세요.</p>
      <label class="block space-y-2">
        <span class="font-bold text-[#374151] text-[17px]">받는 분 이름</span>
        <input
          id="direct-recipient-name"
          v-model.trim="recipientName"
          :aria-invalid="Boolean(directFieldErrors.name)"
          aria-describedby="direct-recipient-name-error"
          @blur="directTouched.name = true"
          maxlength="100"
          placeholder="예: 박친구"
          class="w-full min-h-[58px] rounded-[16px] border-2 border-[#E5E7EB] focus:border-[#FFBC00] outline-none px-4 text-[18px] font-bold"
        />
        <p v-if="directFieldErrors.name" id="direct-recipient-name-error" class="text-[14px] text-[#B91C1C]" role="alert">
          {{ directFieldErrors.name }}
        </p>
      </label>
      <div class="space-y-2">
        <p class="font-bold text-[#374151] text-[17px]">은행 선택</p>
        <button
          :aria-invalid="Boolean(directFieldErrors.bank)"
          aria-describedby="direct-recipient-bank-error"
          @click="directTouched.bank = true; showBanks = !showBanks"
          :class="[
            'w-full rounded-[16px] border-2 px-4 text-left flex items-center justify-between min-h-[58px] text-[18px] font-bold',
            selectedBank ? 'border-[#FFBC00] text-[#111827]' : 'border-[#E5E7EB] text-[#9CA3AF]'
          ]"
        >
          <span>{{ selectedBank?.name || '은행 선택' }}</span>
          <Ic name="ChevR" />
        </button>
        <p v-if="directFieldErrors.bank" id="direct-recipient-bank-error" class="text-[14px] text-[#B91C1C]" role="alert">
          {{ directFieldErrors.bank }}
        </p>
        <div v-if="showBanks" class="grid grid-cols-2 gap-2 pt-1">
          <button
            v-for="bankOption in BANKS"
            :key="bankOption.code"
            @click="bankCode = bankOption.code; showBanks = false"
            :class="[
              'rounded-[12px] border-2 font-bold h-[52px]',
              bankCode === bankOption.code ? 'border-[#FFBC00] bg-[#FFFBEB] text-[#92650A]' : 'border-[#E5E7EB] text-[#374151]'
            ]"
          >{{ bankOption.name }}</button>
        </div>
      </div>
      <label class="block space-y-2">
        <span class="font-bold text-[#374151] text-[17px]">계좌 번호</span>
        <input
          id="direct-recipient-account"
          type="tel"
          :value="accountNumber"
          :aria-invalid="Boolean(directFieldErrors.account)"
          aria-describedby="direct-recipient-account-help direct-recipient-account-error"
          @input="accountNumber = $event.target.value.replace(/[^0-9-]/g, '')"
          @blur="directTouched.account = true"
          maxlength="50"
          placeholder="000-00-000000"
          inputmode="numeric"
          class="w-full min-h-[58px] rounded-[16px] border-2 border-[#E5E7EB] focus:border-[#FFBC00] outline-none px-4 text-[20px] font-bold"
        />
        <p id="direct-recipient-account-help" class="text-[14px] text-[#6B7280]">숫자와 하이픈을 포함해 8자 이상 입력해 주세요.</p>
        <p v-if="directFieldErrors.account" id="direct-recipient-account-error" class="text-[14px] text-[#B91C1C]" role="alert">
          {{ directFieldErrors.account }}
        </p>
      </label>
      <p v-if="directInputError" class="rounded-xl bg-[#FEF2F2] p-3 text-[#991B1B]">{{ directInputError }}</p>
      <p v-else-if="store.transferError" class="rounded-xl bg-[#FEF2F2] p-3 text-[#991B1B]" role="alert">{{ store.transferError }}</p>
      <div class="bg-[#FFF7ED] border border-[#FED7AA] rounded-[16px] p-4 flex items-start gap-2">
        <Ic name="Warning" />
        <p class="text-[#92400E] flex-1 text-[15px]">입력한 계좌는 이번 송금에만 사용되고 등록 목록에는 저장되지 않아요.</p>
      </div>
      <Btn :disabled="!canProceedDirect" @click="proceedNewAccount">다음</Btn>
    </div>
  </div>

  <!-- 등록 수취인 선택 -->
  <div v-else-if="flowStep === 'guide-person'" class="flex flex-col h-full bg-[#FAFAF8]">
    <SafeArea />
    <TopBar title="받는 사람 선택" :onBack="store.goBack" rightLabel="취소" :onRight="store.cancelTransfer" />
    <StepBar :current="3" :total="6" />
    <div class="flex-1 overflow-y-auto px-4 pt-4 pb-6 space-y-4">
      <p class="font-bold text-[#111827] text-[26px]">보낼 사람을 선택해 주세요.</p>
      <p v-if="store.transferError" class="rounded-xl bg-[#FEF2F2] p-3 text-[#991B1B]" role="alert">{{ store.transferError }}</p>
      <p v-if="store.financeLoading" class="rounded-2xl bg-white p-5 text-[#6B7280]">등록된 사람을 불러오고 있어요…</p>
      <div v-else-if="store.financeError" class="rounded-2xl border border-[#FCA5A5] bg-[#FEF2F2] p-5 space-y-3">
        <p class="text-[#991B1B]">{{ store.financeError }}</p>
        <Btn variant="secondary" @click="store.loadFinancialData(true)">다시 시도</Btn>
      </div>
      <div v-else-if="store.people.length === 0" class="rounded-2xl bg-white p-5 text-center space-y-3">
        <p class="font-bold text-[#111827]">등록된 사람이 없어요.</p>
        <Btn variant="secondary" @click="store.navigate('direct-newaccount')">새 계좌 직접 입력</Btn>
      </div>
      <div v-else class="space-y-3 rounded-[24px] p-2 bg-white">
        <button
          v-for="person in store.people"
          :key="person.id"
          :data-guide-target="person.id === store.activePattern?.personId ? 'registered-person-list' : null"
          @click="handleSelectFamilyPerson(person.id)"
          class="w-full text-left"
        >
          <Card class="p-5">
            <div class="flex items-center gap-4">
              <div class="w-14 h-14 rounded-full bg-[#FFF3CC] border border-[#FFBC00] flex items-center justify-center text-[28px]">{{ person.emoji }}</div>
              <div class="flex-1 min-w-0">
                <p class="font-bold text-[#111827] text-[21px]">{{ person.name }}</p>
                <p class="text-[#6B7280] text-[15px]">{{ person.relation }} · 수취 계좌 {{ getAccCount(person.id) }}개</p>
              </div>
              <span v-if="store.selectedPersonId === person.id" class="font-bold text-[#92650A]">선택됨</span>
              <Ic v-else name="ChevR" />
            </div>
          </Card>
        </button>
      </div>
    </div>
  </div>

  <!-- 등록 수취 계좌 선택 -->
  <div v-else-if="flowStep === 'guide-account'" class="flex flex-col h-full bg-[#FAFAF8]">
    <SafeArea />
    <TopBar title="받는 계좌 선택" :onBack="store.goBack" rightLabel="취소" :onRight="store.cancelTransfer" />
    <StepBar :current="3" :total="6" />
    <div class="flex-1 overflow-y-auto px-4 pt-4 pb-6 space-y-4">
      <p class="font-bold text-[#111827] text-[26px]">보낼 계좌를 선택해 주세요.</p>
      <div v-if="personAccs.length" class="space-y-3 rounded-[24px] p-2 bg-white">
        <button v-for="account in personAccs" :key="account.accountId" :data-guide-target="account.accountId === store.activePattern?.recipientAccountId ? 'recipient-account-list' : null" @click="handleSelectAccount(account)" class="w-full text-left">
          <Card class="p-5">
            <div class="flex items-center gap-4">
              <div class="w-12 h-12 rounded-[14px] flex items-center justify-center font-black bg-[#FFBC00] text-[#111827] text-[12px]">
                {{ account.bankName?.slice(0, 2) }}
              </div>
              <div class="flex-1">
                <p class="font-bold text-[#111827] text-[18px]">{{ account.bankName }}</p>
                <p class="font-mono text-[#374151] text-[14px]">{{ account.masked }}</p>
              </div>
              <Ic name="ChevR" />
            </div>
          </Card>
        </button>
      </div>
      <div v-else class="rounded-2xl bg-white p-5 text-center space-y-3">
        <p class="font-bold text-[#111827]">등록된 수취 계좌가 없어요.</p>
        <Btn variant="secondary" @click="store.navigate('contact-manage')">계좌 관리로 이동</Btn>
      </div>
    </div>
  </div>

  <!-- 금액 입력 -->
  <div v-else-if="flowStep === 'amount-input'" class="relative flex flex-col h-full bg-[#FAFAF8]">
    <SafeArea />
    <TopBar title="얼마를 보낼까요?" :onBack="store.goBack" rightLabel="취소" :onRight="store.cancelTransfer" />
    <StepBar :current="4" :total="6" />
    <div class="flex-1 overflow-y-auto px-4 pt-4 pb-6 space-y-3">
      <p v-if="store.transferError" class="rounded-xl bg-[#FEF2F2] p-3 text-[#991B1B]" role="alert">{{ store.transferError }}</p>
      <AmountKeypad data-guide-target="amount-keypad" role="group" aria-label="송금 금액 입력" :initialValue="store.transferAmount" @complete="handleAmountComplete" />
    </div>
  </div>

  <!-- 송금 내용 확인 -->
  <div v-else-if="flowStep === 'final-confirm'" class="flex flex-col h-full bg-[#FAFAF8]">
    <SafeArea />
    <TopBar title="보내기 전에 확인해 주세요" :onBack="store.goBack" rightLabel="취소" :onRight="store.cancelTransfer" />
    <StepBar :current="5" :total="6" />
    <div class="flex-1 overflow-y-auto px-4 pt-4 pb-6 space-y-4">
      <Card class="overflow-hidden">
        <div
          v-for="(row, index) in reviewRows"
          :key="row.label"
          :class="['flex items-center justify-between gap-4 px-5 py-4', index < reviewRows.length - 1 ? 'border-b border-[#F3F4F6]' : '']"
        >
          <span class="text-[#6B7280] text-[16px]">{{ row.label }}</span>
          <span :class="['font-bold text-right text-[#111827]', row.emphasis ? 'text-[26px]' : 'text-[17px]']">{{ row.value }}</span>
        </div>
      </Card>
      <Btn v-if="store.anomaly" @click="returnToWarning">경고 화면으로 돌아가기</Btn>
      <Btn v-else data-guide-target="transfer-summary" @click="store.navigate('pin-entry')">확인했어요</Btn>
      <Btn data-guide-exempt variant="secondary" @click="store.goBack">내용 수정하기</Btn>
    </div>
  </div>

  <!-- PIN 입력 및 서버 송금 요청 -->
  <div v-else-if="flowStep === 'pin-entry'" class="relative flex flex-col h-full bg-[#FAFAF8]">
    <SafeArea />
    <TopBar
      title="계좌 비밀번호 입력"
      :onBack="store.goBack"
      :backDisabled="store.transferSubmitting"
      rightLabel="취소"
      :onRight="store.cancelTransfer"
      :rightDisabled="store.transferSubmitting"
    />
    <StepBar :current="6" :total="6" />
    <div class="flex-1 overflow-y-auto px-4 pt-5 pb-6 space-y-5">
      <p class="text-[#374151] text-center text-[17px]">선택한 출금 계좌의 비밀번호 4자리를<br />직접 입력해 주세요.</p>
      <div class="bg-[#FFFBEB] border border-[#FFBC00] rounded-2xl px-4 py-3 flex items-center gap-2 text-[#92650A]">
        <Ic name="Shield" />
        <p class="font-bold text-[15px]">비밀번호는 저장하거나 기록하지 않아요.</p>
      </div>
      <p v-if="store.transferError" class="rounded-xl bg-[#FEF2F2] p-3 text-[#991B1B]" role="alert">{{ store.transferError }}</p>
      <p v-if="store.transferSubmitting" class="text-center font-bold text-[#6B7280]">안전하게 확인하고 있어요…</p>
      <PinEntry data-guide-target="pin-keypad" role="group" aria-label="계좌 비밀번호 입력" :disabled="store.transferSubmitting" @complete="handlePinComplete" />
    </div>
  </div>

  <!-- 서버 FDS 경고 -->
  <div v-else-if="flowStep === 'fraud-warning'" class="flex flex-col h-full bg-[#FAFAF8]">
    <SafeArea />
    <TopBar
      title="잠깐, 확인이 필요해요"
      rightLabel="취소"
      :onRight="cancelAnomaly"
      :rightDisabled="store.anomalyResolving"
    />
    <div class="flex-1 overflow-y-auto px-4 pt-4 pb-6 space-y-4">
      <template v-if="store.anomaly">
      <div class="flex flex-col items-center gap-3 py-2">
        <div class="rounded-full bg-[#FFF0F0] border-2 border-[#FECACA] flex items-center justify-center text-[#EF4444] w-[72px] h-[72px]">
          <Ic name="Warning" />
        </div>
        <span class="rounded-full bg-[#7F1D1D] px-4 py-2 text-white font-black">{{ riskLabel }}</span>
        <p class="text-[#374151] text-center text-[17px]">서버가 아래 이상 징후를 확인했어요.</p>
      </div>
      <div class="bg-[#FFF7ED] border border-[#FED7AA] rounded-[20px] p-4 space-y-3">
        <div v-for="reason in fraudReasons" :key="reason" class="flex items-start gap-2">
          <span class="w-2 h-2 mt-2 rounded-full bg-[#F97316] flex-shrink-0" />
          <span class="text-[#9A3412] text-[16px]">{{ reason }}</span>
        </div>
        <p class="border-t border-[#FED7AA] pt-3 text-[#9A3412] text-[15px]">
          최근 10분간 완료된 송금 {{ store.anomaly.recentTransferCount }}건
        </p>
      </div>
      <Card class="p-5 space-y-2">
        <p class="text-[#6B7280]">받는 분</p>
        <p class="font-bold text-[#111827] text-[20px]">{{ store.anomaly?.recipient?.name }}</p>
        <p class="text-[#6B7280]">{{ store.anomaly?.recipient?.bankName }} · {{ store.anomaly?.recipient?.masked }}</p>
        <p class="font-black text-[#111827] text-[28px]">{{ formatWon(store.anomaly?.amount) }}</p>
      </Card>
      <p v-if="store.transferError" class="rounded-xl bg-[#FEF2F2] p-3 text-[#991B1B]" role="alert">{{ store.transferError }}</p>
      <div v-if="store.notificationResult" class="rounded-2xl border border-[#93C5FD] bg-[#EFF6FF] p-4">
        <p class="font-bold text-[#1E3A8A]">{{ notificationTitle }}</p>
        <p class="text-[#1E40AF] mt-1">{{ store.notificationResult.detail }}</p>
        <p v-if="store.notificationResult.result === 'SENT' && store.notificationResult.sentAt" class="text-[#1E40AF] mt-1">
          발송 시각 {{ formatDate(store.notificationResult.sentAt) }}
        </p>
      </div>
      <div class="space-y-3">
        <Btn
          v-if="store.anomaly?.riskLevel === 'HIGH' && guardianShareAgreed"
          variant="danger"
          :disabled="store.notificationSending || Boolean(store.notificationResult)"
          @click="notifyGuardian"
        >{{ notificationButtonLabel }}</Btn>
        <div
          v-else-if="store.anomaly?.riskLevel === 'HIGH'"
          class="rounded-2xl border border-[#FCD34D] bg-[#FFFBEB] p-4 space-y-3"
        >
          <p class="font-bold text-[#92400E]">보호자 공유 동의가 꺼져 있어 카카오 알림을 보내지 않아요.</p>
          <Btn variant="secondary" @click="store.navigate('consent', { query: { edit: '1' } })">
            동의 설정 변경하기
          </Btn>
        </div>
        <p v-if="store.supportLoading" class="rounded-2xl bg-white p-4 text-center text-[#6B7280]">보호자 번호를 불러오고 있어요…</p>
        <div v-else-if="store.supportError" class="rounded-2xl border border-[#FCA5A5] bg-[#FEF2F2] p-4 space-y-3">
          <p class="text-[#991B1B]" role="alert">{{ store.supportError }}</p>
          <Btn variant="secondary" @click="store.loadSupport(true)">번호 다시 불러오기</Btn>
        </div>
        <a
          v-else-if="guardianPhone"
          :href="'tel:' + guardianPhone"
          class="flex min-h-[56px] w-full items-center justify-center rounded-[18px] border-2 border-[#EF4444] font-bold text-[#B91C1C]"
        >보호자에게 전화하기 · {{ guardianPhone }}</a>
        <div v-else class="rounded-2xl border border-[#E5E7EB] bg-white p-4 space-y-3 text-center">
          <p class="font-bold text-[#6B7280]">보호자 번호가 등록되지 않았어요.</p>
          <Btn variant="secondary" @click="store.navigate('settings')">보호자 번호 등록하기</Btn>
        </div>
        <Btn variant="secondary" :disabled="store.anomalyResolving" @click="recheckTransfer">거래 정보 다시 확인</Btn>
        <Btn :disabled="store.anomalyResolving" @click="continueTransfer">
          {{ store.anomalyResolving ? '처리 중…' : '확인 후 계속 송금' }}
        </Btn>
        <button
          :disabled="store.anomalyResolving"
          @click="cancelAnomaly"
          class="w-full min-h-[52px] text-center text-[#6B7280] font-bold disabled:opacity-50"
        >송금 취소하기</button>
      </div>
      </template>
      <template v-else>
        <div class="rounded-2xl bg-white p-5 text-center space-y-3">
          <p class="font-bold text-[#111827] text-[22px]">확인할 이상거래 정보가 없어요.</p>
          <p class="text-[#6B7280]">새 송금을 시작하거나 거래내역을 확인해 주세요.</p>
          <Btn @click="finishToHome">홈으로 돌아가기</Btn>
        </div>
      </template>
    </div>
  </div>

  <!-- 완료 -->
  <div v-else-if="flowStep === 'complete'" class="flex flex-col h-full items-center justify-center px-6 gap-5 bg-[#FAFAF8]">
    <SafeArea />
    <template v-if="store.transferResult">
      <div class="w-28 h-28 rounded-full flex items-center justify-center bg-[#22C55E]"><Ic name="Check" /></div>
      <div class="text-center space-y-2">
        <p class="font-bold text-[#111827] text-[28px]">송금이 완료됐어요!</p>
        <p class="text-[#374151] text-[18px]">{{ store.transferResult.recipientName }}님에게</p>
        <p class="font-black text-[#111827] text-[32px]">{{ formatWon(store.transferResult.amount) }}</p>
      </div>
      <Card class="w-full p-5 space-y-2">
        <div class="flex justify-between gap-4"><span class="text-[#6B7280]">거래 번호</span><strong>#{{ store.transferResult.transactionId }}</strong></div>
        <div class="flex justify-between gap-4"><span class="text-[#6B7280]">송금 후 잔액</span><strong>{{ formatWon(store.transferResult.balanceAfter) }}</strong></div>
      </Card>
      <div v-if="store.postTransferSyncError" class="w-full rounded-2xl border border-[#FDE68A] bg-[#FFFBEB] p-4 space-y-3">
        <p class="text-[#92400E]">{{ store.postTransferSyncError }}</p>
        <Btn variant="secondary" @click="store.refreshAfterTransfer">최신 정보 다시 불러오기</Btn>
      </div>
      <Btn @click="goToHistory">거래내역에서 확인</Btn>
      <Btn variant="secondary" @click="finishToHome">홈으로 돌아가기</Btn>
    </template>
    <template v-else>
      <p class="font-bold text-[#111827] text-[25px] text-center">확인할 송금 완료 정보가 없어요.</p>
      <p class="text-[#6B7280] text-center">새 송금을 시작하거나 거래내역을 조회해 주세요.</p>
      <Btn @click="finishToHome">홈으로 돌아가기</Btn>
    </template>
  </div>

  <!-- 취소 결과 -->
  <div v-else-if="flowStep === 'cancelled'" class="flex flex-col h-full items-center justify-center px-6 gap-6 bg-[#FAFAF8]">
    <SafeArea />
    <template v-if="store.transferCancelled">
      <div class="w-24 h-24 rounded-full flex items-center justify-center bg-[#E5E7EB]"><Ic name="Check" /></div>
      <div class="text-center space-y-2">
        <p class="font-bold text-[#111827] text-[27px]">송금을 취소했어요.</p>
        <p class="text-[#6B7280] text-[17px]">잔액과 거래내역은 바뀌지 않았어요.</p>
      </div>
    </template>
    <template v-else>
      <p class="font-bold text-[#111827] text-[25px] text-center">확인할 송금 취소 정보가 없어요.</p>
      <p class="text-[#6B7280] text-center">새 송금을 시작하거나 홈으로 돌아가 주세요.</p>
    </template>
    <Btn @click="finishToHome">홈으로 돌아가기</Btn>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { useAppStore } from '../stores/appStore';
import { BANKS } from '../constants/banks';
import SafeArea from '../components/common/SafeArea.vue';
import TopBar from '../components/common/TopBar.vue';
import StepBar from '../components/common/StepBar.vue';
import Card from '../components/common/Card.vue';
import Btn from '../components/common/Btn.vue';
import Ic from '../components/common/Ic.vue';
import AmountKeypad from '../components/common/AmountKeypad.vue';
import PinEntry from '../components/common/PinEntry.vue';

const props = defineProps({
  flowStep: { type: String, required: true },
});

const store = useAppStore();
const recipientName = ref(store.directRecipient?.name ?? '');
const bankCode = ref(store.directRecipient?.bankCode ?? '');
const accountNumber = ref(store.directRecipient?.accountNumber ?? '');
const showBanks = ref(false);
const directInputError = ref('');
const directTouched = ref({ name: false, bank: false, account: false });

const selectedBank = computed(() => BANKS.find((bank) => bank.code === bankCode.value) ?? null);
const personAccs = computed(() => store.accountsByPerson[store.selectedPersonId] ?? []);
const patternTargetMissing = computed(() => (
  store.isPatternTransfer
  && store.financeLoaded
  && (!store.selectedPerson || !store.selectedRecipientAccount)
));
const canProceedDirect = computed(() => (
  recipientName.value.length > 0
  && Boolean(selectedBank.value)
  && /^[0-9-]{8,50}$/.test(accountNumber.value)
));
const directFieldErrors = computed(() => ({
  name: directTouched.value.name && recipientName.value.length === 0
    ? '받는 분 이름을 입력해 주세요.'
    : '',
  bank: directTouched.value.bank && !selectedBank.value
    ? '은행을 선택해 주세요.'
    : '',
  account: directTouched.value.account && !/^[0-9-]{8,50}$/.test(accountNumber.value)
    ? '계좌번호는 숫자와 하이픈을 포함해 8~50자로 입력해 주세요.'
    : '',
}));
const guardianPhone = computed(() => store.support?.guardian?.phoneNumber ?? '');
const guardianShareAgreed = computed(() => Boolean(
  store.currentUser?.consents?.guardianShareAgreed,
));
const riskLabel = computed(() => store.anomaly?.riskLevel === 'HIGH' ? '높은 위험' : '주의 필요');
const notificationTitle = computed(() => {
  const titles = {
    SENT: '카카오 알림을 실제로 보냈어요.',
    MOCKED_NO_TOKEN: '토큰이 없어 Mock 알림으로 확인했어요.',
    MOCKED_AFTER_ACTUAL_FAILURE: '실제 발송 실패 후 Mock 알림으로 대체했어요.',
  };
  return titles[store.notificationResult?.result] ?? '알림 결과를 확인했어요.';
});
const notificationButtonLabel = computed(() => {
  if (store.notificationSending) return '알림 요청 중…';
  if (store.notificationResult) return '알림 요청 완료';
  return '카카오 나에게 알림 보내기';
});
const fraudReasons = computed(() => (store.anomaly?.reasons ?? []).map((reason) => {
  if (reason === 'HIGH_AMOUNT') return '1천만원 이상의 큰 금액이에요.';
  if (reason === 'REPEATED_TRANSFER') {
    return '최근 10분 안에 완료된 송금이 ' + store.anomaly.recentTransferCount + '건 있어요.';
  }
  return null;
}).filter(Boolean));
const reviewRows = computed(() => {
  const recipient = store.isNewAccountFlow
    ? store.directRecipient
    : {
        name: store.selectedPerson?.name,
        bankName: store.selectedRecipientAccount?.bankName,
        masked: store.selectedRecipientAccount?.masked,
      };
  return [
    { label: '출금 계좌', value: store.selectedSourceAccount?.accountAlias || store.selectedSourceAccount?.bankName || '-' },
    { label: '받는 사람', value: recipient?.name || '-' },
    { label: '받는 계좌', value: (recipient?.bankName || '-') + ' · ' + (recipient?.masked || store.selectedAccountMasked || '-') },
    { label: '금액', value: formatWon(Number(store.transferAmount)), emphasis: true },
    { label: '수수료', value: '0원' },
  ];
});

onMounted(async () => {
  if (['transfer-source', 'guide-person', 'guide-account'].includes(props.flowStep)) {
    await store.loadFinancialData();
    if (props.flowStep === 'transfer-source' && !store.selectedSourceAccountId) {
      store.selectedSourceAccountId = store.defaultOwnedAccount?.accountId ?? null;
    }
  }
  if (props.flowStep === 'fraud-warning') await store.loadSupport();
});

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

function proceedFromSource() {
  store.transferError = '';
  if (patternTargetMissing.value) return;
  if (store.isPatternTransfer && store.selectedPersonId) {
    store.navigate('guide-person');
    return;
  }
  store.navigate('direct-transfer');
}

function selectFamily() {
  store.isNewAccountFlow = false;
  store.navigate('guide-person');
}

function proceedNewAccount() {
  directInputError.value = '';
  store.transferError = '';
  if (!canProceedDirect.value) {
    directInputError.value = '이름, 은행, 계좌번호를 모두 확인해 주세요.';
    store.recordPatternAction('wrongTouch');
    return;
  }
  store.setDirectRecipient({
    name: recipientName.value,
    bankCode: selectedBank.value.code,
    bankName: selectedBank.value.name,
    accountNumber: accountNumber.value,
  });
  store.navigate('amount-input');
}

function getAccCount(personId) {
  return store.accountsByPerson[personId]?.length ?? 0;
}

function handleSelectFamilyPerson(personId) {
  store.transferError = '';
  store.isNewAccountFlow = false;
  store.selectPerson(personId);
  const accounts = store.accountsByPerson[personId] ?? [];
  // 저장된 계좌 선택 단계는 수취 계좌가 하나여도 건너뛰지 않는다.
  const hasAccountStep = store.isPatternTransfer && store.activePatternDetail?.steps?.some((step) => step.screenCode === 'guide-account');
  if (accounts.length === 1 && !hasAccountStep) store.navigate('amount-input');
  else store.navigate('guide-account');
}

function handleSelectAccount(account) {
  store.selectRecipientAccount(account);
  store.navigate('amount-input');
}

function handleAmountComplete(amount) {
  store.transferError = '';
  store.transferAmount = amount;
  store.navigate('final-confirm');
}

async function handlePinComplete(pin) {
  try {
    const response = await store.submitTransfer(pin);
    if (response?.status === 'COMPLETED') store.navigate('complete');
    if (response?.status === 'REQUIRES_REVIEW') store.navigate('fraud-warning');
  } catch (error) {
    if (error?.code === 'INSUFFICIENT_BALANCE') replaceTransferStep('amount-input');
    if (error?.code === 'SOURCE_ACCOUNT_NOT_FOUND') {
      await store.loadFinancialData(true);
      replaceTransferStep('transfer-source');
    }
    if (error?.code === 'RECIPIENT_ACCOUNT_NOT_FOUND') {
      await store.loadFinancialData(true);
      replaceTransferStep('guide-person');
    }
    if (error?.code === 'INVALID_RECIPIENT') {
      replaceTransferStep(store.isNewAccountFlow ? 'direct-newaccount' : 'guide-person');
    }
  }
}

function replaceTransferStep(step) {
  store.navigate(step, { replace: true });
}

function recheckTransfer() {
  store.anomalyRechecked = true;
  store.navigate('final-confirm');
}

function returnToWarning() {
  store.navigate('fraud-warning');
}

async function continueTransfer() {
  try {
    const response = await store.resolveAnomaly('CONTINUE');
    if (response?.action === 'CONTINUE') store.navigate('complete');
    if (response?.action === 'CANCEL') store.navigate('cancelled');
  } catch {
    // 처리 실패 시 같은 이상거래 ID를 보존해 재시도할 수 있다.
  }
}

async function cancelAnomaly() {
  try {
    const response = await store.resolveAnomaly('CANCEL');
    if (response?.action === 'CONTINUE') store.navigate('complete');
    if (response?.action === 'CANCEL') store.navigate('cancelled');
  } catch {
    // 처리 실패 시 경고 화면에 머문다.
  }
}

async function notifyGuardian() {
  try {
    await store.sendGuardianNotification();
  } catch {
    // 알림 실패는 사용자의 계속·취소 결정을 막지 않는다.
  }
}

function goToHistory() {
  store.startTransfer();
  store.navigate('task-5', { replace: true });
}

function finishToHome() {
  store.cancelTransfer();
}
</script>
