import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getPacientes, crearPaciente } from '../api/pacientesApi';

export function usePacientes() {
  return useQuery({ queryKey: ['pacientes'], queryFn: getPacientes });
}

export function usePacientesMutations() {
  const queryClient = useQueryClient();
  const crear = useMutation({
    mutationFn: crearPaciente,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['pacientes'] }),
  });
  return { crear };
}