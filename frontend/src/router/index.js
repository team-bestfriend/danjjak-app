import { createRouter, createWebHistory } from "vue-router";
import { createAppNavigationGuard } from "./guards.js";
import OwnedAccountImportView from "../views/OwnedAccountImportView.vue";
import ServiceHelpView from "../views/ServiceHelpView.vue";
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
  meta: { ...protectedMeta, requiresAccounts: name !== 'task-6' },
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
  meta: { ...protectedMeta, requiresAccounts: true },
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

  {
    path: "/settings/accounts/import",
    name: "owned-account-import",
    component: OwnedAccountImportView,
    meta: protectedMeta,
  },
  {
    path: "/settings/help",
    name: "service-help",
    component: ServiceHelpView,
    meta: { requiresAuth: true },
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

export function installRouterGuards(pinia) {
  router.beforeEach(createAppNavigationGuard(useAppStore(pinia)));

  router.afterEach((to, from) => {
    const store = useAppStore(pinia);
    store.recordPatternNavigation(String(from.name ?? ''), String(to.name ?? ''));
  });
}

export default router;
