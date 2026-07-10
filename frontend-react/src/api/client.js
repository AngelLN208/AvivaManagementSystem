import axios from 'axios';

const BASE_URL = 'http://localhost:8080/api';

const client = axios.create({ baseURL: BASE_URL });

client.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

client.interceptors.response.use(
  (res) => res.data.data ?? res.data,
  (err) => {
    const mensaje = err.response?.data?.message || 'Error en la petición';
    return Promise.reject(new Error(mensaje));
  }
);

export default client;