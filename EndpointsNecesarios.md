# Endpoints de la API

Inventario actualizado de las rutas expuestas por el backend.

- Producción: `https://avivamanagementsystem.onrender.com`
- Desarrollo: `http://localhost:8080`
- Swagger: `https://avivamanagementsystem.onrender.com/swagger-ui.html`
- Prefijo general: `/api`

Salvo las rutas de autenticación y Swagger, las solicitudes necesitan:

```http
Authorization: Bearer <token-jwt>
```

## 1. Autenticación y activación

Rutas públicas bajo `/api/auth`.

| Método | Ruta | Descripción |
|---|---|---|
| `POST` | `/api/auth/login` | Autentica cualquier rol y devuelve un JWT. |
| `POST` | `/api/auth/register-patient` | Crea un usuario `PATIENT` junto con su perfil. |
| `POST` | `/api/auth/patient-activation/request` | Busca un paciente existente por DNI y solicita un código. |
| `POST` | `/api/auth/patient-activation/verify-code` | Valida el código y entrega un token temporal. |
| `POST` | `/api/auth/patient-activation/complete` | Crea las credenciales del paciente usando el token temporal. |

## 2. Portal del paciente

Estas rutas usan el paciente vinculado al JWT; el cliente no envía un
`patientId`.

### Citas propias

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/api/appointments/me` | Lista las citas del paciente autenticado. |
| `GET` | `/api/appointments/me/{id}` | Obtiene una cita propia. |
| `POST` | `/api/appointments/me` | Crea una cita propia y su pago pendiente. |
| `PUT` | `/api/appointments/me/{id}/reschedule?newDateTime=...` | Reprograma una cita propia. |
| `PUT` | `/api/appointments/me/{id}/cancel` | Cancela una cita propia. |

### Seguros propios

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/api/patient-insurances/me` | Lista las pólizas activas del paciente. |
| `POST` | `/api/patient-insurances/me` | Vincula una póliza al paciente autenticado. |
| `DELETE` | `/api/patient-insurances/me/{patientInsuranceId}` | Desactiva la vinculación de una póliza propia. |

### Pagos y constancias propias

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/api/payments/me` | Lista los pagos del paciente. |
| `GET` | `/api/payments/me/{id}` | Obtiene un pago propio. |
| `POST` | `/api/payments/me/{id}/pay` | Registra el pago, confirma la cita y genera la constancia. |
| `GET` | `/api/receipts/me` | Lista las constancias del paciente. |
| `GET` | `/api/receipts/me/{id}` | Obtiene una constancia propia. |
| `GET` | `/api/receipts/me/payment/{paymentId}` | Busca la constancia asociada a un pago propio. |
| `GET` | `/api/receipts/me/{id}/pdf` | Descarga la constancia en PDF. |

### Notificaciones propias

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/api/notifications/me` | Lista las notificaciones internas del paciente. |

## 3. Catálogos y disponibilidad

Lecturas disponibles para usuarios autenticados según su rol.

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/api/specialties` | Lista especialidades activas. |
| `GET` | `/api/specialties/{id}` | Obtiene una especialidad. |
| `GET` | `/api/doctors` | Lista médicos activos. |
| `GET` | `/api/doctors/{id}` | Obtiene un médico. |
| `GET` | `/api/doctors/by-specialty/{specialtyId}` | Lista médicos por especialidad. |
| `GET` | `/api/insurances` | Lista aseguradoras activas. |
| `GET` | `/api/appointments/doctor/{doctorId}/available-slots?date=yyyy-MM-dd` | Consulta horas disponibles para una fecha. |

## 4. Pacientes para el personal

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/api/patients` | Lista pacientes activos. |
| `GET` | `/api/patients/{id}` | Obtiene un paciente. |
| `GET` | `/api/patients/search/dni?dni=12345678` | Busca por DNI. |
| `GET` | `/api/patients/search?firstName=...&lastName=...` | Busca por nombre y apellido. |
| `POST` | `/api/patients` | Crea un perfil de paciente desde el panel. |
| `PUT` | `/api/patients/{id}` | Actualiza un paciente. |
| `DELETE` | `/api/patients/{id}` | Desactiva lógicamente un paciente. |

## 5. Administración de médicos y especialidades

| Método | Ruta | Descripción |
|---|---|---|
| `POST` | `/api/specialties` | Crea una especialidad. |
| `PUT` | `/api/specialties/{id}` | Actualiza una especialidad. |
| `DELETE` | `/api/specialties/{id}` | Desactiva una especialidad. |
| `POST` | `/api/doctors` | Crea un médico. |
| `PUT` | `/api/doctors/{id}` | Actualiza un médico. |
| `DELETE` | `/api/doctors/{id}` | Desactiva un médico. |

## 6. Horarios médicos

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/api/medical-schedules/doctor/{doctorId}` | Lista horarios activos del médico. |
| `GET` | `/api/medical-schedules/doctor/{doctorId}/day?day=MONDAY` | Filtra horarios por día. |
| `POST` | `/api/medical-schedules/doctor/{doctorId}` | Crea un bloque de horario. |
| `PUT` | `/api/medical-schedules/{scheduleId}` | Actualiza un horario. |
| `DELETE` | `/api/medical-schedules/{scheduleId}` | Desactiva un horario. |

## 7. Seguros y pólizas para el personal

| Método | Ruta | Descripción |
|---|---|---|
| `POST` | `/api/insurances` | Crea una aseguradora o plan. |
| `GET` | `/api/insurances/{id}` | Obtiene un seguro. |
| `PUT` | `/api/insurances/{id}` | Actualiza un seguro. |
| `DELETE` | `/api/insurances/{id}` | Desactiva un seguro. |
| `POST` | `/api/patient-insurances/patient/{patientId}` | Vincula una póliza a un paciente. |
| `GET` | `/api/patient-insurances/patient/{patientId}` | Lista las pólizas de un paciente. |
| `GET` | `/api/patient-insurances/patient/{patientId}/primary` | Obtiene la póliza primaria. |
| `DELETE` | `/api/patient-insurances/{patientInsuranceId}` | Desactiva una vinculación. |

## 8. Citas para el personal

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/api/appointments` | Lista todas las citas. |
| `GET` | `/api/appointments/{id}` | Obtiene una cita. |
| `GET` | `/api/appointments/patient/{patientId}` | Lista citas por paciente. |
| `GET` | `/api/appointments/doctor/{doctorId}` | Lista citas por médico. |
| `GET` | `/api/appointments/status/{status}` | Lista citas por estado. |
| `POST` | `/api/appointments` | Crea una cita indicando paciente y médico. |
| `PUT` | `/api/appointments/{id}/reschedule?newDateTime=...` | Reprograma una cita. |
| `PUT` | `/api/appointments/{id}/cancel` | Cancela una cita. |

## 9. Pagos y constancias para caja

Estas rutas globales están limitadas a administración y recepción.

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/api/payments` | Lista pagos. |
| `GET` | `/api/payments/{id}` | Obtiene un pago. |
| `GET` | `/api/payments/appointment/{appointmentId}` | Lista pagos de una cita. |
| `GET` | `/api/payments/status/{status}` | Lista pagos por estado. |
| `POST` | `/api/payments/{id}/process?method=CASH` | Procesa un pago, confirma la cita y genera la constancia. |
| `GET` | `/api/receipts` | Lista constancias. |
| `GET` | `/api/receipts/{id}` | Obtiene una constancia. |
| `GET` | `/api/receipts/number/{receiptNumber}` | Busca una constancia por número. |

## 10. Atención clínica

Estas rutas pertenecen al personal y no se exponen en el portal paciente.

| Método | Ruta | Descripción |
|---|---|---|
| `POST` | `/api/triages/{appointmentId}` | Registra el triaje de una cita. |
| `GET` | `/api/triages/{appointmentId}` | Obtiene el triaje. |
| `POST` | `/api/consultations/{appointmentId}` | Registra la consulta de una cita confirmada. |
| `GET` | `/api/consultations/{appointmentId}` | Obtiene la consulta. |

## 11. Notificaciones y auditoría para el personal

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/api/notifications` | Lista el historial general de notificaciones. |
| `GET` | `/api/notifications/user?email=...` | Lista notificaciones de un destinatario. |
| `GET` | `/api/notifications/user/in-app?email=...` | Lista notificaciones internas por correo. |
| `GET` | `/api/notifications/appointment/{appointmentId}` | Lista notificaciones de una cita. |
| `GET` | `/api/audit-logs/appointment/{appointmentId}` | Lista el historial de auditoría de una cita. |
| `GET` | `/api/audit-logs/{id}` | Obtiene un evento de auditoría. |

## Comportamiento importante

- Los endpoints `DELETE` de pacientes, médicos, especialidades, horarios,
  seguros y pólizas realizan desactivación lógica; no eliminan físicamente los
  registros principales.
- La creación de una cita genera automáticamente un pago `PENDING`.
- Al confirmar un pago, el pago cambia a `PAID`, la cita a `CONFIRMED` y se
  genera una constancia.
- El portal paciente usa rutas `/me` para obtener la identidad desde el JWT y
  validar ownership en el backend.
- Swagger es la referencia ejecutable para cuerpos, validaciones y esquemas de
  respuesta.
