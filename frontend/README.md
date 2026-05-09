# 🎨 Frontend - Aviva Management System

Cliente web (HTML/CSS/JavaScript) para gestión de citas médicas.

## 📋 Requisitos

- Navegador moderno (Chrome, Firefox, Safari, Edge)
- Servidor web HTTP (no funcionará con `file://`)
- Backend ejecutándose en `http://localhost:8080`

## 🚀 Iniciar el Frontend

### Opción 1: Usando Python (Recomendado)

```bash
cd frontend

# Python 3
python -m http.server 3000

# Python 2
python -m SimpleHTTPServer 3000
```

### Opción 2: Usando Node.js

```bash
cd frontend

# Instalar http-server (primera vez)
npm install -g http-server

# Ejecutar
http-server -p 3000
```

### Opción 3: Usando Live Server (VS Code)

1. Instalar extensión "Live Server"
2. Click derecho en `index.html`
3. Seleccionar "Open with Live Server"

**URL:** http://localhost:3000

## 📁 Estructura

```
frontend/
├── index.html                       # Página principal
├── views/
│   ├── login/
│   │   └── login.html              # Pantalla de login
│   ├── admin/
│   │   ├── dashboard.html          # Dashboard admin
│   │   ├── pacientes.html
│   │   ├── doctores.html
│   │   ├── especialidades.html
│   │   ├── pagos.html
│   │   └── configuracion.html
│   ├── doctor/
│   │   └── doctor.html             # Panel doctor
│   └── recep/
│       └── recep.html              # Panel recepcionista
├── css/
│   ├── style.css                   # Estilos generales
│   ├── Estilos.css
│   ├── style_login.css             # Estilos login
│   ├── style_doctor.css            # Estilos panel doctor
│   ├── style_recepcionista.css     # Estilos recepcionista
│   └── style_admin.css             # Estilos admin
├── js/
│   ├── config.js                   # ⭐ CONFIGURACIÓN API (IMPORTANTE)
│   ├── app.login.js                # Lógica de autenticación
│   ├── app_admin.js                # Lógica panel admin
│   ├── app_doctor.js               # Lógica panel doctor
│   ├── app_recepcionista.js        # Lógica panel recepcionista
│   └── dashboard.js                # Lógica dashboard
└── img/                            # Imágenes
```

## ⚙️ Configuración

### IMPORTANTE: Cambiar URL del Backend

Editar `js/config.js`:

```javascript
const API_CONFIG = {
    // Cambiar según tu entorno
    BASE_URL: 'http://localhost:8080',    // ← Backend
    API_PATH: '/api'
};
```

**Ejemplos:**
- Desarrollo: `http://localhost:8080`
- Producción: `https://api.tudominio.com`

## 🔐 Autenticación

### Login
1. Usuario: `admin` | Contraseña: `1234` → Rol: Admin
2. Usuario: `doctor` | Contraseña: `1234` → Rol: Doctor
3. Usuario: `recepcionista` | Contraseña: `1234` → Rol: Recepcionista

### Token JWT
El token se guarda en `localStorage`:
```javascript
localStorage.getItem('token')          // Token JWT
localStorage.getItem('login')          // Estado de login
localStorage.getItem('usuario')        // Usuario actual
localStorage.getItem('rol')            // Rol del usuario
```

## 📡 API Integration

### Estructura de Peticiones

```javascript
// Ejemplo: Login
fetch('http://localhost:8080/api/auth/login', {
    method: 'POST',
    headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`  // Si es necesario
    },
    body: JSON.stringify({
        usuario: 'admin',
        password: '1234'
    })
})
.then(response => response.json())
.then(data => console.log(data))
.catch(error => console.error('Error:', error));
```

## 🎨 Estilos

### Bootstrap Integration
El proyecto usa Bootstrap 5 para componentes UI.

### Variables CSS Personalizadas
```css
:root {
    --color-primary: #007bff;
    --color-success: #28a745;
    --color-danger: #dc3545;
    /* ... más variables */
}
```

## 🔄 Flujo de Usuarios

### Admin
1. Login → Dashboard admin
2. Gestionar pacientes, doctores, especialidades
3. Ver pagos y reportes
4. Configuración del sistema

### Doctor
1. Login → Panel doctor
2. Ver citas asignadas
3. Actualizar estado de citas
4. Ver información de pacientes

### Recepcionista
1. Login → Panel recepcionista
2. Agendar citas
3. Confirmar/Cancelar citas
4. Ver disponibilidad de doctores

## 🐛 Debugging

### Consola del Navegador (F12)

```javascript
// Ver token
console.log(localStorage.getItem('token'));

// Ver estado de autenticación
console.log(localStorage.getItem('login'));

// Limpiar datos
localStorage.clear();
```

### Verificar CORS
- Abrir DevTools (F12) → Console
- Si hay error de CORS, verificar que:
  1. Backend está corriendo
  2. `SecurityConfig.java` está bien configurado
  3. URL coincide con configuración CORS

## 📦 Deployment

### Servir en Producción

```bash
# Usar servidor web (nginx)
# Copiar contenido de 'frontend/' a:
# /var/www/html/

# O usar un CDN:
# - Subir archivos a AWS S3
# - CloudFront para distribución
# - Actualizar config.js con API de producción
```

### Build para Producción

```bash
# Minificar CSS/JS (opcional)
npm install -g cssnano-cli terser

# Ejemplo:
terser js/app.login.js -c -m -o js/app.login.min.js
```

## 🔒 Seguridad

- ✅ HTTPS en producción
- ✅ Token JWT en localStorage
- ✅ Validación en cliente
- ✅ CORS configurado
- ⚠️ NO guardar contraseñas en localStorage
- ⚠️ Validar entrada de usuario

## 🎯 Mejoras Futuras

- [ ] Migrar a React/Vue
- [ ] TypeScript para mejor type-safety
- [ ] Testing con Jest/Vitest
- [ ] Compresión gzip
- [ ] Service Workers (PWA)
- [ ] Internacionalización (i18n)

## 📚 Recursos

- [Bootstrap 5 Docs](https://getbootstrap.com/docs/5.0/)
- [Fetch API](https://developer.mozilla.org/es/docs/Web/API/Fetch_API)
- [LocalStorage](https://developer.mozilla.org/es/docs/Web/API/Window/localStorage)
- [JWT Intro](https://jwt.io/introduction)

## 🤝 Contribuir

1. Crear rama: `git checkout -b feature/nueva-funcionalidad`
2. Hacer cambios
3. Commit: `git commit -m "Add nueva funcionalidad"`
4. Push: `git push origin feature/nueva-funcionalidad`
5. Pull Request

## 📝 Notas

- Asegurar que el backend esté corriendo antes de usar el frontend
- Cambiar `config.js` según el ambiente
- Usar HTTPS en producción
- Considerar agregar framework moderno en futuras versiones

---

**Última actualización:** Mayo 2026
