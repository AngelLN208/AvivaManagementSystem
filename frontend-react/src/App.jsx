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
import DoctorLayout from './components/layout/DoctorLayout';
import CitasDoctor from './pages/doctor/CitasDoctor';
import AtencionClinica from './pages/doctor/AtencionClinica';
import HorariosDoctor from './pages/doctor/HorariosDoctor';

function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />

      <Route element={<ProtectedRoute allowedRoles={['RECEPTIONIST']} />}>
        <Route element={<Layout />}>
          <Route path="/" element={<Dashboard />} />
          <Route path="/citas" element={<Citas />} />
          <Route path="/pacientes" element={<Pacientes />} />
          <Route path="/horario-medicos" element={<HorarioMedicos />} />
          <Route path="/pagos" element={<Pagos />} />
          <Route path="/notificaciones" element={<Notificaciones />} />
        </Route>
      </Route>

      <Route element={<ProtectedRoute allowedRoles={['ADMIN']} />}>
        <Route element={<AdminLayout />}>
          <Route path="/admin/dashboard" element={<AdminDashboard />} />
          <Route path="/admin/doctores" element={<DoctoresAdmin />} />
          <Route path="/admin/especialidades" element={<EspecialidadesAdmin />} />
          <Route path="/admin/pacientes" element={<PacientesAdmin />} />
          <Route path="/admin/horarios" element={<HorariosAdmin />} />
          <Route path="/admin/pagos" element={<PagosAdmin />} />
          <Route path="/admin/notificaciones" element={<NotificacionesAdmin />} />
          <Route path="/admin/seguros" element={<Seguros />} />
        </Route>
      </Route>

      <Route element={<ProtectedRoute allowedRoles={['DOCTOR']} />}>
        <Route element={<DoctorLayout />}>
          <Route path="/doctor/citas" element={<CitasDoctor />} />
          {/* Ruta dinámica para atender una cita específica */}
          <Route path="/doctor/atender/:appointmentId" element={<AtencionClinica />} />
          <Route path="/doctor/horarios" element={<HorariosDoctor />} />  
        </Route>
      </Route>
      
    </Routes>
    
    
  );
}

export default App;
