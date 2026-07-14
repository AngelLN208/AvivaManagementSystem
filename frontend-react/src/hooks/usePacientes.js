import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getPacientes, crearPaciente } from '../api/pacientesApi';
import { linkInsuranceToPatient } from '../api/insurancesApi';

export function usePacientes() {
  return useQuery({ queryKey: ['pacientes'], queryFn: getPacientes });
}

export function usePacientesMutations() {
  const queryClient = useQueryClient();

  const crear = useMutation({
    mutationFn: async ({ datosPaciente, datosSeguro }) => {
      const paciente = await crearPaciente(datosPaciente);
      if (datosSeguro) {
        await linkInsuranceToPatient(paciente.id, datosSeguro);
      }
      return paciente;
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['pacientes'] }),
  });

  return { crear };
}