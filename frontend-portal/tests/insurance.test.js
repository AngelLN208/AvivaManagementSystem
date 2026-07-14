import test from 'node:test';
import assert from 'node:assert/strict';
import {
  buildInsurancePayload,
  getPrimaryInsurance,
  validateInsuranceDates,
} from '../src/utils/insurance.js';

test('valida orden y vigencia de fechas de póliza', () => {
  assert.match(validateInsuranceDates('', '', '2026-07-14'), /Completa/);
  assert.match(validateInsuranceDates('2026-08-01', '2026-07-01', '2026-07-14'), /anterior/);
  assert.match(validateInsuranceDates('2025-01-01', '2025-12-31', '2026-07-14'), /vigente/);
  assert.equal(validateInsuranceDates('2026-01-01', '2026-12-31', '2026-07-14'), '');
});

test('construye el contrato propio sin patientId ni isPrimary', () => {
  const payload = buildInsurancePayload({
    insuranceId: '4',
    policyNumber: '  POL-44  ',
    policyHolderName: '  ',
    relationshipToHolder: 'SELF',
    effectiveDate: '2026-01-01',
    expirationDate: '2026-12-31',
  });

  assert.deepEqual(payload, {
    insuranceId: 4,
    policyNumber: 'POL-44',
    policyHolderName: null,
    relationshipToHolder: 'SELF',
    effectiveDate: '2026-01-01',
    expirationDate: '2026-12-31',
  });
  assert.equal('patientId' in payload, false);
  assert.equal('isPrimary' in payload, false);
});

test('elige la póliza primaria y tolera una lista vacía', () => {
  const secondary = { id: 1, isPrimary: false };
  const primary = { id: 2, isPrimary: true };
  assert.equal(getPrimaryInsurance([secondary, primary]), primary);
  assert.equal(getPrimaryInsurance([]), null);
});
