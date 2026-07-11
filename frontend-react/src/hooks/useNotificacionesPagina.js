import { useQuery } from '@tanstack/react-query';
import { getNotificacionesUsuario } from '../api/notificacionesApi';

export function useNotificacionesPagina() {
  const email = localStorage.getItem('email') || '';

  const query = useQuery({
    queryKey: ['notificacionesUsuario', email],
    queryFn: () => getNotificacionesUsuario(email),
    enabled: !!email,
  });

  const notificaciones = [...(query.data || [])].sort(
    (a, b) => new Date(b.createdAt) - new Date(a.createdAt)
  );

  return {
    notificaciones,
    isLoading: query.isLoading,
    isError: query.isError,
  };
}