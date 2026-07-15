import client from './client';

export const getCitas = () => client.get('/appointments');
export const getEspecialidades = () => client.get('/specialties');
export const getMedicosPorEspecialidad = (specialtyId) =>
  client.get(`/doctors/by-specialty/${specialtyId}`);
export const getSlotsDisponibles = (doctorId, fecha) =>
  client.get(`/appointments/doctor/${doctorId}/available-slots`, { params: { date: fecha } });
export const buscarPacientePorDni = (dni) =>
  client.get('/patients/search/dni', { params: { dni } });
export const crearCita = (payload) => client.post('/appointments', payload);
export const cancelarCita = (id) => client.put(`/appointments/${id}/cancel`);
export const reprogramarCita = (id, newDateTime) =>
  client.put(`/appointments/${id}/reschedule`, null, { params: { newDateTime } });
export const getCitasDeDoctor = (doctorId) => 
  client.get(`/appointments/doctor/${doctorId}`);