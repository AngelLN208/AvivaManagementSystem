document.addEventListener('DOMContentLoaded', () => {
    const selectEspecialidad = document.getElementById('especialidad');
    const selectSede = document.getElementById('sede');
    const inputNombreDoctor = document.getElementById('nombre-doctor');
    const doctorCards = document.querySelectorAll('.doctor-card');

    function filtrarDoctores() {
        const especialidadSeleccionada = selectEspecialidad.value;
        const sedeSeleccionada = selectSede.value;
        const textoBusqueda = inputNombreDoctor.value.toLowerCase().trim();

        doctorCards.forEach(card => {
            const especialidadDoctor = card.getAttribute('data-especialidad');
            const sedeDoctor = card.getAttribute('data-sede');
            const nombreDoctor = card.querySelector('.doctor-name').textContent.toLowerCase();
            
            const matchEspecialidad = (especialidadSeleccionada === 'todas' || especialidadSeleccionada === especialidadDoctor);
            const matchSede = (sedeSeleccionada === 'todas' || sedeSeleccionada === sedeDoctor);
            const matchNombre = nombreDoctor.includes(textoBusqueda);

            if (matchEspecialidad && matchSede && matchNombre) {
                card.style.display = 'flex';
            } else {
                card.style.display = 'none';
            }
        });
    }

    selectEspecialidad.addEventListener('change', filtrarDoctores);
    selectSede.addEventListener('change', filtrarDoctores);
    inputNombreDoctor.addEventListener('input', filtrarDoctores);
});