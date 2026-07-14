import axios from 'axios';
import {
  clearPortalSession,
  PORTAL_SESSION_KEY,
  SESSION_EXPIRED_EVENT,
} from '../auth/session.js';

const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_URL || '/api',
  timeout: 15_000,
});

apiClient.interceptors.request.use((config) => {
  try {
    const session = JSON.parse(window.localStorage.getItem(PORTAL_SESSION_KEY));
    if (session?.token) config.headers.Authorization = `Bearer ${session.token}`;
  } catch {
    clearPortalSession();
  }
  return config;
});

apiClient.interceptors.response.use(
  (response) => response.data?.data ?? response.data,
  (error) => {
    const payload = error.response?.data;
    const fieldMessage = payload?.errors
      ? Object.values(payload.errors).find(Boolean)
      : null;
    const message = fieldMessage
      || payload?.message
      || (error.code === 'ECONNABORTED'
        ? 'La solicitud tardó demasiado. Intenta nuevamente.'
        : 'No pudimos comunicarnos con el servidor.');

    const apiError = new Error(message);
    apiError.status = error.response?.status || 0;
    apiError.code = payload?.code || error.code || 'NETWORK_ERROR';
    apiError.fieldErrors = payload?.errors || {};

    const authorization = error.config?.headers?.get?.('Authorization')
      || error.config?.headers?.Authorization;
    const requestUrl = String(error.config?.url || '');
    const isPublicAuthRequest = requestUrl.startsWith('/auth/');
    if (apiError.status === 401 && (authorization || !isPublicAuthRequest)) {
      clearPortalSession();
      window.dispatchEvent(new Event(SESSION_EXPIRED_EVENT));
    }

    return Promise.reject(apiError);
  },
);

export default apiClient;
