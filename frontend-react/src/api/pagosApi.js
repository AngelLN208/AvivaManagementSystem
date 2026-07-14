import client from './client';

export const getPagos = () => client.get('/payments');
export const getCitasPagos = () => client.get('/appointments');
export const getRecibos = () => client.get('/receipts');

// El parametro de la ruta es el ID de Payment, no el ID de Appointment.
export const procesarPago = (paymentId, method) =>
  client.post(`/payments/${paymentId}/process`, null, { params: { method } });
