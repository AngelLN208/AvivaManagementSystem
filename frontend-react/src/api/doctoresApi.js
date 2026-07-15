import client from './client';

export const getDoctores = () => client.get('/doctors');
export const getDoctorPorEspecialidad = (specialtyId) =>
  client.get(`/doctors/by-specialty/${specialtyId}`);
export const crearDoctor = (payload) => client.post('/doctors', payload);
export const actualizarDoctor = (id, payload) => client.put(`/doctors/${id}`, payload);
export const eliminarDoctor = (id) => client.delete(`/doctors/${id}`);
