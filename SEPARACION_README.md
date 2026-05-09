# 🏥 Aviva Management System

Sistema de gestión de citas médicas con arquitectura separada (Backend + Frontend).

## 📁 Estructura del Proyecto

```
ProyectoMarcosWeb/
├── backend/                          # Servidor Spring Boot (Java)
│   └── appointmentsystem/
│       ├── pom.xml
│       ├── src/
│       │   ├── main/
│       │   │   ├── java/com/aviva/appointmentsystem/
│       │   │   │   ├── AppointmentsystemApplication.java
│       │   │   │   ├── controller/
│       │   │   │   ├── dto/
│       │   │   │   ├── entity/
│       │   │   │   ├── repository/
│       │   │   │   ├── security/
│       │   │   │   └── service/
│       │   │   └── resources/
│       │   │       └── application.properties
│       │   └── test/
│       └── target/
│
├── frontend/                         # Cliente (HTML/CSS/JavaScript)
│   ├── index.html
│   ├── css/
│   │   ├── Estilos.css
│   │   ├── style_doctor.css
│   │   ├── style_login.css
│   │   └── style_recepcionista.css
│   ├── js/
│   │   ├── config.js                 # ⭐ Configuración de API
│   │   ├── app.login.js
│   │   ├── app_admin.js
│   │   ├── app_doctor.js
│   │   ├── app_recepcionista.js
│   │   └── dashboard.js
│   ├── views/
│   │   ├── login/
│   │   │   └── login.html
│   │   ├── admin/
│   │   │   ├── dashboard.html
│   │   │   ├── doctores.html
│   │   │   ├── especialidades.html
│   │   │   ├── pacientes.html
│   │   │   ├── pagos.html
│   │   │   └── configuracion.html
│   │   ├── doctor/
│   │   │   └── doctor.html
│   │   └── recep/
│   │       └── recep.html
│   └── img/
│
├── appointmentsystem/               # ⚠️ Original (mantener para referencia)
└── README.md
```

## 🚀 Iniciar la Aplicación

### Backend (Spring Boot)

```bash
cd backend/appointmentsystem
mvn clean install
mvn spring-boot:run
```

**URL:** `http://localhost:8080`

### Frontend (HTML/JavaScript)

```bash
# Opción 1: Usar un servidor web local
# Con Python 3
cd frontend
python -m http.server 3000

# Con Node.js (http-server)
npx http-server frontend -p 3000

# Con Live Server en VS Code
# Clic derecho en index.html → Open with Live Server
```

**URL:** `http://localhost:3000` (o la que indique tu servidor web)

## 🔧 Configuración

### CORS (Backend)
El backend ya está configurado para aceptar peticiones del frontend en:
- `http://localhost:3000`
- `http://localhost:8080`

Archivo: `backend/appointmentsystem/src/main/java/com/aviva/appointmentsystem/security/SecurityConfig.java`

### Endpoints API (Frontend)
Editar `frontend/js/config.js` para cambiar la URL base de la API:

```javascript
const API_CONFIG = {
    BASE_URL: 'http://localhost:8080',  // Cambiar según ambiente
    API_PATH: '/api'
};
```

## 📡 Comunicación Frontend-Backend

### Autenticación
- **Endpoint:** `POST /api/auth/login`
- **Request:**
  ```json
  {
    "usuario": "admin",
    "password": "1234"
  }
  ```
- **Response:**
  ```json
  {
    "token": "eyJhbGciOiJIUzI1NiIs...",
    "rol": "admin"
  }
  ```

### Endpoints Disponibles
Todos los endpoints usan la configuración de `config.js`:

| Recurso | Método | Endpoint |
|---------|--------|----------|
| Pacientes | GET/POST/PUT/DELETE | `/api/pacientes` |
| Doctores | GET/POST/PUT/DELETE | `/api/doctores` |
| Especialidades | GET/POST/PUT/DELETE | `/api/especialidades` |
| Citas | GET/POST/PUT/DELETE | `/api/citas` |
| Pagos | GET/POST/PUT/DELETE | `/api/pagos` |

## 🔐 Seguridad

- CORS habilitado para comunicación entre frontend y backend
- JWT (JSON Web Tokens) para autenticación
- CSRF deshabilitado en desarrollo (considerar habilitar en producción)
- Spring Security integrado

## 📦 Tecnologías

**Backend:**
- Java 21
- Spring Boot 4.0.6
- Spring Security
- JWT (io.jsonwebtoken)
- Maven

**Frontend:**
- HTML 5
- CSS 3
- Vanilla JavaScript (ES6)
- LocalStorage para sesiones

## 🐛 Troubleshooting

### "CORS policy error"
- Verificar que el backend está corriendo en `http://localhost:8080`
- Revisar `SecurityConfig.java` en el backend
- Limpiar cache del navegador (Ctrl+Shift+Delete)

### "Cannot GET /"
- Verificar que el servidor web del frontend está corriendo
- Usar un servidor web HTTP (no abrir HTML localmente con `file://`)

### Autenticación no funciona
- Verificar token en `localStorage` (F12 → Application → LocalStorage)
- Revisar la consola del navegador (F12 → Console)
- Verificar logs del backend

## 📝 Notas

- El archivo `appointmentsystem/` original se mantiene para referencia
- El frontend es desacoplado y puede funcionar con cualquier backend compatible
- Para producción, compilar el frontend y servir desde un CDN o servidor estático
- Considerar agregar TypeScript y frameworks como React/Vue en futuras iteraciones

## 👨‍💻 Desarrollo

```bash
# Clonar repositorio
git clone https://github.com/AngelLN208/AvivaManagementSystem.git
cd ProyectoMarcosWeb

# Backend
cd backend/appointmentsystem
mvn clean install

# Frontend
cd ../../frontend
# Servir archivos estáticos
```

---

**Última actualización:** Mayo 2026
