import test from 'node:test';
import assert from 'node:assert/strict';
import {
  createPatientSession,
  decodeJwtPayload,
  isPatientSession,
  isTokenExpired,
} from '../src/auth/session.js';

function createToken(payload) {
  const encode = (value) => Buffer.from(JSON.stringify(value)).toString('base64url');
  return `${encode({ alg: 'HS256', typ: 'JWT' })}.${encode(payload)}.signature`;
}

test('decodeJwtPayload interpreta JWT con codificación base64url', () => {
  const token = createToken({ sub: 'paciente.demo', role: 'PATIENT', exp: 2_000_000_000 });
  assert.deepEqual(decodeJwtPayload(token), {
    sub: 'paciente.demo',
    role: 'PATIENT',
    exp: 2_000_000_000,
  });
});

test('isTokenExpired rechaza tokens vencidos o malformados', () => {
  const expired = createToken({ exp: 100 });
  assert.equal(isTokenExpired(expired, 101_000), true);
  assert.equal(isTokenExpired('token-invalido', 0), true);
});

test('createPatientSession acepta exclusivamente el rol PATIENT', () => {
  const patientToken = createToken({ sub: 'maria', role: 'PATIENT', exp: 2_000_000_000 });
  const session = createPatientSession({ token: patientToken, role: 'PATIENT', firstName: 'María' });

  assert.equal(session.username, 'maria');
  assert.equal(session.firstName, 'María');
  assert.equal(isPatientSession(session, 1_900_000_000_000), true);

  const staffToken = createToken({ sub: 'recepcion', role: 'RECEPTIONIST', exp: 2_000_000_000 });
  assert.throws(
    () => createPatientSession({ token: staffToken, role: 'RECEPTIONIST' }),
    /exclusivo para pacientes/,
  );

  assert.throws(
    () => createPatientSession({ token: staffToken, role: 'PATIENT' }),
    /exclusivo para pacientes/,
  );
});
