import { useState } from "react";
import { Outlet } from "react-router-dom";
import AdminSidebar from "./AdminSidebar";
import Navbar from "./Navbar";

export default function AdminLayout() {
  const [sidebarAbierto, setSidebarAbierto] = useState(false);

  return (
    <div id="wrapper">
      <AdminSidebar
        abierto={sidebarAbierto}
        onClose={() => setSidebarAbierto(false)}
      />

      <main id="page-content-wrapper" className="fondo-claro-personalizado">
        <Navbar
          onToggleSidebar={() => setSidebarAbierto((prev) => !prev)}
          notifPath="/admin/notificaciones"
        />

        <div className="container-fluid px-4 pb-4">
          <Outlet />
        </div>
      </main>
    </div>
  );
}