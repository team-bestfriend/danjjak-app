export function hasGuardianSharingConsent(consents) {
  // 정식 필드가 있으면 거절·잘못된 값도 이전 필드로 대체하지 않는다.
  if (consents && Object.prototype.hasOwnProperty.call(consents, 'guardianSharing')) {
    return consents.guardianSharing === true;
  }
  return consents?.guardianShareAgreed === true;
}
