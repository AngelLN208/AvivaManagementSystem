# 🏥 Backend — Aviva Medical Management System

REST API construida con **Spring Boot 4.0.6** y **Java 21** para gestionar el ciclo completo de citas médicas: desde el registro de pacientes hasta el cierre clínico (triaje, consulta, pago y auditoría).

---

## ⚡ Inicio rápido

### Pre-requisitos
| Herramienta | Versión mínima |
|-------------|----------------|
| Java | 21 |
| Maven | 3.9+ (incluido vía `./mvnw`) |
| PostgreSQL | 14+ |

### 1. Crear la base de datos
```sql
CREATE DATABASE clinica_aviva;
```

### 2. Ejecutar
```bash
cd appointmentsystem
./mvnw spring-boot:run
```

La API estará disponible en `http://localhost:8080`.

Los datos de demostración están deshabilitados por defecto. Para crearlos en
desarrollo, configura `APP_SEED_ENABLED=true` antes de arrancar. Nunca habilites
esta opción en producción. Cuando está habilitada, `DataInitializer` crea:

| username | password | rol |
|----------|----------|-----|
| `admin` | `admin123` | ADMIN |
| `doctor1` | `doctor123` | DOCTOR |
| `recepcion1` | `recep123` | RECEPTIONIST |
| `paciente1` | `paciente123` | PATIENT |

Desactivar `APP_SEED_ENABLED` no elimina usuarios de demostración creados en
ejecuciones anteriores. Antes de desplegar una base de datos existente en
producción, elimina esas cuentas o cambia y revoca sus credenciales.

---

## 📖 Swagger UI

```
http://localhost:8080/swagger-ui.html
```

Flujo para autenticarse en Swagger:
1. `POST /api/auth/login` → copiar el token del response
2. Botón **Authorize** → pegar `Bearer <token>`

---

## 🗄️ Configuración de base de datos

El archivo `src/main/resources/application.properties` usa variables de entorno con valores por defecto:

```properties
spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/clinica_aviva}
spring.datasource.username=${DB_USER:postgres}
spring.datasource.password=${DB_PASSWORD:1234}
```

Para usar valores distintos sin modificar el archivo:
```bash
DB_URL=jdbc:postgresql://mi-servidor:5432/clinica DB_USER=aviva DB_PASSWORD=segura ./mvnw spring-boot:run
```

---

## 📡 Endpoints disponibles (46 en total)

### Autenticación (pública)
```
POST /api/auth/login
POST /api/auth/register-patient
POST /api/auth/patient-activation/request
POST /api/auth/patient-activation/verify-code
POST /api/auth/patient-activation/complete
```

El registro comienza consultando el DNI. Si no existe, continúa el
autorregistro normal. Si el paciente ya fue creado por el staff y aún no tiene
cuenta, se envía un código de un solo uso al correo que ya está registrado. La
verificación del código entrega un token temporal y un segundo request crea
únicamente el `User` asociado; no duplica el `Patient`.

### Gestión de catálogos
```
GET/POST/PUT/DELETE /api/specialties
GET/POST/PUT/DELETE /api/doctors
GET/POST/PUT/DELETE /api/patients
GET/POST/PUT/DELETE /api/medical-schedules
```

### Citas médicas
```
GET/POST           /api/appointments                         (staff)
GET                /api/appointments/{id}                    (staff)
PUT                /api/appointments/{id}/cancel             (staff)
PUT                /api/appointments/{id}/reschedule         (staff)
GET/POST           /api/appointments/me                      (paciente)
GET                /api/appointments/me/{id}                 (paciente propietario)
PUT                /api/appointments/me/{id}/cancel          (paciente propietario)
PUT                /api/appointments/me/{id}/reschedule      (paciente propietario)
GET                /api/appointments/doctor/{id}/available-slots
```

Los endpoints `/me` identifican al paciente mediante el JWT y nunca reciben
`patientId`. Los endpoints generales conservan su contrato para el portal del
staff y están protegidos por rol.

### Pagos y comprobantes
```
GET                /api/payments
GET                /api/payments/{id}
GET                /api/payments/appointment/{appointmentId}
GET                /api/payments/status/{status}
POST               /api/payments/{id}/process?method=CASH
GET                /api/receipts
GET                /api/receipts/{id}
GET                /api/receipts/number/{receiptNumber}

GET                /api/payments/me                          (paciente propietario)
GET                /api/payments/me/{id}                     (paciente propietario)
POST               /api/payments/me/{id}/pay                 (paciente propietario)
GET                /api/receipts/me                          (paciente propietario)
GET                /api/receipts/me/{id}                     (paciente propietario)
GET                /api/receipts/me/{id}/pdf                 (descarga PDF propia)
GET                /api/receipts/me/payment/{paymentId}      (paciente propietario)
```

`/pay` acepta un JSON como `{"method":"DEBIT_CARD"}`. El servidor resuelve el
paciente, el importe y la cita desde el JWT y el pago persistido; nunca recibe
`patientId`, monto ni números de tarjeta desde el portal.

La descarga PDF se genera en memoria con Apache PDFBox, utiliza los datos
persistidos del recibo y valida ownership antes de producir el archivo. No crea
copias temporales ni altera la entidad histórica `Receipt`.

### Seguro opcional del portal

```text
GET                /api/insurances
GET                /api/patient-insurances/me
POST               /api/patient-insurances/me
DELETE             /api/patient-insurances/me/{id}
```

El portal admite cero o una póliza activa y nunca recibe `patientId`. Una
póliza nueva se toma en cuenta al calcular pagos de citas creadas después de
vincularla; los pagos pendientes ya calculados no se modifican retroactivamente.

### Notificaciones y correo del portal

```text
GET                /api/notifications/me                    (paciente propietario)
PATCH              /api/notifications/me/{id}/read          (paciente propietario)
```

Las notificaciones propias se relacionan mediante la cita y el paciente del
JWT; el cliente no envía un correo para consultar recursos. Los correos se
renderizan desde `src/main/resources/templates/email`, incluyen HTML responsive
y conservan texto plano como respaldo. `PATIENT_PORTAL_URL` define el destino
de los botones incluidos en esos correos.

### Flujo clínico
```
POST/GET           /api/triages/{appointmentId}
POST/GET           /api/consultations/{appointmentId}
```

### Auditoría (solo lectura)
```
GET                /api/audit-logs/appointment/{appointmentId}
GET                /api/audit-logs/{id}
```

---

## 🔄 Flujo de estados de una cita

```
POST /api/appointments
  → Cita PENDING + Payment PENDING (RN-13, RN-21)

POST /api/payments/{paymentId}/process?method=CASH
  → Payment PAID + Cita CONFIRMED + Receipt generado (RN-26, RN-16, RN-28)

POST /api/triages/{appointmentId}
  → Signos vitales registrados

POST /api/consultations/{appointmentId}   ← requiere estado CONFIRMED (RN-31)
  → Diagnóstico y tratamiento registrados

POST /api/appointments/{id}/complete
  → Cita COMPLETED
```

---

## 🏗️ Arquitectura

Flujo estricto de capas:
```
JSON → Controller → Service → Repository → PostgreSQL
```

- **Controladores:** Reciben y devuelven exclusivamente DTOs. Nunca entidades JPA.
- **Servicios:** Contienen toda la lógica de negocio y las reglas de negocio (RNs).
- **Repositorios:** Solo acceso a datos. Consultas complejas vía `@Query` JPQL.
- **Excepciones:** `BusinessRuleException` (409) para violaciones de reglas de negocio, `ResourceNotFoundException` (404), `ValidationException` (400).
- **Inyección:** Constructor injection en todos los componentes (cero `@Autowired`).

---

## 🛠️ Stack tecnológico

| Tecnología | Versión | Uso |
|-----------|---------|-----|
| Java | 21 | Lenguaje |
| Spring Boot | 4.0.6 | Framework principal |
| Spring Security | 7.0.5 | Autenticación / JWT |
| Hibernate ORM | 7.2.12 | ORM / JPA |
| PostgreSQL driver | 42.7.x | Conector DB |
| JJWT | 0.12.6 | Generación de tokens JWT |
| springdoc-openapi | 2.8.8 | Swagger UI / OpenAPI 3 |
| HikariCP | 7.0.2 | Connection pool |

---

## 📦 Build para producción

```bash
./mvnw clean package -DskipTests
java -jar target/appointmentsystem-0.0.1-SNAPSHOT.jar
```

---

## 🐛 Debugging

```bash
# Con debug remoto (puerto 5005)
./mvnw spring-boot:run -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"
```

---

*Última actualización: Junio 2026 — Fase 5 completada (Pagos, Comprobantes, Triaje, Consultas, Auditoría)*
