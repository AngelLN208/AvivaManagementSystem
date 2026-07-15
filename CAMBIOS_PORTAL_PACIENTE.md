# Registro de cambios: portal del paciente

## 2026-07-13 — Fase 1: backend de citas

### Alcance

Se agregó el soporte seguro para que un paciente autenticado gestione sus
propias citas. Se conservaron las rutas generales que ya consume el portal del
staff. La información clínica (triaje, diagnóstico, tratamiento e historial
médico) permanece fuera del alcance del portal del paciente.

### Decisiones de diseño y seguridad

- `Patient` se relaciona de forma uno-a-uno con `User` mediante `user_id`.
- La relación es nullable para no romper pacientes creados internamente por el
  staff ni filas existentes. Un paciente sin cuenta sigue siendo válido en la
  gestión interna, pero no puede usar rutas `/me`.
- Los nuevos autorregistros guardan siempre la relación `Patient–User`.
- El portal nunca envía `patientId`: el backend resuelve el paciente usando el
  `username` autenticado del JWT.
- Consultar una cita inexistente o ajena devuelve el mismo `404`, evitando
  revelar la existencia de datos de otro paciente.
- Un perfil de paciente inactivo no puede usar las operaciones `/me`.
- La auditoría de crear, cancelar o reprogramar desde `/me` guarda el username
  autenticado como actor.
- Los errores de autenticación/autorización producidos por Spring Security se
  devuelven como JSON con `401` o `403`.
- Las rutas de citas se protegen tanto en el filtro HTTP como mediante
  `@PreAuthorize`, evitando que un paciente alcance siquiera la validación del
  DTO de una operación reservada al staff.
- El rol `PATIENT` queda denegado por defecto en el resto de `/api/**`. Durante
  esta fase solo puede usar autenticación, catálogos de médicos/especialidades,
  disponibilidad y las rutas seguras `/appointments/me`.

### Endpoints agregados

| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| `GET` | `/api/appointments/me` | `PATIENT` | Lista las citas propias |
| `GET` | `/api/appointments/me/{id}` | `PATIENT` propietario | Obtiene una cita propia |
| `POST` | `/api/appointments/me` | `PATIENT` | Crea una cita propia sin `patientId` |
| `PUT` | `/api/appointments/me/{id}/reschedule` | `PATIENT` propietario | Reprograma una cita propia |
| `PUT` | `/api/appointments/me/{id}/cancel` | `PATIENT` propietario | Cancela una cita propia |

Ejemplo del cuerpo para crear una cita:

```json
{
  "doctorId": 3,
  "appointmentDateTime": "2026-07-20T10:00:00",
  "reason": "Consulta general"
}
```

### Permisos de rutas existentes

| Operación | Roles permitidos |
|---|---|
| Consultar citas mediante rutas generales | `ADMIN`, `RECEPTIONIST`, `DOCTOR` |
| Crear, cancelar o reprogramar mediante rutas generales | `ADMIN`, `RECEPTIONIST` |
| Consultar horarios disponibles | Cualquier usuario autenticado |
| Usar rutas `/me` | `PATIENT` |

### Archivos modificados

| Archivo | Modificación |
|---|---|
| `entity/Patient.java` | Relación opcional y única con `User` |
| `service/AuthService.java` | Vinculación automática durante el autorregistro |
| `repository/PatientRepository.java` | Búsqueda del paciente por username |
| `dto/PatientAppointmentRequest.java` | DTO del portal sin `patientId` |
| `repository/AppointmentRepository.java` | Búsqueda por cita y propietario |
| `service/AppointmentService.java` | Resolución de identidad, ownership, operaciones propias y auditoría |
| `controller/AppointmentController.java` | Rutas `/me` y restricciones `@PreAuthorize` |
| `security/SecurityConfig.java` | Allowlist del paciente, matriz de citas y respuestas JSON `401/403` |
| `exception/GlobalExceptionHandler.java` | Manejo explícito de acceso denegado por método |
| `config/DataInitializer.java` | Perfil enlazado para el usuario seed `paciente1` |
| `pom.xml` | Dependencia de pruebas de Spring Security |
| `src/test/resources/application.properties` | Base H2 aislada para pruebas |
| `AuthServiceTest.java` | Prueba de asociación durante autorregistro |
| `AppointmentServicePatientPortalTest.java` | Pruebas de identidad, ownership e inactividad |
| `AppointmentControllerSecurityTest.java` | Pruebas de roles y códigos `401/403` |
| `AppointmentsystemApplicationTests.java` | Uso de la configuración de pruebas aislada |
| `backend/README.md` | Documentación de rutas generales y `/me` |

### Cambio en base de datos

Hibernate (`spring.jpa.hibernate.ddl-auto=update`) agregará una columna nullable
`user_id` en `patients` con restricción de unicidad. No se fuerza `NOT NULL`
porque existen pacientes que pueden no tener cuenta de portal.

El inicializador solo se activa con `APP_SEED_ENABLED=true` y crea un perfil de
demostración para:

```text
username: paciente1
password: paciente123
DNI seed: 00000001
```

No se enlazan automáticamente filas existentes usando nombre, DNI o correo. Si
el DNI o email reservado ya está ocupado, el seed registra una advertencia y no
crea ni reclama ese perfil. `APP_SEED_ENABLED` queda deshabilitado por defecto y
no debe habilitarse en producción.

Deshabilitar la propiedad no borra credenciales seed que ya existan por una
ejecución anterior. Esas cuentas deben eliminarse o sus credenciales deben
rotarse y revocarse antes de reutilizar la base de datos en producción.

### Compatibilidad

- Se mantiene `POST /api/appointments` con `patientId` para recepción/admin.
- Se mantienen las rutas generales de consulta, cancelación y reprogramación.
- Los pacientes creados por staff continúan pudiendo existir sin `User`.
- Los autorregistros creados antes de esta modificación pueden quedar sin
  vínculo. Habilitarles una cuenta de forma verificada será una fase separada.

### Verificación

- Compilación Maven sin ejecutar pruebas: exitosa.
- Las pruebas usan H2 en memoria; no utilizan ni modifican PostgreSQL de
  desarrollo.
- Resultado final de `mvn test`: **31 pruebas ejecutadas, 0 fallos, 0 errores y
  0 omitidas** (`BUILD SUCCESS`). Incluye rechazo de `PATIENT` en consultas,
  triaje, pacientes, pagos y auditoría.

### Pendiente para fases posteriores

- Definir un flujo verificado para habilitar cuentas a pacientes creados por el
  staff o autorregistrados antes de esta relación.
- Extender el patrón `/me` a perfil, pagos, comprobantes, seguros y
  notificaciones cuando se implemente cada módulo del portal.
- Endurecer contra reservas simultáneas del mismo médico/horario mediante una
  estrategia transaccional o restricción específica de PostgreSQL.

## Fase 2 - Frontend del portal paciente (14 de julio de 2026)

### Decisión de arquitectura

Se creó una aplicación React/Vite independiente en `frontend-portal`. El
frontend `frontend-react` conserva exclusivamente las funciones del staff y no
fue modificado. Se descartó continuar el prototipo HTML de
`frontend/views/paciente` porque no tenía autenticación común, consumía
respuestas incorrectamente y sus flujos de cita estaban incompletos.

### Funciones implementadas

- Registro público mediante `POST /api/auth/register-patient`, seguido de login.
- Login que acepta exclusivamente cuentas con rol `PATIENT`.
- Inicio con próxima cita y resumen de agenda.
- Catálogo de médicos y especialidades activos.
- Agendamiento por especialidad, médico, fecha y slot disponible.
- Listado de citas propias, separadas entre próximas e historial.
- Reprogramación y cancelación con confirmación y mensajes de resultado.
- Página 404 y redirección de rutas protegidas al login.

El payload de creación contiene únicamente `doctorId`, `appointmentDateTime` y
`reason`; nunca contiene `patientId`. Todas las operaciones de citas usan las
rutas `/api/appointments/me`.

### Sesión y seguridad del cliente

- La sesión se guarda bajo `aviva.portal.session`, separada de la sesión del
  frontend del staff.
- Se valida que el JWT tenga rol `PATIENT` y no esté vencido.
- Una respuesta `401` en una petición autenticada limpia la sesión.
- Los errores del backend conservan `status`, `code` y errores por campo para
  que la interfaz pueda mostrar mensajes útiles.
- Los horarios del día actual que ya pasaron se excluyen antes de mostrarse.
- Las fechas se componen como hora local (`yyyy-MM-ddTHH:mm:ss`) y no se
  convierten a UTC.
- La caché de citas se separa por usuario y se limpia al iniciar/cerrar sesión,
  al vencer el JWT y cuando otra pestaña modifica la sesión.

### Interfaz y accesibilidad

- Diseño responsive basado en la paleta mint/teal existente de Aviva.
- Navegación móvil cerrable con fondo, enlace o tecla Escape.
- El panel móvil cerrado queda recortado dentro del viewport y no genera
  desplazamiento horizontal.
- Modo claro/oscuro, enlace para saltar al contenido y foco visible.
- Mensajes con `role=status`/`role=alert`, labels asociados y controles táctiles.
- Compatibilidad con `prefers-reduced-motion`.
- Se usan tarjetas para citas, evitando tablas difíciles de interpretar en móvil.

### Endurecimiento de integración con el backend

- Crear, consultar disponibilidad o reprogramar con un médico inactivo ahora
  devuelve la regla `DOCTOR_INACTIVE`; el ID directo no permite omitir el filtro
  de médicos activos del portal.
- El autorregistro rechaza fechas de nacimiento futuras antes de persistir el
  usuario o el paciente.
- Los orígenes locales del portal (`localhost:5174` y `127.0.0.1:5174`) se
  incluyen en CORS; una prueba de preflight evita perder esta integración.
- Se agregaron cuatro pruebas de servicio y una de CORS. El resultado final del
  backend es **36 pruebas ejecutadas, 0 fallos, 0 errores y 0 omitidas**.

### Fuera de alcance por decisión funcional

No se muestran información clínica, consultas, diagnósticos, triaje, pagos,
notificaciones, seguros ni administración de otros pacientes. El backend no
expone esos módulos como autoservicio para `PATIENT` en esta fase.

### Verificación del frontend

- `npm test`: **13 pruebas aprobadas, 0 fallos**.
- `npm run lint`: **sin errores ni advertencias**.
- `npm run build`: **compilación de producción exitosa**.
- Prueba local en navegador: login `PATIENT`, protección de rutas, catálogo de
  médicos, preselección del agendamiento y menú responsive verificados.

La guía de ejecución, configuración y estructura quedó registrada en
`frontend-portal/README.md`.

## Fase 3 - Activación, seguro opcional y pagos del portal (14 de julio de 2026)

Esta fase amplía el alcance histórico descrito en la Fase 2. La información
clínica continúa excluida, pero el paciente ahora puede activar un perfil ya
existente, registrar una póliza opcional y gestionar pagos propios.

### Activación segura de un paciente existente

- El registro comienza con el DNI, pero no consume la búsqueda administrativa
  de pacientes ni devuelve sus datos personales.
- Si el DNI es nuevo, se conserva el autorregistro que crea `User + Patient`.
- Si el staff ya creó el paciente y todavía no tiene cuenta, se envía un OTP de
  seis dígitos al correo que ya figura en el perfil. El código se verifica en
  una pantalla independiente y las credenciales se crean recién en el paso
  siguiente; únicamente se agrega un `User` con rol `PATIENT` al mismo
  `Patient`.
- Si ya existe una cuenta, el portal dirige al inicio de sesión; si el perfil no
  está activo o no tiene correo, deriva al staff.
- El OTP se persiste solo como hash, vence en 10 minutos, admite cinco intentos
  y queda invalidado al usarse. Hay un cooldown de 60 segundos y un máximo de
  cinco solicitudes por hora.
- Una verificación correcta emite un token aleatorio de finalización válido por
  cinco minutos. El backend guarda solo su hash, el frontend lo conserva en
  memoria y un reenvío invalida códigos y tokens anteriores.
- La activación usa locks en orden `Patient -> Challenge`; así dos verificaciones
  no pueden crear cuentas duplicadas y no se introduce un deadlock con reenvíos.
- Una carrera por el mismo username se traduce a `409`; el límite de solicitudes
  devuelve `429`. El código OTP no se escribe en logs y el autorregistro del
  portal dejó de registrar el DNI en sus mensajes de aplicación.

Endpoints públicos agregados:

| Método | Ruta | Resultado |
|---|---|---|
| `POST` | `/api/auth/patient-activation/request` | `NEW_PATIENT`, `VERIFICATION_REQUIRED`, `ACCOUNT_EXISTS` o `CONTACT_STAFF` |
| `POST` | `/api/auth/patient-activation/verify-code` | Verifica únicamente el OTP y emite un token temporal |
| `POST` | `/api/auth/patient-activation/complete` | Crea y vincula el `User` usando el token temporal; devuelve el JWT |

### Seguro opcional

- Después del alta o activación se ofrece un paso protegido que puede omitirse.
- El portal permite cero o una póliza activa. La primera se fuerza como primaria
  en el backend, aunque el cliente no envía `isPrimary`.
- Ninguna operación recibe `patientId`: el paciente se resuelve desde el JWT y
  una vinculación ajena responde igual que una inexistente.
- Se bloquea el perfil durante el alta para que dos solicitudes simultáneas no
  superen el límite de una póliza activa.
- Reutilizar la misma aseguradora reactiva la fila existente, respetando la
  restricción única `(patient_id, insurance_id)`.
- La póliza se aplica a citas creadas después de vincularla. No se recalculan
  pagos pendientes de citas anteriores.

Endpoints agregados:

| Método | Ruta | Acceso |
|---|---|---|
| `GET` | `/api/insurances` | Catálogo de lectura para `PATIENT` y staff |
| `GET` | `/api/patient-insurances/me` | Póliza propia activa |
| `POST` | `/api/patient-insurances/me` | Vincular póliza propia |
| `DELETE` | `/api/patient-insurances/me/{id}` | Desvincular póliza propia |

### Pagos y constancias del portal

- El portal lista exclusivamente pagos y constancias del paciente autenticado.
- El pago propio solo permite tarjeta de crédito o débito para saldos positivos
  y no recibe números de tarjeta, montos ni identidad del paciente desde el cliente.
- Una cobertura del 100 % se resuelve en el servidor como `INSURANCE`.
- Todos los pagos confirmados usan el modelo histórico `Payment` y generan una
  constancia con referencia `RCP-*`, sin agregar un indicador de origen.
- Un lock pesimista sobre el pago serializa dobles clics. Solo `PENDING` puede
  pasar a `PAID`, y la cita debe estar futura en `PENDING` o `RESCHEDULED`.
- Pagos `PAID`, `CANCELLED` o `REFUNDED`, citas canceladas/completadas/vencidas
  y recursos ajenos se rechazan sin modificar estados.
- Cancelar una cita desde el portal también cancela su pago pendiente. Si el
  pago ya está confirmado, se exige un flujo explícito de reembolso antes de
  cancelar; no se infiere su origen por método, auditoría ni referencia.
- La cobertura anual se vuelve a validar con lock dentro de la misma transacción.
- Las rutas financieras generales quedaron limitadas a `ADMIN` y
  `RECEPTIONIST`; `DOCTOR` no puede consultar ni procesar pagos globales.

Endpoints agregados:

| Método | Ruta | Acceso |
|---|---|---|
| `GET` | `/api/payments/me` | Pagos propios |
| `GET` | `/api/payments/me/{id}` | Un pago propio |
| `POST` | `/api/payments/me/{id}/pay` | Pago propio con `{ "method": "DEBIT_CARD" }` |
| `GET` | `/api/receipts/me` | Constancias propias |
| `GET` | `/api/receipts/me/{id}` | Una constancia propia |
| `GET` | `/api/receipts/me/payment/{paymentId}` | Constancia de un pago propio |

### Cambios de datos y compatibilidad

- Hibernate crea `patient_activation_challenges` con UUID, hash, intentos,
  expiración, consumo e índice por paciente/fecha.
- El esquema y los DTO históricos de `Payment` y `Receipt` se mantienen sin
  agregar indicadores específicos del portal.
- Se conservan los endpoints generales de registro, seguros, pagos y
  comprobantes para el staff.
- La contraseña de nuevos autorregistros y activaciones exige ahora un mínimo de
  ocho caracteres; el login sigue aceptando las cuentas históricas existentes.

### Registro de archivos

- Activación: `AuthController`, `PatientActivationService`, entidad/repositorio
  de challenges, seis DTOs, dos excepciones, `PatientRepository` y sus pruebas.
- Seguro: `PatientInsuranceController`, `PatientInsuranceService`,
  `PatientInsuranceRepository`, `PortalPatientInsuranceRequest` y sus pruebas.
- Pagos: controladores, servicios, repositorios y DTOs de `Payment`/`Receipt`,
  reglas de ownership/cancelación de `AppointmentService` y pruebas.
- Seguridad/configuración: `SecurityConfig`, `RegisterPatientRequest` y ejemplos
  de CORS/correo.
- Frontend: rutas, cliente API, contexto de sesión, wizard de registro, páginas
  de seguro/pagos, hooks, componentes, utilidades, estilos y pruebas.

### Verificación

- Backend: `mvn test` ejecutó **94 pruebas**, con 0 fallos, 0 errores y 0
  omitidas (`BUILD SUCCESS`). Incluye contexto H2, roles HTTP, ownership,
  activación, seguro, pagos, constancias y compatibilidad de cancelación staff.
- Frontend: `npm test` aprobó **25 pruebas**, `npm run lint` no reportó errores
  y `npm run build` generó correctamente el bundle de producción.
- `git diff --check` no encontró errores de whitespace.
- Revisión visual local: registro verificado en escritorio y a 390 px, sin
  desbordamiento horizontal ni errores de consola. La instancia de backend que
  ya estaba abierta durante esa revisión no contenía aún estas rutas nuevas;
  la integración HTTP actualizada quedó validada por MockMvc.

## Fase 4 - Rediseño profesional del portal (14 de julio de 2026)

### Sistema visual

- Se integró Tailwind CSS v4 mediante el plugin oficial para Vite y se creó una
  base local de componentes shadcn/ui estilo `new-york` en JavaScript.
- La configuración incluye alias `@`, `components.json`, `jsconfig.json`, la
  utilidad `cn` y primitives de botón, tarjeta, campo, etiqueta, badge, alerta y
  diálogo. El código de cada primitive queda dentro del repositorio.
- La interfaz carga Manrope localmente, mantiene tema claro/oscuro y usa tokens
  semánticos adaptados a la paleta teal, navy y menta de Aviva.
- Los resets heredados se movieron a la capa `base` y sus radios se aislaron
  con nombres propios para que no anulen colores, foco ni escala de Tailwind.
- El logo real `src/assets/aviva.png` sustituyó la marca recreada con texto. Un
  componente dedicado corrige mediante recorte CSS el espacio transparente del
  PNG sin alterar el archivo original.

### Navegación y pantallas

- En escritorio se usa una barra lateral fija con `Agendar cita` como acción
  principal. En móvil se usa un drawer de Radix/shadcn con foco atrapado,
  cierre por Escape y restauración del foco.
- Login, registro por pasos, inicio, citas, agendamiento, médicos, pagos,
  seguros y estados vacíos/error se migraron a la nueva jerarquía visual.
- Se conservaron payloads, hooks, cachés, reglas de ownership, OTP, validaciones
  y comportamientos de los formularios; la migración es de presentación.
- Se preservaron skip link, foco al navegar, labels, mensajes accesibles,
  `prefers-reduced-motion`, responsive y estilos de impresión.

### Presentación de pagos

- El paciente ve acciones neutrales como `Registrar pago`, `Pago registrado` y
  `Constancia de pago`; el portal consume el endpoint propio `/pay`.
- Las constancias nuevas usan referencia `RCP-*` y descripciones profesionales.
  En datos históricos, el frontend oculta referencias técnicas y compone una
  descripción pública controlada sin inventar un identificador distinto.
- Correos, notificaciones, logs y auditoría usan el flujo estándar de pago
  confirmado.

### Registro de archivos

- Configuración: `package.json`, lockfile, `vite.config.js`, `components.json`,
  `jsconfig.json` y `styles.css`.
- Marca y UI: `AvivaLogo`, `lib/utils` y primitives en `components/ui`.
- Experiencia: layout/sidebar móvil, páginas públicas y protegidas, componentes
  de citas, seguros, pagos y constancias.
- Backend: presentación de constancia/notificación en `PaymentService`, contrato
  descriptivo de `PaymentController` y pruebas relacionadas.

### Verificación

- Backend: `mvn test` pasó con 94/94 pruebas.
- Portal: `npm test` pasó con 27/27 pruebas; `npm run lint` y
  `npm run build` terminaron correctamente.
- `git diff --check` no encontró errores de whitespace.
- Revisión visual del build en escritorio y a 390 px: login y registro usan el
  logo real, conservan contraste y jerarquía, y no presentan overflow horizontal.

## Fase 5 - Normalización del contrato de pagos (14 de julio de 2026)

### Decisión

- El portal registra un pago normal mediante
  `POST /api/payments/me/{paymentId}/pay` y el DTO `PatientPaymentRequest`.
- `Payment`, `PaymentResponse` y `ReceiptResponse` conservan exactamente su
  estructura histórica: no incorporan un indicador exclusivo del portal.
- La auditoría vuelve a utilizar `PAYMENT_CONFIRMED`, las constancias mantienen
  referencias `RCP-*` y las notificaciones usan el mensaje estándar de pago
  confirmado.

### Compatibilidad del backend

- Los endpoints históricos de pagos y comprobantes del staff no cambiaron,
  incluido `POST /api/payments/{id}/process?method=...`.
- La cancelación administrativa conserva el comportamiento anterior y no
  modifica pagos automáticamente. La cancelación propia sí cancela un pago
  `PENDING`; ante un pago `PAID`, exige un reembolso explícito.
- No se agregó una migración destructiva. Si una base local alcanzó a crear una
  columna provisional, `ddl-auto=update` la deja inerte al retirar el mapping,
  permitiendo volver temporalmente a un binario anterior.

### Frontend y verificación

- API, hooks, mutaciones, pruebas, documentación y CSS del portal usan nombres
  neutrales y conservan sin cambios el rediseño Tailwind/shadcn.
- Las referencias de constancia se muestran solo si cumplen la allowlist
  `RCP-*`; cualquier referencia heredada no reconocida se oculta.
- Backend: 94/94 pruebas. Portal: 27/27 pruebas, lint y build correctos.

## Fase 6 - Correos HTML y notificaciones internas (14 de julio de 2026)

### Plantillas de correo

- Se extendió `EmailSender` con un contrato HTML que conserva el método
  histórico de texto plano para compatibilidad.
- `BrevoEmailSender` envía `textContent` y `htmlContent`; los adaptadores que
  solo implementan el contrato anterior continúan funcionando mediante el
  método por defecto.
- `EmailTemplateService` centraliza asunto, texto de respaldo y presentación.
  Escapa todos los valores dinámicos y carga los archivos desde
  `src/main/resources/templates/email`.
- Se agregaron plantillas responsive para activación OTP, eventos de citas,
  confirmación de pago y mensajes genéricos, con enlaces configurables mediante
  `PATIENT_PORTAL_URL`.
- `PatientActivationService` ya no contiene el texto del correo OTP. El
  scheduler transforma las notificaciones persistidas en HTML al enviarlas;
  los mensajes breves de `AppointmentService` y `PaymentService` se conservan
  para el canal `IN_APP` y como respaldo de texto accesible.

### Notificaciones propias del paciente

- Se agregaron `GET /api/notifications/me` y
  `PATCH /api/notifications/me/{notificationId}/read`, ambos exclusivos de
  `PATIENT`.
- El backend obtiene el perfil desde el JWT y valida ownership mediante la
  relación `Notification -> Appointment -> Patient`. No acepta `email` ni
  `patientId` desde el portal y responde 404 ante recursos inexistentes o ajenos.
- `PatientNotificationResponse` omite correo, reintentos y errores técnicos que
  solo corresponden al panel de staff. Los contratos históricos generales no
  fueron modificados.
- El portal incorpora campana responsive, contador de no leídas, navegación,
  filtros, historial y acciones para marcar una o todas como leídas. React Query
  refresca los avisos cada minuto y aísla su caché por username.

### Registro de archivos

- Backend: `EmailSender`, `BrevoEmailSender`, `EmailContent`,
  `EmailTemplateService`, plantillas HTML, scheduler, controller/service/
  repository de notificaciones, DTO seguro, seguridad y configuración.
- Portal: contrato API, hook, clave de caché, utilidades, campana/sidebar,
  `NotificationsPage`, `NotificationCard` y pruebas unitarias de filtros.

### Verificación pendiente

- Por indicación del usuario no se ejecutaron pruebas, lint ni build durante
  esta fase. Los casos de prueba se actualizaron y quedaron listos para una
  ejecución posterior.

## Fase 7 - Descarga PDF de constancias (14 de julio de 2026)

### Backend

- Se agregó Apache PDFBox `3.0.8` como dependencia exclusiva del backend.
- `ReceiptPdfService` genera una constancia A4 en memoria con identidad visual
  Aviva, referencia `RCP-*`, paciente, cita, profesional, especialidad, método,
  cobertura y total registrado. No incluye motivo, triaje, diagnóstico ni otra
  información clínica.
- `GET /api/receipts/me/{receiptId}/pdf` devuelve `application/pdf` con
  `Content-Disposition: attachment`, `Cache-Control: no-store` y nombre de
  archivo controlado.
- `ReceiptService` valida primero el paciente del JWT y el ownership del recibo.
  Una constancia ajena se mantiene indistinguible de una inexistente mediante
  respuesta 404. Los endpoints históricos del staff no cambian.

### Portal

- El diálogo de constancia incorpora `Descargar PDF` junto a la impresión del
  navegador, con estado de descarga y mensaje de error accesible.
- Axios solicita el archivo como `blob`; el cliente crea una URL temporal,
  dispara la descarga con nombre `Constancia-RCP-*.pdf` y revoca la URL después.
- No se agregó ninguna dependencia al frontend.

### Registro de archivos

- Backend: `pom.xml`, `ReceiptPdfDocument`, `ReceiptPdfService`,
  `ReceiptService`, `ReceiptController` y pruebas relacionadas.
- Portal: contrato de `portalApi`, hook de pagos, utilidades, `PaymentsPage`,
  `ReceiptDialog` y pruebas de nombres de archivo.

### Verificación

- `ReceiptPdfServiceTest` compiló el backend y generó un PDF válido de una sola
  página; verificó cabecera `%PDF`, referencia, paciente y total extraídos.
- La muestra se renderizó a 150 DPI y se revisó visualmente en formato opaco: no presenta
  recortes, superposiciones ni texto ilegible; encabezado, bloques, montos y pie
  mantienen alineación y contraste.
- No se ejecutaron la suite completa del backend, los tests generales del
  portal, lint ni build.

## Fase 8 - Integración segura del frontend de staff (14 de julio de 2026)

### Correcciones sobre `lomasbrabo`

- Se incorporaron los cambios de seguros, pagos y citas de `frontend-react`
  sin retirar el centro de notificaciones del personal.
- Se restauraron la ruta, el menú, la campana, la API y los componentes de
  notificaciones. El hook ya no referencia un módulo eliminado.
- `AuthContext` y `ThemeContext` vuelven a separar providers, contextos y hooks,
  conservando Fast Refresh y las reglas del linter.
- Se retiró el texto informal agregado al navbar y se mantuvo un saludo neutro.
- El login se presenta siempre en modo claro y restaura al salir la preferencia
  de tema del panel, evitando que el modo oscuro se filtre a la pantalla pública.
- El formulario administrativo exige los campos requeridos por el backend,
  valida DNI de ocho dígitos y evita fechas de póliza invertidas.

### Compatibilidad con el backend congelado

- No se agregó ni modificó ningún endpoint del backend para integrar el
  frontend de `lomasbrabo`.
- El alta con seguro utiliza los contratos existentes: primero
  `POST /api/patients` y después
  `POST /api/patient-insurances/patient/{patientId}`.
- Si el paciente se crea y falla la vinculación, el frontend informa que el
  paciente ya existe, cierra el formulario y refresca la lista. Esto evita que
  recepción reintente el alta y provoque un conflicto por DNI duplicado.

### Verificación

- `frontend-react`: lint y build de producción correctos.
- La comparación final confirma que esta fase no deja cambios en `backend/`.

## Fase 9 - Contenedor de despliegue para Render (14 de julio de 2026)

- Se agregó `backend/appointmentsystem/Dockerfile` sin modificar código Java.
- El build multietapa usa Maven con Java 21 y genera una imagen final basada en
  Eclipse Temurin JRE 21, ejecutada con un usuario sin privilegios.
- El contenedor utiliza `PORT=10000` como valor predeterminado compatible con
  Render; Spring continúa aceptando cualquier valor de `PORT` inyectado por el
  entorno.
- Se agregó `.dockerignore` para excluir secretos, pruebas, artefactos locales,
  metadatos de Git y archivos que no participan en la construcción.
- Las credenciales y URLs no se incluyen en la imagen; deben configurarse como
  variables de entorno del servicio de Render.

### Verificación

- El cliente Docker 29.5.2 reconoce la instalación local.
- La construcción local de la imagen queda pendiente porque Docker Engine no
  estaba iniciado durante esta fase. El JAR del backend sí fue empaquetado
  correctamente con Maven antes de preparar el contenedor.
