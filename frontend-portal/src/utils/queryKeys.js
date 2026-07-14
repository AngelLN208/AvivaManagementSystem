/**
 * Separa la caché de citas por identidad para que un cambio de sesión nunca
 * reutilice datos pertenecientes a otro paciente.
 */
export const appointmentsQueryKey = (username) => [
  'portal',
  'appointments',
  username || 'anonymous',
];
