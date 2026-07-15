import { Routes, Route } from 'react-router-dom';
import Layout from './components/layout/Layout';
import ProtectedRoute from './components/layout/ProtectedRoute';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import Citas from './pages/Citas';
import Pacientes from './pages/Pacientes';
import HorarioMedicos from './pages/HorarioMedicos';
import Pagos from './pages/Pagos';
import Notificaciones from './pages/Notificaciones';

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
        </Route>
      </Route>
    </Routes>
  );
}

export default App;
