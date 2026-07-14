import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getCitas, cancelarCita, reprogramarCita, crearCita } from '../api/citasApi';

export function useCitas() {
  return useQuery({ queryKey: ['citas'], queryFn: getCitas });
}

export function useCitasMutations() {
  const queryClient = useQueryClient();

  // Cada cambio de cita genera notificaciones; ambas vistas deben refrescarse juntas.
  const invalidar = () => Promise.all([
    queryClient.invalidateQueries({ queryKey: ['citas'] }),
    queryClient.invalidateQueries({ queryKey: ['notificaciones'] }),
  ]);

  const crear = useMutation({ mutationFn: crearCita, onSuccess: invalidar });
  const cancelar = useMutation({ mutationFn: cancelarCita, onSuccess: invalidar });
  const reprogramar = useMutation({
    mutationFn: ({ id, newDateTime }) => reprogramarCita(id, newDateTime),
    onSuccess: invalidar,
  });

  return { crear, cancelar, reprogramar };
}
