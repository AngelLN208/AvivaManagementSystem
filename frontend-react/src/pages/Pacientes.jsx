import { useState, useMemo } from 'react';
import { usePacientes, usePacientesMutations } from '../hooks/usePacientes';
import { useCitas } from '../hooks/useCitas';
import { esHoy } from '../utils/formatters';
import TablaPacientes from '../components/pacientes/TablaPacientes';
import ModalNuevoPaciente from '../components/pacientes/ModalNuevoPaciente';
import ModalVerPaciente from '../components/pacientes/ModalVerPaciente';

export default function Pacientes() {
  const { data: pacientes = [], isLoading, isError } = usePacientes();
  const { data: citas = [] } = useCitas();
  const { crear } = usePacientesMutations();

  const [pacienteAVer, setPacienteAVer] = useState(null);

  const stats = useMemo(() => {
    const idsConCitaHoy = new Set(
      citas
        .filter((c) => esHoy(c.appointmentDateTime) && c.status !== 'CANCELLED')
        .map((c) => c.patient?.id)
        .filter(Boolean)
    );
    return { total: pacientes.length, citaHoy: idsConCitaHoy.size };
  }, [pacientes, citas]);

  return (
    <>
      <div className="panel-titulo d-flex align-items-center gap-2 mb-4 mt-2">
        <i className="fa-solid fa-bed-pulse texto-primario-personalizado fa-lg"></i>
        <h4 className="fw-bold mb-0">Directorio de Pacientes</h4>
      </div>

      <div className="grid-estadisticas mb-4">
        <div className="card tarjeta-estadistica border-0 rounded-4 p-3 shadow-sm">
          <div className="card-body">
            <h6 className="text-muted mb-2"><i className="fa-solid fa-users me-2"></i> Total Pacientes</h6>
            <h2 className="fw-bold mb-0">{stats.total}</h2>
          </div>
        </div>
        <div className="card tarjeta-estadistica border-0 rounded-4 p-3 shadow-sm">
          <div className="card-body">
            <h6 className="text-muted mb-2"><i className="fa-solid fa-calendar-day me-2"></i> Pacientes con Cita Hoy</h6>
            <h2 className="fw-bold mb-0 texto-info">{stats.citaHoy}</h2>
          </div>
        </div>
      </div>

      <div className="card border-0 rounded-4 shadow-sm p-4">
        <div className="d-flex justify-content-between align-items-center mb-4 flex-wrap gap-2">
          <h5 className="fw-bold mb-0">Registro de Pacientes</h5>
          <button className="btn btn-primary rounded-pill px-4 text-white" data-bs-toggle="modal" data-bs-target="#modalPaciente">
            <i className="fa-solid fa-user-plus me-2"></i> Nuevo Paciente
          </button>
        </div>
        <div className="table-responsive">
          <table className="table table-borderless align-middle tabla-personalizada m-0">
            <thead className="border-bottom">
              <tr>
                <th>Nombre</th><th>DNI</th><th>Edad</th><th>Teléfono</th><th>Correo</th><th>Estado</th><th className="text-end">Acciones</th>
              </tr>
            </thead>
            <tbody>
              <TablaPacientes
                pacientes={pacientes}
                isLoading={isLoading}
                isError={isError}
                onVer={setPacienteAVer}
              />
            </tbody>
          </table>
        </div>
      </div>

      <ModalNuevoPaciente onCrear={crear.mutate} isCreating={crear.isPending} />

      <ModalVerPaciente paciente={pacienteAVer} onClosed={() => setPacienteAVer(null)} />
    </>
  );
}