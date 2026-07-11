import { useEffect, useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { getEspecialidades, getMedicosPorEspecialidad, getSlotsDisponibles } from '../../api/citasApi';

export default function ModalReprogramar({ cita, onConfirmar, onClosed, isSubmitting }) {
  const [especialidadId, setEspecialidadId] = useState('');
  const [medicoId, setMedicoId] = useState('');
  const [fecha, setFecha] = useState('');
  const [horario, setHorario] = useState('');

  const { data: especialidades = [] } = useQuery({ queryKey: ['especialidades'], queryFn: getEspecialidades });

  const { data: medicos = [], isFetching: cargandoMedicos } = useQuery({
    queryKey: ['medicos', especialidadId],
    queryFn: () => getMedicosPorEspecialidad(especialidadId),
    enabled: !!especialidadId,
  });

  const { data: slots = [], isFetching: cargandoSlots } = useQuery({
    queryKey: ['slots', medicoId, fecha],
    queryFn: () => getSlotsDisponibles(medicoId, fecha),
    enabled: !!medicoId && !!fecha,
  });

  const minFecha = new Date().toISOString().split('T')[0];

  useEffect(() => {
    if (cita) {
      setEspecialidadId(cita.doctor?.specialty?.id ? String(cita.doctor.specialty.id) : '');
      setMedicoId(cita.doctor?.id ? String(cita.doctor.id) : '');
      setFecha('');
      setHorario('');
      window.bootstrap?.Modal.getOrCreateInstance(document.getElementById('modalReprogramar')).show();
    }
  }, [cita]);

  useEffect(() => {
    const el = document.getElementById('modalReprogramar');
    const handleHidden = () => onClosed?.();
    el?.addEventListener('hidden.bs.modal', handleHidden);
    return () => el?.removeEventListener('hidden.bs.modal', handleHidden);
  }, [onClosed]);

  function handleConfirmar() {
    if (!medicoId) { alert('❌ Selecciona un médico.'); return; }
    if (!fecha)    { alert('❌ Selecciona una fecha.'); return; }
    if (!horario)  { alert('❌ Selecciona un horario.'); return; }

    onConfirmar(cita.id, `${fecha}T${horario}`, {
      onSuccess: () => window.bootstrap?.Modal.getInstance(document.getElementById('modalReprogramar'))?.hide(),
      onError: (err) => alert(`❌ ${err.message}`),
    });
  }

  const nombrePaciente = cita ? `${cita.patient?.firstName || ''} ${cita.patient?.lastName || ''}`.trim() : '';

  return (
    <div className="modal fade" id="modalReprogramar" tabIndex="-1" aria-hidden="true">
      <div className="modal-dialog modal-dialog-centered">
        <div className="modal-content border-0 rounded-4 shadow">
          <div className="modal-header border-0 pt-4 px-4">
            <h5 className="modal-title fw-bold text-warning"><i className="fa-solid fa-clock me-2"></i> Reprogramar Cita</h5>
            <button type="button" className="btn-close" data-bs-dismiss="modal"></button>
          </div>
          <div className="modal-body px-4 pb-4">
            <div className="alert alert-light border rounded-3 mb-3 py-2 px-3">
              <small className="text-muted">Reprogramando cita de:</small>
              <p className="fw-bold mb-0 text-dark">{nombrePaciente}</p>
            </div>

            <div className="mb-3">
              <label className="form-label small fw-bold">Especialidad</label>
              <select
                className="form-select rounded-3"
                value={especialidadId}
                onChange={(e) => { setEspecialidadId(e.target.value); setMedicoId(''); setFecha(''); setHorario(''); }}
              >
                <option value="" disabled>Seleccionar especialidad...</option>
                {especialidades.map((esp) => <option key={esp.id} value={esp.id}>{esp.name}</option>)}
              </select>
            </div>

            <div className="mb-3">
              <label className="form-label small fw-bold">Médico</label>
              <select
                className="form-select rounded-3"
                value={medicoId}
                onChange={(e) => { setMedicoId(e.target.value); setFecha(''); setHorario(''); }}
                disabled={!especialidadId || cargandoMedicos}
              >
                <option value="" disabled>{cargandoMedicos ? 'Cargando...' : 'Seleccionar médico...'}</option>
                {medicos.map((m) => <option key={m.id} value={m.id}>{m.firstName} {m.lastName}</option>)}
              </select>
            </div>

            <div className="mb-3">
              <label className="form-label small fw-bold">Nueva Fecha</label>
              <input
                type="date"
                className="form-control rounded-3"
                min={minFecha}
                value={fecha}
                onChange={(e) => { setFecha(e.target.value); setHorario(''); }}
                disabled={!medicoId}
              />
            </div>

            <div className="mb-3">
              <label className="form-label small fw-bold">Horario Disponible</label>
              <select
                className="form-select rounded-3"
                value={horario}
                onChange={(e) => setHorario(e.target.value)}
                disabled={!medicoId || !fecha || cargandoSlots}
              >
                <option value="" disabled>{cargandoSlots ? 'Cargando horarios...' : 'Seleccionar horario...'}</option>
                {slots.map((s) => (
                  <option key={s.startTime} value={s.startTime}>{s.startTime.substring(0, 5)} — {s.endTime.substring(0, 5)}</option>
                ))}
              </select>
            </div>

            <button className="btn btn-warning w-100 rounded-pill mt-2 text-white fw-bold" onClick={handleConfirmar} disabled={isSubmitting}>
              <i className="fa-solid fa-calendar-days me-2"></i> {isSubmitting ? 'Guardando...' : 'Confirmar Reprogramación'}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}