const TRANSFER_CONTEXT_ROUTES = new Set([
  "direct-transfer",
  "direct-newaccount",
  "guide-person",
  "guide-account",
  "amount-input",
  "pin-entry",
  "fraud-warning",
  "final-confirm",
]);

const PUBLIC_ENTRY_ROUTES = new Set(["onboarding", "feature-intro", "login"]);

export function createAppNavigationGuard(store) {
  return async (to) => {
    const authenticated = await store.checkSession();
    const consentCompleted = Boolean(store.currentUser?.consents?.completed);
    const hadTransferSource = Boolean(store.selectedSourceAccountId);

    // 인증이 필요한 화면에 비로그인 사용자가 접근한 경우
    if (to.meta.requiresAuth && !authenticated) {
      return {
        name: "login",
        query: {
          redirect: to.fullPath,
        },
      };
    }

    // 로그인된 사용자가 시작·소개·로그인 화면에 접근한 경우
    if (authenticated && PUBLIC_ENTRY_ROUTES.has(String(to.name))) {
      return {
        name: consentCompleted ? "home" : "consent",
      };
    }

    // 이미 동의를 완료한 사용자가 동의 화면에 접근한 경우
    if (to.name === "consent" && consentCompleted && to.query.edit !== "1") {
      return {
        name: "home",
      };
    }

    // 필수 동의를 완료하지 않은 사용자의 보호 화면 접근 차단
    if (to.meta.requiresConsent && !consentCompleted) {
      return {
        name: "consent",
      };
    }

    if (authenticated && (to.name === "home" || to.meta.requiresAccounts)) {
      const loaded = await store.loadOwnedAccounts();
      if (!loaded || !store.ownedAccounts.length) return { name: "owned-account-import" };
    }

    // 송금 과정에 필요한 출금 계좌가 없는 경우
    if (
      TRANSFER_CONTEXT_ROUTES.has(String(to.name)) &&
      !hadTransferSource
    ) {
      store.startTransfer();
      store.transferError = '송금 정보가 초기화됐어요. 처음부터 다시 진행해 주세요.';

      return {
        name: "transfer-source",
      };
    }

    // 완료된 송금 결과가 없는데 완료 화면으로 접근한 경우
    if (to.name === "complete" && !store.transferResult?.transactionId) {
      store.startTransfer();

      return {
        name: "transfer-source",
      };
    }

    // 실제 취소 상태가 아닌데 취소 화면으로 접근한 경우
    if (to.name === "cancelled" && !store.transferCancelled) {
      return {
        name: "transfer-source",
      };
    }

    return true;
  };
}
