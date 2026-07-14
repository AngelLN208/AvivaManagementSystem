import { useState, useEffect } from 'react';
import { getPacientes } from '../../api/pacientesApi';
import { getDoctores } from '../../api/doctoresApi';
import { getEspecialidades } from '../../api/especialidadesApi';
import { getPagos } from '../../api/pagosApi';

export default function AdminDashboard() {
  const [stats, setStats] = useState({
    pacientes: 0,
    doctores: 0,
    especialidades: 0,
    pagos: 0,
  });
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);

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
    <div>
      <div className="mb-4">
        <h3 className="fw-semibold">Bienvenido, Administrador 👋</h3>
        <small className="text-muted">Panel de Administración</small>
      </div>

      {isLoading ? (
        <div className="text-center py-5">
          <div className="spinner-border" role="status">
            <span className="visually-hidden">Cargando...</span>
          </div>
        </div>
      ) : (
        <div className="row g-4">
          <div className="col-md-6 col-lg-3">
            <div className="dashboard-card">
              <div className="d-flex justify-content-between align-items-center">
                <div>
                  <p className="text-muted mb-2">Pacientes</p>
                  <h3 className="fw-bold mb-0">{stats.pacientes}</h3>
                  <small className="text-success">Total en el sistema</small>
                </div>
                <div style={{ fontSize: '2.5rem', color: '#0d4d7d' }}>
                  <i className="fa-solid fa-users"></i>
                </div>
              </div>
            </div>
          </div>

          <div className="col-md-6 col-lg-3">
            <div className="dashboard-card">
              <div className="d-flex justify-content-between align-items-center">
                <div>
                  <p className="text-muted mb-2">Doctores</p>
                  <h3 className="fw-bold mb-0">{stats.doctores}</h3>
                  <small className="text-success">Total activos</small>
                </div>
                <div style={{ fontSize: '2.5rem', color: '#7ed6c2' }}>
                  <i className="fa-solid fa-user-doctor"></i>
                </div>
              </div>
            </div>
          </div>

          <div className="col-md-6 col-lg-3">
            <div className="dashboard-card">
              <div className="d-flex justify-content-between align-items-center">
                <div>
                  <p className="text-muted mb-2">Especialidades</p>
                  <h3 className="fw-bold mb-0">{stats.especialidades}</h3>
                  <small className="text-success">Disponibles</small>
                </div>
                <div style={{ fontSize: '2.5rem', color: '#2f8cc7' }}>
                  <i className="fa-solid fa-hospital"></i>
                </div>
              </div>
            </div>
          </div>

          <div className="col-md-6 col-lg-3">
            <div className="dashboard-card">
              <div className="d-flex justify-content-between align-items-center">
                <div>
                  <p className="text-muted mb-2">Pagos</p>
                  <h3 className="fw-bold mb-0">{stats.pagos}</h3>
                  <small className="text-success">Registrados</small>
                </div>
                <div style={{ fontSize: '2.5rem', color: '#ffc107' }}>
                  <i className="fa-solid fa-money-bill"></i>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
