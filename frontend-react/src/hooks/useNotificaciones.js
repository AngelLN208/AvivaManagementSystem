import { useQuery } from '@tanstack/react-query';
import { getNotificaciones } from '../api/notificacionesApi';

export function useNotificaciones() {
  const query = useQuery({
    queryKey: ['notificaciones'],
    queryFn: getNotificaciones,
    refetchInterval: 60000,
  });

  const notificaciones = query.data || [];

  // Recepcion monitorea entregas; la lectura IN_APP pertenece al portal paciente.
  const alertas = notificaciones.filter(
    (notification) => notification.status === 'PENDING' || notification.status === 'FAILED'
  );

  return {
    notificaciones,
    alertas,
    isLoading: query.isLoading,
    isError: query.isError,
  };
}
