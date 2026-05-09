# 🔧 Backend - Aviva Management System

Servidor REST API desarrollado con Spring Boot para gestión de citas médicas.

## 📋 Requisitos

- **Java 21** o superior
- **Maven 3.6+**
- **Git**

## 🚀 Iniciar el Backend

```bash
# Navegar a la carpeta del backend
cd appointmentsystem

# Compilar el proyecto
mvn clean install

# Ejecutar el servidor
mvn spring-boot:run
```

El servidor estará disponible en: **http://localhost:8080**

## 🏗️ Estructura

```
appointmentsystem/
├── pom.xml                          # Configuración Maven
├── src/
│   ├── main/
│   │   ├── java/com/aviva/appointmentsystem/
│   │   │   ├── AppointmentsystemApplication.java    # Clase principal
│   │   │   ├── controller/                          # Controladores REST
│   │   │   ├── dto/                                 # Data Transfer Objects
│   │   │   ├── entity/                              # Entidades JPA
│   │   │   ├── repository/                          # Repositorios
│   │   ├── security/                                # Configuración seguridad
│   │   └── service/                                 # Lógica de negocio
│   │   └── resources/
│   │       └── application.properties               # Configuración app
│   └── test/                                        # Tests unitarios
└── target/                                          # Archivos compilados
```

## ⚙️ Configuración

Editar `src/main/resources/application.properties`:

```properties
# Puerto
server.port=8080

# Base de datos
spring.datasource.url=jdbc:mysql://localhost:3306/appointmentsystem
spring.datasource.username=root
spring.datasource.password=

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# JWT
jwt.secret=your-secret-key-here
jwt.expiration=86400000
```

## 📡 API Endpoints

### Autenticación
```
POST /api/auth/login
POST /api/auth/logout
GET  /api/auth/verify
```

### Pacientes
```
GET    /api/pacientes              # Listar todos
GET    /api/pacientes/{id}         # Obtener uno
POST   /api/pacientes              # Crear
PUT    /api/pacientes/{id}         # Actualizar
DELETE /api/pacientes/{id}         # Eliminar
```

### Doctores
```
GET    /api/doctores
GET    /api/doctores/{id}
POST   /api/doctores
PUT    /api/doctores/{id}
DELETE /api/doctores/{id}
```

### Especialidades
```
GET    /api/especialidades
GET    /api/especialidades/{id}
POST   /api/especialidades
PUT    /api/especialidades/{id}
DELETE /api/especialidades/{id}
```

### Citas
```
GET    /api/citas
GET    /api/citas/{id}
POST   /api/citas
PUT    /api/citas/{id}
DELETE /api/citas/{id}
```

### Pagos
```
GET    /api/pagos
GET    /api/pagos/{id}
POST   /api/pagos
PUT    /api/pagos/{id}
DELETE /api/pagos/{id}
```

## 🔐 Seguridad

- **CORS:** Habilitado para `http://localhost:3000` y `http://localhost:8080`
- **JWT:** Autenticación con tokens
- **Spring Security:** Protección de endpoints
- **CSRF:** Deshabilitado en desarrollo

Archivo: `src/main/java/com/aviva/appointmentsystem/security/SecurityConfig.java`

## 🗄️ Base de Datos

El proyecto usa Hibernate JPA para gestión de base de datos.

### Entidades principales
- `Usuario` - Usuarios del sistema
- `Paciente` - Pacientes
- `Doctor` - Médicos
- `Especialidad` - Especialidades médicas
- `Cita` - Citas médicas
- `Pago` - Pagos

## 🧪 Testing

```bash
# Ejecutar tests
mvn test

# Con cobertura
mvn clean test jacoco:report
```

## 📦 Build para Producción

```bash
# Crear JAR ejecutable
mvn clean package

# El JAR se crea en: target/appointmentsystem-0.0.1-SNAPSHOT.jar

# Ejecutar JAR
java -jar target/appointmentsystem-0.0.1-SNAPSHOT.jar
```

## 🔗 Comunicación con Frontend

El frontend en `../../frontend/` se comunica con este backend a través de:

```javascript
// Configurado en frontend/js/config.js
const API_CONFIG = {
    BASE_URL: 'http://localhost:8080',
    API_PATH: '/api'
};
```

## 🐛 Debugging

### Logs
```bash
# Habilitar debug mode
mvn spring-boot:run -Dspring-boot.run.arguments="--debug"
```

### Conectar Debugger
1. Ejecutar: `mvn spring-boot:run -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"`
2. En tu IDE, adjuntar debugger al puerto 5005

## 📚 Dependencias Principales

```xml
<!-- Spring Boot -->
<groupId>org.springframework.boot</groupId>

<!-- Spring Security -->
<groupId>org.springframework.security</groupId>

<!-- JWT -->
<groupId>io.jsonwebtoken</groupId>
<artifactId>jjwt-*</artifactId>
<version>0.12.6</version>

<!-- Database -->
<groupId>mysql</groupId>
<artifactId>mysql-connector-java</artifactId>
```

Ver `pom.xml` para la lista completa.

## 🤝 Contribuir

1. Crear rama feature: `git checkout -b feature/nueva-funcionalidad`
2. Hacer cambios y tests
3. Commit: `git commit -m "Add nueva funcionalidad"`
4. Push: `git push origin feature/nueva-funcionalidad`
5. Pull Request

## 📝 Notas

- Usar Java 21+ para compatibilidad
- Seguir convenciones de Spring Boot
- Agregar tests para nuevos endpoints
- Documentar cambios en API

---

**Última actualización:** Mayo 2026
