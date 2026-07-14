export function normalizePositiveId(value) {
  if (!/^\d+$/.test(String(value || ''))) return '';
  const numericId = Number(value);
  return Number.isSafeInteger(numericId) && numericId > 0 ? String(numericId) : '';
}

export function requirePositiveId(value) {
  const normalized = normalizePositiveId(value);
  if (!normalized) throw new Error('El identificador solicitado no es válido.');
  return normalized;
}
