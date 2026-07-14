import client from './client';

export const getNotificaciones = () => client.get('/notifications');

// Se conserva para el futuro portal del paciente.
export const getNotificacionesUsuario = (email) =>
  client.get('/notifications/user', { params: { email } });
