import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  createMyInsurance,
  deleteMyInsurance,
  getInsurances,
  getMyInsurances,
} from '../api/portalApi.js';
import { insurancesQueryKey } from '../utils/queryKeys.js';
import { useAuth } from './useAuth.js';

export const useInsuranceCatalog = () => useQuery({
  queryKey: ['portal', 'insurance-catalog'],
  queryFn: getInsurances,
  staleTime: 5 * 60_000,
});

export function useMyInsurances() {
  const { session } = useAuth();
  return useQuery({
    queryKey: insurancesQueryKey(session?.username),
    queryFn: getMyInsurances,
    enabled: Boolean(session?.username),
  });
}

export function useInsuranceMutations() {
  const queryClient = useQueryClient();
  const { session } = useAuth();
  const refresh = () => queryClient.invalidateQueries({
    queryKey: insurancesQueryKey(session?.username),
  });

  const createInsurance = useMutation({
    mutationFn: createMyInsurance,
    onSuccess: refresh,
  });

  const deleteInsurance = useMutation({
    mutationFn: deleteMyInsurance,
    onSuccess: refresh,
  });

  return { createInsurance, deleteInsurance };
}
