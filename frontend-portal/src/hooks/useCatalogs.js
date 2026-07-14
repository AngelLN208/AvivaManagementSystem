import { useQuery } from '@tanstack/react-query';
import {
  getAvailableSlots,
  getDoctors,
  getDoctorsBySpecialty,
  getSpecialties,
} from '../api/portalApi.js';

export const useSpecialties = () => useQuery({
  queryKey: ['portal', 'specialties'],
  queryFn: getSpecialties,
  staleTime: 5 * 60_000,
});

export const useDoctors = () => useQuery({
  queryKey: ['portal', 'doctors'],
  queryFn: getDoctors,
  staleTime: 2 * 60_000,
});

export const useDoctorsBySpecialty = (specialtyId) => useQuery({
  queryKey: ['portal', 'doctors', 'specialty', specialtyId],
  queryFn: () => getDoctorsBySpecialty(specialtyId),
  enabled: Boolean(specialtyId),
  staleTime: 2 * 60_000,
});

export const useAvailableSlots = (doctorId, date) => useQuery({
  queryKey: ['portal', 'slots', doctorId, date],
  queryFn: () => getAvailableSlots(doctorId, date),
  enabled: Boolean(doctorId && date),
  staleTime: 15_000,
});
