import { Outlet, Link, useLocation, useNavigate } from 'react-router-dom';
import { useContext } from 'react';
import AuthContext from '../../context/authContext';

export default function DoctorLayout() {
  const { usuario, logout } = useContext(AuthContext);
  const location = useLocation();
  const navigate = useNavigate();

  // Color celeste principal de la clínica
  const primaryColor = "#00a4e4"; 

  return (
    <div className="d-flex bg-light" style={{ minHeight: '100vh', fontFamily: 'Arial, sans-serif' }}>
      
      {/* Sidebar Bootstrap */}
      <div className="bg-white border-end d-flex flex-column" style={{ width: '260px' }}>
        <div className="text-center py-4 border-bottom">
          <h3 className="fw-bold mb-0" style={{ color: primaryColor }}>
            <i className="bi bi-heart-pulse-fill me-2"></i>Aviva<span className="text-dark">Clinic</span>
          </h3>
          <small className="text-muted">Portal Médico</small>
        </div>

        <div className="list-group list-group-flush mt-3 px-2 flex-grow-1">
          <Link 
            to="/doctor/citas" 
            className={`list-group-item list-group-item-action border-0 rounded mb-1 ${location.pathname.includes('/doctor/citas') && !location.pathname.includes('/atender') ? 'active' : ''}`}
            style={location.pathname.includes('/doctor/citas') && !location.pathname.includes('/atender') ? { backgroundColor: primaryColor, color: 'white' } : {}}
          >
            <i className="bi bi-grid-fill me-2"></i> Visión General
          </Link>
          <Link 
            to="/doctor/horarios" 
            className={`list-group-item list-group-item-action border-0 rounded mb-1 ${location.pathname.includes('/doctor/horarios') ? 'active' : ''}`}
            style={location.pathname.includes('/doctor/horarios') ? { backgroundColor: primaryColor, color: 'white' } : {}}
          >
            <i className="bi bi-calendar-week-fill me-2"></i> Mi Calendario
          </Link>
        </div>

        <div className="p-3 border-top">
          <button onClick={logout} className="btn btn-outline-danger w-100">
            <i className="bi bi-box-arrow-left me-2"></i> Cerrar Sesión
          </button>
        </div>
      </div>

      {/* Contenido Principal */}
      <div className="flex-grow-1 d-flex flex-column overflow-hidden">
        
        {/* Navbar Superior */}
        <nav className="navbar navbar-expand-lg navbar-light bg-white border-bottom px-4 py-3">
          <div className="container-fluid">
            <div className="input-group w-50">
              <span className="input-group-text bg-light border-end-0"><i className="bi bi-search"></i></span>
              <input type="text" className="form-control border-start-0 bg-light" placeholder="Buscar pacientes o citas..." />
            </div>
            <div className="d-flex align-items-center ms-auto">
              <div className="text-end me-3">
                <p className="mb-0 fw-bold text-dark">Dr. {usuario?.username || 'Emile Chen'}</p>
                <small className="text-muted">Medicina General</small>
              </div>
              <div className="rounded-circle bg-secondary text-white d-flex justify-content-center align-items-center" style={{ width: '45px', height: '45px' }}>
                <i className="bi bi-person-fill fs-4"></i>
              </div>
            </div>
          </div>
        </nav>

        {/* Área donde cargan las páginas */}
        <main className="p-4 overflow-auto flex-grow-1" style={{ backgroundColor: '#f4f7f6' }}>
          <Outlet />
        </main>

      </div>
    </div>
  );
}