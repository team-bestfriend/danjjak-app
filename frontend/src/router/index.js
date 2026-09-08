import { createRouter, createWebHistory } from "vue-router";
import { useAppStore } from "../stores/appStore.js";

import AddPersonView from "../views/AddPersonView.vue";
import AnalysisView from "../views/AnalysisView.vue";
import AuthCallbackView from "../views/AuthCallbackView.vue";
import ConsentView from "../views/ConsentView.vue";
import ContactManageView from "../views/ContactManageView.vue";
import FeatureIntroView from "../views/FeatureIntroView.vue";
import HomeView from "../views/HomeView.vue";
import LoginView from "../views/LoginView.vue";
import NotFoundView from "../views/NotFoundView.vue";
import OnboardingView from "../views/OnboardingView.vue";
import PatternListView from "../views/PatternListView.vue";
import PatternRegisterView from "../views/PatternRegisterView.vue";
import SettingsView from "../views/SettingsView.vue";
import StepVoiceEditView from "../views/StepVoiceEditView.vue";
import TaskView from "../views/TaskView.vue";
import TransferFlowView from "../views/TransferFlowView.vue";
import VoiceEditView from "../views/VoiceEditView.vue";

const protectedMeta = {
  requiresAuth: true,
  requiresConsent: true,
};

const taskRoutes = [
  ["task-transfer", "/tasks/transfer"],
  ["task-2", "/tasks/pension"],
  ["task-3", "/tasks/maintenance-fee"],
  ["task-4", "/tasks/balance"],
  ["task-5", "/tasks/transactions"],
  ["task-6", "/tasks/customer-center"],
  ["task-8", "/tasks/utilities"],
  ["task-9", "/tasks/automatic-transfers"],
  ["task-10", "/tasks/card-usage"],
  ["task-11", "/tasks/deposit-maturity"],
  ["task-12", "/tasks/exchange-rate"],
  ["pension-history", "/tasks/pension/history"],
].map(([name, path]) => ({
  path,
  name,
  component: TaskView,
  props: {
    taskName: name,
  },
  meta: protectedMeta,
}));

const transferRoutes = [
  ["transfer-source", "/transfer/source"],
  ["direct-transfer", "/transfer/recipient"],
  ["direct-newaccount", "/transfer/new-account"],
  ["guide-person", "/transfer/person"],
  ["guide-account", "/transfer/account"],
  ["amount-input", "/transfer/amount"],
  ["pin-entry", "/transfer/pin"],
  ["fraud-warning", "/transfer/review"],
  ["final-confirm", "/transfer/confirm"],
  ["complete", "/transfer/complete"],
  ["cancelled", "/transfer/cancelled"],
].map(([name, path]) => ({
  path,
  name,
  component: TransferFlowView,
  props: {
    flowStep: name,
  },
  meta: protectedMeta,
}));

const routes = [
  {
    path: "/",
    redirect: {
      name: "onboarding",
    },
  },
  {
    path: "/onboarding",
    name: "onboarding",
    component: OnboardingView,
  },
  {
    path: "/intro",
    name: "feature-intro",
    component: FeatureIntroView,
  },
  {
    path: "/login",
    name: "login",
    component: LoginView,
  },
  {
    path: "/auth/callback",
    name: "auth-callback",
    component: AuthCallbackView,
  },
  {
    path: "/consent",
    name: "consent",
    component: ConsentView,
    meta: {
      requiresAuth: true,
    },
  },
  {
    path: "/home",
    name: "home",
    component: HomeView,
    meta: protectedMeta,
  },

  ...taskRoutes,
  ...transferRoutes,

  {
    path: "/patterns",
    name: "patterns",
    component: PatternListView,
    props: {
      viewMode: "patterns",
    },
    meta: protectedMeta,
  },
  {
    path: "/patterns/:patternId",
    name: "pattern-detail",
    component: PatternListView,
    props: {
      viewMode: "pattern-detail",
    },
    meta: protectedMeta,
  },
  {
    path: "/patterns/new",
    name: "pattern-register",
    component: PatternRegisterView,
    meta: protectedMeta,
  },
  {
    path: "/patterns/:patternId/voice",
    name: "voice-edit",
    component: VoiceEditView,
    meta: protectedMeta,
  },
  {
    path: "/patterns/:patternId/steps/voice",
    name: "step-voice-list",
    component: StepVoiceEditView,
    meta: protectedMeta,
  },
  {
    path: "/patterns/:patternId/steps/:stepOrder/voice",
    name: "step-voice-edit",
    component: StepVoiceEditView,
    meta: protectedMeta,
  },
  {
    path: "/analysis",
    name: "analysis",
    component: AnalysisView,
    meta: {
      requiresAuth: true,
    },
  },
  {
    path: "/settings",
    name: "settings",
    component: SettingsView,
    meta: protectedMeta,
  },
  {
    path: "/settings/people",
    name: "contact-manage",
    component: ContactManageView,
    meta: protectedMeta,
  },
  {
    path: "/settings/people/edit",
    name: "add-person",
    component: AddPersonView,
    meta: protectedMeta,
  },
  {
    path: "/:pathMatch(.*)*",
    name: "not-found",
    component: NotFoundView,
  },
];

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
});

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

export function installRouterGuards(pinia) {
  router.beforeEach(async (to) => {
    const store = useAppStore(pinia);
    const authenticated = await store.checkSession();
    const consentCompleted = Boolean(store.currentUser?.consents?.completed);

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

    // 송금 과정에 필요한 출금 계좌가 없는 경우
    if (
      TRANSFER_CONTEXT_ROUTES.has(String(to.name)) &&
      !store.selectedSourceAccountId
    ) {
      store.startTransfer();

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
  });

  router.afterEach((to, from) => {
    const store = useAppStore(pinia);

    store.recordPatternNavigation(
      String(from.name ?? ""),
      String(to.name ?? ""),
    );
  });
}

export default router;
