import { useEffect, useMemo, useRef, useState } from 'react';
import { useAvailableSlots } from '../../hooks/useCatalogs.js';
import { doctorDisplayName } from '../../utils/appointments.js';
import {
  combineLocalDateTime,
  formatSlotTime,
  getLocalDateInputValue,
  isPastSlot,
} from '../../utils/dates.js';

export default function RescheduleAppointmentDialog({ appointment, isSubmitting, error, onConfirm, onClose }) {
  if (!appointment) return null;

  return (
    <RescheduleAppointmentDialogContent
      key={`${appointment.id}-${error?.status === 409 ? 'conflict' : 'ready'}`}
      appointment={appointment}
      isSubmitting={isSubmitting}
      error={error}
      onConfirm={onConfirm}
      onClose={onClose}
    />
  );
}

function RescheduleAppointmentDialogContent({ appointment, isSubmitting, error, onConfirm, onClose }) {
  const dialogRef = useRef(null);
  const [date, setDate] = useState('');
  const [time, setTime] = useState('');
  const doctorId = appointment.doctor?.id;
  const slotsQuery = useAvailableSlots(doctorId, date);
  const minimumDate = getLocalDateInputValue();

  useEffect(() => {
    const dialog = dialogRef.current;
    if (dialog && !dialog.open) dialog.showModal();
    return () => {
      if (dialog?.open) dialog.close();
    };
  }, []);

  const availableSlots = useMemo(
    () => (slotsQuery.data || []).filter((slot) => !isPastSlot(date, slot.startTime)),
    [slotsQuery.data, date],
  );

  function handleSubmit(event) {
    event.preventDefault();
    if (!date || !time) return;
    onConfirm(combineLocalDateTime(date, time));
  }

  function requestClose() {
    dialogRef.current?.close();
    onClose();
  }

  return (
    <dialog
      ref={dialogRef}
      className="portal-dialog"
      aria-labelledby="reschedule-dialog-title"
      onCancel={(event) => {
        event.preventDefault();
        if (!isSubmitting) requestClose();
      }}
      onClick={(event) => {
        if (event.target === event.currentTarget && !isSubmitting) requestClose();
      }}
    >
      <form className="portal-dialog__content" onSubmit={handleSubmit}>
        <button type="button" className="dialog-close" aria-label="Cerrar" disabled={isSubmitting} onClick={requestClose}>×</button>
        <p className="eyebrow">Nueva fecha y hora</p>
        <h2 id="reschedule-dialog-title">Reprogramar cita</h2>
        <p className="dialog-copy">El médico se mantiene: <strong>{doctorDisplayName(appointment.doctor)}</strong>.</p>

        <div className="form-field">
          <label htmlFor="reschedule-date">Fecha</label>
          <input
            id="reschedule-date"
            type="date"
            min={minimumDate}
            value={date}
            onChange={(event) => {
              setDate(event.target.value);
              setTime('');
            }}
            required
          />
        </div>

        <fieldset className="slot-fieldset" disabled={!date || slotsQuery.isLoading || isSubmitting}>
          <legend>Horario disponible</legend>
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
            <p className="helper-text">No hay horarios disponibles para esta fecha.</p>
          )}
          <div className="slot-grid">
            {availableSlots.map((slot) => (
              <label key={slot.startTime} className={`slot-option ${time === slot.startTime ? 'is-selected' : ''}`}>
                <input
                  type="radio"
                  name="reschedule-time"
                  value={slot.startTime}
                  checked={time === slot.startTime}
                  onChange={() => setTime(slot.startTime)}
                />
                {formatSlotTime(slot.startTime)}
              </label>
            ))}
          </div>
        </fieldset>

        {error && <p className="form-error" role="alert">{error.message}</p>}
        <div className="dialog-actions">
          <button type="button" className="button button--secondary" disabled={isSubmitting} onClick={requestClose}>Volver</button>
          <button type="submit" className="button button--primary" disabled={!date || !time || isSubmitting}>
            {isSubmitting ? 'Guardando…' : 'Confirmar cambio'}
          </button>
        </div>
      </form>
    </dialog>
  );
}
