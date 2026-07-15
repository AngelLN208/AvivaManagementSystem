import client from './client';

export const getPacientes = () => client.get('/patients');
export const getCitasDePaciente = (id) => client.get(`/appointments/patient/${id}`);
export const crearPaciente = (payload) => client.post('/patients', payload);
export const actualizarPaciente = (id, payload) => client.put(`/patients/${id}`, payload);
export const eliminarPaciente = (id) => client.delete(`/patients/${id}`);
