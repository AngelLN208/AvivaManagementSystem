import client from './client';

export const getPacientes = () => client.get('/patients');
export const getCitasDePaciente = (id) => client.get(`/appointments/patient/${id}`);
export const crearPaciente = (payload) => client.post('/patients', payload);
