export function formatFecha(fechaStr) {
  if (!fechaStr) return '—';
  const d = new Date(fechaStr);
  if (isNaN(d)) return fechaStr;
  return d.toLocaleDateString('es-PE', { day: '2-digit', month: '2-digit', year: 'numeric' });
}

export function formatFechaHora(fechaStr) {
  if (!fechaStr) return '—';
  const d = new Date(fechaStr);
  if (isNaN(d)) return fechaStr;
  return d.toLocaleString('es-PE', {
    day: '2-digit', month: '2-digit', year: 'numeric',
    hour: '2-digit', minute: '2-digit'
  });
}

export function calcularEdad(fechaNac) {
  if (!fechaNac) return '—';
  const hoy = new Date();
  const nac = new Date(fechaNac);
  let edad = hoy.getFullYear() - nac.getFullYear();
  const m = hoy.getMonth() - nac.getMonth();
  if (m < 0 || (m === 0 && hoy.getDate() < nac.getDate())) edad--;
  return edad;
}

export function esHoy(fechaStr) {
  if (!fechaStr) return false;
  const d = new Date(fechaStr);
  const hoy = new Date();
  return d.getDate() === hoy.getDate() &&
         d.getMonth() === hoy.getMonth() &&
         d.getFullYear() === hoy.getFullYear();
}

export const ESTADO_MAP = {
  PENDING:     { clase: 'fondo-advertencia-sutil texto-advertencia', label: 'Pendiente' },
  CONFIRMED:   { clase: 'fondo-exito-sutil texto-exito',             label: 'Confirmada' },
  COMPLETED:   { clase: 'fondo-info-sutil texto-info',               label: 'Completada' },
  CANCELLED:   { clase: 'fondo-error-sutil texto-error',             label: 'Cancelada' },
  RESCHEDULED: { clase: 'fondo-advertencia-sutil texto-advertencia', label: 'Reprogramada' },
  NO_SHOW:     { clase: 'fondo-error-sutil texto-error',             label: 'No asistió' },
  PAID:        { clase: 'fondo-exito-sutil texto-exito',             label: 'Pagado' },
  REFUNDED:    { clase: 'fondo-info-sutil texto-info',               label: 'Reembolsado' },
  ACTIVE:      { clase: 'fondo-exito-sutil texto-exito',             label: 'Activo' },
  INACTIVE:    { clase: 'fondo-error-sutil texto-error',             label: 'Inactivo' },
};