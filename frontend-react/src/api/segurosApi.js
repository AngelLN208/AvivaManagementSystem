import client from './client';

export const getSeguros = () => client.get('/insurances');
export const getSeguroPorId = (id) => client.get(`/insurances/${id}`);
export const crearSeguro = (payload) => client.post('/insurances', payload);
export const actualizarSeguro = (id, payload) => client.put(`/insurances/${id}`, payload);
export const eliminarSeguro = (id) => client.delete(`/insurances/${id}`);