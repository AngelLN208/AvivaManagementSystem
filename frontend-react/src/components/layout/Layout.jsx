import { useState } from 'react';
import { Outlet } from 'react-router-dom';
import Sidebar from './Sidebar';
import Navbar from './Navbar';

export default function Layout() {
  const [sidebarAbierto, setSidebarAbierto] = useState(false);

  return (
    <div id="wrapper">
      <Sidebar abierto={sidebarAbierto} onClose={() => setSidebarAbierto(false)} />

      <main id="page-content-wrapper" className="fondo-claro-personalizado">
        <Navbar onToggleSidebar={() => setSidebarAbierto(prev => !prev)} />

        <div className="container-fluid px-4 pb-4">
          <Outlet />
        </div>
      </main>
    </div>
  );
}