import { NavLink } from "react-router-dom";
import { useAuth } from "../../hooks/useAuth";

const items = [
  { to: "/admin/dashboard", icon: "fa-solid fa-border-all", label: "Dashboard" },
  { to: "/admin/pacientes", icon: "fa-solid fa-bed-pulse", label: "Pacientes" },
  { to: "/admin/doctores", icon: "fa-solid fa-user-doctor", label: "Doctores" },
  { to: "/admin/especialidades", icon: "fa-solid fa-hospital", label: "Especialidades" },
  { to: "/admin/horarios", icon: "fa-solid fa-calendar", label: "Horarios" },
  { to: "/admin/pagos", icon: "fa-solid fa-money-bill", label: "Pagos" },
  { to: "/admin/notificaciones", icon: "fa-solid fa-bell", label: "Notificaciones" },
];

export default function AdminSidebar({ onClose, abierto }) {
  const { logout } = useAuth();

  return (
    <aside
      id="sidebar-wrapper"
      className={`shadow-sm ${abierto ? "menu-abierto" : ""}`}
    >
      <div className="sidebar-heading d-flex align-items-center justify-content-between px-3">
        <span>
          <i className="fa-solid fa-notes-medical texto-primario-personalizado me-2"></i>
          AVIVA
        </span>

        <button
          className="btn btn-sm d-lg-none text-muted p-0"
          style={{ fontSize: "1.1rem" }}
          onClick={onClose}
        >
          <i className="fa-solid fa-xmark"></i>
        </button>
      </div>

      <div className="list-group list-group-flush px-3 pt-3 gap-2">
        {items.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            className={({ isActive }) =>
              `list-group-item list-group-item-action rounded-pill ${
                isActive ? "nav-activo" : "nav-link-custom"
              }`
            }
          >
            <i className={`${item.icon} me-3`}></i>
            {item.label}
          </NavLink>
        ))}
      </div>

      <div className="px-3 pt-2 pb-3 mt-auto">
        <a
          href="#"
          className="list-group-item list-group-item-action rounded-pill text-danger fw-semibold"
          onClick={(e) => {
            e.preventDefault();
            logout();
          }}
        >
          <i className="fa-solid fa-right-from-bracket me-3"></i>
          Cerrar sesión
        </a>
      </div>
    </aside>
  );
}