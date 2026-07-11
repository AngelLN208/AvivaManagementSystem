import client from './client';

export const getPagos = () => client.get('/payments');
export const getCitasPagos = () => client.get('/appointments');
export const getRecibos = () => client.get('/receipts');
export const procesarPago = (appointmentId, method) =>
  client.post(`/payments/${appointmentId}/process`, null, { params: { method } });