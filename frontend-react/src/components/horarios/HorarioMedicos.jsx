import { useState, useMemo } from 'react';
import { useHorarios } from '../hooks/useHorarios';
import FiltrosHorarios from '../components/horarios/FiltrosHorarios';
import TablaHorarios from '../components/horarios/TablaHorarios';
import ModalDisponibilidad from '../components/horarios/ModalDisponibilidad';

export default function HorarioMedicos() {
  const { doctores, horarios, isLoading, isError } = useHorarios();
  const [filtros, setFiltros] = useState({ nombre: '', especialidad: '' });
  const [doctorAVer, setDoctorAVer] = useState(null);

  const especialidades = useMemo(
    () => [...new Set(doctores.map((d) => d.specialty?.name).filter(Boolean))],
    [doctores]
  );

  const horariosFiltrados = useMemo(() => {
    return horarios.filter(({ doctor: d }) => {
      const nombre = `${d.firstName} ${d.lastName}`.toLowerCase();
      const especialidad = d.specialty?.name || '';
      const okNombre = !filtros.nombre || nombre.includes(filtros.nombre.toLowerCase());
      const okEspecialidad = !filtros.especialidad || especialidad === filtros.especialidad;
      return okNombre && okEspecialidad;
    });
  }, [horarios, filtros]);

  return (
    <>
      <div className="panel-titulo d-flex align-items-center gap-2 mb-4 mt-2">
        <i className="fa-solid fa-stethoscope texto-primario-personalizado fa-lg"></i>
        <h4 className="fw-bold mb-0">Horario de Médicos</h4>
      </div>

      <div className="card border-0 rounded-4 shadow-sm p-4">
        <div className="d-flex justify-content-between align-items-center mb-3 flex-wrap gap-2">
          <h5 className="fw-bold mb-0">Agenda Médica</h5>
        </div>

        <FiltrosHorarios
          filtros={filtros}
          setFiltros={setFiltros}
          especialidades={especialidades}
          onLimpiar={() => setFiltros({ nombre: '', especialidad: '' })}
        />

        <div className="table-responsive">
          <table className="table table-borderless align-middle tabla-personalizada m-0">
            <thead className="border-bottom">
              <tr>
                <th>Médico</th><th>Especialidad</th><th>Días</th><th>Horario</th>
                <th>Duración Cita</th><th>Máx. Citas/Día</th><th>Estado</th><th className="text-end">Acciones</th>
              </tr>
            </thead>
            <tbody>
              <TablaHorarios
                horarios={horariosFiltrados}
                isLoading={isLoading}
                isError={isError}
                onVerDisponibilidad={setDoctorAVer}
              />
            </tbody>
          </table>
        </div>
      </div>

      <ModalDisponibilidad doctor={doctorAVer} onClosed={() => setDoctorAVer(null)} />
    </>
  );
}