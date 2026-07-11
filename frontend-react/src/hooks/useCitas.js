import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getCitas, cancelarCita, reprogramarCita, crearCita } from '../api/citasApi';

export function useCitas() {
  return useQuery({ queryKey: ['citas'], queryFn: getCitas });
}

export function useCitasMutations() {
  const queryClient = useQueryClient();
  const invalidar = () => queryClient.invalidateQueries({ queryKey: ['citas'] });

  const crear = useMutation({ mutationFn: crearCita, onSuccess: invalidar });
  const cancelar = useMutation({ mutationFn: cancelarCita, onSuccess: invalidar });
  const reprogramar = useMutation({
    mutationFn: ({ id, newDateTime }) => reprogramarCita(id, newDateTime),
    onSuccess: invalidar,
  });

  return { crear, cancelar, reprogramar };
}