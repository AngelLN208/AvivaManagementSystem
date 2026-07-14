import { Navigate, Route, Routes } from 'react-router-dom';
import GuestRoute from './components/layout/GuestRoute.jsx';
import PortalLayout from './components/layout/PortalLayout.jsx';
import ProtectedRoute from './components/layout/ProtectedRoute.jsx';
import AppointmentsPage from './pages/AppointmentsPage.jsx';
import DashboardPage from './pages/DashboardPage.jsx';
import DoctorsPage from './pages/DoctorsPage.jsx';
import LoginPage from './pages/LoginPage.jsx';
import NotFoundPage from './pages/NotFoundPage.jsx';
import RegisterPage from './pages/RegisterPage.jsx';
import SchedulePage from './pages/SchedulePage.jsx';

export default function App() {
  return (
    <Routes>
      <Route element={<GuestRoute />}>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/registro" element={<RegisterPage />} />
      </Route>

      <Route element={<ProtectedRoute />}>
        <Route element={<PortalLayout />}>
          <Route index element={<DashboardPage />} />
          <Route path="/citas" element={<AppointmentsPage />} />
          <Route path="/agendar" element={<SchedulePage />} />
          <Route path="/medicos" element={<DoctorsPage />} />
        </Route>
      </Route>

      <Route path="/inicio" element={<Navigate to="/" replace />} />
      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  );
}
