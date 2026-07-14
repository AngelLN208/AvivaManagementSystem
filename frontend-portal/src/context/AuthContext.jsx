import { useCallback, useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useQueryClient } from '@tanstack/react-query';
import { loginPatient, registerPatient } from '../api/authApi.js';
import {
  clearPortalSession,
  createPatientSession,
  decodeJwtPayload,
  isTokenExpired,
  PORTAL_SESSION_KEY,
  readPortalSession,
  SESSION_EXPIRED_EVENT,
  writePortalSession,
} from '../auth/session.js';
import AuthContext from './authContext.js';

export function AuthProvider({ children }) {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [session, setSession] = useState(readPortalSession);

  const login = useCallback(async (username, password) => {
    const response = await loginPatient(username, password);
    const nextSession = createPatientSession(response);
    // Evita que un paciente herede datos cacheados de una sesión anterior.
    queryClient.clear();
    writePortalSession(nextSession);
    setSession(nextSession);
    return nextSession;
  }, [queryClient]);

  const register = useCallback(async (patientData) => {
    await registerPatient(patientData);
    try {
      return await login(patientData.username, patientData.password);
    } catch (error) {
      // El alta ya fue confirmada: el formulario no debe intentar crearla otra vez.
      error.accountCreated = true;
      throw error;
    }
  }, [login]);

  const logout = useCallback(() => {
    clearPortalSession();
    queryClient.clear();
    setSession(null);
    navigate('/login', { replace: true });
  }, [navigate, queryClient]);

  useEffect(() => {
    const handleExpiredSession = () => {
      clearPortalSession();
      queryClient.clear();
      setSession(null);
      navigate('/login?sesion=expirada', { replace: true });
    };

    window.addEventListener(SESSION_EXPIRED_EVENT, handleExpiredSession);
    return () => window.removeEventListener(SESSION_EXPIRED_EVENT, handleExpiredSession);
  }, [navigate, queryClient]);

  useEffect(() => {
    const handleStorage = (event) => {
      if (event.key !== PORTAL_SESSION_KEY) return;

      const nextSession = readPortalSession();
      queryClient.clear();
      setSession(nextSession);
      if (!nextSession) navigate('/login', { replace: true });
    };

    window.addEventListener('storage', handleStorage);
    return () => window.removeEventListener('storage', handleStorage);
  }, [navigate, queryClient]);

  useEffect(() => {
    if (!session?.token) return undefined;

    const expireSession = () => {
      clearPortalSession();
      queryClient.clear();
      setSession(null);
      navigate('/login?sesion=expirada', { replace: true });
    };

    const expirationMs = (decodeJwtPayload(session.token)?.exp || 0) * 1000;
    const timerId = window.setTimeout(expireSession, Math.max(0, expirationMs - Date.now()));
    const verifyWhenVisible = () => {
      if (document.visibilityState === 'visible' && isTokenExpired(session.token)) {
        expireSession();
      }
    };

    document.addEventListener('visibilitychange', verifyWhenVisible);
    return () => {
      window.clearTimeout(timerId);
      document.removeEventListener('visibilitychange', verifyWhenVisible);
    };
  }, [session, navigate, queryClient]);

  const value = useMemo(() => ({
    session,
    isAuthenticated: Boolean(session),
    login,
    register,
    logout,
  }), [session, login, register, logout]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
