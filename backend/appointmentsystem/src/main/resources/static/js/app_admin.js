// ===============================
// 🔍 BUSCADOR PACIENTES
// ===============================
const buscarPaciente = document.getElementById("buscarPaciente");

if (buscarPaciente) {
    buscarPaciente.addEventListener("keyup", function () {
        let filtro = buscarPaciente.value.toLowerCase();

        document.querySelectorAll("#tablaPacientes tbody tr")
            .forEach(fila => {
                fila.style.display = fila.innerText.toLowerCase().includes(filtro) ? "" : "none";
            });
    });
}

// ===============================
// 🔍 BUSCADOR DOCTORES
// ===============================
const buscarDoctor = document.getElementById("buscarDoctor");

if (buscarDoctor) {
    buscarDoctor.addEventListener("keyup", function () {
        let filtro = buscarDoctor.value.toLowerCase();

        document.querySelectorAll("#tablaDoctores tbody tr")
            .forEach(fila => {
                fila.style.display = fila.innerText.toLowerCase().includes(filtro) ? "" : "none";
            });
    });
}

// ===============================
// 🔍 BUSCADOR ESPECIALIDADES
// ===============================
const buscarEspecialidad = document.getElementById("buscarEspecialidad");

if (buscarEspecialidad) {
    buscarEspecialidad.addEventListener("keyup", function () {
        let filtro = buscarEspecialidad.value.toLowerCase();

        document.querySelectorAll("#tablaEspecialidades tbody tr")
            .forEach(fila => {
                fila.style.display = fila.innerText.toLowerCase().includes(filtro) ? "" : "none";
            });
    });
}


// ===============================
// 👨‍⚕️ VER DOCTOR
// ===============================
function verDoctor(nombre, especialidad, horario) {
    alert(
        "👨‍⚕️ " + nombre +
        "\nEspecialidad: " + especialidad +
        "\nHorario: " + horario
    );
}

// ===============================
// 🎨 CAMBIO DE TEMA
// ===============================
function applyTheme(theme) {
    document.body.classList.toggle('dark-theme', theme === 'dark');
    localStorage.setItem('theme', theme);
    showNotification(`Tema ${theme === 'dark' ? 'oscuro' : 'claro'} aplicado.`, 'success');
}

function loadTheme() {
    const theme = localStorage.getItem('theme') || 'light';
    applyTheme(theme);
}

// ===============================
// ⚙️ CONFIGURACIONES GLOBALES
// ===============================
function guardarHorarios() {
    const apertura = document.getElementById('horaApertura')?.value;
    const cierre = document.getElementById('horaCierre')?.value;
    if (apertura && cierre) {
        localStorage.setItem('horaApertura', apertura);
        localStorage.setItem('horaCierre', cierre);
        showNotification('Horarios guardados correctamente.', 'success');
    }
}

function cargarHorarios() {
    const apertura = localStorage.getItem('horaApertura') || '08:00';
    const cierre = localStorage.getItem('horaCierre') || '18:00';
    const aperturaEl = document.getElementById('horaApertura');
    const cierreEl = document.getElementById('horaCierre');
    if (aperturaEl) aperturaEl.value = apertura;
    if (cierreEl) cierreEl.value = cierre;
}

function guardarTarifas() {
    const especialidades = ['Cardiología', 'Dermatología', 'Pediatría', 'Ginecología'];
    especialidades.forEach(esp => {
        const input = document.getElementById(`tarifa${esp.replace('í', 'i')}`);
        if (input) localStorage.setItem(`tarifa${esp}`, input.value);
    });
    showNotification('Tarifas guardadas correctamente.', 'success');
}

function cargarTarifas() {
    const especialidades = ['Cardiología', 'Dermatología', 'Pediatría', 'Ginecología'];
    const container = document.getElementById('tarifasContainer');
    if (!container) return;

    container.innerHTML = especialidades.map(esp => {
        const tarifa = localStorage.getItem(`tarifa${esp}`) || '120';
        return `
            <div class="col-md-3">
                <label class="form-label">${esp}</label>
                <input type="number" id="tarifa${esp.replace('í', 'i')}" class="form-control" value="${tarifa}" min="0">
            </div>
        `;
    }).join('');
}

// ===============================
// 👤 GESTIÓN DE PERFIL
// ===============================
function guardarPerfil() {
    const nombre = document.getElementById('nombrePerfil')?.value;
    const correo = document.getElementById('correoPerfil')?.value;
    const telefono = document.getElementById('telefonoPerfil')?.value;
    
    if (!nombre || !correo) {
        showNotification('Completa los campos obligatorios.', 'error');
        return;
    }
    
    localStorage.setItem('perfilNombre', nombre);
    localStorage.setItem('perfilCorreo', correo);
    localStorage.setItem('perfilTelefono', telefono);
    showNotification('Perfil actualizado correctamente.', 'success');
}

function cargarPerfil() {
    const nombre = localStorage.getItem('perfilNombre') || 'Administrador';
    const correo = localStorage.getItem('perfilCorreo') || 'admin@aviva.com';
    const telefono = localStorage.getItem('perfilTelefono') || '+51 987 654 321';
    
    const nombreEl = document.getElementById('nombrePerfil');
    const correoEl = document.getElementById('correoPerfil');
    const telefonoEl = document.getElementById('telefonoPerfil');
    
    if (nombreEl) nombreEl.value = nombre;
    if (correoEl) correoEl.value = correo;
    if (telefonoEl) telefonoEl.value = telefono;
}

function cambiarFoto() {
    showNotification('Función de cambiar foto: Integración con backend necesaria.', 'warning');
}

function cambiarContrasena() {
    const actual = document.getElementById('passActual')?.value;
    const nueva = document.getElementById('passNueva')?.value;
    const confirm = document.getElementById('passConfirm')?.value;
    
    if (!actual || !nueva || !confirm) {
        showNotification('Completa todos los campos.', 'error');
        return;
    }
    
    if (nueva !== confirm) {
        showNotification('Las contraseñas no coinciden.', 'error');
        return;
    }
    
    if (nueva.length < 6) {
        showNotification('La contraseña debe tener al menos 6 caracteres.', 'error');
        return;
    }
    
    document.getElementById('passActual').value = '';
    document.getElementById('passNueva').value = '';
    document.getElementById('passConfirm').value = '';
    showNotification('Contraseña cambiada correctamente.', 'success');
}

// ===============================
// 📊 REPORTES FINANCIEROS
// ===============================
function generarReporteFinanciero() {
    const ingresos = 4850; // Simulado
    const pendientes = 8;
    const consultas = 42;
    const reporte = `
        <h5>Reporte Financiero - ${new Date().toLocaleDateString()}</h5>
        <p>Ingresos del Día: S/ ${ingresos}</p>
        <p>Pagos Pendientes: ${pendientes}</p>
        <p>Consultas Pagadas: ${consultas}</p>
        <button class="btn btn-sm btn-primary" onclick="exportarReporte()">Exportar PDF</button>
    `;
    document.getElementById('reporteFinanciero').innerHTML = reporte;
}

function exportarReporte() {
    alert('Simulación: Reporte exportado a PDF.');
}

// ===============================
// 🏥 GESTIÓN DE ESPECIALIDADES
// ===============================
function editarEspecialidad(button) {
    const row = button.closest('tr');
    const nombre = row.children[0].textContent.trim();
    const costo = row.children[1].textContent.replace('S/ ', '');
    const estado = row.querySelector('.badge').textContent.trim();

    document.getElementById('nombreEspecialidad').value = nombre;
    document.getElementById('costoEspecialidad').value = costo;
    document.getElementById('estadoEspecialidad').value = estado;

    especialidadEditRow = row;
    document.getElementById('modalEspecialidadTitle').textContent = 'Editar Especialidad';
    const modal = new bootstrap.Modal(document.getElementById('modalEspecialidad'));
    modal.show();
}

function agregarEspecialidad() {
    const nombre = document.getElementById('nombreEspecialidad').value.trim();
    const costo = document.getElementById('costoEspecialidad').value;
    const estado = document.getElementById('estadoEspecialidad').value;

    if (!nombre || !costo) {
        showNotification('Complete todos los campos.', 'error');
        return;
    }

    const badgeClass = estado === 'Activa' ? 'bg-success' : 'bg-secondary';
    const tabla = document.querySelector('#tablaEspecialidades tbody');

    if (especialidadEditRow) {
        especialidadEditRow.children[0].innerHTML = `<strong>${nombre}</strong>`;
        especialidadEditRow.children[1].textContent = `S/ ${costo}`;
        especialidadEditRow.children[2].textContent = '0'; // Doctores asignados (simulado)
        especialidadEditRow.children[3].innerHTML = `<span class="badge ${badgeClass}">${estado}</span>`;
        especialidadEditRow.children[4].innerHTML = `
            <div class="d-flex gap-2">
                <button class="btn btn-light btn-sm" onclick="editarEspecialidad(this)">
                    <i class="bi bi-pencil"></i>
                </button>
                <button class="btn btn-light btn-sm text-danger" onclick="eliminarEspecialidad(this)">
                    <i class="bi bi-trash"></i>
                </button>
            </div>
        `;
    } else {
        const nuevaFila = document.createElement('tr');
        nuevaFila.innerHTML = `
            <td><strong>${nombre}</strong></td>
            <td>S/ ${costo}</td>
            <td>0</td>
            <td><span class="badge ${badgeClass}">${estado}</span></td>
            <td>
                <div class="d-flex gap-2">
                    <button class="btn btn-light btn-sm" onclick="editarEspecialidad(this)">
                        <i class="bi bi-pencil"></i>
                    </button>
                    <button class="btn btn-light btn-sm text-danger" onclick="eliminarEspecialidad(this)">
                        <i class="bi bi-trash"></i>
                    </button>
                </div>
            </td>
        `;
        tabla.appendChild(nuevaFila);
    }

    const modal = bootstrap.Modal.getInstance(document.getElementById('modalEspecialidad'));
    modal.hide();
    showNotification('Especialidad guardada correctamente.', 'success');
}

function eliminarEspecialidad(button) {
    const row = button.closest('tr');
    row.remove();
    showNotification('Especialidad eliminada.', 'warning');
}

let especialidadEditRow = null;

function resetEspecialidadModal() {
    document.getElementById('nombreEspecialidad').value = '';
    document.getElementById('costoEspecialidad').value = '';
    document.getElementById('estadoEspecialidad').value = 'Activa';
    especialidadEditRow = null;
    document.getElementById('modalEspecialidadTitle').textContent = 'Agregar Especialidad';
}

function showNotification(message, type = "success") {
    const banner = document.getElementById("notificationBanner");
    if (!banner) return;

    const title = type === "error" ? "Conflicto de horario" : type === "warning" ? "Atención" : "Éxito";
    const icon = type === "error" ? "<i class='bi bi-exclamation-triangle-fill'></i>" : type === "warning" ? "<i class='bi bi-exclamation-circle-fill'></i>" : "<i class='bi bi-check-circle-fill'></i>";

    banner.innerHTML = `
        <div class="notification-content">
            <div class="notification-icon">${icon}</div>
            <div>
                <div class="notification-title">${title}</div>
                <div class="notification-text">${message}</div>
            </div>
            <button type="button" class="notification-close" aria-label="Cerrar notificación">×</button>
        </div>
    `;

    banner.className = `notification-banner notification-${type}`;
    banner.style.display = "block";
    banner.style.opacity = "1";

    const closeBtn = banner.querySelector(".notification-close");
    if (closeBtn) {
        closeBtn.addEventListener("click", () => {
            banner.style.opacity = "0";
            setTimeout(() => {
                banner.style.display = "none";
            }, 300);
        });
    }

    clearTimeout(notificationTimeout);
    notificationTimeout = setTimeout(() => {
        banner.style.opacity = "0";
        setTimeout(() => {
            banner.style.display = "none";
        }, 300);
    }, 4200);
}

const diasSemana = ['Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes', 'Sábado'];

function cargarHorariosForm() {
    const container = document.getElementById('horariosContainer');
    if (!container) return;

    container.innerHTML = diasSemana.map(dia => `
        <div class="card mb-2 p-3">
            <div class="row g-2 align-items-end">
                <div class="col-md-2">
                    <label class="form-label small fw-bold">${dia}</label>
                </div>
                <div class="col-md-3">
                    <div class="form-check">
                        <input class="form-check-input" type="checkbox" id="check_${dia}" onchange="toggleDia('${dia}')">
                        <label class="form-check-label small">Trabaja</label>
                    </div>
                </div>
                <div class="col-md-3">
                    <input type="time" class="form-control form-control-sm" id="inicio_${dia}" disabled>
                </div>
                <div class="col-md-3">
                    <input type="time" class="form-control form-control-sm" id="fin_${dia}" disabled>
                </div>
            </div>
        </div>
    `).join('');
}

function toggleDia(dia) {
    const checkbox = document.getElementById(`check_${dia}`);
    const inicio = document.getElementById(`inicio_${dia}`);
    const fin = document.getElementById(`fin_${dia}`);
    
    if (checkbox.checked) {
        inicio.disabled = false;
        fin.disabled = false;
        inicio.value = '08:00';
        fin.value = '16:00';
    } else {
        inicio.disabled = true;
        fin.disabled = true;
        inicio.value = '';
        fin.value = '';
    }
}

function obtenerHorariosDoctor() {
    const horarios = {};
    diasSemana.forEach(dia => {
        const checkbox = document.getElementById(`check_${dia}`);
        if (checkbox && checkbox.checked) {
            const inicio = document.getElementById(`inicio_${dia}`).value;
            const fin = document.getElementById(`fin_${dia}`).value;
            if (inicio && fin) {
                horarios[dia] = { inicio, fin };
            }
        }
    });
    return horarios;
}

function resetDoctorModal() {
    doctorEditRow = null;
    document.getElementById("modalDoctorTitle").textContent = "Agregar Doctor";
    document.getElementById("guardarDoctorBtn").textContent = "Guardar Doctor";
    document.getElementById("nombreDoctor").value = "";
    document.getElementById("dniDoctor").value = "";
    document.getElementById("celularDoctor").value = "";
    document.getElementById("correoDoctor").value = "";
    document.getElementById("especialidadDoctor").value = "";
    document.getElementById("estadoDoctor").value = "Activo";
    cargarHorariosForm();
}

function editarDoctor(button) {
    const row = button.closest("tr");
    if (!row) return;

    doctorEditRow = row;
    document.getElementById("modalDoctorTitle").textContent = "Editar Doctor";
    document.getElementById("guardarDoctorBtn").textContent = "Guardar cambios";

    document.getElementById("nombreDoctor").value = row.querySelector("td:first-child strong").textContent.trim();
    document.getElementById("dniDoctor").value = row.children[1].textContent.trim();
    document.getElementById("celularDoctor").value = row.dataset.celular || "";
    document.getElementById("correoDoctor").value = row.dataset.correo || "";
    document.getElementById("especialidadDoctor").value = row.children[2].textContent.trim();
    document.getElementById("estadoDoctor").value = row.querySelector("td:nth-child(5) .badge").textContent.trim();

    cargarHorariosForm();
    
    // Cargar horarios guardados
    if (row.dataset.horarios) {
        try {
            const horarios = JSON.parse(row.dataset.horarios);
            Object.entries(horarios).forEach(([dia, h]) => {
                const checkbox = document.getElementById(`check_${dia}`);
                const inicio = document.getElementById(`inicio_${dia}`);
                const fin = document.getElementById(`fin_${dia}`);
                if (checkbox) {
                    checkbox.checked = true;
                    inicio.disabled = false;
                    fin.disabled = false;
                    inicio.value = h.inicio;
                    fin.value = h.fin;
                }
            });
        } catch (e) {
            console.log("Error cargando horarios:", e);
        }
    }

    const modalEl = document.getElementById("modalDoctor");
    const modal = new bootstrap.Modal(modalEl);
    modal.show();
}

function eliminarDoctor(button) {
    const row = button.closest("tr");
    if (!row) return;

    row.remove();
    showNotification("Doctor eliminado correctamente.", "warning");
}

function verBoleta(id, paciente, monto, estado) {
    alert(
        "🧾 BOLETA\n\n" +
        "ID: " + id +
        "\nPaciente: " + paciente +
        "\nMonto: " + monto +
        "\nEstado: " + estado
    );
}

function editarPago(id) {
    alert("✏️ Editar pago: " + id);
}

function nuevaBoleta() {
    alert("💳 Registrar nueva boleta");
}

function filtrarPorEspecialidad(valor) {
    const buscador = document.getElementById("buscarDoctor");
    if (buscador) {
        buscador.value = valor;
        const evento = new Event('keyup');
        buscador.dispatchEvent(evento);
    }
}

if (window.location.pathname.includes("/admin/")) {
    let token = localStorage.getItem("token");
    if (!token) {
        window.location.href = "/views/login/login.html";
    }
}

function logout() {
    localStorage.removeItem("token");
    localStorage.removeItem("role");
    localStorage.removeItem("username");
    window.location.href = "/views/login/login.html";
}

const buscarPago = document.getElementById("buscarPago");
if (buscarPago) {
    buscarPago.addEventListener("keyup", function () {
        const texto = buscarPago.value.toLowerCase();
        const filas = document.querySelectorAll("#tablaPagos tbody tr");
        filas.forEach(fila => {
            const contenido = fila.textContent.toLowerCase();
            fila.style.display = contenido.includes(texto) ? "" : "none";
        });
    });
}

function normalizeDoctorRows() {
    const filas = document.querySelectorAll("#tablaDoctores tbody tr");

    filas.forEach(fila => {
        if (!fila.dataset.celular) fila.dataset.celular = "";
        if (!fila.dataset.correo) fila.dataset.correo = "";

        const scheduleSmall = fila.querySelector("td:nth-child(4) small");
        if (scheduleSmall) {
            scheduleSmall.classList.add("doctor-schedule");
        }

        const statusBadge = fila.querySelector("td:nth-child(5) .badge");
        if (statusBadge) {
            statusBadge.classList.add("doctor-status");
        }

        const actionsCell = fila.lastElementChild;
        if (actionsCell) {
            actionsCell.classList.add("table-actions");
            if (!actionsCell.querySelector('.btn-icon')) {
                const editButton = actionsCell.querySelector('[onclick="editarDoctor(this)"]');
                const deleteButton = actionsCell.querySelector('[onclick="eliminarDoctor(this)"]');

                if (editButton) {
                    editButton.classList.add('btn-icon');
                    editButton.title = 'Editar doctor';
                }
                if (deleteButton) {
                    deleteButton.classList.add('btn-icon');
                    deleteButton.title = 'Eliminar doctor';
                }
            }
        }
    });
}

function agregarDoctor() {
    const nombre = document.getElementById("nombreDoctor").value.trim();
    const dni = document.getElementById("dniDoctor").value.trim();
    const celular = document.getElementById("celularDoctor").value.trim();
    const correo = document.getElementById("correoDoctor").value.trim();
    const especialidad = document.getElementById("especialidadDoctor").value;
    const estado = document.getElementById("estadoDoctor").value;
    const horarios = obtenerHorariosDoctor();

    if (!nombre || !dni || !especialidad || Object.keys(horarios).length === 0) {
        showNotification("Complete nombre, DNI, especialidad y al menos un horario.", "error");
        return;
    }

    const badgeClass = estado === "Activo" ? "bg-success" : estado === "Inactivo" ? "bg-secondary" : "bg-warning text-dark";
    const resumenHorarios = Object.entries(horarios).map(([dia, h]) => `${dia} ${h.inicio}-${h.fin}`).join(" | ");

    if (doctorEditRow) {
        doctorEditRow.children[0].querySelector("strong").textContent = nombre;
        doctorEditRow.children[1].textContent = dni;
        doctorEditRow.children[2].textContent = especialidad;
        doctorEditRow.children[3].innerHTML = `<small>${resumenHorarios}</small>`;
        doctorEditRow.children[4].innerHTML = `<span class="badge ${badgeClass}">${estado}</span>`;
        doctorEditRow.dataset.horarios = JSON.stringify(horarios);
        doctorEditRow.dataset.celular = celular;
        doctorEditRow.dataset.correo = correo;
        showNotification("Doctor actualizado correctamente.", "success");
    } else {
        const inicial = nombre.charAt(0).toUpperCase();
        const tabla = document.querySelector("#tablaDoctores tbody");
        const nuevaFila = document.createElement("tr");

        nuevaFila.dataset.horarios = JSON.stringify(horarios);
        nuevaFila.dataset.celular = celular;
        nuevaFila.dataset.correo = correo;
        nuevaFila.innerHTML = `
            <td>
                <div class="d-flex align-items-center gap-3">
                    <div class="table-avatar">${inicial}</div>
                    <div>
                        <strong>${nombre}</strong>
                        <div class="text-muted small">${especialidad}</div>
                    </div>
                </div>
            </td>
            <td>${dni}</td>
            <td>${especialidad}</td>
            <td><small>${resumenHorarios}</small></td>
            <td>
                <span class="badge ${badgeClass}">${estado}</span>
            </td>
            <td class="table-actions">
                <div class="d-flex gap-2">
                    <button class="btn btn-light btn-sm" onclick="editarDoctor(this)">
                        <i class="bi bi-pencil"></i>
                    </button>
                    <button class="btn btn-light btn-sm text-danger" onclick="eliminarDoctor(this)">
                        <i class="bi bi-trash"></i>
                    </button>
                </div>
            </td>
        `;

        tabla.appendChild(nuevaFila);
        showNotification("Doctor agregado correctamente.", "success");
    }

    const modalEl = document.getElementById("modalDoctor");
    const modalInstance = bootstrap.Modal.getInstance(modalEl);
    if (modalInstance) {
        modalInstance.hide();
    }

    resetDoctorModal();
}

const horariosDisponibles = [
    "5:00am - 1:00pm",
    "6:00am - 12:00pm",
    "8:00am - 2:00pm",
    "8:00am - 4:00pm",
    "9:00am - 3:00pm",
    "10:00am - 7:00pm",
    "1:00pm - 9:00pm"
];

window.addEventListener("DOMContentLoaded", function () {
    // Cargar horarios por día en el formulario
    cargarHorariosForm();

    normalizeDoctorRows();

    const addDoctorButton = document.querySelector("[data-bs-target='#modalDoctor']");
    if (addDoctorButton) {
        addDoctorButton.addEventListener("click", resetDoctorModal);
    }

    loadTheme();
    cargarHorarios();
    cargarPerfil();

    const themeLight = document.getElementById('themeLight');
    const themeDark = document.getElementById('themeDark');
    if (themeLight) themeLight.addEventListener('click', () => applyTheme('light'));
    if (themeDark) themeDark.addEventListener('click', () => applyTheme('dark'));

    if (document.getElementById('reporteFinanciero')) {
        generarReporteFinanciero();
    }
});