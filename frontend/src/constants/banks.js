export const BANKS = [
  { code: '004', name: '국민은행', aliases: ['KB국민은행', '국민은행'] },
  { code: '088', name: '신한은행', aliases: ['신한은행'] },
  { code: '020', name: '우리은행', aliases: ['우리은행'] },
  { code: '081', name: '하나은행', aliases: ['하나은행'] },
  { code: '011', name: 'NH농협', aliases: ['NH농협', '농협은행'] },
  { code: '090', name: '카카오뱅크', aliases: ['카카오뱅크'] },
  { code: '089', name: '케이뱅크', aliases: ['케이뱅크'] },
  { code: '092', name: '토스뱅크', aliases: ['토스뱅크'] },
];

export function findBankByName(name) {
  return BANKS.find((bank) => bank.aliases.includes(name) || bank.name === name) ?? null;
}
