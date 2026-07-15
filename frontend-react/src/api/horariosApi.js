import client from './client';

export const getDoctores = () => client.get('/doctors');
export const getHorariosPorDoctor = (doctorId) => client.get(`/medical-schedules/doctor/${doctorId}`);
export const getSlotsDisponibles = (doctorId, fecha) =>
  client.get(`/appointments/doctor/${doctorId}/available-slots`, { params: { date: fecha } });
export const getHorariosDeDoctor = (doctorId) => 
  client.get(`/medical-schedules/doctor/${doctorId}`);