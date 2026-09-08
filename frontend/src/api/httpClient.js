const configuredBaseUrl = import.meta.env?.VITE_API_BASE_URL?.replace(/\/$/, '') ?? '';

export function apiUrl(path) {
  return `${configuredBaseUrl}${path}`;
}

export class ApiError extends Error {
  constructor(code, message, status) {
    super(message);
    this.name = 'ApiError';
    this.code = code;
    this.status = status;
  }
}

export async function request(path, options = {}) {
  const { headers: optionHeaders, ...requestOptions } = options;
  const response = await fetch(apiUrl(path), {
    credentials: 'include',
    ...requestOptions,
    headers: {
      ...(options.body ? { 'Content-Type': 'application/json' } : {}),
      ...optionHeaders,
    },
  });

  if (!response.ok) {
    let errorBody = null;
    try {
      const rawBody = await response.text();
      errorBody = rawBody ? JSON.parse(rawBody.replace(/^\uFEFF/, '')) : null;
    } catch {
      // 응답 본문이 JSON이 아니어도 사용자에게 일관된 오류를 제공한다.
    }
    const apiError = new ApiError(
      errorBody?.code ?? 'NETWORK_ERROR',
      errorBody?.message ?? '서버 요청을 처리하지 못했습니다.',
      response.status,
    );
    if (response.status === 401 && typeof window !== 'undefined') {
      window.dispatchEvent(new CustomEvent('danjjak:session-expired'));
    }
    throw apiError;
  }

  if (response.status === 204) return null;
  return response.json();
}
