# Portal de pacientes Aviva

Aplicación React/Vite separada del frontend del staff. Permite que un usuario
con rol `PATIENT` se registre, inicie sesión y gestione únicamente sus propias
citas.

## Alcance actual

- Inicio con resumen y próxima cita.
- Registro e inicio de sesión de pacientes.
- Listado de próximas citas e historial.
- Creación de citas sin enviar `patientId`.
- Reprogramación y cancelación de citas propias.
- Catálogo de especialidades y médicos activos.
- Consulta de horarios disponibles.
- Diseño responsive, modo oscuro y estados accesibles de carga/error.

No incluye información clínica, consultas, diagnósticos, triaje, pagos,
notificaciones ni administración de otros pacientes.

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

## Configuración

El valor predeterminado de la API es `/api`. Para usar un backend remoto, crea
un archivo `.env` basado en `.env.example`:

```text
VITE_API_URL=https://backend.ejemplo.com/api
```

Cuando frontend y backend estén en dominios distintos, el dominio público del
portal también debe agregarse a `CORS_ALLOWED_ORIGINS` del backend.

### Despliegue en Vercel

Configura el proyecto de Vercel con estos valores:

- **Root Directory:** `frontend-portal`
- **Build Command:** `npm run build`
- **Output Directory:** `dist`
- **Variable de build:** `VITE_API_URL=https://backend-publico.example/api`

La URL debe usar HTTPS y terminar en `/api`. Después agrega el origen exacto
del portal a `CORS_ALLOWED_ORIGINS` del backend y vuelve a desplegar. Si se
omite `VITE_API_URL`, Vercel tratará `/api` como una ruta del SPA y el portal no
podrá comunicarse con el backend.

## Rutas

| Ruta | Acceso | Función |
|---|---|---|
| `/login` | Público | Inicio de sesión exclusivo para `PATIENT` |
| `/registro` | Público | Autorregistro de paciente y acceso automático |
| `/` | `PATIENT` | Resumen de citas |
| `/citas` | `PATIENT` | Próximas citas e historial |
| `/agendar` | `PATIENT` | Selección de especialidad, médico, fecha y horario |
| `/medicos` | `PATIENT` | Catálogo de médicos activos |

La sesión usa la clave aislada `aviva.portal.session`; no comparte las claves
del frontend del staff. El token se rechaza si está vencido o si su rol no es
`PATIENT`, y una respuesta autenticada `401` limpia la sesión.

## Verificación

```bash
npm test
npm run lint
npm run build
```

Las 13 pruebas unitarias cubren la validación de sesión/JWT, el aislamiento de
caché entre pacientes, los identificadores de ruta, el tratamiento local de
fechas y horarios, y la clasificación de las citas.

## Estructura principal

```text
src/
├── api/            # Cliente HTTP y endpoints permitidos al paciente
├── auth/           # Persistencia y validación de sesión
├── components/     # Layout, estados y componentes de citas
├── context/        # Estado de autenticación
├── hooks/          # React Query para citas y catálogos
├── pages/          # Páginas del portal
└── utils/          # Fechas locales y reglas de presentación
```
