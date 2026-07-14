import test from 'node:test';
import assert from 'node:assert/strict';
import { appointmentsQueryKey } from '../src/utils/queryKeys.js';

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
