import { request } from './httpClient.js';

export const usageAnalysisApi = {
  getUsageAnalysis: (from, to) => {
    const query = new URLSearchParams({ from, to });
    return request(`/api/usage-analysis?${query}`);
  },
};
