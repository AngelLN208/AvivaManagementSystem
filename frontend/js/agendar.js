document.addEventListener('DOMContentLoaded', () => {
    const selectEspecialidad = document.getElementById('especialidad');
    const selectSede = document.getElementById('sede');
    const inputNombreDoctor = document.getElementById('nombre-doctor');
    const contenedorDoctores = document.getElementById('lista-doctores');

    // 1. FUNCIÓN PARA OBTENER LOS DOCTORES DEL BACKEND
    async function cargarDoctores() {
        try {
            // Sacamos la "llave" que guardamos al hacer login
            const token = localStorage.getItem('token');
            if (!token) {
                alert('Sesión expirada. Por favor, inicia sesión de nuevo.');
                window.location.href = '../login/login.html';
                return;
            }

            // Llamamos al endpoint que encontraste en Swagger
            const response = await fetch('http://localhost:8080/api/doctors', {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}` // Metemos la llave en el candado
                }
            });

            if (response.ok) {
                const datos = await response.json();
                
                // Spring Boot a veces manda listas puras o las mete dentro de "content" si hay paginación.
                const doctores = Array.isArray(datos) ? datos : (datos.content || []);
                console.log("Lista de doctores que mandó el backend:", doctores);
                
                renderizarDoctores(doctores);
            } else {
                console.error('Error al cargar la lista de doctores del servidor');
            }
        } catch (error) {
            console.error('Error de conexión:', error);
        }
    }

    // 2. FUNCIÓN PARA DIBUJAR LAS TARJETAS EN PANTALLA
    function renderizarDoctores(doctores) {
        contenedorDoctores.innerHTML = ''; // Limpiamos el contenedor por si acaso

        doctores.forEach(doctor => {
            // Adaptamos los datos de tu base de datos
            const nombre = doctor.firstName || doctor.first_name || 'Doctor';
            const apellido = doctor.lastName || doctor.last_name || '';
            const especialidad = doctor.specialty?.name || 'General';
            // Como no vi "Sede" en tu base de datos, pondremos Lima Centro por defecto para que el filtro funcione
            const sede = 'Lima Centro'; 

            // Creamos la tarjeta HTML
            const cardHTML = `
                <div class="doctor-card" data-especialidad="${especialidad.toLowerCase()}" data-sede="${sede.toLowerCase().replace(' ', '-')}">
                    <img src="../../img/Djorge.png" alt="Foto del doctor" class="doctor-photo">
                    <div class="doctor-details">
                        <h3 class="doctor-name">Dr(a). ${nombre} ${apellido}</h3>
                        <p class="specialty-text">Especialidad: ${especialidad}</p>
                        <p class="sede-text">Sede: ${sede}</p>
                        <a href="perfil.html" class="btn-outline">Ver perfil</a>
                    </div>
                    <div class="doctor-schedule">
                        <div class="schedule-tabs">
                            <button class="tab active">Horarios Disponibles</button>
                        </div>
                        <div class="time-slots">
                            <a href="datos.html" class="time-btn">Seleccionar Horario</a>
                        </div>
                    </div>
                </div>
            `;
            // Inyectamos la tarjeta en el HTML vacío
            contenedorDoctores.innerHTML += cardHTML;
        });

        // Una vez que ya están dibujados, activamos los filtros
        filtrarDoctores();
    }

    // 3. FUNCIÓN DE FILTROS (La misma de antes, pero adaptada)
    function filtrarDoctores() {
        const especialidadSeleccionada = selectEspecialidad.value.toLowerCase();
        const sedeSeleccionada = selectSede.value.toLowerCase();
        const textoBusqueda = inputNombreDoctor.value.toLowerCase().trim();
        
        const doctorCards = document.querySelectorAll('.doctor-card');

        doctorCards.forEach(card => {
            const especialidadDoctor = card.getAttribute('data-especialidad');
            const sedeDoctor = card.getAttribute('data-sede');
            const nombreDoctor = card.querySelector('.doctor-name').textContent.toLowerCase();
            
            // Verificamos si coinciden (includes permite buscar partes de la palabra)
            const matchEspecialidad = (especialidadSeleccionada === 'todas' || especialidadDoctor.includes(especialidadSeleccionada));
            const matchSede = (sedeSeleccionada === 'todas' || sedeDoctor === sedeSeleccionada);
            const matchNombre = nombreDoctor.includes(textoBusqueda);

            if (matchEspecialidad && matchSede && matchNombre) {
                card.style.display = 'flex';
            } else {
                card.style.display = 'none';
            }
        });
    }

    // 4. EVENTOS
    selectEspecialidad.addEventListener('change', filtrarDoctores);
    selectSede.addEventListener('change', filtrarDoctores);
    inputNombreDoctor.addEventListener('input', filtrarDoctores);

    // Arrancamos la carga ni bien abre la página
    cargarDoctores();
});