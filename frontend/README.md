# Danjjak Frontend

Vue 3와 Vite를 사용하는 JavaScript 프론트엔드입니다.

```powershell
npm install
npm run dev
```

개발 서버는 <http://localhost:5173>에서 실행됩니다. 개발 중 `/api` 요청은 Vite 프록시를 통해 로컬 Tomcat의 `/danjjak/api`로 전달됩니다.

다른 API 주소를 사용하려면 `.env.example`을 `.env`로 복사하고 `VITE_API_BASE_URL`을 설정합니다.

