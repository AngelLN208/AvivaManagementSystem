import client from './client';

export const getTriajePorCita = (appointmentId) => 
    client.get(`/triages/${appointmentId}`);