import { useEffect, useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { getSlotsDisponibles } from '../../api/citasApi';

export default function ModalReprogramar({ cita, onConfirmar, onClosed, isSubmitting }) {
  if (!cita) return null;

  return (
    <ModalReprogramarContent
      key={cita.id}
      cita={cita}
      onConfirmar={onConfirmar}
      onClosed={onClosed}
      isSubmitting={isSubmitting}
    />
  );
}

function ModalReprogramarContent({ cita, onConfirmar, onClosed, isSubmitting }) {
  const [fecha, setFecha] = useState('');
  const [horario, setHorario] = useState('');

  // El endpoint de reprogramacion conserva al medico y solo cambia fecha/hora.
  const doctorId = cita.doctor?.id;

  const { data: slots = [], isFetching: cargandoSlots } = useQuery({
    queryKey: ['slots', doctorId, fecha],
    queryFn: () => getSlotsDisponibles(doctorId, fecha),
    enabled: !!doctorId && !!fecha,
  });

  const minFecha = new Date().toISOString().split('T')[0];

  useEffect(() => {
    window.bootstrap?.Modal.getOrCreateInstance(
      document.getElementById('modalReprogramar')
    ).show();
  }, []);

  useEffect(() => {
    const element = document.getElementById('modalReprogramar');
    const handleHidden = () => onClosed?.();
    element?.addEventListener('hidden.bs.modal', handleHidden);
    return () => element?.removeEventListener('hidden.bs.modal', handleHidden);
  }, [onClosed]);

  function handleConfirmar() {
    if (!fecha) {
      alert('Selecciona una fecha.');
      return;
    }
    if (!horario) {
      alert('Selecciona un horario.');
      return;
    }

    onConfirmar(cita.id, `${fecha}T${horario}`, {
      onSuccess: () => window.bootstrap?.Modal
        .getInstance(document.getElementById('modalReprogramar'))
        ?.hide(),
      onError: (error) => alert(error.message),
    });
  }

  const paciente = `${cita.patient?.firstName || ''} ${cita.patient?.lastName || ''}`.trim();
  const medico = `${cita.doctor?.firstName || ''} ${cita.doctor?.lastName || ''}`.trim();

  return (
    <div className="modal fade" id="modalReprogramar" tabIndex="-1" aria-hidden="true">
      <div className="modal-dialog modal-dialog-centered">
        <div className="modal-content border-0 rounded-4 shadow">
          <div className="modal-header border-0 pt-4 px-4">
            <h5 className="modal-title fw-bold text-warning">
              <i className="fa-solid fa-clock me-2"></i> Reprogramar Cita
            </h5>
            <button type="button" className="btn-close" data-bs-dismiss="modal"></button>
          </div>

          <div className="modal-body px-4 pb-4">
            <div className="alert alert-light border rounded-3 mb-3 py-2 px-3">
              <small className="text-muted d-block">Paciente</small>
              <span className="fw-bold d-block">{paciente || '—'}</span>
              <small className="text-muted d-block mt-2">Médico asignado</small>
              <span className="fw-semibold">{medico || '—'}</span>
            </div>

            <div className="mb-3">
              <label className="form-label small fw-bold">Nueva fecha</label>
              <input
                type="date"
                className="form-control rounded-3"
                min={minFecha}
                value={fecha}
                onChange={(event) => {
                  setFecha(event.target.value);
                  setHorario('');
                }}
              />
            </div>

            <div className="mb-3">
              <label className="form-label small fw-bold">Horario disponible</label>
              <select
                className="form-select rounded-3"
                value={horario}
                onChange={(event) => setHorario(event.target.value)}
                disabled={!fecha || cargandoSlots}
              >
                <option value="" disabled>
                  {cargandoSlots ? 'Cargando horarios...' : 'Seleccionar horario...'}
                </option>
                {slots.map((slot) => (
                  <option key={slot.startTime} value={slot.startTime}>
                    {slot.startTime.substring(0, 5)} - {slot.endTime.substring(0, 5)}
                  </option>
                ))}
              </select>
            </div>

            <button
              className="btn btn-warning w-100 rounded-pill mt-2 text-white fw-bold"
              onClick={handleConfirmar}
              disabled={isSubmitting}
            >
              <i className="fa-solid fa-calendar-days me-2"></i>
              {isSubmitting ? 'Guardando...' : 'Confirmar Reprogramación'}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
