import test from 'node:test';
import assert from 'node:assert/strict';
import {
  filterPayments,
  formatCurrency,
  formatReceiptNumberForDisplay,
  getReceiptDescriptionForDisplay,
  NEUTRAL_RECEIPT_DESCRIPTION,
  receiptPdfFilename,
  receiptsByPaymentId,
  sortPayments,
} from '../src/utils/payments.js';

const PAYMENTS = [
  { id: 1, status: 'PAID', createdAt: '2026-07-10T10:00:00' },
  { id: 2, status: 'PENDING', createdAt: '2026-07-12T10:00:00' },
  { id: 3, status: 'PENDING', createdAt: '2026-07-11T10:00:00' },
];

test('ordena pagos recientes primero y filtra por estado', () => {
  assert.deepEqual(sortPayments(PAYMENTS).map((payment) => payment.id), [2, 3, 1]);
  assert.deepEqual(filterPayments(PAYMENTS, 'pending').map((payment) => payment.id), [2, 3]);
  assert.deepEqual(filterPayments(PAYMENTS, 'paid').map((payment) => payment.id), [1]);
});

test('relaciona constancias con pagos usando IDs normalizados', () => {
  const receipt = { id: 9, paymentId: 3 };
  assert.equal(receiptsByPaymentId([receipt]).get('3'), receipt);
});

test('formatea montos en soles y tolera valores inválidos', () => {
  assert.match(formatCurrency('125.5'), /125[,.]50/);
  assert.match(formatCurrency(null), /0[,.]00/);
});

test('oculta referencias técnicas históricas sin inventar otro identificador', () => {
  assert.equal(formatReceiptNumberForDisplay('LEGACY-2026-0042'), null);
  assert.equal(formatReceiptNumberForDisplay('referencia-interna'), null);
  assert.equal(formatReceiptNumberForDisplay('RCP-2026-0043'), 'RCP-2026-0043');
  assert.equal(formatReceiptNumberForDisplay('RCP-'), null);
  assert.equal(formatReceiptNumberForDisplay(null), null);
});

test('usa una descripción pública controlada para toda constancia', () => {
  assert.equal(
    getReceiptDescriptionForDisplay({
      receiptNumber: 'LEGACY-2026-0042',
      description: 'Pago de consulta médica.',
    }),
    NEUTRAL_RECEIPT_DESCRIPTION,
  );
  assert.equal(
    getReceiptDescriptionForDisplay({
      receiptNumber: 'RCP-2026-0043',
      description: 'Registro interno.',
    }),
    NEUTRAL_RECEIPT_DESCRIPTION,
  );
  assert.equal(
    getReceiptDescriptionForDisplay({
      receiptNumber: 'RCP-2026-0044',
      description: 'Payload de prueba generado por el backend.',
    }),
    NEUTRAL_RECEIPT_DESCRIPTION,
  );
  assert.equal(
    getReceiptDescriptionForDisplay({
      receiptNumber: 'RCP-2026-0045',
      description: '  Pago correspondiente a consulta de cardiología.  ',
    }),
    NEUTRAL_RECEIPT_DESCRIPTION,
  );
});

test('construye un nombre seguro para la descarga PDF', () => {
  assert.equal(
    receiptPdfFilename('RCP-2026-0043'),
    'Constancia-RCP-2026-0043.pdf',
  );
  assert.equal(receiptPdfFilename('referencia interna'), 'Constancia-Aviva.pdf');
});
