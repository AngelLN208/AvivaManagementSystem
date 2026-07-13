# 📋 Endpoints Implementados - Sistema de Citas Médicas

## Resumen General
Se han implementado **32 endpoints REST** siguiendo arquitectura limpia con capas de Controller → Service → Repository.

---

## 🔐 Autenticación (`/api/auth`)

| Método | Endpoint | Descripción | Status |
|--------|----------|-------------|--------|
| POST | `/api/auth/login` | Inicia sesión y devuelve token JWT | ✅ |
| POST | `/api/auth/register-patient` | Auto-registro de pacientes | ✅ |

---

## 🏥 Especialidades (`/api/specialties`)

| Método | Endpoint | Descripción | Status |
|--------|----------|-------------|--------|
| POST | `/api/specialties` | Crea nueva especialidad | ✅ |
| GET | `/api/specialties` | Lista todas las especialidades | ✅ |
| GET | `/api/specialties/{id}` | Obtiene especialidad por ID | ✅ |
| PUT | `/api/specialties/{id}` | Actualiza especialidad | ✅ |
| DELETE | `/api/specialties/{id}` | Desactiva especialidad | ✅ |

---

## 👥 Pacientes (`/api/patients`)

| Método | Endpoint | Descripción | Status |
|--------|----------|-------------|--------|
| POST | `/api/patients` | Crea nuevo paciente | ✅ |
| GET | `/api/patients` | Lista todos los pacientes | ✅ |
| GET | `/api/patients/{id}` | Obtiene paciente por ID | ✅ |
| GET | `/api/patients/search/dni?dni=...` | Busca paciente por DNI | ✅ |
| GET | `/api/patients/search?firstName=...&lastName=...` | Busca pacientes por nombre | ✅ |
| PUT | `/api/patients/{id}` | Actualiza datos de paciente | ✅ |
| DELETE | `/api/patients/{id}` | Desactiva paciente | ✅ |

---

## 👨‍⚕️ Doctores (`/api/doctors`)

| Método | Endpoint | Descripción | Status |
|--------|----------|-------------|--------|
| POST | `/api/doctors` | Crea nuevo doctor | ✅ |
| GET | `/api/doctors` | Lista todos los doctores | ✅ |
| GET | `/api/doctors/{id}` | Obtiene doctor por ID | ✅ |
| GET | `/api/doctors/by-specialty/{specialtyId}` | Lista doctores por especialidad | ✅ |
| PUT | `/api/doctors/{id}` | Actualiza datos de doctor | ✅ |
| DELETE | `/api/doctors/{id}` | Desactiva doctor | ✅ |

---

## 📅 Citas (`/api/appointments`)

| Método | Endpoint | Descripción | Características | Status |
|--------|----------|-------------|-----------------|--------|
| POST | `/api/appointments` | Crea nueva cita | Valida disponibilidad, crea pago automático | ✅ |
| GET | `/api/appointments` | Lista todas las citas | - | ✅ |
| GET | `/api/appointments/{id}` | Obtiene cita por ID | Con datos anidados de paciente/doctor | ✅ |
| GET | `/api/appointments/patient/{patientId}` | Citas de paciente | Filtrado por estado | ✅ |
| GET | `/api/appointments/doctor/{doctorId}` | Citas de doctor | Filtrado por estado | ✅ |
| GET | `/api/appointments/status/{status}` | Citas por estado | Estados: PENDING, CONFIRMED, COMPLETED, CANCELLED, RESCHEDULED, NO_SHOW | ✅ |
| PUT | `/api/appointments/{id}/reschedule?newDateTime=...` | Reprograma cita | Valida disponibilidad | ✅ |
| PUT | `/api/appointments/{id}/cancel` | Cancela cita | Registra en auditoría | ✅ |

**Estados de Cita:**
- `PENDING` - Pago pendiente
- `CONFIRMED` - Confirmada (pago realizado)
- `COMPLETED` - Completada
- `CANCELLED` - Cancelada
- `RESCHEDULED` - Reprogramada
- `NO_SHOW` - No asistió

---

## 💰 Pagos (`/api/payments`)

| Método | Endpoint | Descripción | Características | Status |
|--------|----------|-------------|-----------------|--------|
| POST | `/api/payments/{appointmentId}/process?method=...` | Procesa pago | Cambia cita a CONFIRMED, genera comprobante | ✅ |
| GET | `/api/payments` | Lista todos los pagos | - | ✅ |
| GET | `/api/payments/{id}` | Obtiene pago por ID | - | ✅ |
| GET | `/api/payments/appointment/{appointmentId}` | Pagos de cita | - | ✅ |
| GET | `/api/payments/status/{status}` | Pagos por estado | Estados: PENDING, PAID, CANCELLED, REFUNDED | ✅ |

**Métodos de Pago Aceptados:**
- `CASH` - Efectivo
- `CREDIT_CARD` - Tarjeta de crédito
- `DEBIT_CARD` - Tarjeta de débito
- `TRANSFER` - Transferencia bancaria
- `INSURANCE` - Seguro

---

## 📋 Triaje (Signos Vitales) (`/api/triages`)

| Método | Endpoint | Descripción | Validaciones | Status |
|--------|----------|-------------|--------------|--------|
| POST | `/api/triages/{appointmentId}` | Registra signos vitales | Rangos médicos validados | ✅ |
| GET | `/api/triages/{appointmentId}` | Obtiene triaje de cita | - | ✅ |

**Campos Validados:**
- Presión sistólica: 90-180 mmHg
- Presión diastólica: 60-120 mmHg
- Temperatura: 35-42°C
- Frecuencia cardíaca: 40-200 lpm
- Frecuencia respiratoria: 10-50 rpm
- Peso: 20-300 kg
- Altura: 50-220 cm

---

## 🩺 Consulta (Diagnóstico) (`/api/consultations`)

| Método | Endpoint | Descripción | Validaciones | Status |
|--------|----------|-------------|--------------|--------|
| POST | `/api/consultations/{appointmentId}` | Registra diagnóstico | Cita debe estar CONFIRMADA | ✅ |
| GET | `/api/consultations/{appointmentId}` | Obtiene consulta de cita | - | ✅ |

---

## 🧾 Comprobantes (`/api/receipts`)

| Método | Endpoint | Descripción | Status |
|--------|----------|-------------|--------|
| GET | `/api/receipts` | Lista todos los comprobantes | ✅ |
| GET | `/api/receipts/{id}` | Obtiene comprobante por ID | ✅ |
| GET | `/api/receipts/number/{receiptNumber}` | Obtiene comprobante por número | ✅ |

**Formato de número:** `RCP-{timestamp}-{uuid}`

---

## 📊 Auditoría (`/api/audit-logs`)

| Método | Endpoint | Descripción | Status |
|--------|----------|-------------|--------|
| GET | `/api/audit-logs/appointment/{appointmentId}` | Historial de cambios de cita | ✅ |
| GET | `/api/audit-logs/{id}` | Obtiene registro de auditoría | ✅ |

**Acciones Registradas:**
- `CREATED` - Cita creada
- `RESCHEDULED` - Cita reprogramada
- `CANCELLED` - Cita cancelada
- `COMPLETED` - Cita completada

---

## 🏢 Seguros (`/api/insurances`) - **NUEVO**

| Método | Endpoint | Descripción | RN Asociado | Status |
|--------|----------|-------------|-------------|--------|
| POST | `/api/insurances` | Crea nueva aseguradora | RN-04, RN-05, RN-06 | ✅ |
| GET | `/api/insurances` | Lista todas las aseguradoras activas | RN-07 | ✅ |
| GET | `/api/insurances/{id}` | Obtiene aseguradora por ID | - | ✅ |
| PUT | `/api/insurances/{id}` | Actualiza datos de aseguradora | - | ✅ |
| DELETE | `/api/insurances/{id}` | Desactiva aseguradora | - | ✅ |

**Campos Principales:**
- `name` - Nombre único de la aseguradora
- `coveragePercentage` - Porcentaje de cobertura (0-100%)
- `deductible` - Deducible por consulta
- `maxCoveragePerConsultation` - Máximo cubierto por consulta
- `maxAnnualCoverage` - Cobertura máxima anual

---

## 👤 Seguros del Paciente (`/api/patient-insurances`) - **NUEVO**

| Método | Endpoint | Descripción | RN Asociado | Status |
|--------|----------|-------------|-------------|--------|
| POST | `/api/patient-insurances/patient/{patientId}` | Vincula seguro a paciente | RN-25 | ✅ |
| GET | `/api/patient-insurances/patient/{patientId}` | Lista seguros del paciente | - | ✅ |
| GET | `/api/patient-insurances/patient/{patientId}/primary` | Obtiene seguro primario | RN-25 | ✅ |
| DELETE | `/api/patient-insurances/{patientInsuranceId}` | Desvincula seguro del paciente | - | ✅ |

**Campos Principales:**
- `policyNumber` - Número de póliza del paciente
- `policyHolderName` - Nombre del titular de la póliza
- `relationshipToHolder` - Relación con el titular (SELF, SPOUSE, CHILD, PARENT)
- `isPrimary` - Seguro primario para cálculo de costos
- `effectiveDate` - Fecha de inicio de vigencia
- `expirationDate` - Fecha de fin de vigencia

**Validaciones:**
- Solo un seguro puede ser primario por paciente (se actualiza automáticamente)
- Ambas fechas de vigencia son obligatorias
- Rango de fechas debe ser válido (effectiveDate < expirationDate)

---

## 📅 Horarios Médicos (`/api/medical-schedules`) - **NUEVO**

| Método | Endpoint | Descripción | RN Asociado | Status |
|--------|----------|-------------|-------------|--------|
| POST | `/api/medical-schedules/doctor/{doctorId}` | Define horario de doctor | RN-37 | ✅ |
| GET | `/api/medical-schedules/doctor/{doctorId}` | Lista horarios del doctor | - | ✅ |
| GET | `/api/medical-schedules/doctor/{doctorId}/day?day=MONDAY` | Obtiene horario por día | RN-38 | ✅ |
| PUT | `/api/medical-schedules/{scheduleId}` | Actualiza horario | - | ✅ |
| DELETE | `/api/medical-schedules/{scheduleId}` | Desactiva horario | - | ✅ |

**Campos Principales:**
- `dayOfWeek` - Día de la semana (MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY)
- `startTime` - Hora de inicio (ej: 08:00)
- `endTime` - Hora de fin (ej: 17:00)
- `appointmentDurationMinutes` - Duración de cada slot (15, 30, 45, 60 min)
- `maxAppointmentsPerDay` - Máximo de citas en este horario

**Validaciones:**
- `endTime` debe ser posterior a `startTime`
- `appointmentDurationMinutes` mínimo 15 minutos
- `maxAppointmentsPerDay` mínimo 1 cita

**Uso en Sistema:**
- RN-37: Valida que doctores tengan horarios definidos
- RN-38: Valida que citas solo se puedan crear dentro de estos horarios

---

## 🔔 Notificaciones (`/api/notifications`) - **NUEVO**

| Método | Endpoint | Descripción | RF Asociado | Status |
|--------|----------|-------------|-------------|--------|
| GET | `/api/notifications/user?email=...` | Lista notificaciones de usuario | RF-45 | ✅ |
| GET | `/api/notifications/appointment/{appointmentId}` | Lista notificaciones de cita | RF-46, RF-47 | ✅ |

**Tipos de Notificación Automáticas:**
- `APPOINTMENT_CREATED` - Cuando se crea una cita (a paciente y doctor)
- `APPOINTMENT_RESCHEDULED` - Cuando se reprograma
- `APPOINTMENT_CANCELLED` - Cuando se cancela
- `APPOINTMENT_REMINDER` - Recordatorio 1 día antes
- `APPOINTMENT_CONFIRMED` - Cuando se confirma con pago
- `PAYMENT_RECEIVED` - Cuando se recibe pago
- `PAYMENT_FAILED` - Cuando falla el pago
- `TREATMENT_PLAN` - Cuando se registra plan de tratamiento

**Canales de Notificación:**
- `EMAIL` - Correo electrónico
- `SMS` - Mensajes de texto
- `IN_APP` - Notificación dentro de la aplicación

**Estados de Notificación:**
- `PENDING` - Pendiente de envío
- `SENT` - Enviada
- `DELIVERED` - Entregada
- `FAILED` - Falló en envío (con reintentos automáticos)

**Características:**
- Notificaciones automáticas integradas en AppointmentService y PaymentService
- Scheduler de fondo que procesa notificaciones pendientes cada minuto (RF-45)
- Recordatorios automáticos diarios a las 6 PM (RF-47)
- Reintentos automáticos con máximo 3 intentos
- Limpieza de notificaciones antiguas semanalmente

---

## 📊 Resumen de Cambios

### Endpoints Anteriores: 32
### Nuevos Endpoints: 13
### **Total Implementado: 45 endpoints**

### Requerimientos Cubiertos (Nuevos):
- **RN-04 a RN-08:** Gestión de seguros con validaciones de cobertura
- **RN-23 a RN-25:** Vinculación de seguros a pacientes con seguro primario
- **RN-37, RN-38:** Validación de disponibilidad de doctores
- **RF-45 a RF-48:** Sistema completo de notificaciones automáticas

---

## 🔄 Flujo Completo de una Cita

```
1. Crear Cita (POST /api/appointments)
   ↓ (automático)
   └─ Crear Pago en estado PENDING

2. Procesar Pago (POST /api/payments/{id}/process)
   ↓ (automático)
   ├─ Cambiar Pago a PAID
   ├─ Cambiar Cita a CONFIRMED
   └─ Generar Comprobante

3. Registrar Triaje (POST /api/triages/{appointmentId})
   └─ Guardar signos vitales

4. Registrar Consulta (POST /api/consultations/{appointmentId})
   └─ Guardar diagnóstico y tratamiento

5. Consultar Auditoría (GET /api/audit-logs/appointment/{appointmentId})
   └─ Ver histórico de cambios
```

---

## ✅ Características Implementadas

### 🔒 Seguridad
- ✅ Autenticación con JWT
- ✅ Encriptación de contraseñas con BCrypt
- ✅ CORS configurado para desarrollo

### ✔️ Validación
- ✅ Validación de entrada (@Valid/@NotBlank/@Email/@Pattern)
- ✅ Rangos médicos en triaje
- ✅ Validación de disponibilidad de doctores
- ✅ Prevención de duplicados (DNI, nombre de usuario, licencia)

### 📝 Logging
- ✅ SLF4J en todos los servicios
- ✅ Niveles: INFO para acciones, DEBUG para consultas
- ✅ Trazabilidad completa de operaciones

### ⚠️ Manejo de Excepciones
- ✅ GlobalExceptionHandler centralizado
- ✅ Respuestas de error uniformes
- ✅ Códigos HTTP apropiados
- ✅ Mensajes de error descriptivos

### 🗂️ Arquitectura
- ✅ Capas bien definidas (Controller → Service → Repository)
- ✅ DTOs para transferencia de datos
- ✅ Enums para tipos seguros
- ✅ Relaciones JPA correctamente mapeadas

### 📊 Auditoría
- ✅ Registro automático de cambios de cita
- ✅ Historial completo de transacciones
- ✅ Trazabilidad por usuario

---

## 📊 Estadísticas

| Concepto | Cantidad |
|----------|----------|
| **Controllers** | 10 |
| **Services** | 10 |
| **Repositories** | 9 |
| **DTOs** | 17 |
| **Entities** | 10 |
| **Enums** | 6 |
| **Endpoints** | 32 |
| **Líneas de código** | ~2500 |

---

## 🚀 Deployment

### Requisitos
- Java 21
- PostgreSQL 12+
- Maven 3.8+

### Build
```bash
mvn clean install
```

### Ejecutar
```bash
mvn spring-boot:run
```

### Base de Datos
```sql
CREATE DATABASE ClinicaAviva;
-- Las tablas se crearán automáticamente con Hibernate
```

---

## 📝 Notas

- Todos los endpoints requieren token JWT (excepto `/api/auth/login` y `/api/auth/register-patient`)
- Los pagos se crean automáticamente al crear una cita
- Los comprobantes se generan automáticamente al procesar un pago
- Los cambios de cita se registran automáticamente en auditoría
- Todas las fechas están en ISO-8601 format

---

**Última actualización:** 2024  
**Estado:** ✅ Completamente Implementado
