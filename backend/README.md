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

Al arrancar, el `DataInitializer` crea automáticamente los usuarios de prueba:

| username | password | rol |
|----------|----------|-----|
| `admin` | `admin123` | ADMIN |
| `doctor1` | `doctor123` | DOCTOR |
| `recepcion1` | `recep123` | RECEPTIONIST |
| `paciente1` | `paciente123` | PATIENT |

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

## 📡 Endpoints disponibles (45 en total)

### Autenticación (pública)
```
POST /api/auth/login
POST /api/auth/register-patient
```

### Gestión de catálogos
```
GET/POST/PUT/DELETE /api/specialties
GET/POST/PUT/DELETE /api/doctors
GET/POST/PUT/DELETE /api/patients
GET/POST/PUT/DELETE /api/medical-schedules
```

### Citas médicas
```
GET/POST           /api/appointments
GET/PUT            /api/appointments/{id}
POST               /api/appointments/{id}/confirm
POST               /api/appointments/{id}/cancel
POST               /api/appointments/{id}/reschedule
POST               /api/appointments/{id}/complete
POST               /api/appointments/{id}/no-show
```

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
```

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
