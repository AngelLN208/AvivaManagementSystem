import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  getMyPayments,
  getMyReceiptPdf,
  getMyReceipts,
  payMyPayment,
} from '../api/portalApi.js';
import {
  appointmentsQueryKey,
  paymentsQueryKey,
  receiptsQueryKey,
} from '../utils/queryKeys.js';
import { useAuth } from './useAuth.js';

export function useMyPayments() {
  const { session } = useAuth();
  return useQuery({
    queryKey: paymentsQueryKey(session?.username),
    queryFn: getMyPayments,
    enabled: Boolean(session?.username),
  });
}

export function useMyReceipts() {
  const { session } = useAuth();
  return useQuery({
    queryKey: receiptsQueryKey(session?.username),
    queryFn: getMyReceipts,
    enabled: Boolean(session?.username),
  });
}

export function usePaymentMutations() {
  const queryClient = useQueryClient();
  const { session } = useAuth();

  const payPayment = useMutation({
    mutationFn: ({ paymentId, method }) => payMyPayment(paymentId, method),
    onSuccess: async () => {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: paymentsQueryKey(session?.username) }),
        queryClient.invalidateQueries({ queryKey: receiptsQueryKey(session?.username) }),
        queryClient.invalidateQueries({ queryKey: appointmentsQueryKey(session?.username) }),
      ]);
    },
  });

  const downloadReceiptPdf = useMutation({
    mutationFn: getMyReceiptPdf,
  });

  return { payPayment, downloadReceiptPdf };
}
