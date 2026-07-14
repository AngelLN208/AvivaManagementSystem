import { NavLink } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";

const items = [
  {
    to: "/admin/dashboard",
    icon: "fa-solid fa-border-all",
    label: "Dashboard",
  },
  {
    to: "/admin/pacientes",
    icon: "fa-solid fa-bed-pulse",
    label: "Pacientes",
  },
  {
    to: "/admin/doctores",
    icon: "fa-solid fa-user-doctor",
    label: "Doctores",
  },
  {
    to: "/admin/especialidades",
    icon: "fa-solid fa-hospital",
    label: "Especialidades",
  },
  {
    to: "/admin/horarios",
    icon: "fa-solid fa-calendar",
    label: "Horarios",
  },
  {
    to: "/admin/pagos",
    icon: "fa-solid fa-money-bill",
    label: "Pagos",
  },
];

export default function AdminSidebar() {
  const { logout } = useAuth();

  return (
    <aside className="sidebar">
      <div className="sidebar-heading">
        <i className="fa-solid fa-notes-medical me-2"></i>
        AVIVA
      </div>

      <nav className="list-group list-group-flush">
        {items.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            className={({ isActive }) =>
              `nav-link ${isActive ? "nav-activo" : ""}`
            }
          >
            <i className={item.icon}></i>
            {item.label}
          </NavLink>
        ))}
      </nav>

      <div className="mt-4">
        <button className="btn btn-link w-100 text-start" onClick={logout}>
          <i className="fa-solid fa-right-from-bracket me-2"></i>
          Cerrar sesión
        </button>
      </div>
    </aside>
  );
}
