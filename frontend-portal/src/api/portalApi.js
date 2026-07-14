import apiClient from './client.js';
import { requirePositiveId } from '../utils/ids.js';

export const getMyAppointments = () => apiClient.get('/appointments/me');

export const createMyAppointment = (appointment) =>
  apiClient.post('/appointments/me', appointment);

export const cancelMyAppointment = (appointmentId) =>
  apiClient.put(`/appointments/me/${requirePositiveId(appointmentId)}/cancel`);

export const rescheduleMyAppointment = (appointmentId, newDateTime) =>
  apiClient.put(`/appointments/me/${requirePositiveId(appointmentId)}/reschedule`, null, {
    params: { newDateTime },
  });

export const getSpecialties = () => apiClient.get('/specialties');

export const getDoctors = () => apiClient.get('/doctors');

export const getDoctorsBySpecialty = (specialtyId) =>
  apiClient.get(`/doctors/by-specialty/${requirePositiveId(specialtyId)}`);

export const getAvailableSlots = (doctorId, date) =>
  apiClient.get(`/appointments/doctor/${requirePositiveId(doctorId)}/available-slots`, {
    params: { date },
  });

export const getInsurances = () => apiClient.get('/insurances');

export const getMyInsurances = () => apiClient.get('/patient-insurances/me');

export const createMyInsurance = (insurance) =>
  apiClient.post('/patient-insurances/me', insurance);

export const deleteMyInsurance = (patientInsuranceId) =>
  apiClient.delete(`/patient-insurances/me/${requirePositiveId(patientInsuranceId)}`);

export const getMyPayments = () => apiClient.get('/payments/me');

export const payMyPayment = (paymentId, method) =>
  apiClient.post(`/payments/me/${requirePositiveId(paymentId)}/pay`, { method });

export const getMyReceipts = () => apiClient.get('/receipts/me');

export const getMyReceiptPdf = (receiptId) =>
  apiClient.get(`/receipts/me/${requirePositiveId(receiptId)}/pdf`, {
    responseType: 'blob',
  });

export const getMyNotifications = () => apiClient.get('/notifications/me');

export const markMyNotificationAsRead = (notificationId) =>
  apiClient.patch(`/notifications/me/${requirePositiveId(notificationId)}/read`);
