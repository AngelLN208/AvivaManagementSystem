import { useEffect, useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { getSlotsDisponibles } from '../../api/horariosApi';

export default function ModalDisponibilidad({ doctor, onClosed }) {
  const [fecha, setFecha] = useState('');
  const minFecha = new Date().toISOString().split('T')[0];

  const { data: slots = [], isFetching } = useQuery({
    queryKey: ['slotsDisponibilidad', doctor?.id, fecha],
    queryFn: () => getSlotsDisponibles(doctor.id, fecha),
    enabled: !!doctor && !!fecha,
  });

  useEffect(() => {
    if (doctor) {
      setFecha('');
      window.bootstrap?.Modal.getOrCreateInstance(document.getElementById('modalDisponibilidad')).show();
    }
  }, [doctor]);

  useEffect(() => {
    const el = document.getElementById('modalDisponibilidad');
    const handleHidden = () => onClosed?.();
    el?.addEventListener('hidden.bs.modal', handleHidden);
    return () => el?.removeEventListener('hidden.bs.modal', handleHidden);
  }, [onClosed]);

  const nombre = doctor ? `Dr. ${doctor.firstName} ${doctor.lastName}` : '—';
  const especialidad = doctor?.specialty?.name || '—';
  const iniciales = doctor ? (doctor.firstName?.[0] || '') + (doctor.lastName?.[0] || '') : '--';

  return (
    <div className="modal fade" id="modalDisponibilidad" tabIndex="-1" aria-hidden="true">
      <div className="modal-dialog modal-dialog-centered">
        <div className="modal-content border-0 rounded-4 shadow">
          <div className="modal-header border-bottom-0 pt-4 px-4">
            <h5 className="modal-title fw-bold">
              <i className="fa-solid fa-calendar-check me-2 text-primary"></i> Disponibilidad del Médico
            </h5>
            <button type="button" className="btn-close" data-bs-dismiss="modal"></button>
          </div>
          <div className="modal-body px-4 pb-4">
            <div className="alert alert-light border rounded-3 p-3 mb-3">
              <div className="d-flex align-items-center gap-3">
                <div className="bg-primary text-white rounded-circle d-flex align-items-center justify-content-center flex-shrink-0 fw-bold"
                  style={{ width: 48, height: 48, fontSize: '1rem' }}>
                  {iniciales}
                </div>
                <div>
                  <p className="fw-bold mb-0">{nombre}</p>
                  <small className="text-muted">{especialidad}</small>
                </div>
              </div>
            </div>

            <div className="mb-3">
              <label className="form-label small fw-bold">Seleccionar Fecha</label>
              <input
                type="date"
                className="form-control rounded-3"
                min={minFecha}
                value={fecha}
                onChange={(e) => setFecha(e.target.value)}
              />
            </div>

            {fecha && (
              isFetching ? (
                <p className="text-muted small">Cargando...</p>
              ) : slots.length > 0 ? (
                <div>
                  <label className="form-label small fw-bold">Horarios Disponibles</label>
                  <div className="d-flex flex-wrap gap-2">
                    {slots.map((s) => (
                      <span key={s.startTime} className="badge fondo-exito-sutil texto-exito rounded-pill px-3 py-2">
                        {s.startTime.substring(0, 5)} — {s.endTime.substring(0, 5)}
                      </span>
                    ))}
                  </div>
                </div>
              ) : (
                <p className="text-muted text-center py-2">No hay horarios disponibles para esta fecha.</p>
              )
            )}
          </div>
        </div>
      </div>
    </div>
  );
}