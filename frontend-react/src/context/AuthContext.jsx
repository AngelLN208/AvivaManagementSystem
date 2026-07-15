import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { login as loginApi } from '../api/authApi';
import { parseJwt } from '../utils/helpers';
import AuthContext from './authContext';

export function AuthProvider({ children }) {
  const navigate = useNavigate();
  const [usuario, setUsuario] = useState(() => {
    const token = localStorage.getItem('token');
    const role = localStorage.getItem('role');
    const username = localStorage.getItem('username');
    return token && role ? { token, role, username } : null;
  });

  async function login(username, password) {
      const data = await loginApi(username, password);
      const token = data.token;
      const payload = parseJwt(token);

      // 1. Agregar DOCTOR a los roles permitidos
      const allowedRoles = ['RECEPTIONIST', 'ADMIN', 'DOCTOR']; 
      if (!allowedRoles.includes(payload.role)) {
        throw new Error('Acceso denegado.');
      }

      localStorage.setItem('token', token);
      localStorage.setItem('role', payload.role);
      localStorage.setItem('username', payload.sub);
      // IMPORTANTE: Si el JWT trae el ID del doctor, guárdalo aquí también
      // localStorage.setItem('doctorId', payload.doctorId);

      setUsuario({ token, role: payload.role, username: payload.sub });

      // 2. Redirección basada en el rol
      if (payload.role === 'ADMIN') {
        navigate('/admin/dashboard');
      } else if (payload.role === 'DOCTOR') {
        navigate('/doctor/citas'); // Nueva ruta para el doctor
      } else {
        navigate('/');
      }
    }

  function logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('role');
    localStorage.removeItem('username');
    setUsuario(null);
    navigate('/login');
  }

  return (
    <AuthContext.Provider value={{ usuario, login, logout, isAuthenticated: !!usuario }}>
      {children}
    </AuthContext.Provider>
  );
}
