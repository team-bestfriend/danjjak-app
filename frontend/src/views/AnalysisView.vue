<template>
  <div class="flex flex-col h-full" style="background: #FAFAF8;">
    <SafeArea />
    <div class="bg-white px-5 border-b border-[#EEEEED] flex-shrink-0" style="padding-top: 18px; padding-bottom: 16px;">
      <h1 class="font-bold text-[#111827]" style="font-size: 26px;">어머님 이용 분석</h1>
      <p class="text-[#6B7280] mt-1 text-[15px]">최근 7일 · {{ report?.from ?? period.from }} ~ {{ report?.to ?? period.to }}</p>
    </div>
    <main class="flex-1 overflow-y-auto px-4 pt-4 pb-4 space-y-4" :aria-busy="loading">
      <Card v-if="loading" className="p-5" role="status">
        <p class="text-[17px] text-[#374151]">이용 기록을 불러오는 중이에요…</p>
      </Card>
      <Card v-else-if="errorMessage" className="p-5 space-y-4">
        <p role="alert" class="text-[17px] text-[#B91C1C]">{{ errorMessage }}</p>
        <Btn :disabled="loading" @click="loadReport">다시 시도하기</Btn>
      </Card>
      <Card v-else-if="report?.status === 'CONSENT_REQUIRED'" className="p-5 space-y-4">
        <h2 class="text-[19px] font-semibold text-[#111827]">이용 기록 동의를 확인해 주세요</h2>
        <p class="text-[17px] text-[#4B5563]">아직 동의 선택이 완료되지 않았어요. 이용 분석을 보려면 이용 기록 저장에 대한 선택을 완료해 주세요.</p>
        <Btn @click="store.navigate('consent', { query: { edit: '1' } })">동의 선택하러 가기</Btn>
      </Card>
      <Card v-else-if="report?.status === 'CONSENT_DECLINED'" className="p-5 space-y-4">
        <h2 class="text-[19px] font-semibold text-[#111827]">이용 기록을 수집하지 않고 있어요</h2>
        <p class="text-[17px] text-[#4B5563]">이용 기록 저장에 동의하지 않은 상태예요. 설정의 ‘선택 동의 관리’에서 변경할 수 있어요.</p>
        <Btn @click="store.navTo('settings')">설정으로 가기</Btn>
      </Card>
      <Card v-else-if="report?.status === 'NO_DATA'" className="p-5 space-y-4">
        <h2 class="text-[19px] font-semibold text-[#111827]">이 기간에는 이용 기록이 없어요</h2>
        <p class="text-[17px] text-[#4B5563]">선택 기간에 종료된 금융 업무 기록이 아직 없어요. 기록이 쌓이면 이용 현황을 보여드릴게요.</p>
        <Btn @click="store.navTo('home')">홈으로 가기</Btn>
      </Card>
      <template v-else-if="report?.status === 'AVAILABLE'">
        <div class="rounded-[20px] border border-[#FFBC00] flex items-center justify-between px-6" style="background: #FFFBEB; min-height: 92px;">
          <p class="font-semibold text-[16px] text-[#92650A]">금융 업무 실행</p>
          <p class="flex items-baseline gap-1">
            <span class="font-bold text-[#111827] text-[38px] leading-none">{{ totalCount }}</span>
            <span class="font-semibold text-[17px] text-[#92650A]">회</span>
          </p>
        </div>
        <Card className="p-4">
          <h2 class="font-semibold text-[#111827] mb-4 text-[19px]">패턴별 이용 현황</h2>
          <div class="space-y-4">
            <div v-for="pattern in displayPatterns" :key="pattern.patternId">
              <div class="flex items-center justify-between mb-2">
                <span class="flex items-center gap-2 font-normal text-[#374151] text-[16px]">
                  <span
                    v-if="pattern.num !== undefined"
                    class="font-bold px-2 py-0.5 rounded-md text-white"
                    :style="{ fontSize: '12px', background: pattern.color }"
                  >
                    {{ pattern.num }}
                  </span>
                  {{ pattern.title }}
                </span>
                <span class="font-bold text-[#111827] text-[15px] whitespace-nowrap">{{ pattern.completedCount }}회</span>
              </div>
              <div class="h-3 bg-[#F3F4F6] rounded-full overflow-hidden" aria-hidden="true">
                <div
                  class="h-full rounded-full transition-all duration-700"
                  :style="{ width: (maxCount ? (pattern.completedCount / maxCount) * 100 : 0) + '%', background: pattern.color }"
                />
              </div>
            </div>
          </div>
        </Card>
        <Card className="p-5 border-2 border-[#FFBC00]" style="background: #FFFDF5;">
          <div class="flex items-center gap-3 mb-4">
            <div class="w-11 h-11 rounded-full bg-[#FFF3CC] flex items-center justify-center flex-shrink-0 text-[22px]">📊</div>
            <div>
              <h2 class="font-semibold text-[#111827] text-[17px]">이용 기록 살펴보기</h2>
              <p class="font-semibold text-[17px] text-[#92650A]">기록에서 확인된 단계</p>
            </div>
          </div>
          <template v-if="report.difficultStep">
            <div class="bg-[#FFF3CC] border border-[#FFBC00] rounded-[18px] p-4 space-y-3">
              <p class="font-normal text-[#374151] leading-relaxed text-[16px]">
                <span class="font-semibold text-[#111827]">{{ difficultPatternTitle }}</span>의
                <span class="font-semibold text-[#111827]">{{ report.difficultStep.stepName }}</span> 단계 기록이에요.
              </p>
              <dl class="text-[16px] text-[#374151] space-y-2">
                <div class="flex justify-between gap-2"><dt>방문 횟수</dt><dd class="font-semibold text-[#111827]">{{ report.difficultStep.visitCount }}회</dd></div>
                <div class="flex justify-between gap-2"><dt>오류 행동 점수</dt><dd class="font-semibold text-[#111827]">{{ report.difficultStep.errorScore }}점</dd></div>
                <div class="flex justify-between gap-2"><dt>평균 소요 시간</dt><dd class="font-semibold text-[#111827]">{{ report.difficultStep.averageDurationSeconds === null ? '측정 기록 없음' : report.difficultStep.averageDurationSeconds + '초' }}</dd></div>
              </dl>
            </div>
          </template>
          <p v-else class="text-[17px] text-[#4B5563]">분석할 단계 방문 기록이 없어 어려운 단계를 표시할 수 없어요.</p>
        </Card>
      </template>
    </main>
    <NavBar active="analysis" :onSelect="store.navTo" />
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { usageAnalysisApi } from '../api/usageAnalysisApi.js';
import { useAppStore } from '../stores/appStore';
import SafeArea from '../components/common/SafeArea.vue';
import Card from '../components/common/Card.vue';
import Btn from '../components/common/Btn.vue';
import NavBar from '../components/common/NavBar.vue';

const store = useAppStore();
const report = ref(null);
const loading = ref(false);
const errorMessage = ref('');
// 기존 최근 7일 화면을 유지하며 UTC 변환 없이 로컬 날짜의 양 끝을 포함한다.
const today = new Date();
const start = new Date(today);
start.setDate(start.getDate() - 6);
function formatDate(date) {
  return [date.getFullYear(), String(date.getMonth() + 1).padStart(2, '0'), String(date.getDate()).padStart(2, '0')].join('-');
}
const period = { from: formatDate(start), to: formatDate(today) };
const totalCount = computed(() => report.value?.patterns.reduce((sum, pattern) => sum + pattern.completedCount, 0) ?? 0);
const maxCount = computed(() => Math.max(0, ...(report.value?.patterns.map((pattern) => pattern.completedCount) ?? [])));
const displayPatterns = computed(() => report.value?.patterns.map((pattern) => {
  const storedPattern = store.patterns.find((item) => item.patternId === pattern.patternId);
  return {
    ...pattern,
    num: storedPattern?.num,
    color: storedPattern?.color ?? '#9CA3AF',
  };
}) ?? []);
const difficultPatternTitle = computed(() => report.value?.patterns.find(
  (pattern) => pattern.patternId === report.value?.difficultStep?.patternId,
)?.title);

async function loadReport() {
  if (loading.value) return;
  loading.value = true;
  errorMessage.value = '';
  report.value = null;
  try {
    report.value = await usageAnalysisApi.getUsageAnalysis(period.from, period.to);
  } catch {
    errorMessage.value = '이용 기록을 불러오지 못했어요. 잠시 후 다시 시도해 주세요.';
  } finally {
    loading.value = false;
  }
}

onMounted(loadReport);
</script>
