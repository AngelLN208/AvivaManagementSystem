import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { getEspecialidades, getMedicosPorEspecialidad, getSlotsDisponibles, buscarPacientePorDni } from '../../api/citasApi';
import { calcularEdad } from '../../utils/formatters';

export default function ModalNuevaCita({ onCrear, isCreating }) {
  const [dni, setDni] = useState('');
  const [paciente, setPaciente] = useState(null);
  const [pacienteNoEncontrado, setPacienteNoEncontrado] = useState(false);
  const [buscando, setBuscando] = useState(false);
  const [especialidadId, setEspecialidadId] = useState('');
  const [medicoId, setMedicoId] = useState('');
  const [fecha, setFecha] = useState('');
  const [horario, setHorario] = useState('');
  const [motivo, setMotivo] = useState('');

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

  async function handleBuscarDni() {
    if (!dni.trim()) return;
    setBuscando(true);
    try {
      const p = await buscarPacientePorDni(dni.trim());
      setPaciente(p);
      setPacienteNoEncontrado(false);
    } catch {
      setPaciente(null);
      setPacienteNoEncontrado(true);
    } finally {
      setBuscando(false);
    }
  }

  function resetForm() {
    setDni(''); setPaciente(null); setPacienteNoEncontrado(false);
    setEspecialidadId(''); setMedicoId(''); setFecha(''); setHorario(''); setMotivo('');
  }

  function handleSubmit(e) {
    e.preventDefault();
    if (!paciente)  { alert('❌ Primero busca y selecciona un paciente.'); return; }
    if (!medicoId)  { alert('❌ Selecciona un médico.'); return; }
    if (!fecha)     { alert('❌ Selecciona una fecha.'); return; }
    if (!horario)   { alert('❌ Selecciona un horario.'); return; }

    onCrear(
      {
        patientId: paciente.id,
        doctorId: parseInt(medicoId),
        appointmentDateTime: `${fecha}T${horario}`,
        reason: motivo || null,
      },
      {
        onSuccess: () => {
          resetForm();
          window.bootstrap?.Modal.getInstance(document.getElementById('modalNuevaCita'))?.hide();
        },
        onError: (err) => alert(`❌ ${err.message}`),
      }
    );
  }

  return (
    <div className="modal fade" id="modalNuevaCita" tabIndex="-1" aria-hidden="true">
      <div className="modal-dialog modal-dialog-centered modal-lg">
        <div className="modal-content border-0 rounded-4 shadow">
          <div className="modal-header border-bottom-0 pt-4 px-4">
            <h5 className="modal-title fw-bold"><i className="fa-solid fa-calendar-plus me-2 text-primary"></i> Registrar Nueva Cita</h5>
            <button type="button" className="btn-close" data-bs-dismiss="modal"></button>
          </div>
          <div className="modal-body px-4 pb-4">
            <form onSubmit={handleSubmit} noValidate>

              <div className="mb-3">
                <label className="form-label small fw-bold">DNI del Paciente <span className="text-danger">*</span></label>
                <div className="input-group">
                  <input
                    type="text"
                    className="form-control rounded-3"
                    placeholder="Ingresa el DNI del paciente"
                    maxLength={15}
                    value={dni}
                    onChange={(e) => setDni(e.target.value)}
                    onKeyDown={(e) => { if (e.key === 'Enter') { e.preventDefault(); handleBuscarDni(); } }}
                    required
                  />
                  <button type="button" className="btn btn-outline-primary rounded-3 ms-2" onClick={handleBuscarDni} disabled={buscando}>
                    <i className="fa-solid fa-search me-1"></i>{buscando ? 'Buscando...' : 'Buscar'}
                  </button>
                </div>
              </div>

              {paciente && (
                <div className="mb-3">
                  <div className="alert alert-light border rounded-3 p-3">
                    <div className="d-flex align-items-center gap-3">
                      <div className="bg-primary text-white rounded-circle d-flex align-items-center justify-content-center flex-shrink-0 fw-bold" style={{ width: 48, height: 48, fontSize: '1rem' }}>
                        {(paciente.firstName?.[0] || '') + (paciente.lastName?.[0] || '')}
                      </div>
                      <div>
                        <p className="fw-bold mb-0">{paciente.firstName} {paciente.lastName}</p>
                        <small className="text-muted">
                          Edad: {calcularEdad(paciente.dateOfBirth)} años · Tel: {paciente.phone || '—'} · {paciente.status || '—'}
                        </small>
                      </div>
                    </div>
                  </div>
                </div>
              )}

              {pacienteNoEncontrado && (
                <div className="mb-3">
                  <div className="alert alert-warning border-0 rounded-3 py-2 px-3">
                    <i className="fa-solid fa-triangle-exclamation me-2"></i>Paciente no encontrado. <a href="/pacientes" className="fw-bold">Registrarlo primero</a>.
                  </div>
                </div>
              )}

              <div className="mb-3">
                <label className="form-label small fw-bold">Especialidad <span className="text-danger">*</span></label>
                <select
                  className="form-select rounded-3"
                  value={especialidadId}
                  onChange={(e) => { setEspecialidadId(e.target.value); setMedicoId(''); setFecha(''); setHorario(''); }}
                  required
                >
                  <option value="" disabled>Seleccionar especialidad...</option>
                  {especialidades.map((esp) => <option key={esp.id} value={esp.id}>{esp.name}</option>)}
                </select>
              </div>

              <div className="mb-3">
                <label className="form-label small fw-bold">Médico <span className="text-danger">*</span></label>
                <select
                  className="form-select rounded-3"
                  value={medicoId}
                  onChange={(e) => { setMedicoId(e.target.value); setFecha(''); setHorario(''); }}
                  disabled={!especialidadId || cargandoMedicos}
                  required
                >
                  <option value="" disabled>
                    {!especialidadId ? 'Primero selecciona una especialidad...' : cargandoMedicos ? 'Cargando...' : 'Seleccionar médico...'}
                  </option>
                  {medicos.map((m) => <option key={m.id} value={m.id}>{m.firstName} {m.lastName}</option>)}
                </select>
              </div>

              <div className="mb-3">
                <label className="form-label small fw-bold">Fecha <span className="text-danger">*</span></label>
                <input
                  type="date"
                  className="form-control rounded-3"
                  min={minFecha}
                  value={fecha}
                  onChange={(e) => { setFecha(e.target.value); setHorario(''); }}
                  disabled={!medicoId}
                  required
                />
              </div>

              <div className="mb-3">
                <label className="form-label small fw-bold">Horario Disponible <span className="text-danger">*</span></label>
                <select
                  className="form-select rounded-3"
                  value={horario}
                  onChange={(e) => setHorario(e.target.value)}
                  disabled={!medicoId || !fecha || cargandoSlots}
                  required
                >
                  <option value="" disabled>
                    {!medicoId || !fecha ? 'Primero selecciona médico y fecha...' : cargandoSlots ? 'Cargando horarios...' : 'Seleccionar horario...'}
                  </option>
                  {slots.map((s) => (
                    <option key={s.startTime} value={s.startTime}>{s.startTime.substring(0, 5)} — {s.endTime.substring(0, 5)}</option>
                  ))}
                </select>
                <small className="text-muted">
                  {medicoId && fecha && !cargandoSlots && slots.length === 0 && 'No hay horarios disponibles para esta fecha.'}
                  {medicoId && fecha && !cargandoSlots && slots.length > 0 && `${slots.length} horarios disponibles.`}
                </small>
              </div>

              <div className="mb-3">
                <label className="form-label small fw-bold">Motivo de consulta</label>
                <input
                  type="text"
                  className="form-control rounded-3"
                  placeholder="Ej: Revisión rutinaria, dolor de cabeza..."
                  value={motivo}
                  onChange={(e) => setMotivo(e.target.value)}
                />
              </div>

              <div className="d-grid mt-4">
                <button type="submit" className="btn btn-primary rounded-pill py-2 text-white fw-bold" disabled={isCreating}>
                  <i className="fa-solid fa-calendar-check me-2"></i> {isCreating ? 'Guardando...' : 'Guardar Cita'}
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  );
}