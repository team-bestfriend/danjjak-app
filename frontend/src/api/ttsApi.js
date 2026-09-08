const apiBaseUrl = (import.meta.env?.VITE_API_BASE_URL ?? '').replace(/\/$/, '');

export async function createSpeech(text, speed = 'NORMAL', signal) {
  const response = await fetch(`${apiBaseUrl}/api/tts`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ text, speed }),
    credentials: 'include',
    signal,
  });

  if (response.status === 401 && typeof window !== 'undefined') {
    window.dispatchEvent(new CustomEvent('danjjak:session-expired'));
  }
  if (!response.ok) {
    throw new Error('음성 안내를 불러오지 못했습니다.');
  }

  return response.blob();
}
