import { useEffect, useRef } from 'react';
import { doctorDisplayName } from '../../utils/appointments.js';
import { formatLongDate, formatTime } from '../../utils/dates.js';

export default function CancelAppointmentDialog({ appointment, isSubmitting, error, onConfirm, onClose }) {
  const dialogRef = useRef(null);

  useEffect(() => {
    const dialog = dialogRef.current;
    if (appointment && dialog && !dialog.open) dialog.showModal();
    return () => {
      if (dialog?.open) dialog.close();
    };
  }, [appointment]);

  if (!appointment) return null;

  function requestClose() {
    dialogRef.current?.close();
    onClose();
  }

  return (
    <dialog
      ref={dialogRef}
      className="portal-dialog"
      aria-labelledby="cancel-dialog-title"
      onCancel={(event) => {
        event.preventDefault();
        if (!isSubmitting) requestClose();
      }}
      onClick={(event) => {
        if (event.target === event.currentTarget && !isSubmitting) requestClose();
      }}
    >
      <div className="portal-dialog__content">
        <button type="button" className="dialog-close" aria-label="Cerrar" disabled={isSubmitting} onClick={requestClose}>×</button>
        <span className="dialog-symbol dialog-symbol--danger" aria-hidden="true">!</span>
        <p className="eyebrow">Confirmar cancelación</p>
        <h2 id="cancel-dialog-title">¿Deseas cancelar esta cita?</h2>
        <p className="dialog-copy">
          {doctorDisplayName(appointment.doctor)} · {formatLongDate(appointment.appointmentDateTime)} a las {formatTime(appointment.appointmentDateTime)}.
        </p>
        {error && <p className="form-error" role="alert">{error.message}</p>}
        <div className="dialog-actions">
          <button type="button" className="button button--secondary" disabled={isSubmitting} onClick={requestClose}>Volver</button>
          <button type="button" className="button button--danger" disabled={isSubmitting} onClick={onConfirm}>
            {isSubmitting ? 'Cancelando…' : 'Sí, cancelar'}
          </button>
        </div>
      </div>
    </dialog>
  );
}
