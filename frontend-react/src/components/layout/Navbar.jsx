import { useTheme } from '../../context/ThemeContext';
import { useAuth } from '../../context/AuthContext';
import { getIniciales } from '../../utils/helpers';

function saludo() {
  const hora = new Date().getHours();
  if (hora < 12) return 'Buenos días';
  if (hora < 19) return 'Buenas tardes';
  return 'Buenas noches';
}

export default function Navbar({ onToggleSidebar }) {
  const { tema, toggleTema } = useTheme();
  const { logout } = useAuth();

  const username = localStorage.getItem('username') || 'Usuario';

  return (
    <nav className="navbar navbar-light py-3 px-4 navbar-superior">
      <div className="d-flex align-items-center w-100 gap-3">
        <button className="btn btn-white rounded-2 d-lg-none boton-hamburgesa shadow-sm" onClick={onToggleSidebar}>
          <i className="fa-solid fa-bars"></i>
        </button>

        <div className="flex-grow-1">
          <span className="text-muted fw-semibold d-none d-md-inline">{saludo()}, {username} I Aveces sales vivo, aveces no xd</span>
        </div>

        <div className="d-flex align-items-center gap-2 ms-auto">
          <button className="btn btn-white rounded-circle shadow-sm boton-icono" onClick={toggleTema}>
            <i className={tema === 'oscuro' ? 'fa-solid fa-sun text-warning' : 'fa-solid fa-moon'}></i>
          </button>

          <div className="dropdown ms-2">
            <a href="#" className="d-flex align-items-center text-decoration-none dropdown-toggle usuario-dropdown" data-bs-toggle="dropdown">
              <div className="bg-primary text-white rounded-circle d-flex align-items-center justify-content-center me-2 shadow-sm" style={{ width: 40, height: 40, fontWeight: 'bold' }}>
                {getIniciales(username)}
              </div>
              <strong className="fw-semibold d-none d-lg-block">{username}</strong>
            </a>
            <ul className="dropdown-menu dropdown-menu-end shadow border-0 mt-2">
              <li><a className="dropdown-item" href="#"><i className="fa-solid fa-user me-2 text-muted"></i>Mi Perfil</a></li>
              <li><hr className="dropdown-divider" /></li>
              <li><a className="dropdown-item text-danger" href="#" onClick={logout}><i className="fa-solid fa-right-from-bracket me-2"></i>Cerrar Sesión</a></li>
            </ul>
          </div>
        </div>
      </div>
    </nav>
  );
}