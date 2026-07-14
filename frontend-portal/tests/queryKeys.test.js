import test from 'node:test';
import assert from 'node:assert/strict';
import {
  appointmentsQueryKey,
  insurancesQueryKey,
  paymentsQueryKey,
  receiptsQueryKey,
} from '../src/utils/queryKeys.js';

test('la caché de citas queda separada por paciente', () => {
  assert.notDeepEqual(
    appointmentsQueryKey('paciente.uno'),
    appointmentsQueryKey('paciente.dos'),
  );
});

test('la clave sin sesión no coincide con un paciente autenticado', () => {
  assert.notDeepEqual(
    appointmentsQueryKey(),
    appointmentsQueryKey('paciente.uno'),
  );
});

test('seguros, pagos y comprobantes también separan su caché por paciente', () => {
  const factories = [insurancesQueryKey, paymentsQueryKey, receiptsQueryKey];
  for (const createKey of factories) {
    assert.notDeepEqual(createKey('paciente.uno'), createKey('paciente.dos'));
    assert.notDeepEqual(createKey(), createKey('paciente.uno'));
  }
});
