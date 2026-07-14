import test from 'node:test';
import assert from 'node:assert/strict';
import {
  filterNotifications,
  notificationLabel,
  unreadNotificationCount,
} from '../src/utils/notifications.js';

const NOTIFICATIONS = [
  { id: 1, type: 'APPOINTMENT_CREATED', read: false },
  { id: 2, type: 'PAYMENT_RECEIVED', read: true },
  { id: 3, type: 'APPOINTMENT_CANCELLED', read: false },
];

test('counts and filters unread notifications', () => {
  assert.equal(unreadNotificationCount(NOTIFICATIONS), 2);
  assert.deepEqual(
    filterNotifications(NOTIFICATIONS, 'unread').map(({ id }) => id),
    [1, 3],
  );
});

test('returns patient-facing labels with a safe fallback', () => {
  assert.equal(notificationLabel('PAYMENT_RECEIVED'), 'Pago confirmado');
  assert.equal(notificationLabel('UNKNOWN_EVENT'), 'Notificación');
});
