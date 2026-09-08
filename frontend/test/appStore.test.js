import assert from 'node:assert/strict';
import test from 'node:test';
import { createPinia, setActivePinia } from 'pinia';
import { useAppStore } from '../src/stores/appStore.js';

const originalFetch = globalThis.fetch;

function jsonResponse(body, status = 200) {
  return new Response(JSON.stringify(body), {
    status,
    headers: { 'Content-Type': 'application/json' },
  });
}

function createStore() {
  setActivePinia(createPinia());
  return useAppStore();
}

function prepareDirectTransfer(store) {
  store.ownedAccounts = [{
    accountId: 1,
    bankName: '신한은행',
    accountNumber: '110-000-000001',
    accountAlias: '생활비 통장',
    balance: 50000000,
    primary: true,
  }];
  store.startTransfer();
  store.selectedSourceAccountId = 1;
  store.transferAmount = '10000';
  store.setDirectRecipient({
    name: '박친구',
    bankCode: '004',
    bankName: '국민은행',
    accountNumber: '777-00-123456',
  });
}

function mockRefreshResponses(url) {
  if (url === '/api/accounts') {
    return jsonResponse([{
      accountId: 1,
      bankName: '신한은행',
      accountNumber: '110-000-000001',
      accountAlias: '생활비 통장',
      balance: 49990000,
      primary: true,
    }]);
  }
  if (url === '/api/registered-persons') return jsonResponse([]);
  if (url === '/api/accounts/1/balance') {
    return jsonResponse({ accountId: 1, balance: 49990000 });
  }
  if (url === '/api/accounts/1/transactions') return jsonResponse([]);
  throw new Error(`예상하지 못한 요청: ${url}`);
}

test.afterEach(() => {
  globalThis.fetch = originalFetch;
});

test('인증 세션은 서버 사용자와 접근성 설정을 적용하고 중복 조회하지 않는다', async () => {
  const store = createStore();
  let requestCount = 0;
  globalThis.fetch = async (url, options = {}) => {
    requestCount += 1;
    assert.equal(url, '/api/auth/session');
    assert.equal(options.credentials, 'include');
    return jsonResponse({
      authenticated: true,
      user: {
        userId: 1,
        name: '김단짝',
        consents: { completed: true, usageLogAgreed: true, guardianShareAgreed: false },
        settings: { fontSize: 'LARGE', voiceSpeed: 'SLOW', guideVoiceType: 'TTS' },
      },
    });
  };

  assert.equal(await store.checkSession(true), true);
  assert.equal(await store.checkSession(), true);
  assert.equal(store.authStatus, 'authenticated');
  assert.equal(store.userName, '김단짝');
  assert.equal(store.currentUser.settings.fontSize, 'LARGE');
  assert.equal(store.currentUser.settings.voiceSpeed, 'SLOW');
  assert.equal(requestCount, 1);
});

test('선택 동의와 접근성 설정 저장 응답을 현재 세션 상태에 반영한다', async () => {
  const store = createStore();
  store.applyCurrentUser({
    userId: 1,
    name: '김단짝',
    consents: { completed: false, usageLogAgreed: false, guardianShareAgreed: false },
    settings: { fontSize: 'NORMAL', voiceSpeed: 'NORMAL', guideVoiceType: 'TTS' },
  });
  const requests = [];
  globalThis.fetch = async (url, options = {}) => {
    requests.push({ url, method: options.method, body: JSON.parse(options.body) });
    if (url === '/api/users/me/consents') {
      return jsonResponse({ completed: true, usageLogAgreed: true, guardianShareAgreed: false });
    }
    if (url === '/api/users/me/settings') {
      return jsonResponse({ fontSize: 'LARGE', voiceSpeed: 'SLOW', guideVoiceType: 'TTS' });
    }
    throw new Error(`예상하지 못한 요청: ${url}`);
  };

  await store.saveConsents({ usageLogAgreed: true, guardianShareAgreed: false });
  await store.saveAccessibilitySettings({ fontSize: 'LARGE', voiceSpeed: 'SLOW', guideVoiceType: 'TTS' });

  assert.equal(store.currentUser.consents.completed, true);
  assert.equal(store.currentUser.consents.usageLogAgreed, true);
  assert.equal(store.currentUser.settings.fontSize, 'LARGE');
  assert.equal(store.currentUser.settings.voiceSpeed, 'SLOW');
  assert.deepEqual(requests, [
    {
      url: '/api/users/me/consents',
      method: 'PUT',
      body: { usageLogAgreed: true, guardianShareAgreed: false },
    },
    {
      url: '/api/users/me/settings',
      method: 'PUT',
      body: { fontSize: 'LARGE', voiceSpeed: 'SLOW', guideVoiceType: 'TTS' },
    },
  ]);
});

test('직접 송금 완료 응답과 서버 재조회 결과를 상태에 반영한다', async () => {
  const store = createStore();
  prepareDirectTransfer(store);
  let submittedBody;

  globalThis.fetch = async (url, options = {}) => {
    if (url === '/api/transfers') {
      submittedBody = JSON.parse(options.body);
      return jsonResponse({
        status: 'COMPLETED',
        riskLevel: 'NORMAL',
        reasons: [],
        recentTransferCount: 0,
        anomalyEventId: null,
        transactionId: 52,
        balanceAfter: 49990000,
      });
    }
    return mockRefreshResponses(url);
  };

  const result = await store.submitTransfer('1234');

  assert.equal(result.status, 'COMPLETED');
  assert.equal(store.transferResult.transactionId, 52);
  assert.equal(store.transferResult.recipientName, '박친구');
  assert.equal(store.transferResult.balanceAfter, 49990000);
  assert.equal(store.ownedAccounts[0].balance, 49990000);
  assert.deepEqual(submittedBody.directRecipient, {
    name: '박친구',
    bankCode: '004',
    bankName: '국민은행',
    accountNumber: '777-00-123456',
  });
  assert.equal('registeredRecipientAccountId' in submittedBody, false);
  assert.equal(store.people.length, 0);
});

test('등록 수취 계좌 송금은 등록 계좌 식별자만 요청에 사용한다', async () => {
  const store = createStore();
  store.ownedAccounts = [{ accountId: 1, primary: true, balance: 50000000 }];
  store.people = [{ id: 7, name: '김민수', relation: '아들' }];
  store.accountsByPerson = {
    7: [{ accountId: 3, bankCode: '020', bankName: '우리은행', masked: '100-****-001' }],
  };
  store.startTransfer();
  store.selectedSourceAccountId = 1;
  store.selectPerson(7);
  store.transferAmount = '10000';
  let submittedBody;
  globalThis.fetch = async (url, options = {}) => {
    if (url === '/api/transfers') {
      submittedBody = JSON.parse(options.body);
      return jsonResponse({
        status: 'COMPLETED',
        riskLevel: 'NORMAL',
        reasons: [],
        recentTransferCount: 0,
        anomalyEventId: null,
        transactionId: 53,
        balanceAfter: 49990000,
      });
    }
    return mockRefreshResponses(url);
  };

  await store.submitTransfer('1234');

  assert.equal(submittedBody.registeredRecipientAccountId, 3);
  assert.equal('directRecipient' in submittedBody, false);
  assert.equal(store.transferResult.recipientName, '김민수');
});

test('PIN 오류는 완료 상태를 만들지 않고 안전한 송금 입력을 유지한다', async () => {
  const store = createStore();
  prepareDirectTransfer(store);
  globalThis.fetch = async () => jsonResponse({
    code: 'PIN_MISMATCH',
    message: '출금 계좌 PIN이 일치하지 않습니다.',
  }, 422);

  await assert.rejects(store.submitTransfer('0000'), (error) => {
    assert.equal(error.code, 'PIN_MISMATCH');
    return true;
  });

  assert.equal(store.transferResult, null);
  assert.equal(store.transferAmount, '10000');
  assert.equal(store.directRecipient.name, '박친구');
  assert.equal(store.transferError, '계좌 비밀번호가 맞지 않습니다. 다시 입력해 주세요.');
  assert.equal(store.transferSubmitting, false);
});

test('응답 대기 중 같은 송금 요청을 한 번만 전송한다', async () => {
  const store = createStore();
  prepareDirectTransfer(store);
  let completeRequest;
  let requestCount = 0;

  globalThis.fetch = () => {
    requestCount += 1;
    return new Promise((resolve) => {
      completeRequest = resolve;
    });
  };

  const first = store.submitTransfer('1234');
  const second = await store.submitTransfer('1234');

  assert.equal(second, null);
  assert.equal(requestCount, 1);
  completeRequest(jsonResponse({ code: 'PIN_MISMATCH', message: 'PIN 오류' }, 422));
  await assert.rejects(first);
  assert.equal(store.transferSubmitting, false);
});

test('서버의 HIGH 응답만으로 이상거래 상태를 구성한다', async () => {
  const store = createStore();
  prepareDirectTransfer(store);
  store.transferAmount = '10000000';
  globalThis.fetch = async () => jsonResponse({
    status: 'REQUIRES_REVIEW',
    riskLevel: 'HIGH',
    reasons: ['HIGH_AMOUNT', 'REPEATED_TRANSFER'],
    recentTransferCount: 2,
    anomalyEventId: 9,
    transactionId: null,
    balanceAfter: 50000000,
  });

  await store.submitTransfer('1234');

  assert.equal(store.transferResult, null);
  assert.equal(store.anomaly.anomalyEventId, 9);
  assert.equal(store.anomaly.riskLevel, 'HIGH');
  assert.deepEqual(store.anomaly.reasons, ['HIGH_AMOUNT', 'REPEATED_TRANSFER']);
  assert.equal(store.anomaly.recipient.name, '박친구');
  assert.equal(store.anomaly.amount, 10000000);
});

test('모순된 완료 응답은 성공으로 표시하지 않는다', async () => {
  const store = createStore();
  prepareDirectTransfer(store);
  globalThis.fetch = async () => jsonResponse({
    status: 'COMPLETED',
    riskLevel: 'NORMAL',
    reasons: [],
    recentTransferCount: 0,
    anomalyEventId: 4,
    transactionId: 52,
    balanceAfter: 49990000,
  });

  await assert.rejects(store.submitTransfer('1234'), (error) => {
    assert.equal(error.code, 'INVALID_RESPONSE');
    return true;
  });

  assert.equal(store.transferResult, null);
  assert.equal(store.transferError, '서버 응답을 확인할 수 없습니다. 거래내역에서 처리 결과를 확인해 주세요.');
});

test('이미 취소된 이상거래의 저장 결과를 그대로 따른다', async () => {
  const store = createStore();
  store.anomaly = {
    anomalyEventId: 12,
    recipient: { name: '박친구' },
    amount: 10000000,
  };
  globalThis.fetch = async () => jsonResponse({
    anomalyEventId: 12,
    action: 'CANCEL',
    transactionId: null,
    balanceAfter: null,
  });

  const result = await store.resolveAnomaly('CONTINUE');

  assert.equal(result.action, 'CANCEL');
  assert.equal(store.transferCancelled, true);
  assert.equal(store.transferResult, null);
});

test('이상거래 결정 실패는 같은 anomaly ID와 안전한 입력을 유지한다', async () => {
  const store = createStore();
  store.anomaly = {
    anomalyEventId: 13,
    riskLevel: 'MEDIUM',
    recipient: { name: '박친구' },
    amount: 10000000,
  };
  store.transferAmount = '10000000';
  globalThis.fetch = async () => jsonResponse({
    code: 'TEMPORARY_ERROR',
    message: '잠시 후 다시 시도해 주세요.',
  }, 503);

  await assert.rejects(store.resolveAnomaly('CONTINUE'));

  assert.equal(store.anomaly.anomalyEventId, 13);
  assert.equal(store.anomaly.recipient.name, '박친구');
  assert.equal(store.transferAmount, '10000000');
  assert.equal(store.transferResult, null);
  assert.equal(store.anomalyResolving, false);
  assert.equal(store.transferError, '잠시 후 다시 시도해 주세요.');
});

test('본인 계좌와 등록 수취 계좌를 분리하고 기본 계좌를 선택한다', async () => {
  const store = createStore();
  globalThis.fetch = async (url) => {
    if (url === '/api/accounts') {
      return jsonResponse([
        {
          accountId: 1,
          bankName: '신한은행',
          accountNumber: '110-000-000001',
          accountAlias: '생활비 통장',
          balance: 50000000,
          primary: true,
        },
        {
          accountId: 2,
          bankName: '국민은행',
          accountNumber: '123-000-000002',
          accountAlias: '저축 통장',
          balance: 30000000,
          primary: false,
        },
      ]);
    }
    if (url === '/api/registered-persons') {
      return jsonResponse([{
        registeredPersonId: 7,
        name: '김민수',
        relationship: '아들',
        account: {
          accountId: 3,
          bankCode: '020',
          bankName: '우리은행',
          accountNumber: '100-000-000001',
          accountAlias: '민수 계좌',
        },
      }]);
    }
    throw new Error(`예상하지 못한 요청: ${url}`);
  };

  await store.loadFinancialData();

  assert.equal(store.ownedAccounts.length, 2);
  assert.equal(store.selectedSourceAccountId, 1);
  assert.equal(store.selectedInquiryAccountId, 1);
  assert.equal(store.ownedAccounts.some((account) => account.accountId === 3), false);
  assert.equal(store.accountsByPerson[7][0].accountId, 3);
  assert.equal(store.accountsByPerson[7][0].balance, undefined);
});

test('금융 목록 재조회 실패 시 오래된 상세 데이터를 제거한다', async () => {
  const store = createStore();
  store.ownedAccounts = [{ accountId: 99, balance: 1 }];
  store.people = [{ id: 99, name: '오래된 인물' }];
  store.accountsByPerson = { 99: [{ accountId: 100 }] };
  store.selectedSourceAccountId = 99;
  store.selectedInquiryAccountId = 99;
  store.selectedPersonId = 99;
  store.selectedRecipientAccountId = 100;
  globalThis.fetch = async () => {
    throw new TypeError('network down');
  };

  await store.loadFinancialData(true);

  assert.deepEqual(store.ownedAccounts, []);
  assert.deepEqual(store.people, []);
  assert.deepEqual(store.accountsByPerson, {});
  assert.equal(store.selectedSourceAccountId, null);
  assert.equal(store.selectedInquiryAccountId, null);
  assert.equal(store.selectedPersonId, null);
  assert.equal(store.selectedRecipientAccountId, null);
  assert.equal(store.financeLoaded, false);
  assert.equal(store.financeError, '계좌와 등록 인물을 불러오지 못했습니다.');
});

test('거래 조회 실패 시 이전 계좌의 잔액과 거래를 남기지 않는다', async () => {
  const store = createStore();
  store.inquiryBalance = { accountId: 2, balance: 30000000 };
  store.inquiryTransactions = [{ transactionId: 1 }];
  globalThis.fetch = async (url) => {
    if (url === '/api/accounts/1/balance') {
      return jsonResponse({ accountId: 1, balance: 50000000 });
    }
    throw new TypeError('network down');
  };

  await store.loadInquiry(1);

  assert.equal(store.inquiryBalance, null);
  assert.deepEqual(store.inquiryTransactions, []);
  assert.equal(store.inquiryError, '금융 정보를 불러오지 못했습니다.');
});

test('보호자 저장 응답을 이후 전화 연결의 기준 상태로 사용한다', async () => {
  const store = createStore();
  store.support = {
    guardian: { guardianContactId: 1, phoneNumber: '010-0000-1004' },
    customerCenterPhone: '1588-0000',
  };
  let requestBody;
  globalThis.fetch = async (url, options = {}) => {
    assert.equal(url, '/api/support/guardian');
    requestBody = JSON.parse(options.body);
    return jsonResponse({ guardianContactId: 1, phoneNumber: '010-1111-2222' });
  };

  const saved = await store.saveGuardian('010-1111-2222');

  assert.deepEqual(requestBody, { phoneNumber: '010-1111-2222' });
  assert.equal(saved.phoneNumber, '010-1111-2222');
  assert.equal(store.support.guardian.phoneNumber, '010-1111-2222');
});

test('보호자 저장 요청이 진행 중이면 같은 서버 응답을 공유한다', async () => {
  const store = createStore();
  let completeRequest;
  let requestCount = 0;
  globalThis.fetch = () => {
    requestCount += 1;
    return new Promise((resolve) => {
      completeRequest = resolve;
    });
  };

  const first = store.saveGuardian('010-2222-3333');
  const second = store.saveGuardian('010-2222-3333');
  assert.equal(requestCount, 1);

  completeRequest(jsonResponse({ guardianContactId: 1, phoneNumber: '010-2222-3333' }));
  const [firstResult, secondResult] = await Promise.all([first, second]);

  assert.deepEqual(secondResult, firstResult);
  assert.equal(store.support.guardian.phoneNumber, '010-2222-3333');
  assert.equal(store.guardianSaving, false);
});

test('부분 보호자 저장 상태는 고객센터를 포함한 전체 조회를 막지 않는다', async () => {
  const store = createStore();
  let supportReadCount = 0;
  globalThis.fetch = async (url, options = {}) => {
    if (url === '/api/support/guardian' && options.method === 'PUT') {
      return jsonResponse({ guardianContactId: 1, phoneNumber: '010-3333-4444' });
    }
    if (url === '/api/support') {
      supportReadCount += 1;
      return jsonResponse({
        guardian: { guardianContactId: 1, phoneNumber: '010-3333-4444' },
        customerCenterPhone: '1588-0000',
      });
    }
    throw new Error(`예상하지 못한 요청: ${url}`);
  };

  await store.saveGuardian('010-3333-4444');
  assert.equal(store.supportLoaded, false);
  await store.loadSupport();

  assert.equal(supportReadCount, 1);
  assert.equal(store.supportLoaded, true);
  assert.equal(store.support.customerCenterPhone, '1588-0000');
});

test('인물 생성 성공 후 서버 목록을 재조회해 새 식별자를 반영한다', async () => {
  const store = createStore();
  let createBody;
  globalThis.fetch = async (url, options = {}) => {
    if (url === '/api/registered-persons' && options.method === 'POST') {
      createBody = JSON.parse(options.body);
      return jsonResponse({
        registeredPersonId: 3,
        name: '박친구',
        relationship: '친구',
        account: {
          accountId: 5,
          bankCode: '004',
          bankName: '국민은행',
          accountNumber: '999-11-223344',
          accountAlias: null,
        },
      }, 201);
    }
    if (url === '/api/accounts') return jsonResponse([]);
    if (url === '/api/registered-persons') {
      return jsonResponse([{
        registeredPersonId: 3,
        name: '박친구',
        relationship: '친구',
        account: {
          accountId: 5,
          bankCode: '004',
          bankName: '국민은행',
          accountNumber: '999-11-223344',
          accountAlias: null,
        },
      }]);
    }
    throw new Error(`예상하지 못한 요청: ${url}`);
  };

  const saved = await store.saveRegisteredPerson({
    name: '박친구',
    relationship: '친구',
    bankCode: '004',
    bankName: '국민은행',
    accountNumber: '999-11-223344',
    accountAlias: null,
  });

  assert.equal(saved.registeredPersonId, 3);
  assert.equal(store.people[0].id, 3);
  assert.equal(store.accountsByPerson[3][0].accountId, 5);
  assert.equal(createBody.bankCode, '004');
});

test('인물 저장 중 기존 목록 조회가 끝나도 저장 후 목록을 다시 조회한다', async () => {
  const store = createStore();
  const initialResolvers = [];
  let accountReadCount = 0;
  let personReadCount = 0;
  globalThis.fetch = async (url, options = {}) => {
    if (url === '/api/accounts') {
      accountReadCount += 1;
      if (accountReadCount === 1) {
        return new Promise((resolve) => initialResolvers.push(() => resolve(jsonResponse([]))));
      }
      return jsonResponse([]);
    }
    if (url === '/api/registered-persons' && options.method === 'POST') {
      return jsonResponse({
        registeredPersonId: 4,
        name: '이정훈',
        relationship: '사위',
        account: {
          accountId: 6,
          bankCode: '088',
          bankName: '신한은행',
          accountNumber: '110-222-333333',
          accountAlias: null,
        },
      }, 201);
    }
    if (url === '/api/registered-persons') {
      personReadCount += 1;
      if (personReadCount === 1) {
        return new Promise((resolve) => initialResolvers.push(() => resolve(jsonResponse([]))));
      }
      return jsonResponse([{
        registeredPersonId: 4,
        name: '이정훈',
        relationship: '사위',
        account: {
          accountId: 6,
          bankCode: '088',
          bankName: '신한은행',
          accountNumber: '110-222-333333',
          accountAlias: null,
        },
      }]);
    }
    throw new Error(`예상하지 못한 요청: ${url}`);
  };

  const initialLoad = store.loadFinancialData();
  await Promise.resolve();
  const save = store.saveRegisteredPerson({
    name: '이정훈',
    relationship: '사위',
    bankCode: '088',
    bankName: '신한은행',
    accountNumber: '110-222-333333',
    accountAlias: null,
  });
  for (const resolve of initialResolvers) resolve();
  await Promise.all([initialLoad, save]);

  assert.equal(accountReadCount, 2);
  assert.equal(personReadCount, 2);
  assert.equal(store.people[0].id, 4);
  assert.equal(store.accountsByPerson[4][0].accountId, 6);
});

test('보호자 Mock 알림 결과를 한 이상거래에 한 번만 요청한다', async () => {
  const store = createStore();
  store.anomaly = { anomalyEventId: 22, riskLevel: 'HIGH' };
  let requestCount = 0;
  globalThis.fetch = async () => {
    requestCount += 1;
    return jsonResponse({
      anomalyEventId: 22,
      deliveryMode: 'MOCK',
      result: 'MOCKED_NO_TOKEN',
      actualAttempted: false,
      actualSucceeded: false,
      detail: '카카오 토큰이 없어 Mock 알림으로 대체했습니다.',
      sentAt: null,
    });
  };

  const first = await store.sendGuardianNotification();
  const second = await store.sendGuardianNotification();

  assert.equal(first.result, 'MOCKED_NO_TOKEN');
  assert.equal(second.result, 'MOCKED_NO_TOKEN');
  assert.equal(requestCount, 1);
});

test('보호자 공유 동의 거부 시 알림 API를 호출하지 않는다', async () => {
  const store = createStore();
  store.currentUser = {
    consents: { completed: true, usageLogAgreed: true, guardianShareAgreed: false },
  };
  store.anomaly = { anomalyEventId: 22, riskLevel: 'HIGH' };
  let requestCount = 0;
  globalThis.fetch = async () => {
    requestCount += 1;
    return jsonResponse({});
  };

  await assert.rejects(store.sendGuardianNotification(), (error) => {
    assert.equal(error.code, 'GUARDIAN_SHARE_CONSENT_REQUIRED');
    return true;
  });
  assert.equal(requestCount, 0);
});

test('보호자 알림 실패 후에도 같은 이상거래를 취소할 수 있다', async () => {
  const store = createStore();
  store.anomaly = {
    anomalyEventId: 25,
    riskLevel: 'HIGH',
    recipient: { name: '김민수' },
    amount: 10000000,
  };
  globalThis.fetch = async (url) => {
    if (url.endsWith('/guardian-notification')) {
      return jsonResponse({ code: 'KAKAO_PROVIDER_ERROR', message: '알림 제공자 오류' }, 502);
    }
    if (url.endsWith('/resolve')) {
      return jsonResponse({
        anomalyEventId: 25,
        action: 'CANCEL',
        transactionId: null,
        balanceAfter: null,
      });
    }
    throw new Error(`예상하지 못한 요청: ${url}`);
  };

  await assert.rejects(store.sendGuardianNotification());
  assert.equal(store.notificationResult, null);
  assert.equal(store.anomaly.anomalyEventId, 25);

  const resolution = await store.resolveAnomaly('CANCEL');
  assert.equal(resolution.action, 'CANCEL');
  assert.equal(store.transferCancelled, true);
});

test('기본 계좌가 없으면 첫 번째 본인 계좌를 선택하고 데이터 경고를 남긴다', async () => {
  const store = createStore();
  globalThis.fetch = async (url) => {
    if (url === '/api/accounts') {
      return jsonResponse([
        { accountId: 8, bankName: '우리은행', accountNumber: '100-1', balance: 10, primary: false },
        { accountId: 9, bankName: '국민은행', accountNumber: '200-2', balance: 20, primary: false },
      ]);
    }
    if (url === '/api/registered-persons') return jsonResponse([]);
    throw new Error(`예상하지 못한 요청: ${url}`);
  };

  await store.loadFinancialData();

  assert.equal(store.selectedSourceAccountId, 8);
  assert.equal(store.selectedInquiryAccountId, 8);
  assert.equal(store.financeWarning, '기본 계좌가 지정되지 않아 첫 번째 본인 계좌를 선택했어요.');
});

test('송금 요청 처리 중에는 로컬 취소로 상태를 지우지 않는다', async () => {
  const store = createStore();
  prepareDirectTransfer(store);
  store.screen = 'pin-entry';
  let completeRequest;
  globalThis.fetch = () => new Promise((resolve) => {
    completeRequest = resolve;
  });

  const submission = store.submitTransfer('1234');
  const cancelled = store.cancelTransfer();

  assert.equal(cancelled, false);
  assert.equal(store.screen, 'pin-entry');
  assert.equal(store.directRecipient.name, '박친구');
  completeRequest(jsonResponse({ code: 'PIN_MISMATCH', message: 'PIN 오류' }, 422));
  await assert.rejects(submission);
});

test('MEDIUM 이상거래는 보호자 알림 API를 호출하지 않는다', async () => {
  const store = createStore();
  store.anomaly = { anomalyEventId: 23, riskLevel: 'MEDIUM' };
  let requestCount = 0;
  globalThis.fetch = async () => {
    requestCount += 1;
    return jsonResponse({});
  };

  const result = await store.sendGuardianNotification();

  assert.equal(result, null);
  assert.equal(requestCount, 0);
});

test('실제 알림의 발송 시각이 유효하지 않으면 성공으로 표시하지 않는다', async () => {
  const store = createStore();
  store.anomaly = { anomalyEventId: 24, riskLevel: 'HIGH' };
  globalThis.fetch = async () => jsonResponse({
    anomalyEventId: 24,
    deliveryMode: 'ACTUAL',
    result: 'SENT',
    actualAttempted: true,
    actualSucceeded: true,
    detail: '발송 완료',
    sentAt: 'not-a-date',
  });

  await assert.rejects(store.sendGuardianNotification(), (error) => {
    assert.equal(error.code, 'INVALID_RESPONSE');
    return true;
  });
  assert.equal(store.notificationResult, null);
});

test('인물 수정은 기존 식별자의 PUT 요청 후 서버 목록으로 갱신한다', async () => {
  const store = createStore();
  let updateBody;
  globalThis.fetch = async (url, options = {}) => {
    if (url === '/api/registered-persons/2' && options.method === 'PUT') {
      updateBody = JSON.parse(options.body);
      return jsonResponse({
        registeredPersonId: 2,
        name: '김지영',
        relationship: '보호자',
        account: {
          accountId: 4,
          bankCode: '081',
          bankName: '하나은행',
          accountNumber: '333-44-555555',
          accountAlias: '지영 계좌',
        },
      });
    }
    if (url === '/api/accounts') return jsonResponse([]);
    if (url === '/api/registered-persons') {
      return jsonResponse([{
        registeredPersonId: 2,
        name: '김지영',
        relationship: '보호자',
        account: {
          accountId: 4,
          bankCode: '081',
          bankName: '하나은행',
          accountNumber: '333-44-555555',
          accountAlias: '지영 계좌',
        },
      }]);
    }
    throw new Error(`예상하지 못한 요청: ${url}`);
  };

  await store.saveRegisteredPerson({
    name: '김지영',
    relationship: '보호자',
    bankCode: '081',
    bankName: '하나은행',
    accountNumber: '333-44-555555',
    accountAlias: '지영 계좌',
  }, 2);

  assert.equal(updateBody.relationship, '보호자');
  assert.equal(store.people[0].id, 2);
  assert.equal(store.people[0].relation, '보호자');
  assert.equal(store.accountsByPerson[2][0].accountId, 4);
});

test('카테고리 조회는 서버 필터 쿼리를 사용하고 잔액과 결과를 함께 반영한다', async () => {
  const store = createStore();
  const urls = [];
  globalThis.fetch = async (url) => {
    urls.push(url);
    if (url === '/api/accounts/1/balance') {
      return jsonResponse({ accountId: 1, balance: 50000000 });
    }
    if (url === '/api/accounts/1/transactions?category=UTILITY_BILL') {
      return jsonResponse([{ transactionId: 31, category: 'UTILITY_BILL' }]);
    }
    throw new Error(`예상하지 못한 요청: ${url}`);
  };

  await store.loadInquiry(1, 'UTILITY_BILL');

  assert.deepEqual(urls.sort(), [
    '/api/accounts/1/balance',
    '/api/accounts/1/transactions?category=UTILITY_BILL',
  ]);
  assert.equal(store.inquiryBalance.balance, 50000000);
  assert.equal(store.inquiryTransactions[0].category, 'UTILITY_BILL');
});

test('늦게 끝난 이전 계좌 조회가 현재 선택 결과를 덮지 않는다', async () => {
  const store = createStore();
  const firstResolvers = [];
  globalThis.fetch = async (url) => {
    if (url.startsWith('/api/accounts/1/')) {
      return new Promise((resolve) => firstResolvers.push({ url, resolve }));
    }
    if (url === '/api/accounts/2/balance') {
      return jsonResponse({ accountId: 2, balance: 30000000 });
    }
    if (url === '/api/accounts/2/transactions') {
      return jsonResponse([{ transactionId: 202, category: 'GENERAL' }]);
    }
    throw new Error(`예상하지 못한 요청: ${url}`);
  };

  const first = store.loadInquiry(1);
  await Promise.resolve();
  await store.loadInquiry(2);
  for (const request of firstResolvers) {
    request.resolve(request.url.endsWith('/balance')
      ? jsonResponse({ accountId: 1, balance: 50000000 })
      : jsonResponse([{ transactionId: 101, category: 'GENERAL' }]));
  }
  await first;

  assert.equal(store.selectedInquiryAccountId, 2);
  assert.equal(store.inquiryBalance.accountId, 2);
  assert.equal(store.inquiryTransactions[0].transactionId, 202);
  assert.equal(store.inquiryLoading, false);
});

test('패턴은 명시적 시작 요청 뒤에만 실행 식별자를 사용한다', async () => {
  const store = createStore();
  const requests = [];
  const detail = {
    patternId: 2,
    shortcutNumber: 2,
    patternType: 'BALANCE_CHECK',
    title: '잔액 확인',
    description: '잔액을 확인합니다.',
    linkedAccount: null,
    steps: [{
      stepId: 21,
      stepOrder: 1,
      stepCode: 'CHECK_RESULT',
      stepName: '잔액 확인',
      instructionText: '잔액을 확인해 주세요.',
      screenCode: 'task-4',
    }],
  };
  globalThis.fetch = async (url, options = {}) => {
    requests.push({ url, body: options.body ? JSON.parse(options.body) : null });
    if (url === '/api/patterns/2/executions') {
      return jsonResponse({ loggingEnabled: true, executionId: 90, startedAt: '2026-09-06T10:00:00', pattern: detail }, 201);
    }
    if (url === '/api/pattern-executions/90/visits') {
      return jsonResponse({
        visitId: 901,
        executionId: 90,
        stepId: 21,
        visitNumber: 1,
        retryCount: 0,
        backCount: 0,
        wrongTouchCount: 0,
        routeDeviation: false,
        completed: false,
        startedAt: '2026-09-06T10:00:01',
        endedAt: null,
        durationSeconds: null,
      }, 201);
    }
    throw new Error(`예상하지 못한 요청: ${url}`);
  };

  assert.equal(requests.length, 0);
  await store.startPatternExecution({ patternId: 2, patternType: 'BALANCE_CHECK' });
  await store.syncPatternStep('task-4');

  assert.equal(store.patternExecutionId, 90);
  assert.equal(store.currentStepVisit.visitNumber, 1);
  assert.deepEqual(requests.map((item) => item.url), [
    '/api/patterns/2/executions',
    '/api/pattern-executions/90/visits',
  ]);
});

test('이용 기록 거부 응답에서는 단계 로그 요청을 보내지 않는다', async () => {
  const store = createStore();
  const requests = [];
  const detail = {
    patternId: 4,
    shortcutNumber: 4,
    patternType: 'BALANCE_CHECK',
    title: '잔액 확인',
    description: '잔액을 확인합니다.',
    linkedAccount: null,
    steps: [{
      stepId: 41,
      stepOrder: 1,
      stepCode: 'CHECK_RESULT',
      stepName: '잔액 확인',
      instructionText: '잔액을 확인해 주세요.',
      screenCode: 'task-4',
    }],
  };
  globalThis.fetch = async (url) => {
    requests.push(url);
    if (url === '/api/patterns/4/executions') {
      return jsonResponse({ loggingEnabled: false, executionId: null, startedAt: null, pattern: detail }, 201);
    }
    throw new Error(`예상하지 못한 요청: ${url}`);
  };

  await store.startPatternExecution({ patternId: 4, patternType: 'BALANCE_CHECK' });
  await store.syncPatternStep('task-4');

  assert.equal(store.executionLoggingEnabled, false);
  assert.equal(store.patternExecutionId, null);
  assert.deepEqual(requests, ['/api/patterns/4/executions']);
});

test('패턴 단계 밖으로 이동하면 현재 방문에 경로 이탈을 저장한다', async () => {
  const store = createStore();
  const updateBodies = [];
  const detail = {
    patternId: 2,
    shortcutNumber: 2,
    patternType: 'BALANCE_CHECK',
    title: '잔액 확인',
    description: '잔액을 확인합니다.',
    linkedAccount: null,
    steps: [{
      stepId: 21,
      stepOrder: 1,
      stepCode: 'CHECK_RESULT',
      stepName: '잔액 확인',
      instructionText: '잔액을 확인해 주세요.',
      screenCode: 'task-4',
    }],
  };
  globalThis.fetch = async (url, options = {}) => {
    if (url === '/api/patterns/2/executions') {
      return jsonResponse({ loggingEnabled: true, executionId: 90, startedAt: '2026-09-06T10:00:00', pattern: detail }, 201);
    }
    if (url === '/api/pattern-executions/90/visits') {
      return jsonResponse({
        visitId: 901,
        executionId: 90,
        stepId: 21,
        visitNumber: 1,
        retryCount: 0,
        backCount: 0,
        wrongTouchCount: 0,
        routeDeviation: false,
        completed: false,
        startedAt: '2026-09-06T10:00:01',
        endedAt: null,
        durationSeconds: null,
      }, 201);
    }
    if (url === '/api/pattern-executions/90/visits/901') {
      updateBodies.push(JSON.parse(options.body));
      return jsonResponse({});
    }
    throw new Error(`예상하지 못한 요청: ${url}`);
  };

  await store.startPatternExecution({ patternId: 2, patternType: 'BALANCE_CHECK' });
  await store.syncPatternStep('task-4');
  await store.syncPatternStep('home');

  assert.equal(updateBodies.length, 1);
  assert.equal(updateBodies[0].routeDeviation, true);
  assert.equal(updateBodies[0].completed, true);
});

test('단축번호 순서 저장 중에는 두 번째 이동 요청을 보내지 않는다', async () => {
  const store = createStore();
  store.setPatterns([
    { patternId: 1, num: 1, label: '잔액 확인', patternType: 'BALANCE_CHECK' },
    { patternId: 2, num: 2, label: '거래내역', patternType: 'TRANSACTION_HISTORY' },
  ]);
  let requestCount = 0;
  let completeRequest;
  globalThis.fetch = async () => {
    requestCount += 1;
    return new Promise((resolve) => { completeRequest = resolve; });
  };

  const first = store.reorder(1, 2);
  await Promise.resolve();
  const second = await store.reorder(2, 1);

  assert.equal(second, false);
  assert.equal(requestCount, 1);
  completeRequest(jsonResponse([
    { patternId: 1, shortcutNumber: 2, patternType: 'BALANCE_CHECK', title: '잔액 확인', description: '', linkedAccount: null },
    { patternId: 2, shortcutNumber: 1, patternType: 'TRANSACTION_HISTORY', title: '거래내역', description: '', linkedAccount: null },
  ]));
  assert.equal(await first, true);
  assert.equal(store.patternOrderSaving, false);
});

test('이전 패턴 단계로 이동하면 브라우저 방식과 무관하게 back 행동을 기록한다', async () => {
  const store = createStore();
  store.activePatternDetail = {
    steps: [
      { stepId: 21, stepOrder: 1, screenCode: 'task-4' },
      { stepId: 22, stepOrder: 2, screenCode: 'task-5' },
    ],
  };
  store.patternExecutionId = 90;
  store.executionLoggingEnabled = true;
  store.currentStepVisit = { visitId: 902, stepId: 22 };
  const updates = [];
  globalThis.fetch = async (url, options = {}) => {
    if (url === '/api/pattern-executions/90/visits/902') {
      updates.push(JSON.parse(options.body));
      return jsonResponse({});
    }
    if (url === '/api/pattern-executions/90/visits') {
      return jsonResponse({ visitId: 903, stepId: 21, visitNumber: 1 }, 201);
    }
    throw new Error(`예상하지 못한 요청: ${url}`);
  };

  store.recordPatternNavigation('task-5', 'task-4');
  await store.syncPatternStep('task-4');

  assert.equal(updates[0].backCount, 1);
  assert.equal(updates.at(-1).completed, true);
});
