import { useQuery } from '@tanstack/react-query';
import { getDoctores, getHorariosPorDoctor } from '../api/horariosApi';

export function useHorarios() {
  const doctoresQuery = useQuery({ queryKey: ['doctores'], queryFn: getDoctores });

  const horariosQuery = useQuery({
    queryKey: ['horarios', doctoresQuery.data?.map((d) => d.id)],
    queryFn: async () => {
      const doctores = doctoresQuery.data;
      const resultados = [];
      await Promise.all(
        doctores.map(async (d) => {
          try {
            const schedules = await getHorariosPorDoctor(d.id);
            schedules.forEach((s) => resultados.push({ doctor: d, schedule: s }));
          } catch {
            // doctor sin horario, se omite igual que en el original
          }
        })
      );
      return resultados;
    },
    enabled: !!doctoresQuery.data,
  });

  return {
    doctores: doctoresQuery.data || [],
    horarios: horariosQuery.data || [],
    isLoading: doctoresQuery.isLoading || horariosQuery.isLoading,
    isError: doctoresQuery.isError,
  };
}