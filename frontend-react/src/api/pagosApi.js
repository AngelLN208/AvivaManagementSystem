import client from './client';

export const getPagos = () => client.get('/payments');
export const getCitasPagos = () => client.get('/appointments');
export const procesarPago = (paymentId, method) =>
  client.post(`/payments/${paymentId}/process`, null, { params: { method } });