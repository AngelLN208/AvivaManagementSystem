import { useDashboard } from '../hooks/useDashboard';
import StatCard from '../components/ui/StatCard';
import TablaCitasHoy from '../components/ui/TablaCitasHoy';
import { Link } from 'react-router-dom';

export default function Dashboard() {
  const {
    isLoading,
    isError,
    totalPacientes,
    totalRealizadasHoy,
    totalPendientesHoy,
    proximasCitas,
  } = useDashboard();

  if (isError) {
    return <p className="text-danger">Error al cargar el panel principal.</p>;
  }

  return (
    <>
      <div className="panel-titulo d-flex align-items-center gap-2 mb-4 mt-2">
        <i className="fa-solid fa-border-all texto-primario-personalizado fa-lg"></i>
        <h4 className="fw-bold mb-0">Panel Principal</h4>
      </div>

      {/* TARJETAS ESTADÍSTICAS */}
      <div className="grid-estadisticas">
        <StatCard
          icon="fa-solid fa-users"
          title="Pacientes Registrados"
          value={totalPacientes}
          subtitle="Total en el sistema"
          isLoading={isLoading}
        />
        <StatCard
          icon="fa-solid fa-calendar-check"
          title="Citas Realizadas Hoy"
          value={totalRealizadasHoy}
          subtitle="Consultas completadas hoy"
          isLoading={isLoading}
        />
        <StatCard
          icon="fa-solid fa-clock"
          title="Citas Pendientes Hoy"
          value={totalPendientesHoy}
          subtitle="Por atender hoy"
          badge="Pendientes"
          isLoading={isLoading}
        />
      </div>

      {/* ACCESOS RÁPIDOS */}
      <div className="row g-3 mb-4">
        <div className="col-6 col-md-3">
          <Link to="/citas" className="card border-0 rounded-4 shadow-sm p-3 text-decoration-none acceso-rapido d-flex flex-column align-items-center justify-content-center text-center">
            <div className="acceso-icon mb-2"><i className="fa-regular fa-calendar-check fa-2x texto-primario-personalizado"></i></div>
            <span className="fw-semibold" style={{ fontSize: '0.9rem', color: 'var(--texto-oscuro)' }}>Gestión de Citas</span>
          </Link>
        </div>
        <div className="col-6 col-md-3">
          <Link to="/pacientes" className="card border-0 rounded-4 shadow-sm p-3 text-decoration-none acceso-rapido d-flex flex-column align-items-center justify-content-center text-center">
            <div className="acceso-icon mb-2"><i className="fa-solid fa-bed-pulse fa-2x texto-primario-personalizado"></i></div>
            <span className="fw-semibold" style={{ fontSize: '0.9rem', color: 'var(--texto-oscuro)' }}>Pacientes</span>
          </Link>
        </div>
        <div className="col-6 col-md-3">
          <Link to="/horario-medicos" className="card border-0 rounded-4 shadow-sm p-3 text-decoration-none acceso-rapido d-flex flex-column align-items-center justify-content-center text-center">
            <div className="acceso-icon mb-2"><i className="fa-solid fa-stethoscope fa-2x texto-primario-personalizado"></i></div>
            <span className="fw-semibold" style={{ fontSize: '0.9rem', color: 'var(--texto-oscuro)' }}>Horario Médicos</span>
          </Link>
        </div>
        <div className="col-6 col-md-3">
          <Link to="/pagos" className="card border-0 rounded-4 shadow-sm p-3 text-decoration-none acceso-rapido d-flex flex-column align-items-center justify-content-center text-center">
            <div className="acceso-icon mb-2"><i className="fa-solid fa-money-bill-wave fa-2x texto-primario-personalizado"></i></div>
            <span className="fw-semibold" style={{ fontSize: '0.9rem', color: 'var(--texto-oscuro)' }}>Pagos</span>
          </Link>
        </div>
      </div>

      {/* TABLA PRÓXIMAS CITAS */}
      <div className="card border-0 rounded-4 shadow-sm p-4">
        <div className="d-flex justify-content-between align-items-center mb-4">
          <h5 className="fw-bold mb-0">Próximas Citas (Hoy)</h5>
          <Link to="/citas" className="text-muted text-decoration-none small">Ver todas →</Link>        </div>
        <div className="table-responsive">
          <table className="table table-borderless align-middle tabla-personalizada m-0">
            <thead className="border-bottom">
              <tr><th>Hora</th><th>Paciente</th><th>Especialidad</th><th>Médico</th><th>Estado</th></tr>
            </thead>
            <tbody>
              <TablaCitasHoy citas={proximasCitas} isLoading={isLoading} />
            </tbody>
          </table>
        </div>
      </div>
    </>
  );
}