export const POLICY_RELATIONSHIPS = Object.freeze([
  { value: 'SELF', label: 'Soy el titular' },
  { value: 'SPOUSE', label: 'Cónyuge o pareja' },
  { value: 'CHILD', label: 'Hijo(a)' },
  { value: 'PARENT', label: 'Padre o madre' },
  { value: 'OTHER', label: 'Otra relación' },
]);

export function validateInsuranceDates(effectiveDate, expirationDate, today) {
  if (!effectiveDate || !expirationDate) return 'Completa las fechas de inicio y fin de vigencia.';
  if (expirationDate < effectiveDate) return 'La fecha de fin no puede ser anterior a la fecha de inicio.';
  if (today && expirationDate < today) return 'La póliza debe encontrarse vigente.';
  return '';
}

export function buildInsurancePayload(form) {
  return {
    insuranceId: Number(form.insuranceId),
    policyNumber: form.policyNumber.trim(),
    policyHolderName: form.policyHolderName.trim() || null,
    relationshipToHolder: form.relationshipToHolder || null,
    effectiveDate: form.effectiveDate,
    expirationDate: form.expirationDate,
  };
}

export function getPrimaryInsurance(insurances) {
  return (insurances || []).find((insurance) => insurance.isPrimary)
    || (insurances || [])[0]
    || null;
}
