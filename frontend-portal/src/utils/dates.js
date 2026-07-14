const DATE_FORMATTER = new Intl.DateTimeFormat('es-PE', {
  weekday: 'long',
  day: 'numeric',
  month: 'long',
  year: 'numeric',
});

const SHORT_DATE_FORMATTER = new Intl.DateTimeFormat('es-PE', {
  day: '2-digit',
  month: 'short',
  year: 'numeric',
});

const TIME_FORMATTER = new Intl.DateTimeFormat('es-PE', {
  hour: '2-digit',
  minute: '2-digit',
  hour12: false,
});

export function parseLocalDateTime(value) {
  if (!value) return null;
  const match = String(value).match(
    /^(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2})(?::(\d{2}))?/,
  );
  if (!match) return null;

  const [, year, month, day, hour, minute, second = '0'] = match;
  const date = new Date(
    Number(year),
    Number(month) - 1,
    Number(day),
    Number(hour),
    Number(minute),
    Number(second),
  );
  return Number.isNaN(date.getTime()) ? null : date;
}

export function getLocalDateInputValue(date = new Date()) {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

export function normalizeTimeToSeconds(time) {
  const [hour = '00', minute = '00', second = '00'] = String(time || '').split(':');
  return `${hour.padStart(2, '0')}:${minute.padStart(2, '0')}:${second.slice(0, 2).padStart(2, '0')}`;
}

export function combineLocalDateTime(date, time) {
  if (!date || !time) return '';
  return `${date}T${normalizeTimeToSeconds(time)}`;
}

export function isPastSlot(date, time, now = new Date()) {
  const slotDate = parseLocalDateTime(combineLocalDateTime(date, time));
  return !slotDate || slotDate.getTime() <= now.getTime();
}

export function formatLongDate(value) {
  const date = parseLocalDateTime(value);
  return date ? DATE_FORMATTER.format(date) : 'Fecha no disponible';
}

export function formatShortDate(value) {
  const date = parseLocalDateTime(value);
  return date ? SHORT_DATE_FORMATTER.format(date) : '—';
}

export function formatTime(value) {
  const date = parseLocalDateTime(value);
  return date ? TIME_FORMATTER.format(date) : '—';
}

export function formatSlotTime(value) {
  return String(value || '').slice(0, 5) || '—';
}
