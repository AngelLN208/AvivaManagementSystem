import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { crearPaciente, getPacientes } from '../api/pacientesApi';
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
        try {
          await linkInsuranceToPatient(paciente.id, datosSeguro);
        } catch (error) {
          // El backend expone altas separadas. Se informa el éxito parcial para
          // impedir que recepción reintente y duplique el DNI ya registrado.
          const partialError = new Error(
            `El paciente fue creado, pero no se pudo vincular el seguro: ${error.message}`,
          );
          partialError.patientCreated = true;
          throw partialError;
        }
      }

      return paciente;
    },
    // También refresca ante éxito parcial porque el paciente ya puede existir.
    onSettled: () => queryClient.invalidateQueries({ queryKey: ['pacientes'] }),
  });

  return { crear };
}
