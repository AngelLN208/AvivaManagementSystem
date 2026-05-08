const botonModoOscuro = document.getElementById('boton-modo-oscuro');
const botonHamburgesa = document.getElementById('boton-hamburguesa');
const sidebar = document.getElementById('sidebar-wrapper');
const body = document.body;

// Mapeo de enlaces del menú a paneles
const mapeoMenuPaneles = {
  0: 'panel-principal',
  1: 'panel-citas',
  2: 'panel-pacientes',
  3: 'panel-medicos',
  4: 'panel-departamentos'
};

document.addEventListener('DOMContentLoaded', () => {
  const temaGuardado = localStorage.getItem('tema') || 'claro';
  aplicarTema(temaGuardado);
  
  verificarTamañoPantalla();
  window.addEventListener('resize', verificarTamañoPantalla);
  
  // Configurar eventos de navegación
  configurarNavegacion();
});

botonModoOscuro.addEventListener('click', () => {
  const temaActual = body.getAttribute('data-tema');
  const nuevoTema = temaActual === 'claro' ? 'oscuro' : 'claro';
  aplicarTema(nuevoTema);
  localStorage.setItem('tema', nuevoTema);
});

botonHamburgesa.addEventListener('click', () => {
  sidebar.classList.toggle('menu-abierto');
});

function configurarNavegacion() {
  const enlacesMenu = sidebar.querySelectorAll('.list-group-item');
  
  enlacesMenu.forEach((enlace, index) => {
    enlace.addEventListener('click', (e) => {
      e.preventDefault();
      
      // Remover clase activa de todos
      enlacesMenu.forEach(link => {
        link.classList.remove('nav-activo');
        link.classList.add('nav-link-custom');
      });
      
      // Añadir clase activa al actual
      enlace.classList.add('nav-activo');
      enlace.classList.remove('nav-link-custom');
      
      // Cambiar panel visible
      cambiarPanel(mapeoMenuPaneles[index]);
      
      // Cerrar menú en dispositivos móviles
      if (window.innerWidth < 992) {
        sidebar.classList.remove('menu-abierto');
      }
    });
  });
}

function cambiarPanel(idPanel) {
  // Ocultar todos los paneles
  const paneles = document.querySelectorAll('[id^="panel-"]');
  paneles.forEach(panel => {
    panel.classList.remove('contenido-activo');
    panel.classList.add('contenido-oculto');
  });
  
  // Mostrar panel seleccionado
  const panelActual = document.getElementById(idPanel);
  if (panelActual) {
    panelActual.classList.remove('contenido-oculto');
    panelActual.classList.add('contenido-activo');
  }
}

document.addEventListener('click', (e) => {
  if (!sidebar.contains(e.target) && e.target !== botonHamburgesa && !botonHamburgesa.contains(e.target)) {
    sidebar.classList.remove('menu-abierto');
  }
});

function aplicarTema(tema) {
  body.setAttribute('data-tema', tema);
  
  if (tema === 'oscuro') {
    botonModoOscuro.innerHTML = '<i class="fa-solid fa-sun"></i>';
  } else {
    botonModoOscuro.innerHTML = '<i class="fa-solid fa-moon"></i>';
  }
}

function verificarTamañoPantalla() {
  if (window.innerWidth < 992) {
    botonHamburgesa.classList.remove('d-none');
    sidebar.classList.remove('menu-abierto');
  } else {
    botonHamburgesa.classList.add('d-none');
    sidebar.classList.remove('menu-abierto');
  }
}
