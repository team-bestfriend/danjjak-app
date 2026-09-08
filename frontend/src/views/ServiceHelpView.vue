<template>
  <div class="flex h-full flex-col bg-[#FAFAF8]">
    <SafeArea />
    <TopBar title="서비스 이용방법" :onBack="store.goBack" />
    <main class="min-h-0 flex-1 overflow-y-auto px-5 py-6 space-y-6 text-[20px] leading-relaxed">
      <h1 class="text-[28px] font-bold">단짝과 함께 연습해요</h1>
      <section><h2 class="font-bold">단축번호와 음성</h2><p>홈에서 자주 하는 업무의 번호를 누르거나 말해 보세요. 업무 내용을 확인하고 시작할 수 있어요. 말하기 없이 버튼으로도 이용할 수 있어요.</p></section>
      <section><h2 class="font-bold">가족이 준비한 안내</h2><p>등록된 가족 음성이나 AI 음성으로 안내를 들어요. 안내를 듣지 않아도 화면의 질문과 버튼을 따라 진행할 수 있어요.</p></section>
      <section><h2 class="font-bold">연습용 내 계좌</h2><p>실제 은행에 연결하지 않는 연습용 계좌예요. 설정의 내 계좌 관리에서 준비된 계좌를 불러오면 모의 송금과 잔액 조회를 이용할 수 있어요.</p></section>
      <section><h2 class="font-bold">기본 계좌와 이번 송금</h2><p>설정에서 기본 내 계좌를 저장할 수 있어요. 송금 중에 다른 계좌를 선택해도 저장된 기본 계좌는 바뀌지 않아요.</p></section>
      <section><h2 class="font-bold">이상 거래와 카카오 알림</h2><p>큰 금액이나 반복 송금은 한 번 더 확인해요. 카카오 알림 시연은 로그인한 내 카카오의 ‘나에게 보내기’로 확인하며 보호자 계정으로 직접 보내지 않아요.</p></section>
      <Btn variant="secondary" @click="store.navigate('settings')">설정으로 가기</Btn>
    </main>
  </div>
</template>

<script setup>
import { useAppStore } from '../stores/appStore.js';
import SafeArea from '../components/common/SafeArea.vue';
import TopBar from '../components/common/TopBar.vue';
import Btn from '../components/common/Btn.vue';
const store = useAppStore();
</script>
