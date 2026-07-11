import client from './client';

export const getNotificacionesUsuario = (email) =>
  client.get('/notifications/user', { params: { email } });