import client from './client';

export const getEspecialidades = () => client.get('/specialties');
export const crearEspecialidad = (payload) => client.post('/specialties', payload);
export const actualizarEspecialidad = (id, payload) => client.put(`/specialties/${id}`, payload);
export const eliminarEspecialidad = (id) => client.delete(`/specialties/${id}`);
