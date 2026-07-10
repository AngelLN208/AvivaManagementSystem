import { useQuery } from '@tanstack/react-query';
import { getPacientes, getCitas } from '../api/dashboardApi';
import { esHoy } from '../utils/formatters';

export function useDashboard() {
  const pacientesQuery = useQuery({ queryKey: ['pacientes'], queryFn: getPacientes });
  const citasQuery = useQuery({ queryKey: ['citas'], queryFn: getCitas });

  const pacientes = pacientesQuery.data || [];
  const citas = citasQuery.data || [];

  const citasHoy = citas.filter(c => esHoy(c.appointmentDateTime));
  const realizadasHoy = citasHoy.filter(c => c.status === 'COMPLETED');
  const pendientesHoy = citasHoy.filter(c => c.status === 'PENDING' || c.status === 'CONFIRMED');

  const proximasCitas = citasHoy
    .filter(c => c.status !== 'CANCELLED')
    .sort((a, b) => new Date(a.appointmentDateTime) - new Date(b.appointmentDateTime))
    .slice(0, 10);

  return {
    isLoading: pacientesQuery.isLoading || citasQuery.isLoading,
    isError: pacientesQuery.isError || citasQuery.isError,
    totalPacientes: pacientes.length,
    totalRealizadasHoy: realizadasHoy.length,
    totalPendientesHoy: pendientesHoy.length,
    proximasCitas,
  };
}