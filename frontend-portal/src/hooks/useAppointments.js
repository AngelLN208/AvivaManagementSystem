import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  cancelMyAppointment,
  createMyAppointment,
  getMyAppointments,
  rescheduleMyAppointment,
} from '../api/portalApi.js';
import { appointmentsQueryKey } from '../utils/queryKeys.js';
import { useAuth } from './useAuth.js';

export function useMyAppointments() {
  const { session } = useAuth();
  return useQuery({
    queryKey: appointmentsQueryKey(session?.username),
    queryFn: getMyAppointments,
    enabled: Boolean(session?.username),
  });
}

export function useAppointmentMutations() {
  const queryClient = useQueryClient();
  const { session } = useAuth();
  const refresh = () => queryClient.invalidateQueries({
    queryKey: appointmentsQueryKey(session?.username),
  });
  const refreshSlots = () => queryClient.invalidateQueries({
    queryKey: ['portal', 'slots'],
  });

  const createAppointment = useMutation({
    mutationFn: createMyAppointment,
    onSuccess: refresh,
    onError: refreshSlots,
  });

  const cancelAppointment = useMutation({
    mutationFn: cancelMyAppointment,
    onSuccess: refresh,
  });

  const rescheduleAppointment = useMutation({
    mutationFn: ({ appointmentId, newDateTime }) =>
      rescheduleMyAppointment(appointmentId, newDateTime),
    onSuccess: refresh,
    onError: refreshSlots,
  });

  return { createAppointment, cancelAppointment, rescheduleAppointment };
}
