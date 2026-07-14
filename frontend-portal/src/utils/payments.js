const CURRENCY_FORMATTER = new Intl.NumberFormat('es-PE', {
  style: 'currency',
  currency: 'PEN',
  minimumFractionDigits: 2,
});

export const NEUTRAL_RECEIPT_DESCRIPTION = 'Pago correspondiente a una atención registrada en Clínica Aviva.';

export const PAYMENT_STATUS = Object.freeze({
  PENDING: { label: 'Pendiente', tone: 'warning' },
  PAID: { label: 'Pagado', tone: 'success' },
  CANCELLED: { label: 'Cancelado', tone: 'danger' },
  REFUNDED: { label: 'Reembolsado', tone: 'info' },
});

export const PAYMENT_METHODS = Object.freeze([
  { value: 'CREDIT_CARD', label: 'Tarjeta de crédito' },
  { value: 'DEBIT_CARD', label: 'Tarjeta de débito' },
]);

export const PAYMENT_METHOD_LABELS = Object.freeze({
  CASH: 'Efectivo',
  CREDIT_CARD: 'Tarjeta de crédito',
  DEBIT_CARD: 'Tarjeta de débito',
  TRANSFER: 'Transferencia',
  INSURANCE: 'Seguro',
});

export function formatCurrency(value) {
  const amount = Number(value);
  return CURRENCY_FORMATTER.format(Number.isFinite(amount) ? amount : 0);
}

export function formatReceiptNumberForDisplay(receiptNumber) {
  const value = String(receiptNumber ?? '').trim();
  // Solo se presentan referencias públicas reconocidas. Los identificadores
  // técnicos de versiones anteriores permanecen en los datos, no en el portal.
  return /^RCP-[A-Z0-9][A-Z0-9-]*$/i.test(value) ? value : null;
}

export function getReceiptDescriptionForDisplay() {
  // La constancia compone una descripción pública controlada y nunca expone
  // detalles de implementación recibidos desde versiones anteriores del API.
  return NEUTRAL_RECEIPT_DESCRIPTION;
}

export function receiptPdfFilename(receiptNumber) {
  const publicReference = formatReceiptNumberForDisplay(receiptNumber);
  const safeReference = (publicReference || 'Aviva')
    .replace(/[^A-Za-z0-9_-]/g, '-');
  return `Constancia-${safeReference}.pdf`;
}

export function sortPayments(payments) {
  return [...(payments || [])].sort((left, right) => {
    const rightDate = Date.parse(right.createdAt || right.paymentDate || '') || 0;
    const leftDate = Date.parse(left.createdAt || left.paymentDate || '') || 0;
    return rightDate - leftDate;
  });
}

export function filterPayments(payments, filter) {
  const sorted = sortPayments(payments);
  if (filter === 'pending') return sorted.filter((payment) => payment.status === 'PENDING');
  if (filter === 'paid') return sorted.filter((payment) => payment.status === 'PAID');
  return sorted;
}

export function receiptsByPaymentId(receipts) {
  return new Map((receipts || []).map((receipt) => [String(receipt.paymentId), receipt]));
}
