import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { getPacientes } from '../../api/pacientesApi';
import { getDoctores } from '../../api/doctoresApi';
import { getEspecialidades } from '../../api/especialidadesApi';
import { getPagos } from '../../api/pagosApi';
import StatCard from '../../components/ui/StatCard';
import CitasSemana from '../../components/ui/CitasSemana';

export default function AdminDashboard() {
  const [stats, setStats] = useState({
    pacientes: 0,
    doctores: 0,
    especialidades: 0,
    pagos: 0,
  });
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);

  const username = localStorage.getItem('username') || 'Administrador';

  useEffect(() => {
    async function loadStats() {
      try {
        setIsLoading(true);
        const [pacientes, doctores, especialidades, pagos] = await Promise.all([
          getPacientes(),
          getDoctores(),
          getEspecialidades(),
          getPagos(),
        ]);

        setStats({
          pacientes: Array.isArray(pacientes) ? pacientes.length : 0,
          doctores: Array.isArray(doctores) ? doctores.length : 0,
          especialidades: Array.isArray(especialidades) ? especialidades.length : 0,
          pagos: Array.isArray(pagos) ? pagos.length : 0,
        });
      } catch (err) {
        setError(err.message || 'Error al cargar estadísticas');
      } finally {
        setIsLoading(false);
      }
    }

    loadStats();
  }, []);

  if (error) {
    return <div className="alert alert-danger m-4">{error}</div>;
  }

  return (
    <div className="panel-page">
      <div className="panel-titulo d-flex align-items-center gap-2 mb-1 mt-2">
        <i className="fa-solid fa-border-all texto-primario-personalizado fa-lg"></i>
        <h4 className="fw-bold mb-0">Bienvenido, {username} 👋</h4>
      </div>
      <p className="text-muted mb-4">Panel de administración</p>

      {/* TARJETAS ESTADÍSTICAS */}
      <div className="grid-estadisticas">
        <StatCard
          icon="fa-solid fa-users"
          title="Pacientes"
          value={stats.pacientes}
          subtitle="Total en el sistema"
          accent="primario"
          isLoading={isLoading}
        />
        <StatCard
          icon="fa-solid fa-user-doctor"
          title="Doctores"
          value={stats.doctores}
          subtitle="Total activos"
          accent="info"
          isLoading={isLoading}
        />
        <StatCard
          icon="fa-solid fa-hospital"
          title="Especialidades"
          value={stats.especialidades}
          subtitle="Disponibles"
          accent="advertencia"
          isLoading={isLoading}
        />
        <StatCard
          icon="fa-solid fa-money-bill"
          title="Pagos"
          value={stats.pagos}
          subtitle="Registrados"
          accent="exito"
          isLoading={isLoading}
        />
      </div>

      <div className="row g-4 mt-1">
        {/* ACCESOS RÁPIDOS */}
        <div className="col-lg-7">
          <h6 className="text-muted fw-semibold mb-3">Accesos rápidos</h6>
          <div className="grid-acceso-rapido">
            <Link to="/admin/pacientes" className="acceso-rapido-card">
              <div className="icono-acceso"><i className="fa-solid fa-bed-pulse"></i></div>
              <span className="fw-semibold" style={{ fontSize: '0.85rem' }}>Pacientes</span>
            </Link>
            <Link to="/admin/doctores" className="acceso-rapido-card">
              <div className="icono-acceso"><i className="fa-solid fa-user-doctor"></i></div>
              <span className="fw-semibold" style={{ fontSize: '0.85rem' }}>Doctores</span>
            </Link>
            <Link to="/admin/especialidades" className="acceso-rapido-card">
              <div className="icono-acceso"><i className="fa-solid fa-hospital"></i></div>
              <span className="fw-semibold" style={{ fontSize: '0.85rem' }}>Especialidades</span>
            </Link>
            <Link to="/admin/horarios" className="acceso-rapido-card">
              <div className="icono-acceso"><i className="fa-solid fa-calendar"></i></div>
              <span className="fw-semibold" style={{ fontSize: '0.85rem' }}>Horarios</span>
            </Link>
            <Link to="/admin/pagos" className="acceso-rapido-card">
              <div className="icono-acceso"><i className="fa-solid fa-money-bill"></i></div>
              <span className="fw-semibold" style={{ fontSize: '0.85rem' }}>Pagos</span>
            </Link>
          </div>
        </div>

        {/* GRÁFICA */}
        <div className="col-lg-5">
          <h6 className="text-muted fw-semibold mb-3">Actividad</h6>
          <CitasSemana />
        </div>
      </div>
    </div>
  );
}