import client from './client';

export const getHorariosPorDoctor = (doctorId) => 
  client.get(`/medical-schedules/doctor/${doctorId}`);
export const crearHorario = (doctorId, payload) => 
  client.post(`/medical-schedules/doctor/${doctorId}`, payload);
export const actualizarHorario = (scheduleId, payload) => 
  client.put(`/medical-schedules/${scheduleId}`, payload);
export const eliminarHorario = (scheduleId) => 
  client.delete(`/medical-schedules/${scheduleId}`);
