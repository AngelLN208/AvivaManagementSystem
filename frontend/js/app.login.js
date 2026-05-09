/*
// ===============================
// 🔒 PROTECCIÓN DE RUTAS
// ===============================
if (window.location.pathname.includes("pages")) {
    let logeado = localStorage.getItem("login");

    if (logeado !== "true") {
        window.location.href = "../login.html";
    }
}
// ===============================
// 🔐 LOGIN SIMPLE
// ===============================
function login() {
    let user = document.getElementById("usuario").value;
    let pass = document.getElementById("password").value;

    // Usuario de prueba
    if (user === "admin" && pass === "1234") {

        // Guardar sesión
        localStorage.setItem("login", "true");

        // Redirigir al dashboard
        window.location.href = "../admin/dashboard.html";

    } else if (user === "doctor" && pass === "1234") {

        // Guardar sesión
        localStorage.setItem("login", "true");

        // Redirigir al dashboard
        window.location.href = "../doctor/doctor.html";

    } else   if (user === "recep" && pass === "1234") {

        // Guardar sesión
        localStorage.setItem("login", "true");

        // Redirigir al dashboard
        window.location.href = "../recep/recep.html";

    } else    {
        document.getElementById("error").innerText = "Usuario o contraseña incorrectos";
    }
}
*/

// ===============================
// 🔒 PROTECCIÓN DE RUTAS
// ===============================
if (window.location.pathname.includes("views") && 
    !window.location.pathname.includes("login")) {
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
        const response = await fetch("http://localhost:8080/api/auth/login", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ username, password })
        });

        const data = await response.json();

        if (response.ok) {
            localStorage.setItem("token", data.token);
            localStorage.setItem("role", data.role);
            localStorage.setItem("username", data.username);
            redirigir(data.role);
        } else {
            errorEl.innerText = data.error || "Usuario o contraseña incorrectos";
        }

    } catch (e) {
        errorEl.innerText = "No se pudo conectar al servidor";
    }
}

// ===============================
// 🔀 REDIRECCIÓN POR ROL
// ===============================
function redirigir(role) {
    if (role === "ADMIN")         window.location.href = "/views/admin/dashboard.html";
    else if (role === "RECEPTIONIST") window.location.href = "/views/recep/recep.html";
    else if (role === "DOCTOR")   window.location.href = "/views/doctor/doctor.html";
    else if (role === "PATIENT")  window.location.href = "/views/patient/patient.html";
    else document.getElementById("error").innerText = "Rol no reconocido";
}

// ===============================
// 🚪 LOGOUT
// ===============================
function logout() {
    localStorage.removeItem("token");
    localStorage.removeItem("role");
    localStorage.removeItem("username");
    window.location.href = "/views/login/login.html";
}