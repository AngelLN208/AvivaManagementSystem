import { Link } from 'react-router-dom';
import AppointmentCard from '../components/appointments/AppointmentCard.jsx';
import { ErrorState, LoadingState } from '../components/ui/AsyncState.jsx';
import { useAuth } from '../hooks/useAuth.js';
import { useMyAppointments } from '../hooks/useAppointments.js';
import { useCurrentTime } from '../hooks/useCurrentTime.js';
import { splitAppointments } from '../utils/appointments.js';

export default function DashboardPage() {
  const { session } = useAuth();
  const appointmentsQuery = useMyAppointments();
  const now = useCurrentTime();
  const { upcoming, history } = splitAppointments(appointmentsQuery.data || [], now);
  const completedCount = history.filter((appointment) => appointment.status === 'COMPLETED').length;

  return (
    <div className="page-stack">
      <section className="welcome-hero">
        <div className="welcome-hero__copy">
          <p className="eyebrow eyebrow--light">Tu portal Aviva</p>
          <h1>Hola, {session?.firstName || session?.username}</h1>
          <p>Organiza tus próximas citas y encuentra el horario que mejor se adapte a ti.</p>
          <div className="welcome-hero__actions">
            <Link to="/agendar" className="button button--light">Agendar una cita</Link>
            <Link to="/citas" className="button button--glass">Ver mis citas</Link>
          </div>
        </div>
        <div className="welcome-hero__visual" aria-hidden="true">
          <div className="calendar-illustration">
            <span className="calendar-illustration__rings" />
            <strong>{new Date().getDate()}</strong>
            <small>{new Intl.DateTimeFormat('es-PE', { month: 'short' }).format(new Date())}</small>
            <span className="calendar-illustration__check">✓</span>
          </div>
        </div>
      </section>

      {appointmentsQuery.isLoading && <LoadingState message="Consultando tus citas…" />}
      {appointmentsQuery.isError && (
        <ErrorState message={appointmentsQuery.error.message} onRetry={appointmentsQuery.refetch} />
      )}

      {appointmentsQuery.isSuccess && (
        <>
          <section className="stats-grid" aria-label="Resumen de citas">
            <article className="stat-card stat-card--primary">
              <span className="stat-card__label">Próximas citas</span>
              <strong>{upcoming.length}</strong>
              <small>{upcoming.length === 1 ? 'cita programada' : 'citas programadas'}</small>
            </article>
            <article className="stat-card">
              <span className="stat-card__label">Atenciones completadas</span>
              <strong>{completedCount}</strong>
              <small>registradas en tu historial de citas</small>
            </article>
            <article className="stat-card stat-card--action">
              <span className="stat-card__label">¿Necesitas una cita?</span>
              <strong>Elige día y hora</strong>
              <Link to="/agendar">Revisar disponibilidad <span aria-hidden="true">→</span></Link>
            </article>
          </section>

          <section className="section-card">
            <div className="section-heading">
              <div>
                <p className="eyebrow">Agenda personal</p>
                <h2>{upcoming.length ? 'Tu próxima cita' : 'Aún no tienes citas próximas'}</h2>
              </div>
              {upcoming.length > 0 && <Link to="/citas" className="text-link">Ver todas</Link>}
            </div>

            {upcoming.length > 0 ? (
              <AppointmentCard appointment={upcoming[0]} compact />
            ) : (
              <div className="dashboard-empty">
                <span aria-hidden="true">+</span>
                <p>Cuando agendes una cita, aparecerá aquí para que la tengas siempre a la vista.</p>
                <Link to="/agendar" className="button button--primary">Agendar mi primera cita</Link>
              </div>
            )}
          </section>

          <section className="support-strip">
            <div>
              <p className="eyebrow">Antes de tu cita</p>
              <h2>Llega con 15 minutos de anticipación</h2>
              <p>Ten a la mano tu documento de identidad para agilizar tu atención.</p>
            </div>
            <Link to="/medicos" className="button button--secondary">Conocer especialistas</Link>
          </section>
        </>
      )}
    </div>
  );
}
