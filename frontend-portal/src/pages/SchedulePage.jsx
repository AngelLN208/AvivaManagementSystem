import { useMemo, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { ErrorState, LoadingState } from '../components/ui/AsyncState.jsx';
import InlineAlert from '../components/ui/InlineAlert.jsx';
import PageHeader from '../components/ui/PageHeader.jsx';
import { useAppointmentMutations } from '../hooks/useAppointments.js';
import {
  useAvailableSlots,
  useDoctorsBySpecialty,
  useSpecialties,
} from '../hooks/useCatalogs.js';
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

  if (specialtiesQuery.isLoading) return <LoadingState message="Preparando el agendamiento…" />;
  if (specialtiesQuery.isError) {
    return <ErrorState message={specialtiesQuery.error.message} onRetry={specialtiesQuery.refetch} />;
  }

  return (
    <div className="page-stack">
      <PageHeader
        eyebrow="Reserva en línea"
        title="Agendar una cita"
        description="Selecciona especialidad, médico, fecha y un horario disponible."
      />

      <div className="booking-progress" aria-label={`Progreso: ${completedSteps} de 4 selecciones completadas`}>
        {['Especialidad', 'Médico', 'Fecha', 'Horario'].map((step, index) => (
          <div key={step} className={completedSteps > index ? 'is-complete' : completedSteps === index ? 'is-current' : ''}>
            <span>{completedSteps > index ? '✓' : index + 1}</span>
            <small>{step}</small>
          </div>
        ))}
      </div>

      <form className="booking-layout" onSubmit={handleSubmit}>
        <section className="section-card booking-form-card">
          <div className="form-section">
            <div className="form-section__number">1</div>
            <div className="form-section__content">
              <h2>Elige una especialidad</h2>
              <p>Te mostraremos únicamente médicos activos de la especialidad seleccionada.</p>
              <div className="specialty-grid">
                {specialtiesQuery.data.map((specialty) => (
                  <label key={specialty.id} className={`choice-card ${String(specialtyId) === String(specialty.id) ? 'is-selected' : ''}`}>
                    <input
                      type="radio"
                      name="specialty"
                      value={specialty.id}
                      checked={String(specialtyId) === String(specialty.id)}
                      onChange={() => {
                        setSpecialtyId(String(specialty.id));
                        setDoctorId('');
                        setDate('');
                        setTime('');
                      }}
                    />
                    <span className="choice-card__mark" aria-hidden="true">+</span>
                    <strong>{specialty.name}</strong>
                    <small>{specialty.description || 'Atención especializada'}</small>
                  </label>
                ))}
              </div>
            </div>
          </div>

          <div className="form-section">
            <div className="form-section__number">2</div>
            <div className="form-section__content">
              <h2>Selecciona a tu médico</h2>
              <p>{specialtyId ? 'Elige el profesional con quien deseas atenderte.' : 'Primero selecciona una especialidad.'}</p>

              {doctorsQuery.isFetching && <p className="helper-text" role="status">Cargando médicos…</p>}
              {doctorsQuery.isError && (
                <div className="query-error" role="alert">
                  <p className="form-error">{doctorsQuery.error.message}</p>
                  <button type="button" className="button button--secondary button--small" onClick={() => doctorsQuery.refetch()}>
                    Reintentar
                  </button>
                </div>
              )}
              {specialtyId && !doctorsQuery.isFetching && doctorsQuery.data?.length === 0 && (
                <p className="helper-text">No hay médicos disponibles en esta especialidad.</p>
              )}

              <div className="doctor-choice-grid">
                {(doctorsQuery.data || []).map((doctor) => (
                  <label key={doctor.id} className={`doctor-choice ${String(doctorId) === String(doctor.id) ? 'is-selected' : ''}`}>
                    <input
                      type="radio"
                      name="doctor"
                      value={doctor.id}
                      checked={String(doctorId) === String(doctor.id)}
                      onChange={() => {
                        setDoctorId(String(doctor.id));
                        setDate('');
                        setTime('');
                      }}
                    />
                    <span className="doctor-avatar" aria-hidden="true">{doctor.firstName?.[0]}{doctor.lastName?.[0]}</span>
                    <span><strong>{doctorDisplayName(doctor)}</strong><small>{doctor.specialty?.name}</small></span>
                    <span className="choice-check" aria-hidden="true">✓</span>
                  </label>
                ))}
              </div>
            </div>
          </div>

          <div className="form-section">
            <div className="form-section__number">3</div>
            <div className="form-section__content">
              <h2>Escoge fecha y horario</h2>
              <p>Los horarios ocupados se excluyen automáticamente.</p>
              <div className="form-field booking-date-field">
                <label htmlFor="appointment-date">Fecha de la cita</label>
                <input
                  id="appointment-date"
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
              </div>

              {date && slotsQuery.isLoading && <p className="helper-text" role="status">Consultando horarios…</p>}
              {date && slotsQuery.isError && (
                <div className="query-error" role="alert">
                  <p className="form-error">{slotsQuery.error.message}</p>
                  <button type="button" className="button button--secondary button--small" onClick={() => slotsQuery.refetch()}>
                    Reintentar
                  </button>
                </div>
              )}
              {date && !slotsQuery.isLoading && !slotsQuery.isError && availableSlots.length === 0 && (
                <InlineAlert>No quedan horarios disponibles para este día. Prueba con otra fecha.</InlineAlert>
              )}
              <div className="slot-grid slot-grid--booking">
                {availableSlots.map((slot) => (
                  <label key={slot.startTime} className={`slot-option ${time === slot.startTime ? 'is-selected' : ''}`}>
                    <input
                      type="radio"
                      name="appointment-time"
                      value={slot.startTime}
                      checked={time === slot.startTime}
                      onChange={() => setTime(slot.startTime)}
                    />
                    {formatSlotTime(slot.startTime)}
                  </label>
                ))}
              </div>
            </div>
          </div>

          <div className="form-section form-section--last">
            <div className="form-section__number">4</div>
            <div className="form-section__content">
              <h2>Motivo de la cita</h2>
              <p>Es opcional y ayuda a identificar tu solicitud.</p>
              <div className="form-field">
                <label htmlFor="appointment-reason">Motivo <span>(opcional)</span></label>
                <textarea
                  id="appointment-reason"
                  rows="4"
                  maxLength="500"
                  value={reason}
                  onChange={(event) => setReason(event.target.value)}
                  placeholder="Ej. Control general"
                />
                <small className="character-count">{reason.length}/500</small>
              </div>
            </div>
          </div>
        </section>

        <aside className="booking-summary">
          <p className="eyebrow">Resumen</p>
          <h2>Tu cita</h2>
          <dl>
            <div><dt>Especialidad</dt><dd>{selectedSpecialty?.name || 'Por seleccionar'}</dd></div>
            <div><dt>Médico</dt><dd>{selectedDoctor ? doctorDisplayName(selectedDoctor) : 'Por seleccionar'}</dd></div>
            <div><dt>Fecha</dt><dd>{selectedDateTime ? formatLongDate(selectedDateTime) : 'Por seleccionar'}</dd></div>
            <div><dt>Hora</dt><dd>{selectedDateTime ? formatTime(selectedDateTime) : 'Por seleccionar'}</dd></div>
          </dl>
          <p className="booking-summary__note">La cita quedará inicialmente pendiente de confirmación.</p>
          {(formError || createAppointment.error) && (
            <p className="form-error" role="alert">{formError || createAppointment.error.message}</p>
          )}
          <button type="submit" className="button button--primary button--wide" disabled={!selectedDateTime || createAppointment.isPending}>
            {createAppointment.isPending ? 'Agendando…' : 'Confirmar cita'}
          </button>
        </aside>
      </form>
    </div>
  );
}
