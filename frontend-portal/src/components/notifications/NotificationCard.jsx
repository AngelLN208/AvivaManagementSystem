import { Link } from 'react-router-dom';
import {
  BellRing,
  CalendarCheck2,
  CalendarClock,
  CalendarX2,
  Check,
  CreditCard,
} from 'lucide-react';
import { Badge } from '@/components/ui/badge.jsx';
import { Button } from '@/components/ui/button.jsx';
import { Card, CardContent } from '@/components/ui/card.jsx';
import { cn } from '@/lib/utils.js';
import { formatLongDate, formatTime } from '../../utils/dates.js';
import { notificationLabel } from '../../utils/notifications.js';

const TYPE_ICONS = {
  APPOINTMENT_CREATED: CalendarCheck2,
  APPOINTMENT_RESCHEDULED: CalendarClock,
  APPOINTMENT_CANCELLED: CalendarX2,
  PAYMENT_RECEIVED: CreditCard,
};

export default function NotificationCard({ notification, isMarking, onMarkAsRead }) {
  const Icon = TYPE_ICONS[notification.type] || BellRing;
  const unread = !notification.read;

  return (
    <Card className={cn(
      'overflow-hidden transition-colors',
      unread && 'border-primary/30 bg-primary/[0.035]',
    )}>
      <CardContent className="flex gap-4 p-5 sm:p-6">
        <span className={cn(
          'grid size-11 shrink-0 place-items-center rounded-xl',
          unread ? 'bg-primary/10 text-primary' : 'bg-muted text-muted-foreground',
        )} aria-hidden="true">
          <Icon className="size-5" />
        </span>

        <div className="min-w-0 flex-1">
          <div className="flex flex-wrap items-center gap-2">
            <p className="text-xs font-bold uppercase tracking-[0.12em] text-primary">
              {notificationLabel(notification.type)}
            </p>
            {unread && <Badge variant="info">Nueva</Badge>}
          </div>
          <h2 className="mt-2 text-lg font-bold tracking-tight text-foreground">
            {notification.subject}
          </h2>
          <p className="mt-2 text-sm leading-6 text-muted-foreground">
            {notification.message}
          </p>

          <div className="mt-4 flex flex-col gap-3 border-t border-border/70 pt-4 sm:flex-row sm:items-center sm:justify-between">
            <p className="text-xs text-muted-foreground">
              {formatLongDate(notification.createdAt)} · {formatTime(notification.createdAt)}
            </p>
            <div className="flex flex-wrap gap-2">
              {notification.appointmentId && (
                <Button asChild variant="outline" size="sm">
                  <Link to="/citas">Ver cita</Link>
                </Button>
              )}
              {unread && (
                <Button
                  type="button"
                  variant="ghost"
                  size="sm"
                  disabled={isMarking}
                  onClick={() => onMarkAsRead(notification.id)}
                >
                  <Check aria-hidden="true" />
                  {isMarking ? 'Guardando…' : 'Marcar como leída'}
                </Button>
              )}
            </div>
          </div>
        </div>
      </CardContent>
    </Card>
  );
}
