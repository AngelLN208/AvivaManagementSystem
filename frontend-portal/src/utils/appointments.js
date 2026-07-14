import { parseLocalDateTime } from './dates.js';

export const APPOINTMENT_STATUS = {
  PENDING: { label: 'Pendiente', tone: 'warning' },
  CONFIRMED: { label: 'Confirmada', tone: 'success' },
  RESCHEDULED: { label: 'Reprogramada', tone: 'info' },
  COMPLETED: { label: 'Completada', tone: 'neutral' },
  CANCELLED: { label: 'Cancelada', tone: 'danger' },
  NO_SHOW: { label: 'No asistió', tone: 'danger' },
};

const ACTIVE_STATUSES = new Set(['PENDING', 'CONFIRMED', 'RESCHEDULED']);

export function isUpcomingAppointment(appointment, now = new Date()) {
  const date = parseLocalDateTime(appointment?.appointmentDateTime);
  return Boolean(
    date
      && date.getTime() >= now.getTime()
      && ACTIVE_STATUSES.has(appointment?.status),
  );
}

export function canManageAppointment(appointment, now = new Date()) {
  return isUpcomingAppointment(appointment, now);
}

export function sortAppointments(appointments, direction = 'asc') {
  const multiplier = direction === 'desc' ? -1 : 1;
  return [...(appointments || [])].sort((left, right) => {
    const leftTime = parseLocalDateTime(left.appointmentDateTime)?.getTime() || 0;
    const rightTime = parseLocalDateTime(right.appointmentDateTime)?.getTime() || 0;
    return (leftTime - rightTime) * multiplier;
  });
}

export function splitAppointments(appointments, now = new Date()) {
  const upcoming = [];
  const history = [];

  for (const appointment of appointments || []) {
    (isUpcomingAppointment(appointment, now) ? upcoming : history).push(appointment);
  }

  return {
    upcoming: sortAppointments(upcoming, 'asc'),
    history: sortAppointments(history, 'desc'),
  };
}

export function doctorDisplayName(doctor) {
  if (!doctor) return 'Médico por confirmar';
  const fullName = `${doctor.firstName || ''} ${doctor.lastName || ''}`.trim();
  return fullName ? `Dr(a). ${fullName}` : 'Médico por confirmar';
}
