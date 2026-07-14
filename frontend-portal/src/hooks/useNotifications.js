import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  getMyNotifications,
  markMyNotificationAsRead,
} from '../api/portalApi.js';
import { notificationsQueryKey } from '../utils/queryKeys.js';
import { useAuth } from './useAuth.js';

export function useMyNotifications() {
  const { session } = useAuth();

  return useQuery({
    queryKey: notificationsQueryKey(session?.username),
    queryFn: getMyNotifications,
    enabled: Boolean(session?.username),
    staleTime: 30_000,
    refetchInterval: 60_000,
  });
}

export function useNotificationMutations() {
  const queryClient = useQueryClient();
  const { session } = useAuth();
  const queryKey = notificationsQueryKey(session?.username);

  const markAsRead = useMutation({
    mutationFn: markMyNotificationAsRead,
    onSuccess: (updatedNotification) => {
      queryClient.setQueryData(queryKey, (current = []) => current.map((notification) => (
        notification.id === updatedNotification.id ? updatedNotification : notification
      )));
    },
  });

  return { markAsRead };
}
