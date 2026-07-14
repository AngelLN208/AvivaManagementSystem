import test from 'node:test';
import assert from 'node:assert/strict';
import { normalizePositiveId, requirePositiveId } from '../src/utils/ids.js';

test('normaliza únicamente identificadores enteros positivos', () => {
  assert.equal(normalizePositiveId('0042'), '42');
  assert.equal(normalizePositiveId('../patients'), '');
  assert.equal(normalizePositiveId('-1'), '');
  assert.equal(normalizePositiveId('1.5'), '');
  assert.equal(normalizePositiveId(''), '');
});

test('requirePositiveId rechaza segmentos de ruta inválidos', () => {
  assert.equal(requirePositiveId(9), '9');
  assert.throws(() => requirePositiveId('../9'), /no es válido/);
});
