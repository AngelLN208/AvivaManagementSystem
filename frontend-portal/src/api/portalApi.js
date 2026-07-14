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
