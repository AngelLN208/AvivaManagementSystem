import { useMemo, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import {
  CalendarCheck2,
  CalendarDays,
  Check,
  Clock3,
  ClipboardPenLine,
  CircleAlert,
  Info,
  LoaderCircle,
  RefreshCw,
  Stethoscope,
  UserRound,
} from 'lucide-react';
import { Alert, AlertDescription } from '@/components/ui/alert.jsx';
import { Button } from '@/components/ui/button.jsx';
import { Card, CardContent, CardHeader } from '@/components/ui/card.jsx';
import { Input } from '@/components/ui/input.jsx';
import { Label } from '@/components/ui/label.jsx';
import { ErrorState, LoadingState } from '../components/ui/AsyncState.jsx';
import PageHeader from '../components/ui/PageHeader.jsx';
import { useAppointmentMutations } from '../hooks/useAppointments.js';
import {
  useAvailableSlots,
  useDoctorsBySpecialty,
  useSpecialties,
} from '../hooks/useCatalogs.js';
import { cn } from '@/lib/utils.js';
import { doctorDisplayName } from '../utils/appointments.js';
import {
  combineLocalDateTime,
  formatLongDate,
  formatSlotTime,
  formatTime,
  getLocalDateInputValue,
  isPastSlot,
} from '../utils/dates.js';
import { normalizePositiveId } from '../utils/ids.js';

const BOOKING_STEPS = ['Especialidad', 'Médico', 'Fecha', 'Horario'];

export default function SchedulePage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const [specialtyId, setSpecialtyId] = useState(
    () => normalizePositiveId(searchParams.get('especialidad')),
  );
  const [doctorId, setDoctorId] = useState(
    () => normalizePositiveId(searchParams.get('medico')),
  );
  const [date, setDate] = useState('');
  const [time, setTime] = useState('');
  const [reason, setReason] = useState('');
  const [formError, setFormError] = useState('');

  const specialtiesQuery = useSpecialties();
  const doctorsQuery = useDoctorsBySpecialty(specialtyId);
  const { createAppointment } = useAppointmentMutations();
  const minimumDate = getLocalDateInputValue();

  const selectedSpecialty = specialtiesQuery.data?.find(
    (specialty) => String(specialty.id) === String(specialtyId),
  );
  const selectedDoctor = doctorsQuery.data?.find(
    (doctor) => String(doctor.id) === String(doctorId),
  );
  const slotsQuery = useAvailableSlots(selectedDoctor?.id, date);
  const availableSlots = useMemo(
    () => (slotsQuery.data || []).filter((slot) => !isPastSlot(date, slot.startTime)),
    [slotsQuery.data, date],
  );
  const selectedDateTime = combineLocalDateTime(date, time);
  const completedSteps = [specialtyId, selectedDoctor?.id, date, time].filter(Boolean).length;

  function handleSubmit(event) {
    event.preventDefault();
    setFormError('');
    createAppointment.reset();

    if (!selectedDoctor || !date || !time) {
      setFormError('Completa la especialidad, el médico, la fecha y el horario.');
      return;
    }

    createAppointment.mutate({
      doctorId: Number(selectedDoctor.id),
      appointmentDateTime: selectedDateTime,
      reason: reason.trim() || null,
    }, {
      onSuccess: () => navigate('/citas?creada=1', { replace: true }),
      onError: (error) => {
        setFormError(error.message);
        if (error.status === 409) {
          setTime('');
          slotsQuery.refetch();
        }
      },
    });
  }

  const pageHeader = (
    <PageHeader
      eyebrow="Reserva en línea"
      title="Agendar una cita"
      description="Selecciona especialidad, médico, fecha y un horario disponible."
    />
  );

  if (specialtiesQuery.isLoading) {
    return <div className="space-y-6">{pageHeader}<LoadingState message="Preparando el agendamiento…" /></div>;
  }
  if (specialtiesQuery.isError) {
    return <div className="space-y-6">{pageHeader}<ErrorState message={specialtiesQuery.error.message} onRetry={specialtiesQuery.refetch} /></div>;
  }

  return (
    <div className="space-y-7">
      {pageHeader}

      <BookingProgress completedSteps={completedSteps} />

      <form className="grid items-start gap-6 lg:grid-cols-[minmax(0,1fr)_20rem] xl:grid-cols-[minmax(0,1fr)_22rem]" onSubmit={handleSubmit}>
        <Card className="gap-0 overflow-hidden py-0">
          <CardContent className="p-0">
            <BookingSection
              number="1"
              icon={Stethoscope}
              title="Elige una especialidad"
              description="Te mostraremos únicamente médicos activos de la especialidad seleccionada."
            >
              <div className="grid gap-3 sm:grid-cols-2">
                {specialtiesQuery.data.map((specialty) => {
                  const isSelected = String(specialtyId) === String(specialty.id);
                  return (
                    <label
                      key={specialty.id}
                      className={cn(
                        'group flex min-h-28 cursor-pointer items-start gap-3 rounded-xl border border-border bg-background p-4 transition hover:border-primary/55 hover:bg-accent/45 focus-within:ring-2 focus-within:ring-ring/30',
                        isSelected && 'border-primary bg-primary/8 ring-1 ring-primary/15',
                      )}
                    >
                      <input
                        className="mt-1 size-4 shrink-0 accent-primary"
                        type="radio"
                        name="specialty"
                        value={specialty.id}
                        checked={isSelected}
                        onChange={() => {
                          setSpecialtyId(String(specialty.id));
                          setDoctorId('');
                          setDate('');
                          setTime('');
                        }}
                      />
                      <span className="min-w-0">
                        <span className={cn('mb-2 grid size-9 place-items-center rounded-lg bg-muted text-muted-foreground', isSelected && 'bg-primary/10 text-primary')} aria-hidden="true">
                          <Stethoscope className="size-5" />
                        </span>
                        <strong className="block text-sm font-semibold text-foreground">{specialty.name}</strong>
                        <small className="mt-1 block text-xs leading-5 text-muted-foreground">{specialty.description || 'Atención especializada'}</small>
                      </span>
                    </label>
                  );
                })}
              </div>
            </BookingSection>

            <BookingSection
              number="2"
              icon={UserRound}
              title="Selecciona a tu médico"
              description={specialtyId ? 'Elige el profesional con quien deseas atenderte.' : 'Primero selecciona una especialidad.'}
            >
              {doctorsQuery.isFetching && (
                <p className="flex items-center gap-2 text-sm text-muted-foreground" role="status">
                  <LoaderCircle className="size-4 animate-spin" aria-hidden="true" />
                  Cargando médicos…
                </p>
              )}
              {doctorsQuery.isError && (
                <Alert variant="destructive">
                  <CircleAlert aria-hidden="true" />
                  <AlertDescription className="space-y-3">
                    <p>{doctorsQuery.error.message}</p>
                    <Button type="button" variant="outline" size="sm" onClick={() => doctorsQuery.refetch()}>
                      <RefreshCw className="size-4" aria-hidden="true" />
                      Reintentar
                    </Button>
                  </AlertDescription>
                </Alert>
              )}
              {specialtyId && !doctorsQuery.isFetching && doctorsQuery.data?.length === 0 && (
                <p className="rounded-xl border border-dashed border-border bg-muted/30 px-4 py-6 text-center text-sm text-muted-foreground">No hay médicos disponibles en esta especialidad.</p>
              )}

              <div className="grid gap-3 sm:grid-cols-2">
                {(doctorsQuery.data || []).map((doctor) => {
                  const isSelected = String(doctorId) === String(doctor.id);
                  return (
                    <label
                      key={doctor.id}
                      className={cn(
                        'flex min-h-20 cursor-pointer items-center gap-3 rounded-xl border border-border bg-background p-3.5 transition hover:border-primary/55 hover:bg-accent/45 focus-within:ring-2 focus-within:ring-ring/30',
                        isSelected && 'border-primary bg-primary/8 ring-1 ring-primary/15',
                      )}
                    >
                      <input
                        className="size-4 shrink-0 accent-primary"
                        type="radio"
                        name="doctor"
                        value={doctor.id}
                        checked={isSelected}
                        onChange={() => {
                          setDoctorId(String(doctor.id));
                          setDate('');
                          setTime('');
                        }}
                      />
                      <span className="grid size-11 shrink-0 place-items-center rounded-xl bg-primary/10 text-xs font-bold uppercase text-primary" aria-hidden="true">
                        {doctor.firstName?.[0]}{doctor.lastName?.[0]}
                      </span>
                      <span className="min-w-0 flex-1">
                        <strong className="block truncate text-sm font-semibold text-foreground">{doctorDisplayName(doctor)}</strong>
                        <small className="mt-1 block truncate text-xs text-muted-foreground">{doctor.specialty?.name}</small>
                      </span>
                      {isSelected && <Check className="size-5 shrink-0 text-primary" aria-hidden="true" />}
                    </label>
                  );
                })}
              </div>
            </BookingSection>

            <BookingSection
              number="3"
              icon={CalendarDays}
              title="Escoge fecha y horario"
              description="Los horarios ocupados se excluyen automáticamente."
            >
              <div className="max-w-sm space-y-2">
                <Label htmlFor="appointment-date">Fecha de la cita</Label>
                <Input
                  id="appointment-date"
                  className="h-11"
                  type="date"
                  min={minimumDate}
                  value={date}
                  onChange={(event) => {
                    setDate(event.target.value);
                    setTime('');
                  }}
                  disabled={!selectedDoctor}
                  required
                />
                {!selectedDoctor && <p className="text-xs text-muted-foreground">Selecciona un médico para habilitar el calendario.</p>}
              </div>

              {date && slotsQuery.isLoading && (
                <p className="flex items-center gap-2 text-sm text-muted-foreground" role="status">
                  <LoaderCircle className="size-4 animate-spin" aria-hidden="true" />
                  Consultando horarios…
                </p>
              )}
              {date && slotsQuery.isError && (
                <Alert variant="destructive">
                  <CircleAlert aria-hidden="true" />
                  <AlertDescription className="space-y-3">
                    <p>{slotsQuery.error.message}</p>
                    <Button type="button" variant="outline" size="sm" onClick={() => slotsQuery.refetch()}>
                      <RefreshCw className="size-4" aria-hidden="true" />
                      Reintentar
                    </Button>
                  </AlertDescription>
                </Alert>
              )}
              {date && !slotsQuery.isLoading && !slotsQuery.isError && availableSlots.length === 0 && (
                <Alert variant="info" role="status">
                  <Info aria-hidden="true" />
                  <AlertDescription>No quedan horarios disponibles para este día. Prueba con otra fecha.</AlertDescription>
                </Alert>
              )}
              {date && !slotsQuery.isLoading && !slotsQuery.isError && availableSlots.length > 0 && (
                <p className="text-sm text-muted-foreground" role="status" aria-live="polite">
                  {availableSlots.length} {availableSlots.length === 1 ? 'horario disponible' : 'horarios disponibles'}.
                </p>
              )}

              <div className="grid grid-cols-2 gap-2.5 sm:grid-cols-3 xl:grid-cols-4">
                {availableSlots.map((slot) => {
                  const isSelected = time === slot.startTime;
                  return (
                    <label
                      key={slot.startTime}
                      className={cn(
                        'flex min-h-11 cursor-pointer items-center justify-center gap-2 rounded-lg border border-input bg-background px-3 py-2 text-sm font-semibold transition hover:border-primary/60 hover:bg-accent/50 focus-within:ring-2 focus-within:ring-ring/30',
                        isSelected && 'border-primary bg-primary/10 text-primary ring-1 ring-primary/20',
                      )}
                    >
                      <input
                        className="size-4 shrink-0 accent-primary"
                        type="radio"
                        name="appointment-time"
                        value={slot.startTime}
                        checked={isSelected}
                        onChange={() => setTime(slot.startTime)}
                      />
                      {formatSlotTime(slot.startTime)}
                    </label>
                  );
                })}
              </div>
            </BookingSection>

            <BookingSection
              number="4"
              icon={ClipboardPenLine}
              title="Motivo de la cita"
              description="Es opcional y ayuda a identificar tu solicitud."
              last
            >
              <div className="space-y-2">
                <Label htmlFor="appointment-reason">Motivo <span className="font-normal text-muted-foreground">(opcional)</span></Label>
                <textarea
                  id="appointment-reason"
                  className="min-h-28 w-full resize-y rounded-lg border border-input bg-background px-3 py-2.5 text-sm text-foreground shadow-xs outline-none transition placeholder:text-muted-foreground focus-visible:border-ring focus-visible:ring-2 focus-visible:ring-ring/25"
                  rows="4"
                  maxLength="500"
                  value={reason}
                  onChange={(event) => setReason(event.target.value)}
                  placeholder="Ej. Control general"
                />
                <small className="block text-right text-xs text-muted-foreground">{reason.length}/500</small>
              </div>
            </BookingSection>
          </CardContent>
        </Card>

        <Card className="gap-0 overflow-hidden py-0 lg:sticky lg:top-6">
          <CardHeader className="border-b border-border bg-primary/5 px-5 py-5">
            <span className="grid size-10 place-items-center rounded-xl bg-primary/10 text-primary" aria-hidden="true">
              <CalendarCheck2 className="size-5" />
            </span>
            <p className="mt-3 text-xs font-bold uppercase tracking-[0.16em] text-primary">Resumen</p>
            <h2 className="text-xl font-semibold tracking-tight">Tu cita</h2>
          </CardHeader>
          <CardContent className="space-y-5 px-5 py-5">
            <dl className="divide-y divide-border">
              <SummaryRow term="Especialidad" value={selectedSpecialty?.name || 'Por seleccionar'} />
              <SummaryRow term="Médico" value={selectedDoctor ? doctorDisplayName(selectedDoctor) : 'Por seleccionar'} />
              <SummaryRow term="Fecha" value={selectedDateTime ? formatLongDate(selectedDateTime) : 'Por seleccionar'} />
              <SummaryRow term="Hora" value={selectedDateTime ? formatTime(selectedDateTime) : 'Por seleccionar'} />
            </dl>

            <Alert variant="warning" role="status">
              <Clock3 aria-hidden="true" />
              <AlertDescription>La cita quedará inicialmente pendiente de confirmación.</AlertDescription>
            </Alert>

            {(formError || createAppointment.error) && (
              <Alert variant="destructive" role="alert">
                <CircleAlert aria-hidden="true" />
                <AlertDescription>{formError || createAppointment.error.message}</AlertDescription>
              </Alert>
            )}

            <Button type="submit" size="lg" className="h-12 w-full" disabled={!selectedDateTime || createAppointment.isPending}>
              {createAppointment.isPending && <LoaderCircle className="size-5 animate-spin" aria-hidden="true" />}
              {createAppointment.isPending ? 'Agendando…' : 'Confirmar cita'}
            </Button>
          </CardContent>
        </Card>
      </form>
    </div>
  );
}

function BookingProgress({ completedSteps }) {
  return (
    <div className="relative rounded-2xl border border-border bg-card px-4 py-4 shadow-sm sm:px-6" aria-label={`Progreso: ${completedSteps} de 4 selecciones completadas`}>
      <span className="absolute left-[12.5%] right-[12.5%] top-8 h-px bg-border" aria-hidden="true" />
      <ol className="relative grid grid-cols-4 gap-1">
        {BOOKING_STEPS.map((step, index) => {
          const completed = completedSteps > index;
          const current = completedSteps === index;
          return (
            <li key={step} className="relative flex min-w-0 flex-col items-center gap-2 text-center" aria-current={current ? 'step' : undefined}>
              <span
                className={cn(
                  'relative z-10 grid size-8 place-items-center rounded-full border bg-background text-xs font-bold text-muted-foreground',
                  completed && 'border-primary bg-primary text-primary-foreground',
                  current && 'border-2 border-primary bg-primary/10 text-primary',
                )}
                aria-hidden="true"
              >
                {completed ? <Check className="size-4" /> : index + 1}
              </span>
              <small className={cn('truncate text-[0.68rem] font-medium text-muted-foreground sm:text-xs', current && 'font-semibold text-foreground')}>{step}</small>
            </li>
          );
        })}
      </ol>
    </div>
  );
}

function BookingSection({ number, icon: Icon, title, description, children, last = false }) {
  return (
    <section className={cn('grid gap-4 px-5 py-7 sm:grid-cols-[2.5rem_minmax(0,1fr)] sm:px-7 sm:py-8', !last && 'border-b border-border')}>
      <span className="grid size-10 place-items-center rounded-xl bg-primary/10 text-sm font-bold text-primary" aria-hidden="true">{number}</span>
      <div className="min-w-0">
        <div className="mb-5">
          <div className="flex items-center gap-2">
            <Icon className="size-5 text-primary" aria-hidden="true" />
            <h2 className="text-lg font-semibold tracking-tight sm:text-xl">{title}</h2>
          </div>
          <p className="mt-1 text-sm leading-6 text-muted-foreground">{description}</p>
        </div>
        <div className="space-y-4">{children}</div>
      </div>
    </section>
  );
}

function SummaryRow({ term, value }) {
  return (
    <div className="py-3 first:pt-0 last:pb-0">
      <dt className="text-xs font-semibold text-muted-foreground">{term}</dt>
      <dd className="mt-1 text-sm font-semibold text-foreground">{value}</dd>
    </div>
  );
}
