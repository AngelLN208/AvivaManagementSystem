import StatusBadge from '../ui/StatusBadge.jsx';
import { doctorDisplayName } from '../../utils/appointments.js';
import { formatLongDate, formatTime } from '../../utils/dates.js';

export default function AppointmentCard({ appointment, canManage, onReschedule, onCancel, compact = false }) {
  const doctor = appointment.doctor;
  const actionContext = `${doctorDisplayName(doctor)}, ${formatLongDate(appointment.appointmentDateTime)} a las ${formatTime(appointment.appointmentDateTime)}`;

  return (
    <article className={`appointment-card ${compact ? 'appointment-card--compact' : ''}`}>
      <div className="appointment-card__date" aria-label={`${formatLongDate(appointment.appointmentDateTime)}, ${formatTime(appointment.appointmentDateTime)}`}>
        <strong>{formatTime(appointment.appointmentDateTime)}</strong>
        <span>{formatLongDate(appointment.appointmentDateTime)}</span>
      </div>

      <div className="appointment-card__body">
        <div className="appointment-card__title-row">
          <div>
            <p className="appointment-card__specialty">{doctor?.specialty?.name || 'Consulta general'}</p>
            <h3>{doctorDisplayName(doctor)}</h3>
          </div>
          <StatusBadge status={appointment.status} />
        </div>

        {appointment.reason && !compact && (
          <p className="appointment-card__reason">
            <span>Motivo registrado</span>
            {appointment.reason}
          </p>
        )}

        {canManage && !compact && (
          <div className="appointment-card__actions">
            <button type="button" className="button button--secondary button--small" aria-label={`Reprogramar cita con ${actionContext}`} onClick={() => onReschedule(appointment)}>
              Reprogramar
            </button>
            <button type="button" className="button button--danger-ghost button--small" aria-label={`Cancelar cita con ${actionContext}`} onClick={() => onCancel(appointment)}>
              Cancelar cita
            </button>
          </div>
        )}
      </div>
    </article>
  );
}
