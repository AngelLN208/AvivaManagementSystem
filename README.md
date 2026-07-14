# 🏥 Aviva Management System

Sistema de gestión de citas para Clínica Aviva, con backend Spring Boot y
frontends React separados para el personal y los pacientes.

## Alcance

El sistema permite al personal gestionar citas, pacientes, médicos, horarios,
pagos, notificaciones y procesos de atención según su rol. El portal del
paciente permite autorregistro y autogestión de citas propias.

En esta fase el portal paciente **no incluye** pagos, información clínica,
diagnósticos, triaje, seguros ni resultados médicos.

## Tecnologías

- Java 21 y Spring Boot 4.0.6.
- Spring Security con JWT.
- PostgreSQL en desarrollo/producción y H2 aislado para pruebas.
- React 19, Vite 8, React Router, TanStack Query y Axios.
- Maven y npm.

## Estructura

```text
AvivaManagementSystem/
├── backend/appointmentsystem/ # API Spring Boot
├── frontend-react/            # Aplicación React del staff/recepción
├── frontend-portal/           # Aplicación React del paciente
├── frontend/                  # Prototipo HTML/JS legado
└── CAMBIOS_PORTAL_PACIENTE.md # Registro de implementación del portal
```

## Ejecución local

Backend:

```bash
cd backend/appointmentsystem
mvn spring-boot:run
```

Frontend del staff:

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

Por defecto, staff usa `http://localhost:5173`, portal paciente
`http://localhost:5174` y backend `http://localhost:8080`.

Consulta [frontend-portal/README.md](frontend-portal/README.md) para las rutas,
configuración, verificación y despliegue del portal paciente.
