import { ApiError } from '../../api/httpClient.js';

export function isPhoneNumber(value) {
  return typeof value === 'string' && value.length >= 5 && value.length <= 30
    && /^[0-9]+(?:-[0-9]+)*$/.exec(value)?.[0] === value;
}

export function telephoneHref(value) {
  return isPhoneNumber(value) ? `tel:${value.replace(/-/g, '')}` : null;
}

export function validateGuardian(value, nullable = false) {
  if (!value || !(isPhoneNumber(value.phoneNumber) || (nullable && value.phoneNumber === null))) {
    throw new ApiError('INVALID_RESPONSE', '보호자 번호를 확인하지 못했어요. 다시 조회해 주세요.', 502);
  }
  return value;
}

export function validateSupport(value) {
  validateGuardian(value?.guardian, true);
  if (value.notificationRecipient !== 'SELF' || typeof value.customerCenter?.name !== 'string'
      || !isPhoneNumber(value.customerCenter?.phoneNumber)) {
    throw new ApiError('INVALID_RESPONSE', '연락처 정보를 확인하지 못했어요. 다시 조회해 주세요.', 502);
  }
  return value;
}
