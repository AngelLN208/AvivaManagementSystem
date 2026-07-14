export const REGISTRATION_STEP = Object.freeze({
  DNI: 'DNI',
  NEW_PATIENT: 'NEW_PATIENT',
  VERIFICATION_REQUIRED: 'VERIFICATION_REQUIRED',
  CREATE_ACCESS: 'CREATE_ACCESS',
  ACCOUNT_EXISTS: 'ACCOUNT_EXISTS',
  CONTACT_STAFF: 'CONTACT_STAFF',
});

const STEP_ALIASES = Object.freeze({
  CREATE_PATIENT: REGISTRATION_STEP.NEW_PATIENT,
  REGISTER_PATIENT: REGISTRATION_STEP.NEW_PATIENT,
  VERIFY_EXISTING: REGISTRATION_STEP.VERIFICATION_REQUIRED,
  CODE_REQUIRED: REGISTRATION_STEP.VERIFICATION_REQUIRED,
  EXISTING_ACCOUNT: REGISTRATION_STEP.ACCOUNT_EXISTS,
});

export function normalizeActivationStep(response) {
  const rawStep = response?.nextStep ?? response?.status;
  const normalized = String(rawStep || '').trim().toUpperCase();
  return STEP_ALIASES[normalized] || normalized || null;
}

export function normalizeDni(value) {
  return String(value || '').replace(/\D/g, '').slice(0, 12);
}

export function isValidDni(value) {
  return /^[0-9]{8,12}$/.test(String(value || ''));
}

export function normalizeVerificationCode(value) {
  return String(value || '').replace(/\D/g, '').slice(0, 6);
}

export function getActivationToken(response) {
  const token = response?.activationToken;
  return typeof token === 'string' && token.trim() ? token.trim() : null;
}

export function getActivationGrant(response, fallbackChallengeId) {
  const activationToken = getActivationToken(response);
  const responseChallengeId = response?.challengeId;
  const challengeId = typeof responseChallengeId === 'string' && responseChallengeId.trim()
    ? responseChallengeId.trim()
    : typeof fallbackChallengeId === 'string' && fallbackChallengeId.trim()
      ? fallbackChallengeId.trim()
      : null;

  return activationToken && challengeId ? { activationToken, challengeId } : null;
}

export function isInvalidActivationGrantError(error) {
  const code = String(error?.code || '').toUpperCase();
  return error?.status === 410
    || code === 'INVALID_ACTIVATION_CODE'
    || code.includes('EXPIRED')
    || code.includes('ACTIVATION_TOKEN');
}
