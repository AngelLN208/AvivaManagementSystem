# Portal de pacientes Aviva

Aplicación React/Vite separada del frontend del personal. Permite que una
cuenta con rol `PATIENT` active o cree su acceso, gestione sus citas, registre
un seguro opcional y consulte pagos y constancias propias.

## Alcance actual

- Activación segura de pacientes existentes mediante DNI, código enviado al
  correo que ya consta en la clínica y creación posterior de credenciales. El
  código no crea la cuenta: entrega un grant temporal que vive solo en memoria
  de React. La activación final crea solamente el `User` y conserva el perfil,
  las citas, los seguros y los pagos existentes.
- Alta completa para un DNI que todavía no corresponde a un paciente.
- Inicio de sesión exclusivo para `PATIENT`.
- Registro opcional de cero o una póliza principal desde una ruta `/me`.
- Inicio con resumen y próxima cita.
- Creación, listado, reprogramación y cancelación de citas propias.
- Catálogo de especialidades, médicos activos y horarios disponibles.
- Consulta y registro de pagos propios con tarjeta de crédito o débito.
- Consulta, impresión y descarga PDF de constancias de pago propias.
- Campana con contador, historial y lectura de notificaciones internas propias.
- Diseño responsive con Tailwind CSS, componentes locales de shadcn/ui, modo
  oscuro y estados accesibles de carga/error.

El seguro es opcional. La cobertura se calcula cuando se crea una cita; agregar
o retirar una póliza no recalcula pagos que ya fueron generados.

No se muestra información clínica, diagnósticos, triaje, tratamientos ni datos
de otros pacientes.

## Desarrollo local

Requisitos:

- Node.js `^20.19.0` o `>=22.12.0`.
- Backend Aviva ejecutándose en `http://localhost:8080`.

```bash
cd frontend-portal
npm ci
npm run dev
```

El portal se abre en `http://localhost:5174`. Vite reenvía `/api` al backend
local, por lo que puede ejecutarse simultáneamente con `frontend-react` en el
puerto 5173.

En Windows PowerShell con ejecución de scripts restringida pueden usarse los
equivalentes `npm.cmd test`, `npm.cmd run lint` y `npm.cmd run build`.

## Configuración

El valor predeterminado de la API es `/api`. Para usar un backend remoto, crea
un archivo `.env` basado en `.env.example`:

```text
VITE_API_URL=https://backend.ejemplo.com/api
```

Cuando frontend y backend estén en dominios distintos, el dominio público del
portal debe agregarse a `CORS_ALLOWED_ORIGINS` del backend.

### Despliegue en Vercel

- **Root Directory:** `frontend-portal`
- **Build Command:** `npm run build`
- **Output Directory:** `dist`
- **Variable de build:** `VITE_API_URL=https://backend-publico.example/api`

La URL debe usar HTTPS y terminar en `/api`. Si se omite `VITE_API_URL`, Vercel
tratará `/api` como una ruta del SPA y el portal no podrá comunicarse con el
backend.

## Rutas

| Ruta | Acceso | Función |
|---|---|---|
| `/login` | Público | Inicio de sesión exclusivo para `PATIENT` |
| `/registro` | Público | Consulta DNI, crea paciente nuevo o activa un perfil existente |
| `/` | `PATIENT` | Resumen de citas |
| `/citas` | `PATIENT` | Próximas citas e historial |
| `/agendar` | `PATIENT` | Selección de especialidad, médico, fecha y horario |
| `/medicos` | `PATIENT` | Catálogo de médicos activos |
| `/seguro` | `PATIENT` | Consulta, registro y desvinculación del seguro propio |
| `/pagos` | `PATIENT` | Pagos y constancias propias |
| `/notificaciones` | `PATIENT` | Avisos internos de citas y pagos |

Después de un alta o activación exitosa, el portal dirige a
`/seguro?onboarding=1`. El paciente puede registrar una póliza o elegir
**Omitir por ahora**.

## Contratos consumidos

```text
POST   /api/auth/login
POST   /api/auth/register-patient
POST   /api/auth/patient-activation/request
POST   /api/auth/patient-activation/verify-code
POST   /api/auth/patient-activation/complete

GET    /api/appointments/me
POST   /api/appointments/me
PUT    /api/appointments/me/{id}/cancel
PUT    /api/appointments/me/{id}/reschedule

GET    /api/insurances
GET    /api/patient-insurances/me
POST   /api/patient-insurances/me
DELETE /api/patient-insurances/me/{id}

GET    /api/payments/me
POST   /api/payments/me/{id}/pay
GET    /api/receipts/me
GET    /api/receipts/me/{id}/pdf

GET    /api/notifications/me
PATCH  /api/notifications/me/{id}/read
```

Ninguna operación propia envía `patientId`; el backend resuelve la identidad
desde el JWT y valida la propiedad del recurso.

## Sesión y seguridad del cliente

La sesión usa la clave aislada `aviva.portal.session`; no comparte las claves
del frontend del personal. El token se rechaza si está vencido o si su rol no
es `PATIENT`, y una respuesta autenticada `401` limpia la sesión. Los errores
de endpoints públicos `/auth/**` no se confunden con el vencimiento de una
sesión.

Las cachés de citas, seguros, pagos, constancias y notificaciones se separan por username y se
limpian al cambiar de sesión.

## Verificación

```bash
npm test
npm run lint
npm run build
```

Las pruebas unitarias cubren sesión/JWT, aislamiento de caché, activación,
normalización de DNI/código, seguros, pagos, identificadores, fechas, horarios
y clasificación de citas.

## Estructura principal

```text
src/
├── api/            # Cliente HTTP y contratos permitidos al paciente
├── assets/         # Identidad visual y logo oficial de Aviva
├── auth/           # Persistencia y validación de sesión
├── components/     # Layout, marca, shadcn/ui, citas, seguros y pagos
├── context/        # Estado de autenticación
├── hooks/          # React Query y mutaciones del portal
├── pages/          # Páginas públicas y protegidas
└── utils/          # Fechas, registro, seguros, pagos y presentación
```

La base visual usa Tailwind CSS v4 mediante el plugin oficial de Vite. Los
primitives de shadcn/ui viven dentro de `src/components/ui`, por lo que pueden
adaptarse a los tokens y a la identidad de Clínica Aviva sin una dependencia
visual cerrada.
