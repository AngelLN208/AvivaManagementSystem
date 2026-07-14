import { useEffect } from 'react';
import { useQuery } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { getCitasDePaciente } from '../../api/pacientesApi';
import { calcularEdad, formatFechaHora } from '../../utils/formatters';
import BadgeEstado from '../ui/BadgeEstado';

export default function ModalVerPaciente({ paciente, onClosed }) {
  const navigate = useNavigate();

  const { data: citas = [], isLoading } = useQuery({
    queryKey: ['citasPaciente', paciente?.id],
    queryFn: () => getCitasDePaciente(paciente.id),
    enabled: !!paciente,
  });

  useEffect(() => {
    if (paciente) {
      window.bootstrap?.Modal.getOrCreateInstance(document.getElementById('modalVerPaciente')).show();
    }
  }, [paciente]);

  useEffect(() => {
    const el = document.getElementById('modalVerPaciente');
    const handleHidden = () => onClosed?.();
    el?.addEventListener('hidden.bs.modal', handleHidden);
    return () => el?.removeEventListener('hidden.bs.modal', handleHidden);
  }, [onClosed]);

  function handleNuevaCita() {
    window.bootstrap?.Modal.getInstance(document.getElementById('modalVerPaciente'))?.hide();
    navigate('/citas');
  }

  const nombre = paciente ? `${paciente.firstName || ''} ${paciente.lastName || ''}`.trim() : '—';
  const iniciales = paciente ? (paciente.firstName?.[0] || '') + (paciente.lastName?.[0] || '') : '--';

  return (
    <div className="modal fade" id="modalVerPaciente" tabIndex="-1" aria-hidden="true">
      <div className="modal-dialog modal-dialog-centered">
        <div className="modal-content border-0 rounded-4 shadow">
          <div className="modal-header border-0 pt-4 px-4">
            <h5 className="modal-title fw-bold"><i className="fa-solid fa-user me-2 text-primary"></i> Detalle del Paciente</h5>
            <button type="button" className="btn-close" data-bs-dismiss="modal"></button>
          </div>
          <div className="modal-body px-4 pb-4">
            <div className="d-flex align-items-center gap-3 mb-4">
              <div className="bg-primary text-white rounded-circle d-flex align-items-center justify-content-center shadow-sm flex-shrink-0 fw-bold" style={{ width: 56, height: 56, fontSize: '1.4rem' }}>
                {iniciales}
              </div>
              <div>
                <h5 className="fw-bold mb-0">{nombre}</h5>
                <small className="text-muted">DNI: {paciente?.dni || '—'}</small>
              </div>
            </div>

            <div className="row g-3 mb-3">
              <div className="col-6">
                <div className="p-3 rounded-3 fondo-info-sutil">
                  <small className="text-muted d-block">Edad</small>
                  <span className="fw-bold">{paciente ? calcularEdad(paciente.dateOfBirth) : '—'}</span> años
                </div>
              </div>
              <div className="col-6">
                <div className="p-3 rounded-3 fondo-exito-sutil">
                  <small className="text-muted d-block">Teléfono</small>
                  <span className="fw-bold">{paciente?.phone || '—'}</span>
                </div>
              </div>
              <div className="col-12">
                <div className="p-3 rounded-3 border">
                  <small className="text-muted d-block">Correo</small>
                  <span className="fw-bold">{paciente?.email || '—'}</span>
                </div>
              </div>
            </div>

            <div className="p-3 rounded-3 border mb-3">
              <small className="text-muted d-block mb-2 fw-bold">Citas registradas</small>
              <div className="small text-muted">
                {isLoading ? (
                  'Cargando...'
                ) : !citas.length ? (
                  <span className="text-muted">Sin citas registradas.</span>
                ) : (
                  citas.slice(0, 5).map((c) => (
                    <div key={c.id} className="d-flex justify-content-between align-items-center py-1 border-bottom">
                      <span className="small">
                        {formatFechaHora(c.appointmentDateTime)} — Dr. {c.doctor?.firstName || ''} {c.doctor?.lastName || ''}
                      </span>
                      <BadgeEstado estado={c.status} />
                    </div>
                  ))
                )}
              </div>
            </div>

            <div className="d-flex gap-2">
              <button className="btn btn-outline-secondary flex-grow-1 rounded-pill" data-bs-dismiss="modal">Cerrar</button>
              <button className="btn btn-primary flex-grow-1 rounded-pill text-white" onClick={handleNuevaCita}>
                <i className="fa-solid fa-calendar-plus me-1"></i> Nueva Cita
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}