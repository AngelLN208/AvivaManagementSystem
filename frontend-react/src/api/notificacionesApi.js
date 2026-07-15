import client from './client';

export const getNotificaciones = () => client.get('/notifications');

// El portal de staff consulta el estado global; el paciente usa /notifications/me.
export const getNotificacionesUsuario = (email) =>
  client.get('/notifications/user', { params: { email } });
