import { useEffect, useMemo, useRef, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import AppointmentCard from '../components/appointments/AppointmentCard.jsx';
import CancelAppointmentDialog from '../components/appointments/CancelAppointmentDialog.jsx';
import RescheduleAppointmentDialog from '../components/appointments/RescheduleAppointmentDialog.jsx';
import { EmptyState, ErrorState, LoadingState } from '../components/ui/AsyncState.jsx';
import InlineAlert from '../components/ui/InlineAlert.jsx';
import PageHeader from '../components/ui/PageHeader.jsx';
import { Button } from '../components/ui/button.jsx';
import { Card, CardContent } from '../components/ui/card.jsx';
import { useAppointmentMutations, useMyAppointments } from '../hooks/useAppointments.js';
import { useCurrentTime } from '../hooks/useCurrentTime.js';
import {
  canManageAppointment,
  sortAppointments,
  splitAppointments,
} from '../utils/appointments.js';

const FILTERS = [
  { id: 'upcoming', label: 'Próximas' },
  { id: 'history', label: 'Historial' },
  { id: 'all', label: 'Todas' },
];

export default function AppointmentsPage() {
  const [searchParams] = useSearchParams();
  const appointmentsQuery = useMyAppointments();
  const { cancelAppointment, rescheduleAppointment } = useAppointmentMutations();
  const now = useCurrentTime();
  const lastDialogTrigger = useRef(null);
  const noticeRef = useRef(null);
  const [filter, setFilter] = useState('upcoming');
  const [appointmentToCancel, setAppointmentToCancel] = useState(null);
  const [appointmentToReschedule, setAppointmentToReschedule] = useState(null);
  const [notice, setNotice] = useState(
    searchParams.get('creada') === '1' ? 'Tu cita fue agendada correctamente.' : '',
  );

  useEffect(() => {
    if (notice) noticeRef.current?.focus();
  }, [notice]);

  const { upcoming, history } = useMemo(
    () => splitAppointments(appointmentsQuery.data || [], now),
    [appointmentsQuery.data, now],
  );

  const visibleAppointments = useMemo(() => {
    if (filter === 'upcoming') return upcoming;
    if (filter === 'history') return history;
    return sortAppointments(appointmentsQuery.data || [], 'desc');
  }, [filter, upcoming, history, appointmentsQuery.data]);

  function openCancelDialog(appointment) {
    lastDialogTrigger.current = document.activeElement;
    cancelAppointment.reset();
    setAppointmentToCancel(appointment);
  }

  function openRescheduleDialog(appointment) {
    lastDialogTrigger.current = document.activeElement;
    rescheduleAppointment.reset();
    setAppointmentToReschedule(appointment);
  }

  function restoreDialogFocus() {
    window.requestAnimationFrame(() => lastDialogTrigger.current?.focus());
  }

  function closeCancelDialog() {
    setAppointmentToCancel(null);
    restoreDialogFocus();
  }

  function closeRescheduleDialog() {
    setAppointmentToReschedule(null);
    restoreDialogFocus();
  }

  function confirmCancellation() {
    cancelAppointment.mutate(appointmentToCancel.id, {
      onSuccess: () => {
        closeCancelDialog();
        setNotice('La cita fue cancelada correctamente.');
      },
    });
  }

  function confirmReschedule(newDateTime) {
    rescheduleAppointment.mutate({
      appointmentId: appointmentToReschedule.id,
      newDateTime,
    }, {
      onSuccess: () => {
        closeRescheduleDialog();
        setNotice('Tu cita fue reprogramada correctamente.');
      },
    });
  }

  return (
    <div className="space-y-6">
      <PageHeader
        eyebrow="Agenda personal"
        title="Mis citas"
        description="Consulta tus próximas citas y revisa las atenciones anteriores."
        action={<Button asChild><Link to="/agendar">Agendar cita</Link></Button>}
      />

      {notice && (
        <div ref={noticeRef} className="outline-none" tabIndex={-1}>
          <InlineAlert tone="success" onDismiss={() => setNotice('')}>{notice}</InlineAlert>
        </div>
      )}

      {appointmentsQuery.isLoading && <LoadingState message="Cargando tus citas…" />}
      {appointmentsQuery.isError && (
        <ErrorState message={appointmentsQuery.error.message} onRetry={appointmentsQuery.refetch} />
      )}

      {appointmentsQuery.isSuccess && (
        <Card>
          <CardContent className="space-y-5">
          <div className="inline-flex max-w-full gap-1 overflow-x-auto rounded-xl bg-muted p-1" role="group" aria-label="Filtrar citas">
            {FILTERS.map((item) => {
              const count = item.id === 'upcoming'
                ? upcoming.length
                : item.id === 'history' ? history.length : appointmentsQuery.data.length;
              return (
                <button
                  key={item.id}
                  type="button"
                  aria-pressed={filter === item.id}
                  className={`inline-flex min-h-9 shrink-0 items-center gap-2 rounded-lg px-3 py-2 text-sm font-semibold transition ${filter === item.id ? 'bg-background text-foreground shadow-sm' : 'text-muted-foreground hover:text-foreground'}`}
                  onClick={() => setFilter(item.id)}
                >
                  {item.label} <span className="rounded-full bg-muted px-1.5 py-0.5 text-[0.7rem]">{count}</span>
                </button>
              );
            })}
          </div>

          {visibleAppointments.length === 0 ? (
            <EmptyState
              title={filter === 'upcoming' ? 'No tienes citas próximas' : 'No hay citas en esta sección'}
              description={filter === 'upcoming'
                ? 'Explora las especialidades disponibles y elige el horario que prefieras.'
                : 'Tus citas aparecerán aquí cuando exista información para mostrar.'}
              action={filter === 'upcoming'
                ? <Button asChild><Link to="/agendar">Agendar una cita</Link></Button>
                : null}
            />
          ) : (
            <div className="space-y-4">
              {visibleAppointments.map((appointment) => (
                <AppointmentCard
                  key={appointment.id}
                  appointment={appointment}
                  canManage={canManageAppointment(appointment, now)}
                  onCancel={openCancelDialog}
                  onReschedule={openRescheduleDialog}
                />
              ))}
            </div>
          )}
          </CardContent>
        </Card>
      )}

      <CancelAppointmentDialog
        appointment={appointmentToCancel}
        isSubmitting={cancelAppointment.isPending}
        error={cancelAppointment.error}
        onConfirm={confirmCancellation}
        onClose={() => !cancelAppointment.isPending && closeCancelDialog()}
      />

      <RescheduleAppointmentDialog
        appointment={appointmentToReschedule}
        isSubmitting={rescheduleAppointment.isPending}
        error={rescheduleAppointment.error}
        onConfirm={confirmReschedule}
        onClose={() => !rescheduleAppointment.isPending && closeRescheduleDialog()}
      />
    </div>
  );
}
