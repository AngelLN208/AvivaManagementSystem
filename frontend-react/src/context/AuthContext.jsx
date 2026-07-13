import { createContext, useContext, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { login as loginApi } from '../api/authApi';
import { parseJwt } from '../utils/helpers';

const AuthContext = createContext();

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

    if (payload.role !== 'RECEPTIONIST') {
      throw new Error('Acceso solo para personal de recepción.');
    }

    localStorage.setItem('token', token);
    localStorage.setItem('role', payload.role);
    localStorage.setItem('username', payload.sub);

    setUsuario({ token, role: payload.role, username: payload.sub });
    navigate('/');
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

export function useAuth() {
  return useContext(AuthContext);
}