import { useState, useMemo } from 'react';
import { useQuery } from '@tanstack/react-query';
import { useCitas, useCitasMutations } from '../hooks/useCitas';
import { getEspecialidades } from '../api/citasApi';
import FiltrosCitas from '../components/citas/FiltrosCitas';
import TablaCitas from '../components/citas/TablaCitas';
import ModalNuevaCita from '../components/citas/ModalNuevaCita';
import ModalReprogramar from '../components/citas/ModalReprogramar';
import ModalCancelar from '../components/citas/ModalCancelar';

export default function Citas() {
  const { data: citas = [], isLoading, isError } = useCitas();
  const { data: especialidades = [] } = useQuery({ queryKey: ['especialidades'], queryFn: getEspecialidades });
  const { crear, cancelar, reprogramar } = useCitasMutations();

  const [filtros, setFiltros] = useState({ dni: '', estado: '', especialidad: '' });
  const [citaAReprogramar, setCitaAReprogramar] = useState(null);
  const [citaACancelar, setCitaACancelar] = useState(null);

  const citasFiltradas = useMemo(() => {
    return citas.filter((c) => {
      const dni = c.patient?.dni || '';
      const especialidad = c.doctor?.specialty?.name || '';
      const okDni = !filtros.dni || dni.toLowerCase().includes(filtros.dni.toLowerCase());
      const okEstado = !filtros.estado || c.status === filtros.estado;
      const okEspecialidad = !filtros.especialidad || especialidad === filtros.especialidad;
      return okDni && okEstado && okEspecialidad;
    });
  }, [citas, filtros]);

  const stats = useMemo(() => ({
    confirmadas: citas.filter((c) => c.status === 'CONFIRMED').length,
    pendientes: citas.filter((c) => c.status === 'PENDING').length,
    canceladas: citas.filter((c) => c.status === 'CANCELLED').length,
  }), [citas]);

  return (
    <>
      <div className="panel-titulo d-flex align-items-center gap-2 mb-4 mt-2">
        <i className="fa-regular fa-calendar-check texto-primario-personalizado fa-lg"></i>
        <h4 className="fw-bold mb-0">Gestión de Citas</h4>
      </div>

      <div className="grid-estadisticas mb-4">
        <div className="card tarjeta-estadistica border-0 rounded-4 p-3 shadow-sm">
          <div className="card-body">
            <h6 className="text-muted mb-2"><i className="fa-regular fa-calendar-check me-2"></i> Citas Confirmadas</h6>
            <h2 className="fw-bold mb-0 texto-exito">{stats.confirmadas}</h2>
          </div>
        </div>
        <div className="card tarjeta-estadistica border-0 rounded-4 p-3 shadow-sm">
          <div className="card-body">
            <h6 className="text-muted mb-2"><i className="fa-solid fa-hourglass-end me-2"></i> Citas Pendientes</h6>
            <h2 className="fw-bold mb-0 text-warning">{stats.pendientes}</h2>
          </div>
        </div>
        <div className="card tarjeta-estadistica border-0 rounded-4 p-3 shadow-sm">
          <div className="card-body">
            <h6 className="text-muted mb-2"><i className="fa-solid fa-ban me-2"></i> Citas Canceladas</h6>
            <h2 className="fw-bold mb-0 text-danger">{stats.canceladas}</h2>
          </div>
        </div>
      </div>

      <div className="card border-0 rounded-4 shadow-sm p-4">
        <div className="d-flex justify-content-between align-items-center mb-3 flex-wrap gap-2">
          <h5 className="fw-bold mb-0">Listado General de Citas</h5>
          <button className="btn btn-primary rounded-pill px-4 text-white" data-bs-toggle="modal" data-bs-target="#modalNuevaCita">
            <i className="fa-solid fa-plus me-2"></i> Nueva Cita
          </button>
        </div>

        <FiltrosCitas
          filtros={filtros}
          setFiltros={setFiltros}
          especialidades={especialidades}
          onLimpiar={() => setFiltros({ dni: '', estado: '', especialidad: '' })}
        />

        <div className="table-responsive">
          <table className="table table-borderless align-middle tabla-personalizada m-0">
            <thead className="border-bottom">
              <tr>
                <th>Hora</th><th>Paciente</th><th>DNI</th><th>Especialidad</th><th>Médico</th><th>Estado</th><th className="text-end">Acciones</th>
              </tr>
            </thead>
            <tbody>
              <TablaCitas
                citas={citasFiltradas}
                isLoading={isLoading}
                isError={isError}
                onReprogramar={setCitaAReprogramar}
                onCancelar={setCitaACancelar}
              />
            </tbody>
          </table>
        </div>
      </div>

      <ModalNuevaCita onCrear={crear.mutate} isCreating={crear.isPending} />

      <ModalReprogramar
        cita={citaAReprogramar}
        onConfirmar={(id, newDateTime, opts) => reprogramar.mutate({ id, newDateTime }, opts)}
        onClosed={() => setCitaAReprogramar(null)}
        isSubmitting={reprogramar.isPending}
      />

      <ModalCancelar
        cita={citaACancelar}
        onConfirmar={(id, opts) => cancelar.mutate(id, opts)}
        onClosed={() => setCitaACancelar(null)}
        isSubmitting={cancelar.isPending}
      />
    </>
  );
}