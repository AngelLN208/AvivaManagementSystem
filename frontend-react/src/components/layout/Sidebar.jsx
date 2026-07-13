import { NavLink } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

const items = [
  { to: '/', icon: 'fa-solid fa-border-all', label: 'Panel Principal' },
  { to: '/citas', icon: 'fa-regular fa-calendar-check', label: 'Citas' },
  { to: '/pacientes', icon: 'fa-solid fa-bed-pulse', label: 'Pacientes' },
  { to: '/horario-medicos', icon: 'fa-solid fa-stethoscope', label: 'Horario Médicos' },
  { to: '/pagos', icon: 'fa-solid fa-money-bill-wave', label: 'Pagos' },
  { to: '/notificaciones', icon: 'fa-solid fa-bell', label: 'Notificaciones' },
];

export default function Sidebar({ onClose, abierto }) {
  const { logout } = useAuth();

  return (
    <aside className={`sidebar shadow-sm ${abierto ? 'menu-abierto' : ''}`} id="sidebar-wrapper">
      <div className="sidebar-heading d-flex align-items-center justify-content-between px-3">
        <span><i className="fa-solid fa-notes-medical texto-primario-personalizado me-2"></i> Aviva</span>
        <button className="btn btn-sm d-lg-none text-muted p-0" style={{ fontSize: '1.1rem' }} onClick={onClose}>
          <i className="fa-solid fa-xmark"></i>
        </button>
      </div>

      <div className="list-group list-group-flush px-3 pt-3 gap-2">
        {items.map(item => (
          <NavLink
            key={item.to}
            to={item.to}
            end={item.to === '/'}
            className={({ isActive }) =>
              `list-group-item list-group-item-action rounded-pill ${isActive ? 'nav-activo' : 'nav-link-custom'}`
            }
          >
            <i className={`${item.icon} me-3`}></i> {item.label}
          </NavLink>
        ))}
      </div>

      <div className="px-3 pt-2 pb-3 mt-auto">
        <a href="#" className="list-group-item list-group-item-action rounded-pill text-danger fw-semibold" onClick={logout}>
          <i className="fa-solid fa-right-from-bracket me-3"></i> Cerrar Sesión
        </a>
      </div>
    </aside>
  );
}