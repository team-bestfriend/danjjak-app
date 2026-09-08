import { ref } from 'vue';

let appRouter = null;
const fallbackRouteName = ref('onboarding');

export function bindRouter(router) {
  appRouter = router;
}

export function currentRouteName() {
  return appRouter?.currentRoute.value.name ?? fallbackRouteName.value;
}

export function navigateTo(name, options = {}) {
  if (!appRouter) {
    fallbackRouteName.value = name;
    return Promise.resolve();
  }
  const target = { name, ...(options.params ? { params: options.params } : {}), ...(options.query ? { query: options.query } : {}) };
  return options.replace ? appRouter.replace(target) : appRouter.push(target);
}

export function navigateBack() {
  if (appRouter) appRouter.back();
}

export function replaceWith(name, options = {}) {
  return navigateTo(name, { ...options, replace: true });
}
