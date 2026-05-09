# 🏥 Backend - Sistema de Citas Médicas Clínica Aviva

## 📋 Descripción

Backend REST API construido con **Spring Boot 4.0.6** para gestionar un sistema completo de citas médicas. Incluye autenticación JWT, validación robusta, manejo centralizado de excepciones y auditoría de cambios.

## 🎯 Características

- ✅ **32 Endpoints REST** implementados y funcionales
- ✅ **Autenticación JWT** con encriptación BCrypt
- ✅ **Validación robusta** en todas las capas
- ✅ **Manejo centralizado de excepciones** con respuestas uniformes
- ✅ **Logging completo** con SLF4J
- ✅ **Auditoría de cambios** en citas médicas
- ✅ **Tipos seguros** con Enums
- ✅ **Relaciones JPA** correctamente mapeadas
- ✅ **CORS configurado** para desarrollo

## 🏗️ Arquitectura

```
appointmentsystem/
├── src/
│   ├── main/
│   │   ├── java/com/aviva/appointmentsystem/
│   │   │   ├── entity/              (Modelos JPA)
│   │   │   ├── repository/          (Acceso a datos)
│   │   │   ├── service/             (Lógica de negocio)
│   │   │   ├── controller/          (Endpoints REST)
│   │   │   ├── dto/                 (Transfer objects)
│   │   │   ├── exception/           (Excepciones personalizadas)
│   │   │   ├── security/            (JWT y seguridad)
│   │   │   └── AppointmentystemApplication.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/
├── pom.xml
├── mvnw / mvnw.cmd
└── README.md
```

## 🛠️ Tecnologías

| Tecnología | Versión | Propósito |
|-----------|---------|----------|
| Java | 21 | Lenguaje de programación |
| Spring Boot | 4.0.6 | Framework principal |
| Spring Security | 6.0+ | Autenticación y autorización |
| Spring Data JPA | 3.0+ | ORM y persistencia |
| PostgreSQL | 12+ | Base de datos relacional |
| JWT | 0.12.6 | Autenticación sin estado |
| Jakarta Validation | 3.0+ | Validación de entidades |
| SLF4J | Integrado | Logging |
| Maven | 3.8+ | Gestor de dependencias |

## 📦 Dependencias Principales

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.6</version>
</dependency>

<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
</dependency>
```

## 🚀 Instalación y Ejecución

### Requisitos Previos
- Java 21 instalado
- PostgreSQL 12+ instalado y ejecutándose
- Maven 3.8+ instalado

### 1. Clonar el repositorio
```bash
git clone <tu-repositorio>
cd backend/appointmentsystem
```

### 2. Crear base de datos
```bash
psql -U postgres
CREATE DATABASE ClinicaAviva;
\q
```

### 3. Configurar variables de entorno (opcional)
El archivo `application.properties` incluye:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/ClinicaAviva
spring.datasource.username=postgres
spring.datasource.password=admin
spring.jpa.hibernate.ddl-auto=create
jwt.secret=clave_secreta_aviva_2024_muy_larga_para_seguridad_profesional_123456789
jwt.expiration=86400000
```

### 4. Compilar y ejecutar
```bash
# Compilar
mvn clean install

# Ejecutar
mvn spring-boot:run

# Alternativamente, ejecutar JAR generado
java -jar target/appointmentsystem-0.0.1-SNAPSHOT.jar
```

El servidor estará disponible en `http://localhost:8080`

## 📊 Estructura de Capas

### Entity Layer (Models)
- `User` - Usuarios del sistema
- `Patient` - Información de pacientes
- `Doctor` - Información de doctores
- `Specialty` - Especialidades médicas
- `Appointment` - Citas médicas
- `Triage` - Signos vitales
- `Consultation` - Diagnósticos
- `Payment` - Pagos
- `Receipt` - Comprobantes
- `AuditLog` - Auditoría

### Repository Layer (Data Access)
Interfaces Spring Data JPA con métodos de consulta personalizados:
- `UserRepository`
- `PatientRepository` - Busqueda por DNI, nombre
- `DoctorRepository` - Filtrado por especialidad
- `SpecialtyRepository`
- `AppointmentRepository` - Rango de fechas, por estado
- `TriageRepository`
- `ConsultationRepository`
- `PaymentRepository`
- `ReceiptRepository`
- `AuditLogRepository`

### Service Layer (Business Logic)
Servicios transaccionales:
- `AuthService` - Autenticación y generación de JWT
- `PatientService` - Gestión de pacientes
- `DoctorService` - Gestión de doctores
- `SpecialtyService` - Gestión de especialidades
- `AppointmentService` - Gestión de citas con validación de disponibilidad
- `TriageService` - Registro de signos vitales
- `ConsultationService` - Registro de diagnósticos
- `PaymentService` - Procesamiento de pagos
- `ReceiptService` - Gestión de comprobantes
- `AuditService` - Consulta de auditoría

### Controller Layer (REST Endpoints)
10 controladores exponen 32 endpoints REST

### DTO Layer (Data Transfer Objects)
17 DTOs con validación @Valid:
- Request DTOs - Validación de entrada
- Response DTOs - Serialización de salida

## 🔐 Autenticación

### Flow JWT
```
1. POST /api/auth/login
   ├─ Username + Password
   └─ Retorna JWT Token

2. GET /api/cualquier-recurso
   ├─ Headers: Authorization: Bearer <token>
   └─ Token validado en JwtUtil
```

### Seguridad
- Tokens con expiración (24 horas por defecto)
- Contraseñas encriptadas con BCrypt
- CORS configurado
- CSRF deshabilitado (API stateless)

## ✅ Validación

### Niveles de Validación
1. **DTO Level** - @Valid annotations
2. **Service Level** - Validaciones de negocio
3. **Database Level** - Unique constraints

### Ejemplos
```java
// Email válido
@Email
private String email;

// Tamaño permitido
@Size(min=3, max=50)
private String firstName;

// Patrón de expresión regular
@Pattern(regex = "^[0-9]{8,12}$")
private String dni;

// Valor positivo
@DecimalMin("0.01")
private BigDecimal amount;
```

## 🔄 Flujo de Cita Médica

### Caso de Uso: Cita con Triaje, Consulta y Pago

```
1. Paciente se registra (POST /api/auth/register-patient)
   ↓
2. Crear cita (POST /api/appointments)
   → Validar disponibilidad doctor
   → Crear pago automático (PENDING)
   ↓
3. Procesar pago (POST /api/payments/{id}/process)
   → Cambiar cita a CONFIRMED
   → Generar comprobante
   ↓
4. Registrar triaje (POST /api/triages/{appointmentId})
   → Validar rangos médicos
   ↓
5. Registrar consulta (POST /api/consultations/{appointmentId})
   → Validar que cita esté CONFIRMED
   ↓
6. Ver historial (GET /api/audit-logs/appointment/{appointmentId})
   → Todos los cambios registrados
```

## 📋 Ejemplos de Uso

### 1. Registrar Paciente
```bash
curl -X POST http://localhost:8080/api/auth/register-patient \
  -H "Content-Type: application/json" \
  -d '{
    "dni": "12345678",
    "firstName": "Juan",
    "lastName": "Pérez",
    "gender": "MALE",
    "dateOfBirth": "1990-01-15",
    "phone": "+34666777888",
    "email": "juan@example.com",
    "address": "Calle Principal 123"
  }'
```

### 2. Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "juan@example.com",
    "password": "password123"
  }'
```

### 3. Crear Cita (requiere token)
```bash
curl -X POST http://localhost:8080/api/appointments \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "patientId": 1,
    "doctorId": 1,
    "appointmentDateTime": "2024-12-20T10:30:00",
    "reason": "Consulta de rutina"
  }'
```

### 4. Procesar Pago
```bash
curl -X POST http://localhost:8080/api/payments/1/process?method=CREDIT_CARD \
  -H "Authorization: Bearer <token>"
```

### 5. Registrar Triaje
```bash
curl -X POST http://localhost:8080/api/triages/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "bloodPressureSystolic": 120,
    "bloodPressureDiastolic": 80,
    "temperature": 36.5,
    "heartRate": 72,
    "respiratoryRate": 16,
    "weight": 75.5,
    "height": 180.0,
    "notes": "Paciente sin síntomas"
  }'
```

## 📊 Respuestas API

### Success Response (200 OK)
```json
{
  "success": true,
  "data": {
    "id": 1,
    "firstName": "Juan",
    "lastName": "Pérez",
    ...
  },
  "message": "Paciente obtenido"
}
```

### Error Response (400 Bad Request)
```json
{
  "success": false,
  "code": "VALIDATION_ERROR",
  "message": "Error de validación",
  "status": 400,
  "timestamp": "2024-12-20T10:00:00",
  "fieldErrors": {
    "email": "must be a well-formed email address"
  }
}
```

## 📝 Logging

### Niveles de Log
- `DEBUG` - Consultas y detalles técnicos
- `INFO` - Operaciones importantes (crear, actualizar, eliminar)
- `WARN` - Eventos anormales (validación fallida)
- `ERROR` - Errores de aplicación

### Configuración (application.properties)
```properties
logging.level.root=INFO
logging.level.com.aviva.appointmentsystem=DEBUG
logging.pattern.console=%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n
```

## 🧪 Testing

### Próximas mejoras
- Unit tests para Services
- Integration tests para Controllers
- Test fixtures y builders

## 📚 Documentación de Endpoints

Ver [ENDPOINTS_IMPLEMENTADOS.md](./ENDPOINTS_IMPLEMENTADOS.md) para lista completa de 32 endpoints.

## 🤝 Contribución

1. Crear rama feature: `git checkout -b feature/nueva-funcionalidad`
2. Commit cambios: `git commit -m 'Agregada nueva funcionalidad'`
3. Push a rama: `git push origin feature/nueva-funcionalidad`
4. Crear Pull Request

## 📄 Licencia

Proyecto privado para Clínica Aviva.

## 👥 Autor

Desarrollado por el equipo de desarrollo

## 📞 Soporte

Para problemas o preguntas, contacta al equipo de desarrollo.

---

**Última actualización:** Diciembre 2024  
**Estado:** ✅ Production Ready
