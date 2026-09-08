export const voiceCommandExamples = [
  '아들에게 돈 보내 줘',
  '딸에게 돈 보내 줘',
  '연금 확인해 줘',
  '관리비 확인해 줘',
  '잔액 알려 줘',
  '거래 내역 보여 줘',
  '고객센터 전화해 줘',
  '공과금 확인해 줘',
];

function normalize(text) {
  return String(text ?? '').normalize('NFKC').toLowerCase().replace(/[\s.,!?。！？]/gu, '');
}

const ending = '(?:줘|주세요|줄래|줄래요)?';
const inquiry = `(?:(?:을|를)?(?:확인|조회)(?:해)?${ending}|(?:을|를)?(?:알려|보여)${ending})?`;
const rules = [
  { patternType: 'TRANSFER', relationship: '아들', phrase: new RegExp(`^(?:우리)?아들(?:에게|한테)?(?:돈(?:을)?보내|송금(?:해)?|이체(?:해)?)${ending}$`) },
  { patternType: 'TRANSFER', relationship: '딸', phrase: new RegExp(`^(?:우리)?딸(?:에게|한테)?(?:돈(?:을)?보내|송금(?:해)?|이체(?:해)?)${ending}$`) },
  { patternType: 'PENSION_CHECK', phrase: new RegExp(`^연금(?:입금)?${inquiry}$`) },
  { patternType: 'MANAGEMENT_FEE_CHECK', phrase: new RegExp(`^관리비(?:납부)?${inquiry}$`) },
  { patternType: 'BALANCE_CHECK', phrase: new RegExp(`^(?:통장)?잔액${inquiry}$`) },
  { patternType: 'TRANSACTION_HISTORY', phrase: new RegExp(`^(?:거래내역|입출금내역)${inquiry}$`) },
  { patternType: 'CUSTOMER_CENTER', phrase: new RegExp(`^고객센터(?:에)?(?:(?:전화|연결)(?:해)?${ending})?$`) },
  { patternType: 'UTILITY_BILL_CHECK', phrase: new RegExp(`^공과금(?:납부)?${inquiry}$`) },
];

export function matchShortcutCommand(text, patterns) {
  const command = normalize(text);
  const matchedRules = rules.filter((rule) => rule.phrase.test(command));
  // 제목이나 번호가 바뀌어도 서버의 업무 종류와 수취인 관계로 같은 대상을 찾는다.
  return patterns.filter((pattern) => (
    Number.isInteger(pattern.num) && pattern.num >= 1 && pattern.num <= 12
    && pattern.active !== false && pattern.isActive !== false
    && matchedRules.some((rule) => (
      pattern.patternType === rule.patternType
      && (!rule.relationship || normalize(pattern.linkedAccount?.relationship) === rule.relationship)
    ))
  ));
}
