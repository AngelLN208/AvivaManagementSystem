import { useMemo, useState } from 'react';
import { BellRing, CheckCheck, RefreshCw } from 'lucide-react';
import NotificationCard from '../components/notifications/NotificationCard.jsx';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert.jsx';
import { Button } from '@/components/ui/button.jsx';
import { EmptyState, ErrorState, LoadingState } from '@/components/ui/AsyncState.jsx';
import PageHeader from '@/components/ui/PageHeader.jsx';
import { useMyNotifications, useNotificationMutations } from '../hooks/useNotifications.js';
import {
  filterNotifications,
  unreadNotificationCount,
} from '../utils/notifications.js';

const FILTERS = [
  { id: 'all', label: 'Todas' },
  { id: 'unread', label: 'No leídas' },
];

export default function NotificationsPage() {
  const notificationsQuery = useMyNotifications();
  const { markAsRead } = useNotificationMutations();
  const [filter, setFilter] = useState('all');
  const [markingAll, setMarkingAll] = useState(false);

  const notifications = notificationsQuery.data || [];
  const unreadCount = unreadNotificationCount(notifications);
  const visibleNotifications = useMemo(
    () => filterNotifications(notifications, filter),
    [filter, notifications],
  );

  async function markAllAsRead() {
    const unread = notifications.filter((notification) => !notification.read);
    if (unread.length === 0) return;

    setMarkingAll(true);
    markAsRead.reset();
    try {
      for (const notification of unread) {
        await markAsRead.mutateAsync(notification.id);
      }
    } catch {
      // La mutación conserva el error para mostrarlo en el Alert de la página.
    } finally {
      setMarkingAll(false);
    }
  }

  if (notificationsQuery.isLoading) {
    return <LoadingState message="Cargando tus notificaciones…" />;
  }

  if (notificationsQuery.isError) {
    return (
      <ErrorState
        message={notificationsQuery.error?.message}
        onRetry={() => notificationsQuery.refetch()}
      />
    );
  }

  return (
    <div className="space-y-6">
      <PageHeader
        eyebrow="Centro de avisos"
        title="Notificaciones"
        description="Revisa las novedades relacionadas con tus citas y pagos."
        action={unreadCount > 0 ? (
          <Button
            type="button"
            variant="outline"
            disabled={markingAll}
            onClick={markAllAsRead}
          >
            <CheckCheck aria-hidden="true" />
            {markingAll ? 'Marcando…' : 'Marcar todas como leídas'}
          </Button>
        ) : null}
      />

      <div className="flex flex-wrap gap-2" role="group" aria-label="Filtrar notificaciones">
        {FILTERS.map((item) => (
          <Button
            key={item.id}
            type="button"
            variant={filter === item.id ? 'default' : 'outline'}
            size="sm"
            aria-pressed={filter === item.id}
            onClick={() => setFilter(item.id)}
          >
            {item.label}
            {item.id === 'unread' && unreadCount > 0 && (
              <span className="rounded-full bg-background/20 px-1.5 text-xs">{unreadCount}</span>
            )}
          </Button>
        ))}
      </div>

      {markAsRead.isError && (
        <Alert variant="destructive">
          <RefreshCw aria-hidden="true" />
          <AlertTitle>No pudimos actualizar la notificación</AlertTitle>
          <AlertDescription>{markAsRead.error?.message}</AlertDescription>
        </Alert>
      )}

      {visibleNotifications.length === 0 ? (
        <EmptyState
          title={filter === 'unread' ? 'Estás al día' : 'Aún no tienes notificaciones'}
          description={filter === 'unread'
            ? 'No tienes avisos pendientes de lectura.'
            : 'Aquí aparecerán los cambios relacionados con tus citas y pagos.'}
          action={filter === 'unread' ? (
            <Button type="button" variant="outline" onClick={() => setFilter('all')}>
              Ver todas
            </Button>
          ) : null}
        />
      ) : (
        <section className="grid gap-4" aria-label="Historial de notificaciones">
          {visibleNotifications.map((notification) => (
            <NotificationCard
              key={notification.id}
              notification={notification}
              isMarking={markAsRead.isPending && markAsRead.variables === notification.id}
              onMarkAsRead={(id) => markAsRead.mutate(id)}
            />
          ))}
        </section>
      )}

      <p className="flex items-center gap-2 text-xs text-muted-foreground">
        <BellRing className="size-4" aria-hidden="true" />
        Los nuevos avisos se actualizan automáticamente.
      </p>
    </div>
  );
}
