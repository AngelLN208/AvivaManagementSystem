import test from 'node:test';
import assert from 'node:assert/strict';
import {
  combineLocalDateTime,
  getLocalDateInputValue,
  isPastSlot,
  normalizeTimeToSeconds,
  parseLocalDateTime,
} from '../src/utils/dates.js';

test('compone LocalDateTime sin convertir la zona horaria', () => {
  assert.equal(normalizeTimeToSeconds('09:30'), '09:30:00');
  assert.equal(combineLocalDateTime('2026-07-14', '09:30'), '2026-07-14T09:30:00');

  const parsed = parseLocalDateTime('2026-07-14T09:30:00');
  assert.equal(parsed.getFullYear(), 2026);
  assert.equal(parsed.getMonth(), 6);
  assert.equal(parsed.getDate(), 14);
  assert.equal(parsed.getHours(), 9);
});

test('calcula la fecha mínima usando el calendario local', () => {
  const localNight = new Date(2026, 6, 14, 23, 50, 0);
  assert.equal(getLocalDateInputValue(localNight), '2026-07-14');
});

test('filtra horarios pasados cuando se consulta el día actual', () => {
  const now = new Date(2026, 6, 14, 10, 0, 0);
  assert.equal(isPastSlot('2026-07-14', '09:30:00', now), true);
  assert.equal(isPastSlot('2026-07-14', '10:30:00', now), false);
  assert.equal(isPastSlot('2026-07-15', '08:00:00', now), false);
});
