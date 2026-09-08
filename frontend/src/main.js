import { createApp } from 'vue';
import { createPinia } from 'pinia';
import router, { installRouterGuards } from './router';
import { bindRouter } from './router/navigation.js';
import App from './App.vue';
import './index.css';

const app = createApp(App);
const pinia = createPinia();

bindRouter(router);
installRouterGuards(pinia);
app.use(pinia);
app.use(router);
app.mount('#root');
