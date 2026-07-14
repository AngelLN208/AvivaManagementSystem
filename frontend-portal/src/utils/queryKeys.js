/**
 * Las claves privadas incluyen la identidad para que un cambio de sesión no
 * reutilice citas, seguros o información financiera de otro paciente.
 */
export const appointmentsQueryKey = (username) => [
  'portal',
  'appointments',
  username || 'anonymous',
];

export const insurancesQueryKey = (username) => [
  'portal',
  'insurances',
  username || 'anonymous',
];

export const paymentsQueryKey = (username) => [
  'portal',
  'payments',
  username || 'anonymous',
];

export const receiptsQueryKey = (username) => [
  'portal',
  'receipts',
  username || 'anonymous',
];

export const notificationsQueryKey = (username) => [
  'portal',
  'notifications',
  username || 'anonymous',
];
