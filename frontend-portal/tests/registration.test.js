import test from 'node:test';
import assert from 'node:assert/strict';
import {
  getActivationGrant,
  getActivationToken,
  isInvalidActivationGrantError,
  isValidDni,
  normalizeActivationStep,
  normalizeDni,
  normalizeVerificationCode,
  REGISTRATION_STEP,
} from '../src/utils/registration.js';

test('normaliza tanto nextStep como status del contrato de activación', () => {
  assert.equal(
    normalizeActivationStep({ nextStep: 'VERIFICATION_REQUIRED' }),
    REGISTRATION_STEP.VERIFICATION_REQUIRED,
  );
  assert.equal(
    normalizeActivationStep({ status: 'NEW_PATIENT' }),
    REGISTRATION_STEP.NEW_PATIENT,
  );
  assert.equal(
    normalizeActivationStep({ status: 'verify_existing' }),
    REGISTRATION_STEP.VERIFICATION_REQUIRED,
  );
});

test('acepta únicamente activationToken no vacío del paso OTP', () => {
  assert.equal(getActivationToken({ activationToken: 'grant-temporal' }), 'grant-temporal');
  assert.equal(getActivationToken({ token: 'jwt-que-no-corresponde' }), null);
  assert.equal(getActivationToken({ activationToken: '   ' }), null);
});

test('conserva el challenge del grant o usa el challenge verificado como respaldo', () => {
  assert.deepEqual(
    getActivationGrant(
      { activationToken: 'grant-temporal', challengeId: 'challenge-nuevo' },
      'challenge-original',
    ),
    { activationToken: 'grant-temporal', challengeId: 'challenge-nuevo' },
  );
  assert.deepEqual(
    getActivationGrant({ activationToken: 'grant-temporal' }, 'challenge-original'),
    { activationToken: 'grant-temporal', challengeId: 'challenge-original' },
  );
  assert.equal(getActivationGrant({ activationToken: 'grant-temporal' }), null);
});

test('reconoce los errores que invalidan el grant de activación', () => {
  assert.equal(isInvalidActivationGrantError({ code: 'INVALID_ACTIVATION_CODE' }), true);
  assert.equal(isInvalidActivationGrantError({ code: 'EXPIRED_ACTIVATION_TOKEN' }), true);
  assert.equal(isInvalidActivationGrantError({ status: 410 }), true);
  assert.equal(isInvalidActivationGrantError({ code: 'USERNAME_ALREADY_EXISTS' }), false);
});

test('limpia DNI y código sin aceptar caracteres ajenos', () => {
  assert.equal(normalizeDni(' 12.345-678 abc '), '12345678');
  assert.equal(normalizeVerificationCode('12a 34-567'), '123456');
  assert.equal(isValidDni('12345678'), true);
  assert.equal(isValidDni('1234'), false);
});
