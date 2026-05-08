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