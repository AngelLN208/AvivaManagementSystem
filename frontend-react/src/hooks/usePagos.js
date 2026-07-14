import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getPagos, getCitasPagos, procesarPago } from '../api/pagosApi';

export function usePagos() {
  const pagosQuery = useQuery({ queryKey: ['pagos'], queryFn: getPagos });
  const citasQuery = useQuery({ queryKey: ['citas'], queryFn: getCitasPagos });

  const pagos = pagosQuery.data || [];
  const citas = citasQuery.data || [];

  const pagosConCita = pagos.map((p) => ({
    ...p,
    appointment: citas.find((c) => c.id === p.appointmentId) || null,
  }));

  return {
    pagos: pagosConCita,
    isLoading: pagosQuery.isLoading || citasQuery.isLoading,
    isError: pagosQuery.isError || citasQuery.isError,
  };
}

export function usePagosMutations() {
  const queryClient = useQueryClient();
  const procesar = useMutation({
    mutationFn: ({ paymentId, method }) => procesarPago(paymentId, method),
    // El pago confirma la cita y crea notificaciones, por eso se invalidan los tres recursos.
    onSuccess: () => Promise.all([
      queryClient.invalidateQueries({ queryKey: ['pagos'] }),
      queryClient.invalidateQueries({ queryKey: ['citas'] }),
      queryClient.invalidateQueries({ queryKey: ['notificaciones'] }),
    ]),
  });
  return { procesar };
}
