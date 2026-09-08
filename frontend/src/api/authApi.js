import { apiUrl, request } from './httpClient.js';

export const authApi = {
  getKakaoStartUrl: () => apiUrl('/api/auth/kakao/start'),
  getSession: () => request('/api/auth/session'),
  startDevSession: () => request('/api/auth/dev-session', { method: 'POST' }),
  logout: () => request('/api/auth/logout', { method: 'POST' }),
};

export const userApi = {
  getCurrentUser: () => request('/api/users/me'),
  updateConsents: (payload) => request('/api/users/me/consents', {
    method: 'PUT',
    body: JSON.stringify(payload),
  }),
  updateSettings: (payload) => request('/api/users/me/settings', {
    method: 'PUT',
    body: JSON.stringify(payload),
  }),
};
