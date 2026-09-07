# 음성·단계 안내

[목차](../requirements.md)

## 요구사항

| ID | 기능 | 완료 조건 |
| --- | --- | --- |
| FR-018 | 조작 강조 | 현재 대상 강조. 금액·비밀번호는 질문·음성만 제공 |
| FR-019 | 문구 편집 | 실행 전 설명·각 단계 안내 수정 |
| FR-020 | 기본 문구 | 맞춤 문구 없으면 템플릿 기본값, 편집 중 복원 가능 |
| FR-021 | 화면 안내 | 핵심 질문·행동 유지, 전체 문구 펼치기 |
| FR-022 | AI 음성 | 현재 문구·선택 속도·자연스러운 톤 |
| FR-023 | 가족 음성 | 해당 대상의 저장 녹음 재생 |
| FR-024 | 녹음 | 실제 마이크 녹음·미리 듣기·저장 |
| FR-025 | 교체 | 해당 대상 녹음만 교체 |
| FR-026 | 대체 안내 | 가족 녹음 없음/실패 → 같은 문구의 AI 음성 |
| FR-027 | 음성 입력 | 번호·정해진 업무 문장 → 일치 패턴 확인창 |
| FR-054 | 설명 음성 | 패턴 수정 안에서 설명 문구·방식·녹음 편집 |
| FR-055 | 단계 음성 | 패턴 수정의 실제 단계 목록에서 개별 편집 |
| FR-056 | 공통 대본 | 화면 전체 안내·AI 음성·가족 녹음 대본에 같은 문구 |
| FR-061 | 미리 생성 | 저장 후 생성·재사용 검토. 채택 전 선택 항목 |

## 편집 화면

**패턴 상세 → 패턴 수정 → 설명과 음성 / 단계별 안내와 음성**

| 항목 | 내용 |
| --- | --- |
| 설명 대상 | 실행 전 설명, 업무 단계와 별도 |
| 단계 목록 | 순서·이름·현재 문구·음성 방식·녹음 유무 |
| 공통 편집 | 문구·AI/가족 선택·미리 듣기·기본값·저장 |
| 가족 편집 | 녹음·멈춤·다시 듣기·재녹음 |
| 분석 진입 | 같은 패턴 수정의 해당 단계 |
| 문구 길이 | 비공백, 최대 500자 |
| 유효하지 않은 대상 | 수정 불가 이유·돌아가기 |

## 저장·문구 일치

| 상황 | 처리 |
| --- | --- |
| 편집·기본값·방식 변경·미리 듣기 | 초안만 변경 |
| 등록 중 ‘이 음성 사용’ | 상위 초안 반영, 최종 패턴 저장으로 확정 |
| 저장된 패턴의 개별 편집 | 해당 대상 저장으로 확정 |
| 뒤로·취소 | 계속 편집/버리기 선택, 다른 초안 보존 |
| 새 패턴의 설정 생략 | 기본 문구 + 사용자 기본 방식 |
| 기존 패턴의 설정 생략 | 저장값 유지 |
| 부분 저장 실패 | 저장된 부분 안내, 남은 초안 재시도 |
| 기존 녹음의 문구 변경 | 불일치 안내·재녹음/AI 선택, 기존 녹음 유지 가능 |
| 미저장 녹음 후 문구 재변경 | 재녹음 또는 녹음 초안 버리기 후 저장 |

## 재생

**대상별 선택 → 사용자 기본 방식 → AI 음성**

| 상태 | 동작 |
| --- | --- |
| AI 선택 | 현재 문구 재생 |
| 가족 선택·재생 가능 | 저장 녹음 재생 |
| 가족 없음·실패 | ‘AI 음성으로 안내할게요.’ + 현재 문구 |
| 음성 전체 실패 | 질문·안내 보기·주 행동 유지, 재시도 |
| 단계 진입 | 자동 재생 1회 시도 |
| 자동 재생 차단 | 듣기 버튼 제공 |
| 화면 이동 | 이전 재생·요청 중단 |
| 오류 | 비밀번호 오류 등 중요 피드백을 화면·음성으로 안내 |

| 음성 설정 | 기준 |
| --- | --- |
| 톤 | 차분·명료한 한국어. 과장·빠른 낭독·유아적인 표현 지양 |
| 속도 | 느리게 0.8 / 보통 1.0 / 빠르게 1.2: 초기 설계값 |
| 제어 | 다시 듣기·멈춤 |
| 명칭 | AI 음성 / 가족 음성. 설정·최초 안내에 합성 음성 설명 |
| 화면 정리 | 반복 TTS 표식·대본 제거: [공통 UX](shared-ux.md), D-01·04 |

## 녹음

| 항목 | 기준 |
| --- | --- |
| 시작 | 목적 설명 후 마이크 권한 |
| 길이 | 최대 90초 |
| 파일 | 비어 있지 않은 최대 10 MiB |
| 형식 | WebM·OGG·MP4·MP3·WAV, 실제 기기 지원 확인 |
| 실패 구분 | 권한·미지원·녹음·업로드·재생 |
| 저장 | 사용자 명시적 저장, 본인 파일만 접근 |
| 교체 실패 | 이전 녹음 유지 |
| AI로 변경 | 가족 녹음 삭제 없음 |
| 제외 | 음성 전사·복제·합성 가족 음성, 인증·송금 승인 용도 |

## 번호·업무 음성 입력

**음성으로 말하기 → 한 번 듣기 → 일치 확인 → 기존 실행 전 확인창 → 시작하기**

| 입력 | 대상 |
| --- | --- |
| ‘1’, ‘1번’, ‘일 번’, ‘일번’ | 현재 단축번호 1 |
| ‘십이’, ‘12번’, ‘십이 번’ | 현재 단축번호 12 |
| ‘아들에게 돈 보내 줘’ | 아들 관계의 송금 패턴 |
| ‘딸에게 돈 보내 줘’ | 딸 관계의 송금 패턴 |
| ‘연금 확인해 줘’ / ‘관리비 확인해 줘’ | 해당 조회 |
| ‘잔액 알려 줘’ / ‘거래 내역 보여 줘’ | 해당 조회 |
| ‘고객센터 전화해 줘’ / ‘공과금 확인해 줘’ | 해당 업무 |
| 빈·비활성·범위 밖 번호 | 일치 없음 |
| ‘1번 말고 2번’, ‘1번과 2번’ | 지원하지 않는 문장 |

| 상태 | 동작 |
| --- | --- |
| 번호 범위 | 1–12, 일·이·삼·사·오·육·칠·팔·구·십·십일·십이 |
| 한 개 일치 | 해당 페이지·카드 강조 + 확인창 |
| 없음·여러 개 | 다시 말하기·화면 선택 |
| 권한 거절·무음·미지원·인식 실패 | 원인·수동 선택 |
| 대기 한도 | 초기 20초, 말하기 종료·취소 제공 |
| 확인창 취소 | 실행·기록·금융 요청 없음 |
| 도움말 | 인식 서비스의 오디오 처리 가능성 설명 |

## Agent Notes

- Use one script per target. Preview uses the draft; task playback uses persisted text. Editing stops obsolete previews.
- Keep pre-start and step identities distinct; provide defaults for all seeded/new targets. Refetch the actual target, never infer it from order/title.
- Save only explicit changes. Preserve parent/unrelated drafts; retry partial creation against the existing pattern ID.
- Persist existing-audio mismatch across refetch; clear only on successful replacement. Do not claim retained audio matches new text. Apply the same rule to analysis suggestions.
- Global defaults affect only unconfigured targets. Pre-start choices do not override steps.
- Family speed changes playback rate, not the file. Ignore late audio/recognition results after navigation or cancellation.
- Release microphone tracks on stop/disposal. Validate content, not filename alone; store file references/MIME metadata outside audio binaries in DB.
- Commit new file metadata before deleting the old file. A failed replacement must preserve the old reference.
- Normalize supported number spacing and optional 번; match whole utterances, not embedded digits or negative/compound fragments.
- Number commands follow current numbering; task phrases follow type/recipient relationship. Renaming must not break phrase matching.
- Use deterministic matching, not open-ended interpretation. No natural-language amount entry or voice-only authorization.
- Keep recognized text only during matching; exclude command audio/text from server requests, state, storage, and logs. Do not claim on-device-only recognition.
- Editing/preview/recording creates no financial execution or visit logs. Never speak PIN content.
- Acceptance: [SC-004, 007, 011, 013, 018](validation-scenarios.md).

## Optional: Pre-generation

- Evaluate during the event; see [D-03](design-decisions.md). Do not prepare runtime audio assets before the event.
- Generate after script save, not per keystroke. Match script, voice, speed, and synthesis settings before reuse.
- Track pending/ready/failed; discard obsolete results. Never replace family audio with generated audio.
- If unavailable, generate on demand or retain readable guidance. Audio failure must not block script saving or financial work.
- Dynamic recipient/amount/error guidance must match the current transaction.
