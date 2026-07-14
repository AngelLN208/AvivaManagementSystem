import { useEffect, useRef, useState } from 'react';
import { NavLink, Outlet, useLocation } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth.js';

const NAVIGATION = [
  { to: '/', label: 'Inicio', end: true },
  { to: '/citas', label: 'Mis citas' },
  { to: '/agendar', label: 'Agendar cita' },
  { to: '/medicos', label: 'Médicos' },
];

const PAGE_TITLES = {
  '/': 'Inicio',
  '/citas': 'Mis citas',
  '/agendar': 'Agendar cita',
  '/medicos': 'Médicos',
};

function getInitialTheme() {
  const saved = window.localStorage.getItem('aviva.portal.theme');
  if (saved === 'dark' || saved === 'light') return saved;
  return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
}

export default function PortalLayout() {
  const { session, logout } = useAuth();
  const location = useLocation();
  const menuButtonRef = useRef(null);
  const hasNavigated = useRef(false);
  const [menuOpen, setMenuOpen] = useState(false);
  const [theme, setTheme] = useState(getInitialTheme);

  useEffect(() => {
    document.documentElement.dataset.theme = theme;
    window.localStorage.setItem('aviva.portal.theme', theme);
  }, [theme]);

  useEffect(() => {
    const closeWithEscape = (event) => {
      if (event.key === 'Escape' && menuOpen) {
        setMenuOpen(false);
        menuButtonRef.current?.focus();
      }
    };
    window.addEventListener('keydown', closeWithEscape);
    return () => window.removeEventListener('keydown', closeWithEscape);
  }, [menuOpen]);

  useEffect(() => {
    document.title = `${PAGE_TITLES[location.pathname] || 'Portal paciente'} | Clínica Aviva`;
    if (hasNavigated.current) {
      document.getElementById('contenido-principal')?.focus();
    } else {
      hasNavigated.current = true;
    }
  }, [location.pathname]);

  const displayName = session?.firstName || session?.username || 'Paciente';

  return (
    <div className="portal-shell">
      <a className="skip-link" href="#contenido-principal">Saltar al contenido</a>

      <header className="portal-header">
        <div className="portal-header__inner">
          <NavLink to="/" className="brand" aria-label="Clínica Aviva, inicio">
            <span className="brand__mark" aria-hidden="true"><span>+</span></span>
            <span className="brand__copy">
              <strong>aviva</strong>
              <small>Portal paciente</small>
            </span>
          </NavLink>

          <button
            ref={menuButtonRef}
            type="button"
            className="icon-button menu-button"
            aria-label={menuOpen ? 'Cerrar menú' : 'Abrir menú'}
            aria-expanded={menuOpen}
            aria-controls="portal-navigation"
            onClick={() => setMenuOpen((current) => !current)}
          >
            <span aria-hidden="true">{menuOpen ? '×' : '☰'}</span>
          </button>

          <nav
            id="portal-navigation"
            className={`portal-nav ${menuOpen ? 'portal-nav--open' : ''}`}
            aria-label="Navegación principal"
          >
            {NAVIGATION.map((item) => (
              <NavLink
                key={item.to}
                to={item.to}
                end={item.end}
                onClick={() => setMenuOpen(false)}
                className={({ isActive }) => `portal-nav__link ${isActive ? 'is-active' : ''}`}
              >
                {item.label}
              </NavLink>
            ))}
            <button type="button" className="portal-nav__logout" onClick={logout}>
              Cerrar sesión
            </button>
          </nav>

          <div className="portal-header__actions">
            <button
              type="button"
              className="icon-button"
              aria-label={theme === 'dark' ? 'Activar modo claro' : 'Activar modo oscuro'}
              title={theme === 'dark' ? 'Modo claro' : 'Modo oscuro'}
              onClick={() => setTheme((current) => current === 'dark' ? 'light' : 'dark')}
            >
              <span aria-hidden="true">{theme === 'dark' ? '☀' : '☾'}</span>
            </button>

            <div className="user-summary" aria-label={`Sesión de ${displayName}`}>
              <span className="user-summary__avatar" aria-hidden="true">
                {displayName.charAt(0).toUpperCase()}
              </span>
              <span className="user-summary__copy">
                <small>Hola,</small>
                <strong>{displayName}</strong>
              </span>
            </div>

            <button type="button" className="text-button logout-button" onClick={logout}>
              Cerrar sesión
            </button>
          </div>
        </div>
      </header>

      {menuOpen && (
        <button
          type="button"
          className="menu-backdrop"
          aria-label="Cerrar menú"
          onClick={() => {
            setMenuOpen(false);
            menuButtonRef.current?.focus();
          }}
        />
      )}

      <main id="contenido-principal" className="portal-main" tabIndex="-1">
        <Outlet />
      </main>

      <footer className="portal-footer">
        <span>Clínica Aviva</span>
        <span>Gestiona tus citas de forma segura.</span>
      </footer>
    </div>
  );
}
