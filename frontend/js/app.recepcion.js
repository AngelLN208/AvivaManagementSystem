
// ============================================================
//  AVIVA — app.recepcion.js
//  Módulo recepcionista: panel, citas, pacientes, pagos,
//  horario médicos, notificaciones
// ============================================================

// ─── PROTECCIÓN DE RUTA ──────────────────────────────────────
if (!localStorage.getItem('token')) {
    window.location.href = '/frontend/views/login/login.html';
}

// ─── BASE URL ────────────────────────────────────────────────
const BASE = 'http://localhost:8080/api';

// ─── UTILIDADES ──────────────────────────────────────────────

function getToken() {
    return localStorage.getItem('token');
}

function getHeaders() {
    return {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${getToken()}`
    };
}

async function apiFetch(url, options = {}) {
    const res = await fetch(url, { ...options, headers: getHeaders() });
    const data = await res.json();
    if (!res.ok) throw new Error(data.message || 'Error en la petición');
    return data.data ?? data;
}

function formatFecha(fechaStr) {
    if (!fechaStr) return '—';
    const d = new Date(fechaStr);
    if (isNaN(d)) return fechaStr;
    return d.toLocaleDateString('es-PE', { day: '2-digit', month: '2-digit', year: 'numeric' });
}

function formatFechaHora(fechaStr) {
    if (!fechaStr) return '—';
    const d = new Date(fechaStr);
    if (isNaN(d)) return fechaStr;
    return d.toLocaleString('es-PE', {
        day: '2-digit', month: '2-digit', year: 'numeric',
        hour: '2-digit', minute: '2-digit'
    });
}

function calcularEdad(fechaNac) {
    if (!fechaNac) return '—';
    const hoy = new Date();
    const nac = new Date(fechaNac);
    let edad = hoy.getFullYear() - nac.getFullYear();
    const m = hoy.getMonth() - nac.getMonth();
    if (m < 0 || (m === 0 && hoy.getDate() < nac.getDate())) edad--;
    return edad;
}

function esHoy(fechaStr) {
    if (!fechaStr) return false;
    const d = new Date(fechaStr);
    const hoy = new Date();
    return d.getDate() === hoy.getDate() &&
           d.getMonth() === hoy.getMonth() &&
           d.getFullYear() === hoy.getFullYear();
}

function badgeEstado(estado) {
    const map = {
        'PENDING':     'fondo-advertencia-sutil texto-advertencia',
        'CONFIRMED':   'fondo-exito-sutil texto-exito',
        'COMPLETED':   'fondo-info-sutil texto-info',
        'CANCELLED':   'fondo-error-sutil texto-error',
        'RESCHEDULED': 'fondo-advertencia-sutil texto-advertencia',
        'NO_SHOW':     'fondo-error-sutil texto-error',
        'PAID':        'fondo-exito-sutil texto-exito',
        'REFUNDED':    'fondo-info-sutil texto-info',
        'ACTIVE':      'fondo-exito-sutil texto-exito',
        'INACTIVE':    'fondo-error-sutil texto-error',
    };
    const label = {
        'PENDING':     'Pendiente',
        'CONFIRMED':   'Confirmada',
        'COMPLETED':   'Completada',
        'CANCELLED':   'Cancelada',
        'RESCHEDULED': 'Reprogramada',
        'NO_SHOW':     'No asistió',
        'PAID':        'Pagado',
        'REFUNDED':    'Reembolsado',
        'ACTIVE':      'Activo',
        'INACTIVE':    'Inactivo',
    };
    const cls = map[estado] || 'fondo-info-sutil texto-info';
    return `<span class="badge ${cls} rounded-pill px-3 py-1">${label[estado] || estado}</span>`;
}

function badgeMetodoPago(metodo) {
    const label = {
        'CASH':        'Efectivo',
        'CREDIT_CARD': 'T. Crédito',
        'DEBIT_CARD':  'T. Débito',
        'TRANSFER':    'Transferencia',
        'INSURANCE':   'Seguro',
    };
    return label[metodo] || metodo || '—';
}

function badgeTipoNotif(tipo) {
    const label = {
        'APPOINTMENT_CREATED':     'Cita creada',
        'APPOINTMENT_RESCHEDULED': 'Cita reprogramada',
        'APPOINTMENT_CANCELLED':   'Cita cancelada',
        'APPOINTMENT_REMINDER':    'Recordatorio',
        'APPOINTMENT_CONFIRMED':   'Cita confirmada',
        'PAYMENT_RECEIVED':        'Pago recibido',
        'PAYMENT_FAILED':          'Fallo en pago',
        'TREATMENT_PLAN':          'Plan de tratamiento',
    };
    return label[tipo] || tipo || '—';
}

function mostrarError(mensaje) {
    console.error(mensaje);
    alert(`❌ ${mensaje}`);
}

// ============================================================
//  PÁGINA: PANEL PRINCIPAL (index.html)
// ============================================================

async function cargarPanelPrincipal() {
    if (!document.getElementById('stat-pacientes-registrados') &&
        !document.getElementById('tbody-citas-hoy')) return;

    try {
        const [pacientes, citas] = await Promise.all([
            apiFetch(`${BASE}/patients`),
            apiFetch(`${BASE}/appointments`)
        ]);

        const citasHoy      = citas.filter(c => esHoy(c.appointmentDateTime));
        const realizadasHoy = citasHoy.filter(c => c.status === 'COMPLETED');
        const pendientesHoy = citasHoy.filter(c => c.status === 'PENDING' || c.status === 'CONFIRMED');

        const elPac   = document.getElementById('stat-pacientes-registrados');
        const elReal  = document.getElementById('stat-citas-realizadas');
        const elPend  = document.getElementById('stat-citas-pendientes');
        const elPacS  = document.getElementById('stat-pacientes-registrados-sub');
        const elRealS = document.getElementById('stat-citas-realizadas-sub');
        const elPendS = document.getElementById('stat-citas-pendientes-sub');

        if (elPac)   elPac.textContent   = pacientes.length;
        if (elReal)  elReal.textContent  = realizadasHoy.length;
        if (elPend)  elPend.textContent  = pendientesHoy.length;
        if (elPacS)  elPacS.textContent  = 'Total en el sistema';
        if (elRealS) elRealS.textContent = 'Consultas completadas hoy';
        if (elPendS) elPendS.textContent = 'Por atender hoy';

        // Tabla próximas citas
        const tbodyCitas = document.getElementById('tbody-citas-hoy');
        if (tbodyCitas) {
            const proximas = citasHoy
                .filter(c => c.status !== 'CANCELLED')
                .sort((a, b) => new Date(a.appointmentDateTime) - new Date(b.appointmentDateTime))
                .slice(0, 10);

            if (!proximas.length) {
                tbodyCitas.innerHTML = `<tr><td colspan="5" class="text-center text-muted py-4">No hay citas para hoy.</td></tr>`;
                return;
            }

            tbodyCitas.innerHTML = proximas.map(c => {
                const hora     = c.appointmentDateTime
                    ? new Date(c.appointmentDateTime).toLocaleTimeString('es-PE', { hour: '2-digit', minute: '2-digit' })
                    : '—';
                const paciente = `${c.patient?.firstName || ''} ${c.patient?.lastName || ''}`.trim() || '—';
                const medico   = `${c.doctor?.firstName || ''} ${c.doctor?.lastName || ''}`.trim() || '—';
                const esp      = c.doctor?.specialty?.name || '—';
                return `
                <tr>
                    <td class="fw-semibold">${hora}</td>
                    <td>${paciente}</td>
                    <td>${esp}</td>
                    <td>${medico}</td>
                    <td>${badgeEstado(c.status)}</td>
                </tr>`;
            }).join('');
        }
    } catch (e) {
        console.error('Error cargando panel principal:', e);
    }
}

// ============================================================
//  PÁGINA: CITAS (citas.html)
// ============================================================

let _todasLasCitas = [];

async function cargarCitas() {
    const tbody = document.getElementById('tabla-citas-body');
    if (!tbody) return;

    try {
        const citas = await apiFetch(`${BASE}/appointments`);
        _todasLasCitas = citas;
        renderTablaCitas(citas, tbody);
        actualizarStatsCitas(citas);
        await cargarFiltroEspecialidadesCitas();
    } catch (e) {
        tbody.innerHTML = `<tr><td colspan="7" class="text-center text-danger py-4">${e.message}</td></tr>`;
    }
}

function renderTablaCitas(citas, tbody) {
    if (!citas.length) {
        tbody.innerHTML = `<tr><td colspan="7" class="text-center text-muted py-4">No hay citas registradas.</td></tr>`;
        return;
    }

    tbody.innerHTML = citas.map(c => {
        const hora     = c.appointmentDateTime
            ? new Date(c.appointmentDateTime).toLocaleTimeString('es-PE', { hour: '2-digit', minute: '2-digit' })
            : '—';
        const fecha    = formatFecha(c.appointmentDateTime);
        const paciente = `${c.patient?.firstName || ''} ${c.patient?.lastName || ''}`.trim() || '—';
        const dni      = c.patient?.dni || '—';
        const medico   = `Dr. ${c.doctor?.firstName || ''} ${c.doctor?.lastName || ''}`.trim();
        const esp      = c.doctor?.specialty?.name || '—';
        const cancelada = c.status === 'CANCELLED' || c.status === 'COMPLETED';

        const btnRepro = !cancelada
            ? `<button class="btn btn-sm btn-light text-warning rounded-circle me-1"
                        title="Reprogramar"
                        onclick="abrirReprogramar(${c.id}, '${paciente}', ${c.doctor?.specialty?.id}, ${c.doctor?.id})">
                    <i class="fa-solid fa-clock"></i>
                </button>` : '';
        const btnCancel = !cancelada
            ? `<button class="btn btn-sm btn-light text-danger rounded-circle"
                       title="Cancelar" onclick="abrirCancelar(${c.id}, '${paciente}')">
                   <i class="fa-solid fa-ban"></i>
               </button>` : '';

        return `
        <tr data-status="${c.status}" data-especialidad="${esp}" data-dni="${dni}">
            <td class="fw-semibold">${hora}<br><small class="text-muted">${fecha}</small></td>
            <td>${paciente}</td>
            <td>${dni}</td>
            <td>${esp}</td>
            <td>${medico}</td>
            <td>${badgeEstado(c.status)}</td>
            <td class="text-end">${btnRepro}${btnCancel}</td>
        </tr>`;
    }).join('');
}

function actualizarStatsCitas(citas) {
    const elC = document.getElementById('stat-confirmadas');
    const elP = document.getElementById('stat-pendientes');
    const elX = document.getElementById('stat-canceladas');
    if (elC) elC.textContent = citas.filter(c => c.status === 'CONFIRMED').length;
    if (elP) elP.textContent = citas.filter(c => c.status === 'PENDING').length;
    if (elX) elX.textContent = citas.filter(c => c.status === 'CANCELLED').length;
}

async function cargarFiltroEspecialidadesCitas() {
    const sel = document.getElementById('filtro-especialidad');
    if (!sel) return;
    try {
        const especialidades = await apiFetch(`${BASE}/specialties`);
        sel.innerHTML = `<option value="">Todas las especialidades</option>` +
            especialidades.map(e => `<option value="${e.name}">${e.name}</option>`).join('');
    } catch { /* silencioso */ }
}

function filtrarCitas() {
    const dni = (document.getElementById('filtro-dni')?.value || '').toLowerCase().trim();
    const est = document.getElementById('filtro-estado')?.value || '';
    const esp = document.getElementById('filtro-especialidad')?.value || '';

    document.querySelectorAll('#tabla-citas-body tr').forEach(fila => {
        const okDni = !dni || (fila.dataset.dni || '').toLowerCase().includes(dni);
        const okEst = !est || fila.dataset.status === est;
        const okEsp = !esp || fila.dataset.especialidad === esp;
        fila.style.display = (okDni && okEst && okEsp) ? '' : 'none';
    });
}

// ── Modal nueva cita — especialidades ──
async function cargarEspecialidades() {
    const select = document.getElementById('nc_especialidad');
    if (!select) return;
    try {
        const especialidades = await apiFetch(`${BASE}/specialties`);
        select.innerHTML = `<option value="" disabled selected>Seleccionar especialidad...</option>` +
            especialidades.map(e => `<option value="${e.id}">${e.name}</option>`).join('');
    } catch (e) {
        console.error('Error cargando especialidades:', e);
    }
}

// ── Médicos por especialidad ──
async function cargarMedicosPorEspecialidad(specialtyId) {
    const select     = document.getElementById('nc_medico');
    const fechaInput = document.getElementById('nc_fecha');
    const slotSel    = document.getElementById('nc_horario');
    if (!select) return;

    select.disabled = true;
    select.innerHTML = `<option>Cargando...</option>`;
    if (fechaInput) { fechaInput.disabled = true; fechaInput.value = ''; }
    if (slotSel)    { slotSel.disabled = true; slotSel.innerHTML = `<option value="" disabled selected>Primero selecciona médico y fecha...</option>`; }

    try {
        const medicos = await apiFetch(`${BASE}/doctors/by-specialty/${specialtyId}`);
        if (!medicos.length) {
            select.innerHTML = `<option disabled selected>No hay médicos para esta especialidad</option>`;
            return;
        }
        select.innerHTML = `<option value="" disabled selected>Seleccionar médico...</option>` +
            medicos.map(m => `<option value="${m.id}">${m.firstName} ${m.lastName}</option>`).join('');
        select.disabled = false;
    } catch {
        select.innerHTML = `<option>Error cargando médicos</option>`;
    }
}

// ── Habilitar fecha al elegir médico ──
function habilitarFecha() {
    const medico     = document.getElementById('nc_medico')?.value;
    const fechaInput = document.getElementById('nc_fecha');
    if (!fechaInput || !medico) return;
    fechaInput.disabled = false;
    fechaInput.min = new Date().toISOString().split('T')[0];
    const slotSel = document.getElementById('nc_horario');
    if (slotSel) {
        slotSel.disabled = true;
        slotSel.innerHTML = `<option value="" disabled selected>Primero selecciona médico y fecha...</option>`;
    }
}

// ── Slots disponibles ──
async function cargarSlotsDisponibles() {
    const doctorId = document.getElementById('nc_medico')?.value;
    const fecha    = document.getElementById('nc_fecha')?.value;
    const slotSel  = document.getElementById('nc_horario');
    const hint     = document.getElementById('nc_horario_hint');
    if (!slotSel || !doctorId || !fecha) return;

    slotSel.disabled = true;
    slotSel.innerHTML = `<option>Cargando horarios...</option>`;

    try {
        const slots = await apiFetch(`${BASE}/appointments/doctor/${doctorId}/available-slots?date=${fecha}`);
        if (!slots.length) {
            slotSel.innerHTML = `<option disabled selected>No hay horarios disponibles para esta fecha</option>`;
            if (hint) hint.textContent = 'El médico no tiene horarios disponibles en esta fecha.';
            return;
        }
        slotSel.innerHTML = `<option value="" disabled selected>Seleccionar horario...</option>` +
            slots.map(s => `<option value="${s.startTime}">${s.startTime.substring(0,5)} — ${s.endTime.substring(0,5)}</option>`).join('');
        slotSel.disabled = false;
        if (hint) hint.textContent = `${slots.length} horarios disponibles.`;
    } catch {
        slotSel.innerHTML = `<option>Error cargando horarios</option>`;
    }
}

// ── Buscar paciente por DNI ──
async function buscarPacienteDni() {
    const dni      = document.getElementById('nc_dni')?.value.trim();
    const found    = document.getElementById('datos-paciente-encontrado');
    const notFound = document.getElementById('paciente-no-encontrado');
    if (!dni) return;

    try {
        const paciente = await apiFetch(`${BASE}/patients/search/dni?dni=${dni}`);
        document.getElementById('nc_avatar').textContent =
            (paciente.firstName?.[0] || '') + (paciente.lastName?.[0] || '');
        document.getElementById('nc_nombre_display').textContent =
            `${paciente.firstName} ${paciente.lastName}`;
        document.getElementById('nc_tel_display').textContent    = paciente.phone || '—';
        document.getElementById('nc_estado_display').textContent = paciente.status || '—';
        document.getElementById('nc_edad_display').textContent   = calcularEdad(paciente.dateOfBirth);
        document.getElementById('nc_dni').dataset.patientId      = paciente.id;

        found?.classList.remove('d-none');
        notFound?.classList.add('d-none');
    } catch {
        found?.classList.add('d-none');
        notFound?.classList.remove('d-none');
        document.getElementById('nc_dni').dataset.patientId = '';
    }
}

// ── Registrar nueva cita ──
async function registrarCita(e) {
    e.preventDefault();
    const patientId = document.getElementById('nc_dni')?.dataset.patientId;
    const doctorId  = document.getElementById('nc_medico')?.value;
    const fecha     = document.getElementById('nc_fecha')?.value;
    const hora      = document.getElementById('nc_horario')?.value;
    const motivo    = document.getElementById('nc_motivo')?.value || '';

    if (!patientId) { mostrarError('Primero busca y selecciona un paciente.'); return; }
    if (!doctorId)  { mostrarError('Selecciona un médico.'); return; }
    if (!fecha)     { mostrarError('Selecciona una fecha.'); return; }
    if (!hora)      { mostrarError('Selecciona un horario.'); return; }

    try {
        await apiFetch(`${BASE}/appointments`, {
            method: 'POST',
            body: JSON.stringify({
                patientId:           parseInt(patientId),
                doctorId:            parseInt(doctorId),
                appointmentDateTime: `${fecha}T${hora}`,
                reason:              motivo || null
            })
        });
        bootstrap.Modal.getInstance(document.getElementById('modalNuevaCita'))?.hide();
        document.getElementById('formNuevaCita')?.reset();
        document.getElementById('nc_medico').disabled  = true;
        document.getElementById('nc_fecha').disabled   = true;
        document.getElementById('nc_horario').disabled = true;
        document.getElementById('datos-paciente-encontrado')?.classList.add('d-none');
        cargarCitas();
    } catch (err) {
        mostrarError(err.message);
    }
}

// ── Cancelar cita ──
let _citaACancelar = null;

function abrirCancelar(id, nombre) {
    _citaACancelar = id;
    document.getElementById('cancel_nombre').textContent = nombre;
    new bootstrap.Modal(document.getElementById('modalCancelar')).show();
}

async function confirmarCancelar() {
    if (!_citaACancelar) return;
    try {
        await apiFetch(`${BASE}/appointments/${_citaACancelar}/cancel`, { method: 'PUT' });
        bootstrap.Modal.getInstance(document.getElementById('modalCancelar'))?.hide();
        _citaACancelar = null;
        cargarCitas();
    } catch (err) {
        mostrarError(err.message);
    }
}

// ── Reprogramar cita ──
let _citaAReprogramar = null;

async function abrirReprogramar(id, nombre, especialidadId, doctorId) {
    _citaAReprogramar = id;
    document.getElementById('repro_nombre_paciente').textContent = nombre;
    document.getElementById('repro_cita_id').value = id;
    document.getElementById('repro_doctor_actual_id').value = doctorId;

    // Reset
    document.getElementById('repro_fecha').disabled = true;
    document.getElementById('repro_fecha').value = '';
    document.getElementById('repro_horario').disabled = true;
    document.getElementById('repro_horario').innerHTML = `<option value="" disabled selected>Primero selecciona médico y fecha...</option>`;
    document.getElementById('repro_medico').disabled = true;

    // Cargar especialidades y preseleccionar la actual
    await cargarEspecialidadesRepro(especialidadId);

    new bootstrap.Modal(document.getElementById('modalReprogramar')).show();
}

async function cargarEspecialidadesRepro(especialidadIdActual) {
    const sel = document.getElementById('repro_especialidad');
    try {
        const especialidades = await apiFetch(`${BASE}/specialties`);
        sel.innerHTML = `<option value="" disabled>Seleccionar especialidad...</option>` +
            especialidades.map(e =>
                `<option value="${e.id}" ${e.id == especialidadIdActual ? 'selected' : ''}>${e.name}</option>`
            ).join('');
        // Cargar médicos de la especialidad actual
        await cargarMedicosRepro(especialidadIdActual);
    } catch { sel.innerHTML = `<option>Error cargando especialidades</option>`; }
}

async function cargarMedicosRepro(specialtyId) {
    const sel      = document.getElementById('repro_medico');
    const fechaInp = document.getElementById('repro_fecha');
    const slotSel  = document.getElementById('repro_horario');
    sel.disabled = true;
    sel.innerHTML = `<option>Cargando...</option>`;
    fechaInp.disabled = true;
    fechaInp.value = '';
    slotSel.disabled = true;
    slotSel.innerHTML = `<option value="" disabled selected>Primero selecciona médico y fecha...</option>`;

    try {
        const medicos = await apiFetch(`${BASE}/doctors/by-specialty/${specialtyId}`);
        if (!medicos.length) {
            sel.innerHTML = `<option disabled selected>No hay médicos disponibles</option>`;
            return;
        }
        const doctorActual = document.getElementById('repro_doctor_actual_id').value;
        sel.innerHTML = `<option value="" disabled selected>Seleccionar médico...</option>` +
            medicos.map(m =>
                `<option value="${m.id}" ${m.id == doctorActual ? 'selected' : ''}>${m.firstName} ${m.lastName}</option>`
            ).join('');
        sel.disabled = false;
        // Si hay médico preseleccionado habilitar fecha
        if (sel.value) fechaInp.disabled = false;
    } catch {
        sel.innerHTML = `<option>Error cargando médicos</option>`;
    }
}

async function cargarSlotsRepro() {
    const doctorId = document.getElementById('repro_medico')?.value;
    const fecha    = document.getElementById('repro_fecha')?.value;
    const slotSel  = document.getElementById('repro_horario');
    if (!slotSel || !doctorId || !fecha) return;

    slotSel.disabled = true;
    slotSel.innerHTML = `<option>Cargando horarios...</option>`;

    try {
        const slots = await apiFetch(`${BASE}/appointments/doctor/${doctorId}/available-slots?date=${fecha}`);
        if (!slots.length) {
            slotSel.innerHTML = `<option disabled selected>No hay horarios disponibles</option>`;
            return;
        }
        slotSel.innerHTML = `<option value="" disabled selected>Seleccionar horario...</option>` +
            slots.map(s => `<option value="${s.startTime}">${s.startTime.substring(0,5)} — ${s.endTime.substring(0,5)}</option>`).join('');
        slotSel.disabled = false;
    } catch {
        slotSel.innerHTML = `<option>Error cargando horarios</option>`;
    }
}

async function confirmarReprogramar() {
    if (!_citaAReprogramar) return;
    const doctorId = document.getElementById('repro_medico')?.value;
    const fecha    = document.getElementById('repro_fecha')?.value;
    const hora     = document.getElementById('repro_horario')?.value;

    if (!doctorId) { mostrarError('Selecciona un médico.'); return; }
    if (!fecha)    { mostrarError('Selecciona una fecha.'); return; }
    if (!hora)     { mostrarError('Selecciona un horario.'); return; }

    try {
        await apiFetch(
            `${BASE}/appointments/${_citaAReprogramar}/reschedule?newDateTime=${fecha}T${hora}`,
            { method: 'PUT' }
        );
        bootstrap.Modal.getInstance(document.getElementById('modalReprogramar'))?.hide();
        _citaAReprogramar = null;
        cargarCitas();
    } catch (err) {
        mostrarError(err.message);
    }
}

// ============================================================
//  PÁGINA: PACIENTES (pacientes.html)
// ============================================================

async function cargarPacientes() {
    const tbody = document.getElementById('tabla-pacientes-body');
    if (!tbody) return;

    try {
        const [pacientes, citas] = await Promise.all([
            apiFetch(`${BASE}/patients`),
            apiFetch(`${BASE}/appointments`)
        ]);
        renderTablaPacientes(pacientes, tbody);
        actualizarStatsPacientes(pacientes, citas);
    } catch (e) {
        tbody.innerHTML = `<tr><td colspan="7" class="text-center text-danger py-4">${e.message}</td></tr>`;
    }
}

function renderTablaPacientes(pacientes, tbody) {
    if (!pacientes.length) {
        tbody.innerHTML = `<tr><td colspan="7" class="text-center text-muted py-4">No hay pacientes registrados.</td></tr>`;
        return;
    }

    tbody.innerHTML = pacientes.map(p => {
        const nombre = `${p.firstName || ''} ${p.lastName || ''}`.trim();
        return `
        <tr>
            <td class="fw-semibold">${nombre}</td>
            <td>${p.dni || '—'}</td>
            <td>${calcularEdad(p.dateOfBirth)}</td>
            <td>${p.phone || '—'}</td>
            <td>${p.email || '—'}</td>
            <td>${badgeEstado(p.status)}</td>
            <td class="text-end">
                <button class="btn btn-sm btn-light text-primary rounded-circle"
                        title="Ver paciente" onclick="verPaciente(${p.id})">
                    <i class="fa-solid fa-eye"></i>
                </button>
            </td>
        </tr>`;
    }).join('');
}

function actualizarStatsPacientes(pacientes, citas) {
    const elTotal = document.getElementById('stat-total');
    const elHoy   = document.getElementById('stat-cita-hoy');
    if (elTotal) elTotal.textContent = pacientes.length;
    if (elHoy && citas) {
        const idsConCitaHoy = new Set(
            citas
                .filter(c => esHoy(c.appointmentDateTime) && c.status !== 'CANCELLED')
                .map(c => c.patient?.id)
                .filter(Boolean)
        );
        elHoy.textContent = idsConCitaHoy.size;
    }
}

async function registrarPaciente(e) {
    e.preventDefault();
    const firstName   = document.getElementById('p_nombre')?.value.trim();
    const lastName    = document.getElementById('p_apellido')?.value.trim();
    const dni         = document.getElementById('p_dni')?.value.trim();
    const dateOfBirth = document.getElementById('p_fecha_nac')?.value;
    const gender      = document.getElementById('p_genero')?.value;
    const codigoPais = document.getElementById('p_codigo_pais')?.value.trim();
    const numTel     = document.getElementById('p_telefono')?.value.trim();
    const phone      = (codigoPais && numTel) ? `${codigoPais}${numTel}` : '';
    const email       = document.getElementById('p_email')?.value.trim();
    const address     = document.getElementById('p_direccion')?.value.trim();

    if (!firstName || !lastName || !dni || !dateOfBirth || !gender) {
        mostrarError('Nombre, apellido, DNI, fecha de nacimiento y género son obligatorios.');
        return;
    }

    try {
        await apiFetch(`${BASE}/patients`, {
            method: 'POST',
            body: JSON.stringify({ firstName, lastName, dni, dateOfBirth, gender, phone, email, address })
        });
        bootstrap.Modal.getInstance(document.getElementById('modalPaciente'))?.hide();
        document.getElementById('formPaciente')?.reset();
        cargarPacientes();
    } catch (err) {
        mostrarError(err.message);
    }
}

async function verPaciente(id) {
    try {
        const [p, citas] = await Promise.all([
            apiFetch(`${BASE}/patients/${id}`),
            apiFetch(`${BASE}/appointments/patient/${id}`)
        ]);

        document.getElementById('ver_nombre').textContent   = `${p.firstName} ${p.lastName}`;
        document.getElementById('ver_dni').textContent      = p.dni || '—';
        document.getElementById('ver_edad').textContent     = calcularEdad(p.dateOfBirth);
        document.getElementById('ver_telefono').textContent = p.phone || '—';
        document.getElementById('ver_email').textContent    = p.email || '—';
        document.getElementById('ver_avatar').textContent   =
            (p.firstName?.[0] || '') + (p.lastName?.[0] || '');

        const listaCitas = document.getElementById('ver_citas_lista');
        if (listaCitas) {
            if (!citas.length) {
                listaCitas.innerHTML = `<span class="text-muted">Sin citas registradas.</span>`;
            } else {
                listaCitas.innerHTML = citas.slice(0, 5).map(c => `
                    <div class="d-flex justify-content-between align-items-center py-1 border-bottom">
                        <span class="small">${formatFechaHora(c.appointmentDateTime)} — Dr. ${c.doctor?.firstName || ''} ${c.doctor?.lastName || ''}</span>
                        ${badgeEstado(c.status)}
                    </div>`).join('');
            }
        }

        document.getElementById('btn-ir-nueva-cita').dataset.patientId = id;
        new bootstrap.Modal(document.getElementById('modalVerPaciente')).show();
    } catch (err) {
        mostrarError(err.message);
    }
}

// ============================================================
//  PÁGINA: PAGOS (pagos.html)
// ============================================================

let _todosPagos = [];

async function cargarPagos() {
    const tbody = document.getElementById('tabla-pagos-body');
    if (!tbody) return;

    try {
        const [pagos, citas] = await Promise.all([
            apiFetch(`${BASE}/payments`),
            apiFetch(`${BASE}/appointments`)
        ]);

        // Cruzar pagos con citas
        _todosPagos = pagos.map(p => ({
            ...p,
            appointment: citas.find(c => c.id === p.appointmentId) || null
        }));

        renderTablaPagos(_todosPagos, tbody);
        actualizarStatsPagos(_todosPagos);
    } catch (e) {
        tbody.innerHTML = `<tr><td colspan="6" class="text-center text-danger py-4">${e.message}</td></tr>`;
    }
}

function renderTablaPagos(pagos, tbody) {
    if (!pagos.length) {
        tbody.innerHTML = `<tr><td colspan="6" class="text-center text-muted py-4">No hay pagos registrados.</td></tr>`;
        return;
    }

    tbody.innerHTML = pagos.map(p => {
        const paciente  = `${p.appointment?.patient?.firstName || ''} ${p.appointment?.patient?.lastName || ''}`.trim() || '—';
        const dni       = p.appointment?.patient?.dni || '';
        const fechaCita = formatFechaHora(p.appointment?.appointmentDateTime);
        const monto     = `S/ ${parseFloat(p.amount || 0).toFixed(2)}`;

        const acciones = p.status === 'PENDING'
            ? `<button class="btn btn-sm btn-light text-success rounded-circle"
                       title="Procesar pago"
                       onclick="abrirProcesarPago(${p.id}, '${paciente}', '${fechaCita}', '${monto}', ${p.appointment?.id})">
                   <i class="fa-solid fa-check"></i>
               </button>`
            : p.status === 'PAID'
            ? `<button class="btn btn-sm btn-light text-primary rounded-circle"
                       title="Ver comprobante"
                       onclick="verComprobante(${p.id})">
                   <i class="fa-solid fa-receipt"></i>
               </button>`
            : '';

        return `
        <tr data-status="${p.status}" data-dni="${dni}">
            <td class="fw-semibold">${paciente}</td>
            <td>${fechaCita}</td>
            <td>${badgeMetodoPago(p.method)}</td>
            <td class="fw-semibold">${monto}</td>
            <td>${badgeEstado(p.status)}</td>
            <td class="text-end">${acciones}</td>
        </tr>`;
    }).join('');
}

function actualizarStatsPagos(pagos) {
    const pendientes    = pagos.filter(p => p.status === 'PENDING');
    const realizadosHoy = pagos.filter(p => p.status === 'PAID' && esHoy(p.paymentDate || p.updatedAt));
    const cancelaciones = pagos.filter(p => (p.status === 'CANCELLED' || p.status === 'REFUNDED') && esHoy(p.updatedAt));
    const totalHoy      = realizadosHoy.reduce((s, p) => s + parseFloat(p.amount || 0), 0);

    const elDia  = document.getElementById('stat-pagos-dia');
    const elPend = document.getElementById('stat-pendientes');
    const elReal = document.getElementById('stat-realizados-hoy');
    const elCanc = document.getElementById('stat-cancelaciones');

    if (elDia)  elDia.textContent  = `S/ ${totalHoy.toFixed(2)}`;
    if (elPend) elPend.textContent = pendientes.length;
    if (elReal) elReal.textContent = realizadosHoy.length;
    if (elCanc) elCanc.textContent = cancelaciones.length;
}

function filtrarPagos() {
    const dni = (document.getElementById('filtro-dni')?.value || '').trim();
    const est = document.getElementById('filtro-estado')?.value || '';

    document.querySelectorAll('#tabla-pagos-body tr').forEach(fila => {
        const okDni = !dni || (fila.dataset.dni || '').includes(dni);
        const okEst = !est || fila.dataset.status === est;
        fila.style.display = (okDni && okEst) ? '' : 'none';
    });
}

let _pagoAProcesar = null;

function abrirProcesarPago(pagoId, paciente, fechaCita, monto, appointmentId) {
    _pagoAProcesar = { pagoId, appointmentId };
    document.getElementById('pp_pago_id').value          = pagoId;
    document.getElementById('pp_appointment_id').value   = appointmentId;
    document.getElementById('pp_paciente').textContent   = paciente;
    document.getElementById('pp_fecha_cita').textContent = fechaCita;
    document.getElementById('pp_monto').textContent      = monto;

    const pago   = _todosPagos.find(p => p.id === pagoId);
    const medico = pago?.appointment?.doctor
        ? `${pago.appointment.doctor.firstName} ${pago.appointment.doctor.lastName}`
        : '—';
    document.getElementById('pp_medico').textContent = medico;
    document.getElementById('pp_metodo').value = '';

    new bootstrap.Modal(document.getElementById('modalProcesarPago')).show();
}

async function confirmarPago() {
    if (!_pagoAProcesar) return;
    const metodo = document.getElementById('pp_metodo')?.value;
    if (!metodo) { mostrarError('Selecciona un método de pago.'); return; }

    try {
        await apiFetch(
            `${BASE}/payments/${_pagoAProcesar.appointmentId}/process?method=${metodo}`,
            { method: 'POST' }
        );
        bootstrap.Modal.getInstance(document.getElementById('modalProcesarPago'))?.hide();
        _pagoAProcesar = null;
        cargarPagos();
    } catch (err) {
        mostrarError(err.message);
    }
}

async function verComprobante(pagoId) {
    try {
        const recibos = await apiFetch(`${BASE}/receipts`);
        const pago    = _todosPagos.find(p => p.id === pagoId);
        const recibo  = recibos.find(r => r.payment?.id === pagoId);

        document.getElementById('comp_numero').textContent     = recibo?.receiptNumber || '—';
        document.getElementById('comp_paciente').textContent   =
            `${pago?.appointment?.patient?.firstName || ''} ${pago?.appointment?.patient?.lastName || ''}`.trim() || '—';
        document.getElementById('comp_medico').textContent     =
            `${pago?.appointment?.doctor?.firstName || ''} ${pago?.appointment?.doctor?.lastName || ''}`.trim() || '—';
        document.getElementById('comp_fecha_cita').textContent = formatFechaHora(pago?.appointment?.appointmentDateTime);
        document.getElementById('comp_metodo').textContent     = badgeMetodoPago(pago?.method);
        document.getElementById('comp_fecha_pago').textContent = formatFecha(pago?.paymentDate || pago?.updatedAt);
        document.getElementById('comp_monto').textContent      = `S/ ${parseFloat(pago?.amount || 0).toFixed(2)}`;

        new bootstrap.Modal(document.getElementById('modalComprobante')).show();
    } catch (err) {
        mostrarError(err.message);
    }
}

// ============================================================
//  PÁGINA: HORARIO MÉDICOS (horarioMedicos.html)
// ============================================================

async function cargarHorarioMedicos() {
    const tbody = document.getElementById('tabla-horarios-body');
    if (!tbody) return;

    try {
        const doctores = await apiFetch(`${BASE}/doctors`);
        const horarios = [];

        await Promise.all(doctores.map(async d => {
            try {
                const schedules = await apiFetch(`${BASE}/medical-schedules/doctor/${d.id}`);
                schedules.forEach(s => horarios.push({ doctor: d, schedule: s }));
            } catch { /* doctor sin horario */ }
        }));

        renderTablaHorarios(horarios, tbody);
        cargarFiltroEspecialidadesHorarios(doctores);
    } catch (e) {
        tbody.innerHTML = `<tr><td colspan="7" class="text-center text-danger py-4">${e.message}</td></tr>`;
    }
}

function renderTablaHorarios(horarios, tbody) {
    if (!horarios.length) {
        tbody.innerHTML = `<tr><td colspan="7" class="text-center text-muted py-4">No hay horarios registrados.</td></tr>`;
        return;
    }

    const diasEs = {
        MONDAY: 'Lunes', TUESDAY: 'Martes', WEDNESDAY: 'Miércoles',
        THURSDAY: 'Jueves', FRIDAY: 'Viernes', SATURDAY: 'Sábado', SUNDAY: 'Domingo'
    };

    tbody.innerHTML = horarios.map(({ doctor: d, schedule: s }) => {
        const nombre  = `Dr. ${d.firstName} ${d.lastName}`;
        const esp     = d.specialty?.name || '—';
        const dia     = diasEs[s.dayOfWeek] || s.dayOfWeek;
        const horario = `${s.startTime?.substring(0,5)} — ${s.endTime?.substring(0,5)}`;
        const estado  = s.active
            ? `<span class="badge fondo-exito-sutil texto-exito rounded-pill px-3 py-1">Activo</span>`
            : `<span class="badge fondo-error-sutil texto-error rounded-pill px-3 py-1">Inactivo</span>`;

        return `
            <tr data-nombre="${nombre.toLowerCase()}" data-especialidad="${esp}">
                <td class="fw-semibold">${nombre}</td>
                <td>${esp}</td>
                <td>${dia}</td>
                <td>${horario}</td>
                <td>${s.appointmentDurationMinutes} min</td>
                <td>${s.maxAppointmentsPerDay}</td>
                <td>${estado}</td>
                <td class="text-end">
                    <button class="btn btn-sm btn-light text-primary rounded-circle"
                            title="Ver disponibilidad"
                            onclick="abrirDisponibilidad(${d.id}, '${nombre}', '${esp}')">
                        <i class="fa-solid fa-eye"></i>
                    </button>
                </td>
            </tr>`;
    }).join('');
}

function cargarFiltroEspecialidadesHorarios(doctores) {
    const sel = document.getElementById('filtro-especialidad');
    if (!sel) return;
    const nombres = [...new Set(doctores.map(d => d.specialty?.name).filter(Boolean))];
    sel.innerHTML = `<option value="">Todas las especialidades</option>` +
        nombres.map(n => `<option value="${n}">${n}</option>`).join('');
}

function filtrarHorarios() {
    const nombre = (document.getElementById('filtro-nombre')?.value || '').toLowerCase().trim();
    const esp    = document.getElementById('filtro-especialidad')?.value || '';

    document.querySelectorAll('#tabla-horarios-body tr').forEach(fila => {
        const okNombre = !nombre || (fila.dataset.nombre || '').includes(nombre);
        const okEsp    = !esp    || fila.dataset.especialidad === esp;
        fila.style.display = (okNombre && okEsp) ? '' : 'none';
    });
}

// ============================================================
//  PÁGINA: NOTIFICACIONES (notificaciones.html)
// ============================================================

async function cargarNotificaciones() {
    const tbody = document.getElementById('tabla-notificaciones-body');
    if (!tbody) return;

    try {
        const email = localStorage.getItem('email') || '';
        const notifs = email
            ? await apiFetch(`${BASE}/notifications/user?email=${encodeURIComponent(email)}`)
            : [];

        renderTablaNotificaciones(notifs, tbody);
        actualizarStatsNotificaciones(notifs);
    } catch (e) {
        tbody.innerHTML = `<tr><td colspan="6" class="text-center text-danger py-4">${e.message}</td></tr>`;
    }
}

function renderTablaNotificaciones(notifs, tbody) {
    if (!notifs.length) {
        tbody.innerHTML = `<tr><td colspan="6" class="text-center text-muted py-4">No hay notificaciones.</td></tr>`;
        return;
    }

    const estadoBadge = {
        'PENDING':   'fondo-advertencia-sutil texto-advertencia',
        'SENT':      'fondo-info-sutil texto-info',
        'DELIVERED': 'fondo-exito-sutil texto-exito',
        'FAILED':    'fondo-error-sutil texto-error',
    };
    const estadoLabel = {
        'PENDING': 'Pendiente', 'SENT': 'Enviada', 'DELIVERED': 'Entregada', 'FAILED': 'Fallida'
    };

    const sorted = [...notifs].sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));

    tbody.innerHTML = sorted.map(n => {
        const cls = estadoBadge[n.status] || 'fondo-info-sutil texto-info';
        return `
        <tr data-tipo="${n.type || ''}" data-canal="${n.channel || ''}" data-estado="${n.status || ''}">
            <td>${badgeTipoNotif(n.type)}</td>
            <td>${n.recipientName || n.recipientEmail || '—'}</td>
            <td class="text-truncate" style="max-width:200px" title="${n.subject || ''}">${n.subject || '—'}</td>
            <td>${n.channel || '—'}</td>
            <td><span class="badge ${cls} rounded-pill px-3 py-1">${estadoLabel[n.status] || n.status}</span></td>
            <td>${formatFechaHora(n.createdAt)}</td>
        </tr>`;
    }).join('');
}

function actualizarStatsNotificaciones(notifs) {
    const elTotal = document.getElementById('stat-total-notif');
    if (elTotal) elTotal.textContent = notifs.length;
}

function filtrarNotificaciones() {
    const tipo   = document.getElementById('filtro-tipo')?.value   || '';
    const canal  = document.getElementById('filtro-canal')?.value  || '';
    const estado = document.getElementById('filtro-estado')?.value || '';

    document.querySelectorAll('#tabla-notificaciones-body tr').forEach(fila => {
        const okTipo   = !tipo   || fila.dataset.tipo   === tipo;
        const okCanal  = !canal  || fila.dataset.canal  === canal;
        const okEstado = !estado || fila.dataset.estado === estado;
        fila.style.display = (okTipo && okCanal && okEstado) ? '' : 'none';
    });
}

let _dispDoctorId = null;

function abrirDisponibilidad(doctorId, nombre, especialidad) {
    _dispDoctorId = doctorId;
    document.getElementById('disp_avatar').textContent =
        nombre.replace('Dr. ', '').split(' ').map(n => n[0]).join('').substring(0, 2);
    document.getElementById('disp_nombre').textContent       = nombre;
    document.getElementById('disp_especialidad').textContent = especialidad;
    document.getElementById('disp_fecha').value              = '';
    document.getElementById('disp_fecha').min                = new Date().toISOString().split('T')[0];
    document.getElementById('disp_slots_wrapper').classList.add('d-none');
    document.getElementById('disp_sin_slots').classList.add('d-none');
    new bootstrap.Modal(document.getElementById('modalDisponibilidad')).show();
}

async function cargarSlotsDisponibilidad() {
    const fecha = document.getElementById('disp_fecha')?.value;
    if (!fecha || !_dispDoctorId) return;

    const slotsWrapper = document.getElementById('disp_slots_wrapper');
    const sinSlots     = document.getElementById('disp_sin_slots');
    const slotsDiv     = document.getElementById('disp_slots');

    slotsDiv.innerHTML = `<span class="text-muted small">Cargando...</span>`;
    slotsWrapper.classList.remove('d-none');
    sinSlots.classList.add('d-none');

    try {
        const slots = await apiFetch(`${BASE}/appointments/doctor/${_dispDoctorId}/available-slots?date=${fecha}`);
        if (!slots.length) {
            slotsWrapper.classList.add('d-none');
            sinSlots.classList.remove('d-none');
            return;
        }
        slotsDiv.innerHTML = slots.map(s =>
            `<span class="badge fondo-exito-sutil texto-exito rounded-pill px-3 py-2">
                ${s.startTime.substring(0,5)} — ${s.endTime.substring(0,5)}
            </span>`
        ).join('');
        slotsWrapper.classList.remove('d-none');
    } catch {
        slotsDiv.innerHTML = `<span class="text-danger small">Error cargando horarios.</span>`;
    }
}

// ============================================================
//  INIT
// ============================================================

document.addEventListener('DOMContentLoaded', () => {

    // ── Panel Principal ──
    if (document.getElementById('stat-pacientes-registrados') ||
        document.getElementById('tbody-citas-hoy')) {
        cargarPanelPrincipal();
    }

    // ── Citas ──
    if (document.getElementById('tabla-citas-body')) {
        cargarCitas();
        cargarEspecialidades();

        document.getElementById('nc_especialidad')
            ?.addEventListener('change', function () { cargarMedicosPorEspecialidad(this.value); });

        document.getElementById('nc_medico')
            ?.addEventListener('change', habilitarFecha);

        document.getElementById('nc_fecha')
            ?.addEventListener('change', cargarSlotsDisponibles);

        document.getElementById('btn-buscar-dni')
            ?.addEventListener('click', buscarPacienteDni);

        document.getElementById('nc_dni')
            ?.addEventListener('keydown', e => { if (e.key === 'Enter') { e.preventDefault(); buscarPacienteDni(); } });

        document.getElementById('formNuevaCita')
            ?.addEventListener('submit', registrarCita);

        document.getElementById('btn-confirmar-cancelar')
            ?.addEventListener('click', confirmarCancelar);

        document.getElementById('btn-confirmar-reprogramar')
            ?.addEventListener('click', confirmarReprogramar);

        document.getElementById('filtro-dni')
            ?.addEventListener('input', filtrarCitas);
        document.getElementById('filtro-estado')
            ?.addEventListener('change', filtrarCitas);
        document.getElementById('filtro-especialidad')
            ?.addEventListener('change', filtrarCitas);
        document.getElementById('btn-limpiar-filtros')
            ?.addEventListener('click', () => {
                document.getElementById('filtro-dni').value = '';
                document.getElementById('filtro-estado').value = '';
                document.getElementById('filtro-especialidad').value = '';
                filtrarCitas();
            });
        document.getElementById('repro_especialidad')
            ?.addEventListener('change', function () { cargarMedicosRepro(this.value); });

        document.getElementById('repro_medico')
            ?.addEventListener('change', function () {
                const fechaInp = document.getElementById('repro_fecha');
                if (fechaInp) {
                    fechaInp.disabled = false;
                    fechaInp.min = new Date().toISOString().split('T')[0];
                    fechaInp.value = '';
                }
            document.getElementById('repro_horario').disabled = true;
    });

document.getElementById('repro_fecha')
    ?.addEventListener('change', cargarSlotsRepro);
    }

    // ── Pacientes ──
    if (document.getElementById('tabla-pacientes-body')) {
        cargarPacientes();

        document.getElementById('formPaciente')
            ?.addEventListener('submit', registrarPaciente);

        document.getElementById('btn-ir-nueva-cita')
            ?.addEventListener('click', () => {
                bootstrap.Modal.getInstance(document.getElementById('modalVerPaciente'))?.hide();
                window.location.href = 'citas.html';
            });
    }

    // ── Pagos ──
    if (document.getElementById('tabla-pagos-body')) {
        cargarPagos();

        document.getElementById('btn-confirmar-pago')
            ?.addEventListener('click', confirmarPago);

        document.getElementById('filtro-dni')
            ?.addEventListener('input', filtrarPagos);
        document.getElementById('filtro-estado')
            ?.addEventListener('change', filtrarPagos);
        document.getElementById('btn-limpiar-filtros')
            ?.addEventListener('click', () => {
                document.getElementById('filtro-dni').value = '';
                document.getElementById('filtro-estado').value = '';
                filtrarPagos();
            });
    }

    // ── Horario Médicos ──
    if (document.getElementById('tabla-horarios-body')) {
        cargarHorarioMedicos();

        document.getElementById('filtro-nombre')
            ?.addEventListener('input', filtrarHorarios);
        document.getElementById('filtro-especialidad')
            ?.addEventListener('change', filtrarHorarios);
        document.getElementById('btn-limpiar-filtros')
            ?.addEventListener('click', () => {
                document.getElementById('filtro-nombre').value = '';
                document.getElementById('filtro-especialidad').value = '';
                filtrarHorarios();
            });
        document.getElementById('disp_fecha')
            ?.addEventListener('change', cargarSlotsDisponibilidad);
    }

    // ── Notificaciones ──
    if (document.getElementById('tabla-notificaciones-body')) {
        cargarNotificaciones();

        document.getElementById('filtro-tipo')
            ?.addEventListener('change', filtrarNotificaciones);
        document.getElementById('filtro-canal')
            ?.addEventListener('change', filtrarNotificaciones);
        document.getElementById('filtro-estado')
            ?.addEventListener('change', filtrarNotificaciones);
        document.getElementById('btn-limpiar-filtros')
            ?.addEventListener('click', () => {
                document.getElementById('filtro-tipo').value   = '';
                document.getElementById('filtro-canal').value  = '';
                document.getElementById('filtro-estado').value = '';
                filtrarNotificaciones();
            });
    }

});
