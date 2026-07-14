import test from 'node:test';
import assert from 'node:assert/strict';
import {
  canManageAppointment,
  doctorDisplayName,
  splitAppointments,
} from '../src/utils/appointments.js';

const now = new Date(2026, 6, 14, 10, 0, 0);

test('separa próximas citas del historial y ordena cada grupo', () => {
  const appointments = [
    { id: 1, status: 'COMPLETED', appointmentDateTime: '2026-07-13T09:00:00' },
    { id: 2, status: 'PENDING', appointmentDateTime: '2026-07-16T11:00:00' },
    { id: 3, status: 'CONFIRMED', appointmentDateTime: '2026-07-15T08:00:00' },
    { id: 4, status: 'CANCELLED', appointmentDateTime: '2026-07-20T08:00:00' },
  ];

  const result = splitAppointments(appointments, now);
  assert.deepEqual(result.upcoming.map((item) => item.id), [3, 2]);
  assert.deepEqual(result.history.map((item) => item.id), [4, 1]);
});

test('solo permite modificar citas activas y futuras', () => {
  assert.equal(canManageAppointment({ status: 'PENDING', appointmentDateTime: '2026-07-15T08:00:00' }, now), true);
  assert.equal(canManageAppointment({ status: 'CANCELLED', appointmentDateTime: '2026-07-15T08:00:00' }, now), false);
  assert.equal(canManageAppointment({ status: 'CONFIRMED', appointmentDateTime: '2026-07-13T08:00:00' }, now), false);
});

test('formatea el nombre del médico sin exponer datos adicionales', () => {
  assert.equal(doctorDisplayName({ firstName: 'Ana', lastName: 'Rojas' }), 'Dr(a). Ana Rojas');
  assert.equal(doctorDisplayName(null), 'Médico por confirmar');
});
