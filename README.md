# 🏥 Aviva Management System

Sistema de gestión de citas para Clínica Aviva, con una API Spring Boot y
aplicaciones React separadas para el personal y los pacientes.

## Aplicación desplegada

- [Portal del paciente](https://patient-portal-aviva.vercel.app)
- [Panel del personal](https://staff-management-aviva.vercel.app)
- [API backend](https://avivamanagementsystem.onrender.com)
- [Documentación Swagger](https://avivamanagementsystem.onrender.com/swagger-ui.html)

El panel del personal presenta las funciones de recepción o administración de
acuerdo con el rol incluido en el token JWT.

## Alcance

El sistema permite gestionar pacientes, médicos, especialidades, horarios,
citas, seguros, pagos y notificaciones. Los registros principales se
desactivan de forma lógica para conservar la trazabilidad.

El portal paciente permite:

- Registrar una cuenta o activar un paciente existente mediante DNI y código
  enviado por correo.
- Vincular una póliza opcional.
- Consultar disponibilidad, crear, reprogramar y cancelar citas propias.
- Revisar pagos, registrar pagos permitidos y descargar constancias PDF.
- Consultar notificaciones internas.

La información clínica, diagnósticos, triaje y resultados médicos no se
exponen en el portal paciente.

## Tecnologías

- Java 21 y Spring Boot 4.
- Spring Security con JWT.
- PostgreSQL y Spring Data JPA.
- React 19, Vite 8, React Router, TanStack Query y Axios.
- Tailwind CSS y componentes shadcn/ui en el portal paciente.
- Docker para el backend, Render para API/PostgreSQL y Vercel para los
  frontends.

## Estructura

```text
AvivaManagementSystem/
├── backend/appointmentsystem/  # API Spring Boot y Dockerfile
├── frontend-react/             # Panel de recepción y administración
├── frontend-portal/            # Portal del paciente
├── CAMBIOS_PORTAL_PACIENTE.md  # Registro de implementación
└── EndpointsNecesarios.md      # Inventario de endpoints de la API
```

## Ejecución local

Backend:

```bash
cd backend/appointmentsystem
mvn spring-boot:run
```

Frontend del personal:

```bash
cd frontend-react
npm ci
npm run dev
```

Portal paciente:

```bash
cd frontend-portal
npm ci
npm run dev
```

Direcciones locales predeterminadas:

| Servicio | URL |
|---|---|
| Backend | `http://localhost:8080` |
| Panel del personal | `http://localhost:5173` |
| Portal paciente | `http://localhost:5174` |

## Configuración

El backend utiliza variables de entorno para PostgreSQL, JWT, correo y CORS.
Los frontends requieren:

```env
VITE_API_URL=http://localhost:8080/api
```

En producción, esta variable debe apuntar a:

```env
VITE_API_URL=https://avivamanagementsystem.onrender.com/api
```

No se deben guardar credenciales, contraseñas, claves JWT ni claves de Brevo
en el repositorio.

## Documentación

- [Endpoints de la API](EndpointsNecesarios.md)
- [Registro de cambios del portal](CAMBIOS_PORTAL_PACIENTE.md)
- [Guía del portal paciente](frontend-portal/README.md)
