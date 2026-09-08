import { csrfHeaders, request } from './httpClient.js';

export const accountApi = {
  getOwnedAccounts: () => request('/api/accounts'),
  getImportCandidates: () => request('/api/accounts/import-candidates'),
  importAccounts: (accountIds) => request('/api/accounts/import', {
    method: 'POST',
    headers: csrfHeaders(),
    body: JSON.stringify({ accountIds }),
  }),
  setDefaultAccount: (accountId) => request('/api/accounts/default', {
    method: 'PUT',
    headers: csrfHeaders(),
    body: JSON.stringify({ accountId }),
  }),
  getBalance: (accountId) => request(`/api/accounts/${accountId}/balance`),
  getTransactions: (accountId, category) => {
    const query = category ? `?category=${encodeURIComponent(category)}` : '';
    return request(`/api/accounts/${accountId}/transactions${query}`);
  },
  getRegisteredPersons: () => request('/api/registered-persons'),
  createRegisteredPerson: (payload) => request('/api/registered-persons', {
    method: 'POST',
    headers: csrfHeaders(),
    body: JSON.stringify(payload),
  }),
  updateRegisteredPerson: (registeredPersonId, payload) => request(`/api/registered-persons/${registeredPersonId}`, {
    method: 'PUT',
    headers: csrfHeaders(),
    body: JSON.stringify(payload),
  }),
};

export const transferApi = {
  createTransfer: (payload) => request('/api/transfers', {
    method: 'POST',
    headers: csrfHeaders(),
    body: JSON.stringify(payload),
  }),
  resolveAnomaly: (anomalyEventId, payload) => request(`/api/anomaly-events/${anomalyEventId}/resolve`, {
    method: 'POST',
    headers: csrfHeaders(),
    body: JSON.stringify(payload),
  }),
};

export const supportApi = {
  getSupport: () => request('/api/support'),
  updateGuardian: (phoneNumber) => request('/api/support/guardian', {
    method: 'PUT',
    headers: csrfHeaders(),
    body: JSON.stringify({ phoneNumber }),
  }),
  notifyGuardian: (anomalyEventId, confirmedSelfDemo) => request(`/api/anomaly-events/${anomalyEventId}/guardian-notification`, {
    method: 'POST',
    headers: csrfHeaders(),
    body: JSON.stringify({ confirmedSelfDemo }),
  }),
};
