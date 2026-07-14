# 📋 Guía Técnica del Backend — Aviva Medical Management System

Documentación técnica detallada de la arquitectura, reglas de negocio, DTOs y decisiones de diseño del backend.
Para el inicio rápido, ver [`../README.md`](../README.md).

---

## 🏗️ Estructura del proyecto

```
appointmentsystem/
├── src/main/java/com/aviva/appointmentsystem/
│   ├── AppointmentsystemApplication.java   ← Punto de entrada Spring Boot
│   ├── config/
│   │   ├── DataInitializer.java            ← Seed de usuarios (ApplicationRunner)
│   │   └── OpenApiConfig.java              ← Configuración Swagger / JWT bearer
│   ├── controller/                         ← Capa HTTP: recibe DTOs, devuelve DTOs
│   │   ├── AuthController.java
│   │   ├── SpecialtyController.java
│   │   ├── PatientController.java
│   │   ├── DoctorController.java
│   │   ├── MedicalScheduleController.java
│   │   ├── AppointmentController.java
│   │   ├── PaymentController.java
│   │   ├── ReceiptController.java
│   │   ├── TriageController.java
│   │   ├── ConsultationController.java
│   │   └── AuditController.java
│   ├── service/                            ← Lógica de negocio y reglas RN
│   │   ├── AuthService.java
│   │   ├── SpecialtyService.java
│   │   ├── PatientService.java
│   │   ├── DoctorService.java
│   │   ├── MedicalScheduleService.java
│   │   ├── AppointmentService.java
│   │   ├── PaymentService.java
│   │   ├── ReceiptService.java
│   │   ├── TriageService.java
│   │   ├── ConsultationService.java
│   │   ├── AuditService.java
│   │   └── NotificationService.java
│   ├── repository/                         ← Interfaces JPA (Spring Data)
│   ├── entity/                             ← Entidades JPA (@Entity)
│   ├── dto/                                ← Records de entrada (*Request) y salida (*Response)
│   ├── exception/                          ← Excepciones personalizadas + GlobalExceptionHandler
│   └── security/                           ← JWT filter, JwtUtil, SecurityConfig
├── src/main/resources/
│   └── application.properties
└── pom.xml
```

---

## 📐 Reglas arquitectónicas (metodología UTP)

| Regla | Descripción |
|-------|-------------|
| **Flujo de capas** | `JSON → Controller → Service → Repository → PostgreSQL`. Nunca saltar capas. |
| **DTOs obligatorios** | Los controllers NUNCA reciben ni devuelven entidades JPA. Siempre `*Request` / `*Response`. |
| **Constructor injection** | Cero `@Autowired` en campos. Todos los componentes usan inyección por constructor. |
| **Validación en DTOs** | `@NotBlank`, `@Pattern`, `@Future`, `@NotNull`, etc. en todos los `*Request`. |
| **Excepciones tipadas** | `BusinessRuleException` (409), `ResourceNotFoundException` (404), `ValidationException` (400). |
| **Queries JPQL** | Consultas complejas (disponibilidad, solapamiento) vía `@Query` en repositorios, no en streams Java. |

---

## 🔄 Reglas de Negocio implementadas

### Citas (AppointmentService)

| RN | Descripción | Implementación |
|----|-------------|----------------|
| **RN-13** | Estado inicial al crear = `PENDING` | `appointment.setStatus(PENDING)` |
| **RN-21** | Al crear cita se genera `Payment` en `PENDING` automáticamente | `createPaymentForAppointment()` en `AppointmentService` |
| **RN-38** | Fecha/hora de cita debe coincidir con un `MedicalSchedule` activo del doctor | `@Query` JPQL en `MedicalScheduleRepository` |
| **RN-12** | No puede existir otra cita activa en el mismo slot del doctor | `@Query` JPQL de detección de conflicto en `AppointmentRepository` |

### Pagos (PaymentService)

| RN | Descripción | Implementación |
|----|-------------|----------------|
| **RN-26** | Procesar pago → `PENDING → PAID` | Solo `PENDING`; `PAID`, `CANCELLED` y `REFUNDED` devuelven 409 |
| **RN-16** | Pago procesado → Cita `→ CONFIRMED` automáticamente | Solo si la cita está `PENDING` o `RESCHEDULED` |
| **RN-28** | Generar `Receipt` automáticamente al procesar pago | Referencia única `RCP-*` para staff y portal |

> **Nota importante:** `POST /api/payments/{id}/process` recibe el **ID del Payment**, no de la Appointment. Usar `GET /api/payments/appointment/{appointmentId}` primero para obtener el `paymentId`.

### Flujo clínico

| RN | Descripción | Implementación |
|----|-------------|----------------|
| **RN-31** | Solo citas `CONFIRMED` pueden tener consulta | Guard en `ConsultationService.create()` → 409 |
| **RN-33** | Máximo 1 consulta por cita | Guard `consultationRepository.findByAppointmentId().isPresent()` → 409 |
| **RN-42/43** | Audit logs son inmutables, generados automáticamente | `AuditController` sin endpoints de escritura. `AuditService` con `@Transactional(readOnly=true)` |

---

## 📊 Estados de una Cita

```
            ┌─────────────────────────────────────────────┐
            │              PENDING (inicial)               │
            └──────┬─────────────────┬────────────────────┘
                   │                 │
           [pagar] │                 │ [cancelar]
                   ▼                 ▼
            CONFIRMED           CANCELLED
                   │
         ┌─────────┼─────────┐
         │         │         │
    [atender] [no-show] [reprogramar]
         │         │         │
     COMPLETED  NO_SHOW  RESCHEDULED
```

## 💳 Estados de un Pago

```
PENDING  ──[process()]──►  PAID
   │
   └──[cancelar]──►  CANCELLED
```

---

## 🔐 Seguridad

- **JWT Bearer Token** — Todos los endpoints excepto `/api/auth/**` requieren `Authorization: Bearer <token>`.
- **BCrypt** — Contraseñas hasheadas con `BCryptPasswordEncoder` (factor 10).
- **CORS** — Configurado en `SecurityConfig` para desarrollo local.
- **spring.jpa.open-in-view = false** — Recomendado añadir al `application.properties` para evitar el WARN de Hibernate.

---

## 🧩 DTOs principales

### Entradas (Request)
```
LoginRequest           { username, password }
RegisterPatientRequest { username, password, dni, firstName, lastName, gender, dateOfBirth, phone, email, address }
PatientActivationRequest { dni }
PatientActivationVerifyCodeRequest { challengeId, code }
PatientActivationCompleteRequest { challengeId, activationToken, username, password }
PortalPatientInsuranceRequest { insuranceId, policyNumber, policyHolderName, relationshipToHolder, effectiveDate, expirationDate }
PatientPaymentRequest   { method }
AppointmentRequest     { patientId, doctorId, appointmentDateTime, reason }
TriageRequest          { bloodPressureSystolic, bloodPressureDiastolic, temperature, heartRate, respiratoryRate, weight, height, notes }
ConsultationRequest    { diagnosis, treatment, notes }
```

### Salidas (Response) — nunca exposición de entidades JPA
```
LoginResponse          { token, role, username, firstName, lastName }
PaymentResponse        { id, baseAmount, deductibleApplied, insuranceCoveredAmount, amount, status, method, appointmentId, ... }
ReceiptResponse        { id, receiptNumber, description, totalAmount, paymentId, appointmentId, createdAt }
AuditLogResponse       { id, appointmentId, action, newStatus, details, modifiedBy, createdAt }
```

---

## ⚙️ Variables de configuración

```properties
# Base de datos (con defaults para desarrollo local)
spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/clinica_aviva}
spring.datasource.username=${DB_USER:postgres}
spring.datasource.password=${DB_PASSWORD:1234}

# Hibernate
spring.jpa.hibernate.ddl-auto=update    # usar "create" para esquema limpio
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# JWT
jwt.secret=<clave-de-256-bits-minimo>
jwt.expiration=86400000                  # 24h en ms

# Swagger
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.api-docs.path=/v3/api-docs
```

---

## 🌱 Seed de datos (DataInitializer)

Al arrancar, `DataInitializer` (implements `ApplicationRunner`) crea los siguientes usuarios si no existen:

| username | password | rol | Acceso |
|----------|----------|-----|--------|
| `admin` | `admin123` | ADMIN | Todos los endpoints |
| `doctor1` | `doctor123` | DOCTOR | Triaje, consultas, horarios |
| `recepcion1` | `recep123` | RECEPTIONIST | Citas, pagos |
| `paciente1` | `paciente123` | PATIENT | Ver sus citas |

El seed es **idempotente**: funciona con `ddl-auto=update` (no duplica si ya existen).

---

## 🔍 Consultas JPQL relevantes

```java
// Detección de solapamiento de citas (RN-12)
@Query("SELECT COUNT(a) > 0 FROM Appointment a WHERE a.doctor.id = :doctorId " +
       "AND a.appointmentDateTime = :dateTime AND a.status NOT IN ('CANCELLED', 'NO_SHOW')")
boolean existsConflict(@Param("doctorId") Long doctorId, @Param("dateTime") LocalDateTime dateTime);

// Validación de horario activo del doctor (RN-38)
@Query("SELECT COUNT(ms) > 0 FROM MedicalSchedule ms WHERE ms.doctor.id = :doctorId " +
       "AND ms.dayOfWeek = :dayOfWeek AND ms.startTime <= :time AND ms.endTime >= :time AND ms.active = true")
boolean existsActiveSchedule(...);
```

---

*Documentación técnica — Aviva Medical Management System — Fase 5 completada (Junio 2026)*
