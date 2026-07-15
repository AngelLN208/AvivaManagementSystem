import client from './client';

export const registrarConsulta = (appointmentId, payload) => 
    client.post(`/consultations/${appointmentId}`, payload);

export const getConsultaPorCita = (appointmentId) => 
    client.get(`/consultations/${appointmentId}`);