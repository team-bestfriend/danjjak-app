import { request } from './httpClient.js';

export const patternApi = {
  getTemplates: () => request('/api/pattern-templates'),
  getPatterns: () => request('/api/patterns'),
  getPattern: (patternId) => request(`/api/patterns/${patternId}`),
  createPattern: (payload) => request('/api/patterns', {
    method: 'POST',
    body: JSON.stringify(payload),
  }),
  updatePattern: (patternId, payload) => request(`/api/patterns/${patternId}`, {
    method: 'PATCH',
    body: JSON.stringify(payload),
  }),
  reorderPatterns: (items) => request('/api/patterns/order', {
    method: 'PUT',
    body: JSON.stringify({ items }),
  }),
  deactivatePattern: (patternId) => request(`/api/patterns/${patternId}`, {
    method: 'DELETE',
  }),
  startExecution: (patternId, sourceBankAccountId) => request(`/api/patterns/${patternId}/executions`, {
    method: 'POST',
    body: JSON.stringify(sourceBankAccountId ? { sourceBankAccountId } : {}),
  }),
  startVisit: (executionId, stepId) => request(`/api/pattern-executions/${executionId}/visits`, {
    method: 'POST',
    body: JSON.stringify({ stepId }),
  }),
  updateVisit: (executionId, visitId, payload) => request(`/api/pattern-executions/${executionId}/visits/${visitId}`, {
    method: 'PATCH',
    body: JSON.stringify(payload),
  }),
  finishExecution: (executionId, status) => request(`/api/pattern-executions/${executionId}`, {
    method: 'PATCH',
    body: JSON.stringify({ status }),
  }),
};
