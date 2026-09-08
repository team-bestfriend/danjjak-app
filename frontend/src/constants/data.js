export const INITIAL_PEOPLE = [
  { id: 1, name: "김민수", emoji: "👨", relation: "아들", lastUsed: "오늘", accounts: 2 },
  { id: 2, name: "김미영", emoji: "👩", relation: "딸", lastUsed: "3일 전", accounts: 1 },
  { id: 3, name: "이정훈", emoji: "🧑", relation: "사위", lastUsed: "1주일 전", accounts: 1 },
];

export const INITIAL_ACCOUNTS_BY_PERSON = {
  1: [
    { bank: "KB국민은행", color: "#F5B800", nickname: "주거래 계좌", masked: "123-45-****-90", primary: true },
    { bank: "신한은행", color: "#0077CC", nickname: "생활비 계좌", masked: "321-54-****-54", primary: false },
  ],
  2: [{ bank: "우리은행", color: "#006EAF", nickname: "월급 계좌", masked: "987-65-****-09", primary: true }],
  3: [{ bank: "신한은행", color: "#0077CC", nickname: "계좌", masked: "456-78-****-34", primary: true }],
};

export const RECENT_TX_ACCOUNTS = [
  { masked: "123-45-****-90", bank: "KB국민은행", bankColor: "#F5B800", lastDate: "8월 27일", personId: 1 },
  { masked: "987-65-****-09", bank: "우리은행", bankColor: "#006EAF", lastDate: "8월 14일", personId: 2 },
  { masked: "456-78-****-34", bank: "신한은행", bankColor: "#0077CC", lastDate: "7월 25일", personId: 3 },
  { masked: "111-22-****-33", bank: "케이뱅크", bankColor: "#FFB800", lastDate: "7월 12일" },
  { masked: "789-01-****-56", bank: "카카오뱅크", bankColor: "#3A1D1D", lastDate: "6월 30일" },
];

export const TASKTYPE_SCREEN = {
  transfer: "task-transfer",
  pension: "task-2",
  building: "task-3",
  balance: "task-4",
  history: "task-5",
  support: "task-6",
  utility: "task-8",
  autotransfer: "task-9",
  cardhistory: "task-10",
  deposit: "task-11",
  exchange: "task-12",
};

export const INITIAL_PATTERNS = [
  { id: "p1", num: 1, label: "아들에게 송금", color: "#FF5E5E", sub: "👨 김민수 · 아들", taskType: "transfer", personId: 1, amountOpt: "direct" },
  { id: "p2", num: 2, label: "연금 입금 확인", color: "#FF9943", sub: "KB국민은행 생활비 계좌", taskType: "pension" },
  { id: "p3", num: 3, label: "관리비 확인", color: "#22C55E", sub: "행복아파트 관리비", taskType: "building" },
  { id: "p4", num: 4, label: "잔액 확인", color: "#3B82F6", sub: "KB국민은행 생활비 계좌", taskType: "balance" },
  { id: "p5", num: 5, label: "거래내역 조회", color: "#8B5CF6", sub: "최근 거래 확인", taskType: "history" },
  { id: "p6", num: 6, label: "고객센터 연결", color: "#06B6D4", sub: "KB국민은행 고객센터", taskType: "support" },
  { id: "p7", num: 7, label: "딸에게 송금", color: "#EC4899", sub: "👩 김미영 · 딸", taskType: "transfer", personId: 2, amountOpt: "direct" },
  { id: "p8", num: 8, label: "공과금 확인", color: "#F97316", sub: "지역 공과금 납부", taskType: "utility" },
];

export const TASK_COLORS = {
  transfer: "#FF5E5E", pension: "#FF9943", building: "#22C55E", balance: "#3B82F6",
  history: "#8B5CF6", support: "#06B6D4", utility: "#F97316", autotransfer: "#10B981",
  cardhistory: "#6366F1", deposit: "#A855F7", exchange: "#14B8A6",
};

export const VOICE_SCRIPTS = [
  "엄마, 여기서 송금을 눌러줘.", "엄마, 민수 이름을 눌러줘.",
  "국민은행 계좌를 눌러줘.", "보낼 금액을 입력해줘.",
];

export const ROUTE_STEPS = ["송금 메뉴 이동", "받는 사람 선택", "계좌 선택", "금액 입력", "거래 확인", "계좌 비밀번호 입력", "송금 완료"];

export const TRANSACTIONS = [
  { date: "08.25", desc: "국민연금", amount: "+650,000", income: true },
  { date: "08.26", desc: "OO마트", amount: "-73,500", income: false },
  { date: "08.27", desc: "아들에게 송금", amount: "-50,000", income: false },
  { date: "08.28", desc: "관리비", amount: "-182,400", income: false },
];

export function generatePatternDesc(pat, peopleList = INITIAL_PEOPLE, accountsMap = INITIAL_ACCOUNTS_BY_PERSON) {
  if (!pat) return "금융 업무입니다.";
  const person = pat.personId ? peopleList.find((p) => p.id === pat.personId) : null;
  switch (pat.taskType) {
    case "transfer":
      if (person) return `${person.relation} ${person.name}님에게 송금하는 업무입니다. 시작하려면 시작하기를 눌러주세요.`;
      return "송금하는 업무입니다. 시작하려면 시작하기를 눌러주세요.";
    case "pension": return "연금이 입금되었는지 확인하는 업무입니다.";
    case "building": return "이번 달 관리비를 확인하는 업무입니다.";
    case "balance": return "현재 계좌 잔액을 확인하는 업무입니다.";
    case "history": return "최근 거래 내역을 확인하는 업무입니다.";
    case "support": return "고객센터에 연결하는 업무입니다.";
    case "utility": return "공과금 납부 내역을 확인하는 업무입니다.";
    case "autotransfer": return "자동이체 내역을 확인하는 업무입니다.";
    case "cardhistory": return "카드 이용 내역을 확인하는 업무입니다.";
    case "deposit": return "예금 만기를 확인하는 업무입니다.";
    case "exchange": return "현재 환율을 확인하는 업무입니다.";
    default: return `${pat.label} 업무입니다.`;
  }
}

export function generateStepVoice(stepIdx, personName, bank) {
  const texts = [
    "송금 화면으로 이동합니다.",
    personName ? `${personName}님을 선택해주세요.` : "받는 사람을 선택해주세요.",
    bank ? `${bank} 계좌를 선택해주세요.` : "계좌를 선택해주세요.",
    "보낼 금액을 입력해주세요.",
    "거래 내용을 확인해주세요.",
    "계좌 비밀번호를 입력해주세요.",
    "송금이 완료됐어요.",
  ];
  return texts[stepIdx] ?? "다음 단계로 진행해주세요.";
}

export function reorderPatterns(patterns, sourceNum, targetNum) {
  const src = patterns.find((p) => p.num === sourceNum);
  if (!src) return patterns;
  const tgt = patterns.find((p) => p.num === targetNum);
  if (tgt) {
    return patterns.map((p) => {
      if (p.id === src.id) return { ...p, num: targetNum };
      if (p.id === tgt.id) return { ...p, num: sourceNum };
      return p;
    });
  } else {
    return patterns.map((p) => (p.id === src.id ? { ...p, num: targetNum } : p));
  }
}
