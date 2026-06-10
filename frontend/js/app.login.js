import { API_ENDPOINTS } from "./config.js";

// ===============================
// 🔒 PROTECCIÓN DE RUTAS
// ===============================
if (
    window.location.pathname.includes("views") &&
    !window.location.pathname.includes("login")
) {

    let token = localStorage.getItem("token");

    if (!token) {
        window.location.href = "/views/login/login.html";
    }
}

// ===============================
// 🔐 LOGIN CON BACKEND
// ===============================
async function login() {

    let username = document.getElementById("usuario").value.trim();
    let password = document.getElementById("password").value.trim();
    let errorEl = document.getElementById("error");

    if (!username || !password) {
        errorEl.innerText = "Ingresa usuario y contraseña";
        return;
    }

    try {

        const response = await fetch(API_ENDPOINTS.AUTH.LOGIN, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({ username, password })
        });

        const data = await response.json();

        if (response.ok) {

            const token = data.data.token;

            // Guardar token
            localStorage.setItem("token", token);

            // Decodificar JWT
            const payload = parseJwt(token);

            // Guardar datos opcionales
            localStorage.setItem("role", payload.role);
            localStorage.setItem("username", payload.sub);

            // Redirección
            redirigir(payload.role);

        } else {

            errorEl.innerText =
                data.message || "Usuario o contraseña incorrectos";
        }

    } catch (e) {

        console.error(e);
        errorEl.innerText = "No se pudo conectar al servidor";
    }
}

// ===============================
// 🔓 DECODIFICAR JWT
// ===============================
function parseJwt(token) {

    return JSON.parse(
        atob(token.split('.')[1])
    );
}

// ===============================
// 🔀 REDIRECCIÓN POR ROL
// ===============================
function redirigir(role) {

    if (role === "ADMIN")
        window.location.href = "../admin/dashboard.html";

    else if (role === "RECEPTIONIST")
        window.location.href = "../recep/index.html";

    else if (role === "DOCTOR")
        window.location.href = "../doctor/doctor.html";

    else if (role === "PATIENT")
        window.location.href = "../paciente/index.html";

    else
        document.getElementById("error").innerText =
            "Rol no reconocido";
}

// ===============================
// 🚪 LOGOUT
// ===============================
function logout() {

    localStorage.removeItem("token");
    localStorage.removeItem("role");
    localStorage.removeItem("username");

    window.location.href = "/frontend/views/login/login.html";
}

document
    .getElementById("btnLogin")
    .addEventListener("click", login);