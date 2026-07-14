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

- Adaptar o crear el frontend del portal para consumir `/appointments/me`.
- Definir un flujo verificado para habilitar cuentas a pacientes creados por el
  staff o autorregistrados antes de esta relación.
- Extender el patrón `/me` a perfil, pagos, comprobantes, seguros y
  notificaciones cuando se implemente cada módulo del portal.
- Endurecer contra reservas simultáneas del mismo médico/horario mediante una
  estrategia transaccional o restricción específica de PostgreSQL.
