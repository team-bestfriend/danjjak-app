import { request } from './httpClient.js';

export const accountApi = {
  getOwnedAccounts: () => request('/api/accounts'),
  getBalance: (accountId) => request(`/api/accounts/${accountId}/balance`),
  getTransactions: (accountId, category) => {
    const query = category ? `?category=${encodeURIComponent(category)}` : '';
    return request(`/api/accounts/${accountId}/transactions${query}`);
  },
  getRegisteredPersons: () => request('/api/registered-persons'),
  createRegisteredPerson: (payload) => request('/api/registered-persons', {
    method: 'POST',
    body: JSON.stringify(payload),
  }),
  updateRegisteredPerson: (registeredPersonId, payload) => request(`/api/registered-persons/${registeredPersonId}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  }),
};

export const transferApi = {
  createTransfer: (payload) => request('/api/transfers', {
    method: 'POST',
    body: JSON.stringify(payload),
  }),
  resolveAnomaly: (anomalyEventId, payload) => request(`/api/anomaly-events/${anomalyEventId}/resolve`, {
    method: 'POST',
    body: JSON.stringify(payload),
  }),
};

export const supportApi = {
  getSupport: () => request('/api/support'),
  updateGuardian: (phoneNumber) => request('/api/support/guardian', {
    method: 'PUT',
    body: JSON.stringify({ phoneNumber }),
  }),
  notifyGuardian: (anomalyEventId) => request(`/api/anomaly-events/${anomalyEventId}/guardian-notification`, {
    method: 'POST',
  }),
};
