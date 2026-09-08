import { defineStore } from 'pinia';
import { computed, ref } from 'vue';
import { authApi, userApi } from '../api/authApi.js';
import { accountApi, supportApi, transferApi } from '../api/financeApi.js';
import { ApiError } from '../api/httpClient.js';
import { patternApi } from '../api/patternApi.js';
import { TASK_COLORS, reorderPatterns } from '../constants/data.js';
import { currentRouteName, navigateBack, navigateTo, replaceWith } from '../router/navigation.js';

const ERROR_MESSAGES = {
  PIN_MISMATCH: '계좌 비밀번호가 맞지 않습니다. 다시 입력해 주세요.',
  INSUFFICIENT_BALANCE: '계좌 잔액이 부족합니다. 금액을 다시 확인해 주세요.',
  SOURCE_ACCOUNT_NOT_FOUND: '선택한 출금 계좌를 찾을 수 없습니다. 계좌를 다시 선택해 주세요.',
  RECIPIENT_ACCOUNT_NOT_FOUND: '받는 계좌를 찾을 수 없습니다. 받는 사람을 다시 선택해 주세요.',
  INVALID_RECIPIENT: '받는 계좌 정보를 다시 확인해 주세요.',
  INVALID_REQUEST: '입력한 정보를 다시 확인해 주세요.',
  ACCOUNT_ALREADY_EXISTS: '이미 등록된 계좌입니다.',
  REGISTERED_PERSON_NOT_FOUND: '등록된 사람을 찾을 수 없습니다.',
  ANOMALY_ALREADY_RESOLVED: '이미 처리된 이상거래입니다.',
  SESSION_REQUIRED: '로그인이 만료되었습니다. 카카오 로그인 후 다시 시도해 주세요.',
  KAKAO_NOT_CONFIGURED: '카카오 로그인 설정을 확인해 주세요.',
  DEMO_USER_CAPACITY_EXCEEDED: '연결할 수 있는 시연 사용자가 없습니다.',
  GUARDIAN_SHARE_CONSENT_REQUIRED: '보호자 공유 동의 후 카카오 알림을 요청할 수 있습니다.',
  INVALID_RESPONSE: '서버 응답을 확인할 수 없습니다. 거래내역에서 처리 결과를 확인해 주세요.',
};

const FDS_REASONS = new Set(['HIGH_AMOUNT', 'REPEATED_TRANSFER']);
const EXPECTED_PATTERN_BRANCH_ROUTES = new Set(['fraud-warning', 'cancelled']);

const PATTERN_TYPE_TO_TASK = {
  TRANSFER: 'transfer',
  PENSION_CHECK: 'pension',
  MANAGEMENT_FEE_CHECK: 'building',
  BALANCE_CHECK: 'balance',
  TRANSACTION_HISTORY: 'history',
  CUSTOMER_CENTER: 'support',
  UTILITY_BILL_CHECK: 'utility',
  AUTO_TRANSFER_CHECK: 'autotransfer',
  CARD_HISTORY: 'cardhistory',
  DEPOSIT_MATURITY_CHECK: 'deposit',
};

function toUiPattern(pattern) {
  const taskType = PATTERN_TYPE_TO_TASK[pattern.patternType] ?? 'unknown';
  const linked = pattern.linkedAccount;
  const accountSummary = linked
    ? [linked.registeredPersonName, linked.relationship, linked.bankName, linked.accountAlias]
      .filter(Boolean).join(' · ')
    : '';
  return {
    id: pattern.patternId,
    patternId: pattern.patternId,
    num: pattern.shortcutNumber,
    label: pattern.title,
    description: pattern.description,
    color: TASK_COLORS[taskType] ?? '#6B7280',
    sub: accountSummary || pattern.description,
    taskType,
    patternType: pattern.patternType,
    personId: linked?.registeredPersonId ?? null,
    recipientAccountId: linked?.registeredPersonId ? linked.accountId : null,
    linkedAccount: linked ?? null,
    steps: pattern.steps ?? null,
  };
}

function maskAccountNumber(value = '') {
  const digits = value.replace(/\D/g, '');
  if (digits.length < 6) return value;
  return `${digits.slice(0, 3)}-****-${digits.slice(-3)}`;
}

function toMessage(error, fallback) {
  if (error instanceof ApiError || typeof error?.code === 'string') {
    return ERROR_MESSAGES[error.code] ?? error.message;
  }
  return fallback;
}

function invalidResponse() {
  return new ApiError(
    'INVALID_RESPONSE',
    ERROR_MESSAGES.INVALID_RESPONSE,
    502,
  );
}

function isNonNegativeAmount(value) {
  return Number.isFinite(Number(value)) && Number(value) >= 0;
}

function validateTransferResponse(response) {
  if (!response || !Array.isArray(response.reasons)
      || !Number.isInteger(response.recentTransferCount)
      || response.recentTransferCount < 0
      || !isNonNegativeAmount(response.balanceAfter)
      || response.reasons.some((reason) => !FDS_REASONS.has(reason))) {
    throw invalidResponse();
  }

  if (response.status === 'COMPLETED') {
    if (response.riskLevel !== 'NORMAL'
        || response.reasons.length !== 0
        || !Number.isInteger(response.transactionId)
        || response.transactionId < 1
        || response.anomalyEventId != null
        || !isNonNegativeAmount(response.balanceAfter)) {
      throw invalidResponse();
    }
    return;
  }

  const uniqueReasons = new Set(response.reasons);
  const validMedium = response.riskLevel === 'MEDIUM'
    && response.reasons.length === 1
    && uniqueReasons.size === 1;
  const validHigh = response.riskLevel === 'HIGH'
    && response.reasons.length === 2
    && uniqueReasons.size === 2
    && uniqueReasons.has('HIGH_AMOUNT')
    && uniqueReasons.has('REPEATED_TRANSFER');
  if (response.status !== 'REQUIRES_REVIEW'
      || !Number.isInteger(response.anomalyEventId)
      || response.anomalyEventId < 1
      || response.transactionId != null
      || (!validMedium && !validHigh)) {
    throw invalidResponse();
  }
}

function validateResolutionResponse(response, anomalyEventId) {
  if (!response
      || response.anomalyEventId !== anomalyEventId
      || !['CONTINUE', 'CANCEL'].includes(response.action)) {
    throw invalidResponse();
  }
  if (response.action === 'CONTINUE'
      && (!Number.isInteger(response.transactionId)
        || response.transactionId < 1
        || !isNonNegativeAmount(response.balanceAfter))) {
    throw invalidResponse();
  }
  if (response.action === 'CANCEL'
      && (response.transactionId != null || response.balanceAfter != null)) {
    throw invalidResponse();
  }
}

function validateNotificationResponse(response, anomalyEventId) {
  const result = response?.result;
  const validResult = ['SENT', 'MOCKED_NO_TOKEN', 'MOCKED_AFTER_ACTUAL_FAILURE'].includes(result);
  const requiredFieldsExist = typeof response?.actualAttempted === 'boolean'
    && typeof response?.actualSucceeded === 'boolean'
    && typeof response?.detail === 'string';
  const sentIsConsistent = result !== 'SENT'
    || (response.deliveryMode === 'ACTUAL' && response.actualAttempted
      && response.actualSucceeded && typeof response.sentAt === 'string'
      && !Number.isNaN(Date.parse(response.sentAt)));
  const mockIsConsistent = result === 'SENT'
    || (response.deliveryMode === 'MOCK' && !response.actualSucceeded && !response.sentAt);
  const attemptIsConsistent = result === 'MOCKED_NO_TOKEN'
    ? !response.actualAttempted
    : Boolean(response.actualAttempted);
  if (!response || response.anomalyEventId !== anomalyEventId
      || !validResult || !requiredFieldsExist || !sentIsConsistent
      || !mockIsConsistent || !attemptIsConsistent) {
    throw invalidResponse();
  }
}

export const useAppStore = defineStore('app', () => {
  // 기존 화면 코드의 screen 접근은 유지하되 값의 유일한 기준은 Vue Router다.
  const screen = computed({
    get: () => currentRouteName(),
    set: (nextScreen) => { void navigateTo(nextScreen); },
  });

  const patterns = ref([]);
  const patternTemplates = ref([]);
  const patternLoading = ref(false);
  const patternLoaded = ref(false);
  const patternError = ref('');
  const patternOrderSaving = ref(false);
  const patternMutationSaving = ref(false);
  const activePattern = ref(null);
  const activePatternDetail = ref(null);
  const editingId = ref(null);
  const editingPersonId = ref(null);
  const homePage = ref(1);
  const toast = ref(null);
  const userName = ref('순자');
  const currentUser = ref(null);
  const authStatus = ref('unknown');
  const authLoading = ref(false);
  const authError = ref('');
  const loginStarting = ref(false);
  const devSessionStarting = ref(false);
  const logoutPending = ref(false);
  const sessionNotice = ref('');
  let pendingSessionLoad = null;

  const patternStarting = ref(false);
  const patternExecutionId = ref(null);
  const executionLoggingEnabled = ref(false);
  const currentStepVisit = ref(null);
  const stepActionCounts = ref({ retryCount: 0, backCount: 0, wrongTouchCount: 0, routeDeviation: false });
  const patternExecutionError = ref('');
  let patternStepSync = Promise.resolve();

  const ownedAccounts = ref([]);
  const people = ref([]);
  const accountsByPerson = ref({});
  const financeLoading = ref(false);
  const financeLoaded = ref(false);
  const financeError = ref('');
  const financeWarning = ref('');
  let pendingFinancialDataLoad = null;

  const selectedInquiryAccountId = ref(null);
  const inquiryBalance = ref(null);
  const inquiryTransactions = ref([]);
  const inquiryLoading = ref(false);
  const inquiryError = ref('');
  let inquiryRequestSequence = 0;

  const support = ref(null);
  const supportLoading = ref(false);
  const supportLoaded = ref(false);
  const supportError = ref('');
  const guardianSaving = ref(false);
  let pendingGuardianSave = null;

  const transferAmount = ref('0');
  const isNewAccountFlow = ref(false);
  const isPatternTransfer = ref(false);
  const selectedSourceAccountId = ref(null);
  const selectedPersonId = ref(null);
  const selectedRecipientAccountId = ref(null);
  const selectedAccountMasked = ref(null);
  const directRecipient = ref(null);
  const transferSubmitting = ref(false);
  const transferError = ref('');
  const transferResult = ref(null);
  const anomaly = ref(null);
  const anomalyRechecked = ref(false);
  const anomalyResolving = ref(false);
  const notificationSending = ref(false);
  const notificationResult = ref(null);
  const transferCancelled = ref(false);
  const postTransferSyncError = ref('');

  const defaultOwnedAccount = computed(() => (
    ownedAccounts.value.find((account) => account.primary) ?? ownedAccounts.value[0] ?? null
  ));

  const selectedSourceAccount = computed(() => (
    ownedAccounts.value.find((account) => account.accountId === selectedSourceAccountId.value) ?? null
  ));

  const selectedPerson = computed(() => (
    people.value.find((person) => person.id === selectedPersonId.value) ?? null
  ));

  const selectedRecipientAccount = computed(() => {
    const personAccounts = accountsByPerson.value[selectedPersonId.value] ?? [];
    return personAccounts.find((account) => account.accountId === selectedRecipientAccountId.value)
      ?? null;
  });

  function navigate(nextScreen, options = {}) {
    return navigateTo(nextScreen, options);
  }

  function goBack() {
    navigateBack();
  }

  function recordPatternNavigation(fromScreenCode, toScreenCode) {
    if (!activePatternDetail.value || fromScreenCode === toScreenCode) return;
    const fromStep = activePatternDetail.value.steps?.find(
      (step) => step.screenCode === fromScreenCode,
    );
    const toStep = activePatternDetail.value.steps?.find(
      (step) => step.screenCode === toScreenCode,
    );
    if (fromStep && toStep && toStep.stepOrder < fromStep.stepOrder) {
      recordPatternAction('back');
    }
  }

  function navTo(tab) {
    const nextScreen = tab === 'home'
      ? 'home'
      : tab === 'patterns'
        ? 'patterns'
        : tab === 'analysis'
          ? 'analysis'
          : 'settings';
    return navigateTo(nextScreen);
  }

  function applyCurrentUser(user) {
    currentUser.value = user;
    userName.value = user.name;
    authStatus.value = 'authenticated';
    authError.value = '';
  }

  function clearSession(message = '') {
    currentUser.value = null;
    authStatus.value = 'anonymous';
    authError.value = '';
    sessionNotice.value = message;
    loginStarting.value = false;
    financeLoaded.value = false;
    patterns.value = [];
    patternTemplates.value = [];
    patternLoaded.value = false;
    patternError.value = '';
    resetPatternExecution();
    ownedAccounts.value = [];
    people.value = [];
    accountsByPerson.value = {};
    support.value = null;
    supportLoaded.value = false;
    transferAmount.value = '0';
    selectedSourceAccountId.value = null;
    selectedPersonId.value = null;
    selectedRecipientAccountId.value = null;
    selectedAccountMasked.value = null;
    directRecipient.value = null;
    transferResult.value = null;
    anomaly.value = null;
    notificationResult.value = null;
  }

  function checkSession(force = false) {
    if (pendingSessionLoad) return pendingSessionLoad;
    if (!force && authStatus.value !== 'unknown') {
      return Promise.resolve(authStatus.value === 'authenticated');
    }
    pendingSessionLoad = (async () => {
      authLoading.value = true;
      authError.value = '';
      try {
        const response = await authApi.getSession();
        if (response?.authenticated && response.user) {
          applyCurrentUser(response.user);
          return true;
        }
        clearSession();
        return false;
      } catch (error) {
        authStatus.value = 'unknown';
        authError.value = toMessage(error, '로그인 상태를 확인하지 못했습니다.');
        return false;
      } finally {
        authLoading.value = false;
        pendingSessionLoad = null;
      }
    })();
    return pendingSessionLoad;
  }

  function startKakaoLogin() {
    if (loginStarting.value || typeof window === 'undefined') return false;
    loginStarting.value = true;
    authError.value = '';
    window.location.assign(authApi.getKakaoStartUrl());
    return true;
  }

  async function startDevSession() {
    if (devSessionStarting.value) return false;
    devSessionStarting.value = true;
    authError.value = '';
    try {
      const response = await authApi.startDevSession();
      if (!response?.authenticated || !response.user) throw invalidResponse();
      applyCurrentUser(response.user);
      await replaceWith('home');
      return true;
    } catch (error) {
      authError.value = toMessage(error, '개발용 홈 이동을 시작하지 못했습니다.');
      return false;
    } finally {
      devSessionStarting.value = false;
    }
  }

  async function saveConsents(payload) {
    const saved = await userApi.updateConsents(payload);
    currentUser.value = { ...currentUser.value, consents: saved };
    return saved;
  }

  async function saveAccessibilitySettings(payload) {
    const saved = await userApi.updateSettings(payload);
    currentUser.value = { ...currentUser.value, settings: saved };
    return saved;
  }

  async function logout() {
    if (logoutPending.value) return false;
    logoutPending.value = true;
    try {
      await authApi.logout();
      clearSession();
      await replaceWith('login');
      return true;
    } catch (error) {
      authError.value = toMessage(error, '로그아웃하지 못했습니다. 다시 시도해 주세요.');
      return false;
    } finally {
      logoutPending.value = false;
    }
  }

  function showToast(msg, action, cb) {
    toast.value = { msg, action, cb, key: Date.now() };
  }

  function clearToast() {
    toast.value = null;
  }

  function setPatterns(newPatterns) {
    patterns.value = typeof newPatterns === 'function' ? newPatterns(patterns.value) : newPatterns;
  }

  async function loadPatterns(force = false) {
    if (patternLoaded.value && !force) return true;
    patternLoading.value = true;
    patternError.value = '';
    try {
      const rows = await patternApi.getPatterns();
      patterns.value = rows.map(toUiPattern);
      patternLoaded.value = true;
      return true;
    } catch (error) {
      patternError.value = toMessage(error, '단축번호를 불러오지 못했습니다.');
      if (force) patterns.value = [];
      return false;
    } finally {
      patternLoading.value = false;
    }
  }

  async function loadPatternTemplates(force = false) {
    if (patternTemplates.value.length > 0 && !force) return patternTemplates.value;
    patternLoading.value = true;
    patternError.value = '';
    try {
      patternTemplates.value = await patternApi.getTemplates();
      return patternTemplates.value;
    } catch (error) {
      patternError.value = toMessage(error, '등록 가능한 금융 업무를 불러오지 못했습니다.');
      throw error;
    } finally {
      patternLoading.value = false;
    }
  }

  async function loadPatternDetail(patternId) {
    patternLoading.value = true;
    patternError.value = '';
    try {
      const detail = await patternApi.getPattern(patternId);
      activePatternDetail.value = detail;
      activePattern.value = toUiPattern(detail);
      return detail;
    } catch (error) {
      patternError.value = toMessage(error, '금융 패턴 상세를 불러오지 못했습니다.');
      if (error?.code === 'PATTERN_NOT_FOUND') await loadPatterns(true);
      throw error;
    } finally {
      patternLoading.value = false;
    }
  }

  async function createPattern(payload) {
    patternError.value = '';
    const detail = await patternApi.createPattern(payload);
    await loadPatterns(true);
    activePatternDetail.value = detail;
    activePattern.value = toUiPattern(detail);
    return detail;
  }

  async function updatePattern(patternId, payload) {
    patternError.value = '';
    const detail = await patternApi.updatePattern(patternId, payload);
    await loadPatterns(true);
    activePatternDetail.value = detail;
    activePattern.value = toUiPattern(detail);
    return detail;
  }

  async function deactivatePattern(patternId) {
    if (patternMutationSaving.value) return false;
    patternMutationSaving.value = true;
    patternError.value = '';
    try {
      await patternApi.deactivatePattern(patternId);
      await loadPatterns(true);
      if (activePattern.value?.patternId === patternId) {
        activePattern.value = null;
        activePatternDetail.value = null;
      }
      return true;
    } catch (error) {
      patternError.value = toMessage(error, '패턴을 비활성화하지 못했습니다.');
      throw error;
    } finally {
      patternMutationSaving.value = false;
    }
  }

  async function persistPatternOrder(nextPatterns) {
    const saved = await patternApi.reorderPatterns(nextPatterns.map((pattern) => ({
      patternId: pattern.patternId,
      shortcutNumber: pattern.num,
    })));
    patterns.value = saved.map(toUiPattern);
    return patterns.value;
  }

  async function reorder(sourceNum, targetNum) {
    if (patternOrderSaving.value) return false;
    const previous = [...patterns.value];
    const dragged = patterns.value.find((pattern) => pattern.num === sourceNum);
    const reordered = reorderPatterns(patterns.value, sourceNum, targetNum);
    if (!dragged || sourceNum === targetNum) return false;
    patternOrderSaving.value = true;
    patternError.value = '';
    patterns.value = reordered;
    try {
      await persistPatternOrder(reordered);
      showToast(`${dragged?.label ?? '패턴'}이(가) ${targetNum}번으로 변경됐어요.`, '되돌리기', () => {
        if (patternOrderSaving.value) return;
        patternOrderSaving.value = true;
        void persistPatternOrder(previous)
          .catch(() => {
            patternError.value = '이전 단축번호 순서로 되돌리지 못했습니다.';
          })
          .finally(() => { patternOrderSaving.value = false; });
      });
      return true;
    } catch (error) {
      patterns.value = previous;
      patternError.value = toMessage(error, '단축번호 순서를 저장하지 못했습니다.');
      return false;
    } finally {
      patternOrderSaving.value = false;
    }
  }

  function mapOwnedAccount(account) {
    return {
      ...account,
      bank: account.bankName,
      nickname: account.accountAlias,
      masked: maskAccountNumber(account.accountNumber),
    };
  }

  function mapRegisteredPerson(person) {
    return {
      id: person.registeredPersonId,
      name: person.name,
      emoji: person.relationship === '아들' ? '👨' : person.relationship === '딸' ? '👩' : '👤',
      relation: person.relationship,
      accounts: person.account ? 1 : 0,
    };
  }

  function mapRecipientAccount(account) {
    return {
      ...account,
      id: account.accountId,
      bank: account.bankName,
      nickname: account.accountAlias,
      masked: maskAccountNumber(account.accountNumber),
    };
  }

  function loadFinancialData(force = false) {
    if (pendingFinancialDataLoad) return pendingFinancialDataLoad;
    if (financeLoaded.value && !force) return Promise.resolve(true);
    pendingFinancialDataLoad = (async () => {
      financeLoading.value = true;
      financeError.value = '';
      financeWarning.value = '';
      try {
        const [accountRows, personRows] = await Promise.all([
          accountApi.getOwnedAccounts(),
          accountApi.getRegisteredPersons(),
        ]);
        ownedAccounts.value = accountRows.map(mapOwnedAccount);
        if (ownedAccounts.value.length > 0 && !ownedAccounts.value.some((account) => account.primary)) {
          financeWarning.value = '기본 계좌가 지정되지 않아 첫 번째 본인 계좌를 선택했어요.';
        }
        people.value = personRows.map(mapRegisteredPerson);
        accountsByPerson.value = Object.fromEntries(personRows.map((person) => [
          person.registeredPersonId,
          person.account ? [mapRecipientAccount(person.account)] : [],
        ]));
        if (!ownedAccounts.value.some((account) => account.accountId === selectedInquiryAccountId.value)) {
          selectedInquiryAccountId.value = defaultOwnedAccount.value?.accountId ?? null;
        }
        if (!ownedAccounts.value.some((account) => account.accountId === selectedSourceAccountId.value)) {
          selectedSourceAccountId.value = defaultOwnedAccount.value?.accountId ?? null;
        }
        financeLoaded.value = true;
        return true;
      } catch (error) {
        financeError.value = toMessage(error, '계좌와 등록 인물을 불러오지 못했습니다.');
        ownedAccounts.value = [];
        people.value = [];
        accountsByPerson.value = {};
        selectedSourceAccountId.value = null;
        selectedInquiryAccountId.value = null;
        if (!isNewAccountFlow.value) {
          selectedPersonId.value = null;
          selectedRecipientAccountId.value = null;
          selectedAccountMasked.value = null;
        }
        financeLoaded.value = false;
        financeWarning.value = '';
        return false;
      } finally {
        financeLoading.value = false;
        pendingFinancialDataLoad = null;
      }
    })();
    return pendingFinancialDataLoad;
  }

  async function saveRegisteredPerson(payload, registeredPersonId = null) {
    const saved = registeredPersonId
      ? await accountApi.updateRegisteredPerson(registeredPersonId, payload)
      : await accountApi.createRegisteredPerson(payload);
    if (pendingFinancialDataLoad) await pendingFinancialDataLoad;
    await loadFinancialData(true);
    return saved;
  }

  async function loadInquiry(accountId, category = null) {
    const requestSequence = ++inquiryRequestSequence;
    if (!accountId) {
      inquiryBalance.value = null;
      inquiryTransactions.value = [];
      inquiryError.value = '';
      inquiryLoading.value = false;
      return;
    }
    inquiryLoading.value = true;
    inquiryError.value = '';
    selectedInquiryAccountId.value = accountId;
    try {
      const [balance, transactions] = await Promise.all([
        accountApi.getBalance(accountId),
        accountApi.getTransactions(accountId, category),
      ]);
      if (requestSequence !== inquiryRequestSequence) return;
      inquiryBalance.value = balance;
      inquiryTransactions.value = transactions;
    } catch (error) {
      if (requestSequence !== inquiryRequestSequence) return;
      inquiryBalance.value = null;
      inquiryTransactions.value = [];
      inquiryError.value = toMessage(error, '금융 정보를 불러오지 못했습니다.');
    } finally {
      if (requestSequence === inquiryRequestSequence) inquiryLoading.value = false;
    }
  }

  async function loadSupport(force = false) {
    if (supportLoading.value || (supportLoaded.value && !force)) return;
    supportLoading.value = true;
    supportError.value = '';
    try {
      support.value = await supportApi.getSupport();
      supportLoaded.value = true;
    } catch (error) {
      support.value = null;
      supportLoaded.value = false;
      supportError.value = toMessage(error, '연락처를 불러오지 못했습니다.');
    } finally {
      supportLoading.value = false;
    }
  }

  async function saveGuardian(phoneNumber) {
    if (pendingGuardianSave) return pendingGuardianSave;
    guardianSaving.value = true;
    supportError.value = '';
    pendingGuardianSave = supportApi.updateGuardian(phoneNumber);
    try {
      const guardian = await pendingGuardianSave;
      support.value = { ...support.value, guardian };
      return guardian;
    } catch (error) {
      supportError.value = toMessage(error, '보호자 연락처를 저장하지 못했습니다.');
      throw error;
    } finally {
      guardianSaving.value = false;
      pendingGuardianSave = null;
    }
  }

  function resetPatternExecution() {
    activePatternDetail.value = null;
    patternExecutionId.value = null;
    executionLoggingEnabled.value = false;
    currentStepVisit.value = null;
    stepActionCounts.value = {
      retryCount: 0,
      backCount: 0,
      wrongTouchCount: 0,
      routeDeviation: false,
    };
    patternExecutionError.value = '';
  }

  async function startPatternExecution(pattern) {
    if (patternStarting.value) return false;
    patternStarting.value = true;
    patternExecutionError.value = '';
    try {
      if (pattern.patternType === 'TRANSFER' && !(await loadFinancialData())) {
        throw new ApiError('FINANCE_LOAD_FAILED', financeError.value, 503);
      }
      const response = await patternApi.startExecution(
        pattern.patternId,
        defaultOwnedAccount.value?.accountId ?? null,
      );
      const detail = response.pattern;
      const mapped = toUiPattern(detail);

      if (detail.patternType === 'TRANSFER') {
        startTransfer({
          pattern: true,
          personId: mapped.personId,
          recipientAccountId: mapped.recipientAccountId,
        });
        if (mapped.personId) {
          selectedPersonId.value = mapped.personId;
          selectedRecipientAccountId.value = mapped.recipientAccountId;
          const linkedAccount = (accountsByPerson.value[mapped.personId] ?? [])
            .find((account) => account.accountId === mapped.recipientAccountId);
          selectedAccountMasked.value = linkedAccount?.masked ?? null;
        }
      } else {
        resetPatternExecution();
      }

      activePattern.value = mapped;
      activePatternDetail.value = detail;
      patternExecutionId.value = response.executionId;
      executionLoggingEnabled.value = response.loggingEnabled;
      const firstStep = [...detail.steps].sort((a, b) => a.stepOrder - b.stepOrder)[0];
      if (!firstStep?.screenCode) throw invalidResponse();
      await navigateTo(firstStep.screenCode);
      return true;
    } catch (error) {
      patternExecutionError.value = toMessage(error, '금융 패턴을 시작하지 못했습니다.');
      throw error;
    } finally {
      patternStarting.value = false;
    }
  }

  async function closeCurrentStepVisit() {
    if (!executionLoggingEnabled.value || !patternExecutionId.value || !currentStepVisit.value) return;
    const visit = currentStepVisit.value;
    currentStepVisit.value = null;
    await patternApi.updateVisit(patternExecutionId.value, visit.visitId, {
      ...stepActionCounts.value,
      completed: true,
    });
    stepActionCounts.value = {
      retryCount: 0,
      backCount: 0,
      wrongTouchCount: 0,
      routeDeviation: false,
    };
  }

  function syncPatternStep(screenCode) {
    if (!activePatternDetail.value) return Promise.resolve();
    patternStepSync = patternStepSync.then(async () => {
      const step = activePatternDetail.value?.steps?.find((item) => item.screenCode === screenCode);
      if (currentStepVisit.value?.stepId === step?.stepId) return;
      if (!step && currentStepVisit.value && !EXPECTED_PATTERN_BRANCH_ROUTES.has(screenCode)) {
        stepActionCounts.value.routeDeviation = true;
      }
      await closeCurrentStepVisit();
      if (!step || !executionLoggingEnabled.value || !patternExecutionId.value) return;
      currentStepVisit.value = await patternApi.startVisit(patternExecutionId.value, step.stepId);
    }).catch((error) => {
      patternExecutionError.value = toMessage(error, '단계 이용 기록을 저장하지 못했습니다.');
    });
    return patternStepSync;
  }

  function recordPatternAction(action) {
    if (!executionLoggingEnabled.value || !patternExecutionId.value || !currentStepVisit.value) return;
    if (action === 'routeDeviation') stepActionCounts.value.routeDeviation = true;
    else if (action === 'retry') stepActionCounts.value.retryCount += 1;
    else if (action === 'back') stepActionCounts.value.backCount += 1;
    else if (action === 'wrongTouch') stepActionCounts.value.wrongTouchCount += 1;
    const executionId = patternExecutionId.value;
    const visitId = currentStepVisit.value.visitId;
    const payload = { ...stepActionCounts.value };
    patternStepSync = patternStepSync.then(() => patternApi.updateVisit(executionId, visitId, payload)).catch((error) => {
      patternExecutionError.value = toMessage(error, '단계 행동 기록을 저장하지 못했습니다.');
    });
  }

  async function finishPatternExecution(status) {
    if (!activePatternDetail.value) return null;
    await patternStepSync;
    try {
      await closeCurrentStepVisit();
      if (executionLoggingEnabled.value && patternExecutionId.value) {
        return await patternApi.finishExecution(patternExecutionId.value, status);
      }
      return null;
    } catch (error) {
      patternExecutionError.value = toMessage(error, '패턴 실행 결과를 저장하지 못했습니다.');
      return null;
    } finally {
      patternExecutionId.value = null;
      executionLoggingEnabled.value = false;
      currentStepVisit.value = null;
    }
  }

  function startTransfer(options = {}) {
    if (!options.pattern) resetPatternExecution();
    transferAmount.value = '0';
    isNewAccountFlow.value = false;
    isPatternTransfer.value = Boolean(options.pattern);
    selectedSourceAccountId.value = defaultOwnedAccount.value?.accountId ?? null;
    selectedPersonId.value = options.personId ?? null;
    selectedRecipientAccountId.value = options.recipientAccountId ?? null;
    selectedAccountMasked.value = null;
    directRecipient.value = null;
    transferError.value = '';
    transferResult.value = null;
    anomaly.value = null;
    anomalyRechecked.value = false;
    notificationResult.value = null;
    transferCancelled.value = false;
    postTransferSyncError.value = '';
  }

  function cancelTransfer() {
    if (transferSubmitting.value || anomalyResolving.value) return false;
    if (activePatternDetail.value) {
      void finishPatternExecution('CANCELLED').finally(() => {
        startTransfer();
        void replaceWith('home');
      });
      return true;
    }
    startTransfer();
    void replaceWith('home');
    return true;
  }

  function selectPerson(personId) {
    selectedPersonId.value = personId;
    const accounts = accountsByPerson.value[personId] ?? [];
    selectedRecipientAccountId.value = accounts.length === 1 ? accounts[0].accountId : null;
    selectedAccountMasked.value = accounts.length === 1 ? accounts[0].masked : null;
  }

  function selectRecipientAccount(account) {
    selectedRecipientAccountId.value = account.accountId;
    selectedAccountMasked.value = account.masked;
  }

  function setDirectRecipient(recipient) {
    isNewAccountFlow.value = true;
    selectedPersonId.value = null;
    selectedRecipientAccountId.value = null;
    selectedAccountMasked.value = maskAccountNumber(recipient.accountNumber);
    directRecipient.value = recipient;
  }

  function createTransferPayload(pin) {
    const payload = {
      sourceAccountId: selectedSourceAccountId.value,
      amount: Number(transferAmount.value),
      pin,
    };
    if (patternExecutionId.value) payload.patternExecutionId = patternExecutionId.value;
    if (isNewAccountFlow.value) payload.directRecipient = directRecipient.value;
    else payload.registeredRecipientAccountId = selectedRecipientAccountId.value;
    return payload;
  }

  async function submitTransfer(pin) {
    if (transferSubmitting.value) return null;
    transferSubmitting.value = true;
    transferError.value = '';
    try {
      const response = await transferApi.createTransfer(createTransferPayload(pin));
      validateTransferResponse(response);
      if (response.status === 'REQUIRES_REVIEW') {
        anomaly.value = {
          ...response,
          recipient: isNewAccountFlow.value
            ? { ...directRecipient.value, masked: selectedAccountMasked.value }
            : {
                name: selectedPerson.value?.name,
                relationship: selectedPerson.value?.relation,
                bankName: selectedRecipientAccount.value?.bankName,
                masked: selectedRecipientAccount.value?.masked,
              },
          amount: Number(transferAmount.value),
        };
      } else {
        transferResult.value = {
          ...response,
          recipientName: isNewAccountFlow.value ? directRecipient.value?.name : selectedPerson.value?.name,
          amount: Number(transferAmount.value),
        };
        await refreshAfterTransfer();
        await finishPatternExecution('COMPLETED');
      }
      return response;
    } catch (error) {
      if (error?.code === 'PIN_MISMATCH') recordPatternAction('retry');
      transferError.value = toMessage(error, '송금 요청을 처리하지 못했습니다.');
      throw error;
    } finally {
      transferSubmitting.value = false;
    }
  }

  async function resolveAnomaly(action) {
    if (!anomaly.value?.anomalyEventId || anomalyResolving.value) return null;
    anomalyResolving.value = true;
    transferError.value = '';
    try {
      const anomalyEventId = anomaly.value.anomalyEventId;
      const response = await transferApi.resolveAnomaly(anomalyEventId, {
        action,
        rechecked: anomalyRechecked.value,
      });
      validateResolutionResponse(response, anomalyEventId);
      if (response.action === 'CONTINUE') {
        transferResult.value = {
          ...response,
          recipientName: anomaly.value.recipient?.name,
          amount: anomaly.value.amount,
        };
        await refreshAfterTransfer();
        await finishPatternExecution('COMPLETED');
      } else {
        transferCancelled.value = true;
        await finishPatternExecution('CANCELLED');
      }
      return response;
    } catch (error) {
      transferError.value = toMessage(error, '이상거래 결정을 처리하지 못했습니다.');
      throw error;
    } finally {
      anomalyResolving.value = false;
    }
  }

  async function sendGuardianNotification() {
    if (!anomaly.value?.anomalyEventId || anomaly.value.riskLevel !== 'HIGH'
        || notificationSending.value) return null;
    if (currentUser.value?.consents?.guardianShareAgreed === false) {
      const error = new ApiError(
        'GUARDIAN_SHARE_CONSENT_REQUIRED',
        '보호자 공유 동의 후 카카오 알림을 요청할 수 있습니다.',
        403,
      );
      transferError.value = error.message;
      throw error;
    }
    if (notificationResult.value) return notificationResult.value;
    notificationSending.value = true;
    transferError.value = '';
    try {
      const anomalyEventId = anomaly.value.anomalyEventId;
      const response = await supportApi.notifyGuardian(anomalyEventId);
      validateNotificationResponse(response, anomalyEventId);
      notificationResult.value = response;
      return notificationResult.value;
    } catch (error) {
      transferError.value = toMessage(error, '보호자 알림 요청을 처리하지 못했습니다.');
      throw error;
    } finally {
      notificationSending.value = false;
    }
  }

  async function refreshAfterTransfer() {
    postTransferSyncError.value = '';
    await loadFinancialData(true);
    if (selectedSourceAccountId.value) await loadInquiry(selectedSourceAccountId.value);
    const syncErrors = [financeError.value, inquiryError.value].filter(Boolean);
    if (syncErrors.length > 0) {
      postTransferSyncError.value = '송금은 완료됐지만 최신 잔액과 거래내역을 다시 불러오지 못했습니다.';
    }
  }

  return {
    screen,
    userName,
    currentUser,
    authStatus,
    authLoading,
    authError,
    loginStarting,
    devSessionStarting,
    logoutPending,
    sessionNotice,
    patterns,
    patternTemplates,
    patternLoading,
    patternLoaded,
    patternError,
    patternOrderSaving,
    patternMutationSaving,
    activePattern,
    activePatternDetail,
    patternStarting,
    patternExecutionId,
    executionLoggingEnabled,
    currentStepVisit,
    stepActionCounts,
    patternExecutionError,
    editingId,
    editingPersonId,
    homePage,
    toast,
    ownedAccounts,
    people,
    accountsByPerson,
    financeLoading,
    financeLoaded,
    financeError,
    financeWarning,
    selectedInquiryAccountId,
    inquiryBalance,
    inquiryTransactions,
    inquiryLoading,
    inquiryError,
    support,
    supportLoading,
    supportLoaded,
    supportError,
    guardianSaving,
    transferAmount,
    isNewAccountFlow,
    isPatternTransfer,
    selectedSourceAccountId,
    selectedPersonId,
    selectedRecipientAccountId,
    selectedAccountMasked,
    directRecipient,
    transferSubmitting,
    transferError,
    transferResult,
    anomaly,
    anomalyRechecked,
    anomalyResolving,
    notificationSending,
    notificationResult,
    transferCancelled,
    postTransferSyncError,
    defaultOwnedAccount,
    selectedSourceAccount,
    selectedPerson,
    selectedRecipientAccount,
    navigate,
    goBack,
    navTo,
    applyCurrentUser,
    clearSession,
    checkSession,
    startKakaoLogin,
    startDevSession,
    saveConsents,
    saveAccessibilitySettings,
    logout,
    showToast,
    clearToast,
    setPatterns,
    reorder,
    loadPatterns,
    loadPatternTemplates,
    loadPatternDetail,
    createPattern,
    updatePattern,
    deactivatePattern,
    startPatternExecution,
    syncPatternStep,
    recordPatternAction,
    recordPatternNavigation,
    finishPatternExecution,
    loadFinancialData,
    saveRegisteredPerson,
    loadInquiry,
    loadSupport,
    saveGuardian,
    startTransfer,
    cancelTransfer,
    selectPerson,
    selectRecipientAccount,
    setDirectRecipient,
    submitTransfer,
    resolveAnomaly,
    sendGuardianNotification,
    refreshAfterTransfer,
  };
});
