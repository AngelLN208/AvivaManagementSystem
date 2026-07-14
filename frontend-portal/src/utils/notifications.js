export const NOTIFICATION_LABELS = {
  APPOINTMENT_CREATED: 'Nueva cita',
  APPOINTMENT_RESCHEDULED: 'Cita reprogramada',
  APPOINTMENT_CANCELLED: 'Cita cancelada',
  APPOINTMENT_REMINDER: 'Recordatorio',
  APPOINTMENT_CONFIRMED: 'Cita confirmada',
  APPOINTMENT_UPDATED: 'Cita actualizada',
  PAYMENT_RECEIVED: 'Pago confirmado',
  PAYMENT_FAILED: 'Pago no procesado',
};

export function notificationLabel(type) {
  return NOTIFICATION_LABELS[type] || 'Notificación';
}

export function unreadNotificationCount(notifications) {
  return Array.isArray(notifications)
    ? notifications.filter((notification) => !notification.read).length
    : 0;
}

export function filterNotifications(notifications, filter) {
  const safeNotifications = Array.isArray(notifications) ? notifications : [];
  return filter === 'unread'
    ? safeNotifications.filter((notification) => !notification.read)
    : safeNotifications;
}
