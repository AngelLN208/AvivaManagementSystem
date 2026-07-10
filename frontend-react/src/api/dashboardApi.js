import client from './client';

export const getPacientes = () => client.get('/patients');
export const getCitas = () => client.get('/appointments');