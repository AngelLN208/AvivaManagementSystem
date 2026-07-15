import { Routes, Route } from 'react-router-dom';
import Layout from './components/layout/Layout';
import AdminLayout from './components/layout/AdminLayout';
import ProtectedRoute from './components/layout/ProtectedRoute';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import Citas from './pages/Citas';
import Pacientes from './pages/Pacientes';
import HorarioMedicos from './pages/HorarioMedicos';
import Pagos from './pages/Pagos';
import Notificaciones from './pages/Notificaciones';
import Seguros from './pages/Seguros';
import AdminDashboard from './pages/admin/AdminDashboard';
import DoctoresAdmin from './pages/admin/DoctoresAdmin';
import EspecialidadesAdmin from './pages/admin/EspecialidadesAdmin';
import PacientesAdmin from './pages/admin/PacientesAdmin';
import HorariosAdmin from './pages/admin/HorariosAdmin';
import PagosAdmin from './pages/admin/PagosAdmin';
import NotificacionesAdmin from './pages/admin/NotificacionesAdmin';

function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />

      <Route element={<ProtectedRoute />}>
        <Route element={<Layout />}>
          <Route path="/" element={<Dashboard />} />
          <Route path="/citas" element={<Citas />} />
          <Route path="/pacientes" element={<Pacientes />} />
          <Route path="/horario-medicos" element={<HorarioMedicos />} />
          <Route path="/pagos" element={<Pagos />} />
          <Route path="/notificaciones" element={<Notificaciones />} />
          <Route path="/seguros" element={<Seguros />} />
        </Route>
      </Route>

      <Route element={<ProtectedRoute />}>
        <Route element={<AdminLayout />}>
          <Route path="/admin/dashboard" element={<AdminDashboard />} />
          <Route path="/admin/doctores" element={<DoctoresAdmin />} />
          <Route path="/admin/especialidades" element={<EspecialidadesAdmin />} />
          <Route path="/admin/pacientes" element={<PacientesAdmin />} />
          <Route path="/admin/horarios" element={<HorariosAdmin />} />
          <Route path="/admin/pagos" element={<PagosAdmin />} />
          <Route path="/admin/notificaciones" element={<NotificacionesAdmin />} />
        </Route>
      </Route>
    </Routes>
  );
}

export default App;