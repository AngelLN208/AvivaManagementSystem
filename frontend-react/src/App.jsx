import { Routes, Route } from 'react-router-dom';
import Layout from './components/layout/Layout';
import Dashboard from './pages/Dashboard';
import Citas from './pages/Citas';
import Pacientes from './pages/Pacientes';
import HorarioMedicos from './pages/HorarioMedicos';
import Pagos from './pages/Pagos';
import Notificaciones from './pages/Notificaciones';

function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route path="/" element={<Dashboard />} />
        <Route path="/citas" element={<Citas />} />
        <Route path="/pacientes" element={<Pacientes />} />
        <Route path="/horario-medicos" element={<HorarioMedicos />} />
        <Route path="/pagos" element={<Pagos />} />
        <Route path="/notificaciones" element={<Notificaciones />} />
      </Route>
    </Routes>
  );
}

export default App;