import { useState, useRef, useEffect } from 'react';
import { useTheme } from '../../context/ThemeContext';
import { useNotificaciones } from '../../hooks/useNotificaciones';
import { getIniciales, logout } from '../../utils/helpers';

export default function Navbar({ onToggleSidebar }) {
  const { tema, toggleTema } = useTheme();
  const { noLeidas, marcarLeida, marcarTodasLeidas } = useNotificaciones();
  const [panelAbierto, setPanelAbierto] = useState(false);
  const panelRef = useRef(null);

  const username = localStorage.getItem('username') || 'Usuario';

  useEffect(() => {
    function handleClickFuera(e) {
      if (panelRef.current && !panelRef.current.contains(e.target)) {
        setPanelAbierto(false);
      }
    }
    document.addEventListener('click', handleClickFuera);
    return () => document.removeEventListener('click', handleClickFuera);
  }, []);

  return (
    <nav className="navbar navbar-light py-3 px-4 navbar-superior">
      <div className="d-flex align-items-center w-100 gap-3">
        <button className="btn btn-white rounded-2 d-lg-none boton-hamburgesa shadow-sm" onClick={onToggleSidebar}>
          <i className="fa-solid fa-bars"></i>
        </button>

        <div className="busqueda-wrapper flex-grow-1 position-relative">
          <div className="input-group barra-busqueda rounded-pill overflow-hidden">
            <span className="input-group-text bg-white border-0 text-muted ps-4">
              <i className="fa-solid fa-search"></i>
            </span>
            <input type="text" className="form-control border-0 bg-white shadow-none" placeholder="Buscar..." autoComplete="off" />
          </div>
        </div>

        <div className="d-flex align-items-center gap-2 ms-auto">
          <button className="btn btn-white rounded-circle shadow-sm boton-icono" onClick={toggleTema}>
            <i className={tema === 'oscuro' ? 'fa-solid fa-sun text-warning' : 'fa-solid fa-moon'}></i>
          </button>

          <div className="position-relative" ref={panelRef}>
            <button
              className="btn btn-white rounded-circle shadow-sm boton-icono position-relative"
              onClick={(e) => { e.stopPropagation(); setPanelAbierto(p => !p); }}
            >
              <i className="fa-regular fa-bell"></i>
              {noLeidas.length > 0 && (
                <span className="notif-dot position-absolute top-0 start-100 translate-middle p-1 bg-danger border border-light rounded-circle"></span>
              )}
            </button>

            {panelAbierto && (
              <div className="notif-quick-panel">
                <div className="notif-panel-header">
                  <span className="fw-bold">Notificaciones</span>
                  <button className="btn btn-sm text-primary p-0" onClick={marcarTodasLeidas}>Marcar todas</button>
                </div>
                <div>
                  {noLeidas.length === 0 ? (
                    <div className="notif-empty py-3">
                      <i className="fa-solid fa-bell-slash d-block mb-2" style={{ fontSize: '1.8rem', opacity: 0.3 }}></i>
                      <small>Sin notificaciones nuevas</small>
                    </div>
                  ) : (
                    noLeidas.slice(0, 4).map(n => (
                      <div key={n.id} className="notif-item-quick no-leida" onClick={() => marcarLeida(n.id)}>
                        <div className="notif-icono" style={{ background: n.colorBg, color: n.colorIcon, width: 38, height: 38, minWidth: 38, borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                          <i className={`fa-solid ${n.icono}`} style={{ fontSize: '0.9rem' }}></i>
                        </div>
                        <div className="flex-grow-1">
                          <p className="mb-0 fw-semibold" style={{ fontSize: '0.82rem' }}>{n.titulo}</p>
                          <p className="mb-0 text-muted" style={{ fontSize: '0.77rem' }}>{n.mensaje}</p>
                          <small className="text-muted" style={{ fontSize: '0.72rem' }}>{n.tiempo}</small>
                        </div>
                        <div className="notif-punto ms-1 flex-shrink-0"></div>
                      </div>
                    ))
                  )}
                </div>
                <div className="notif-panel-footer">
                  <a href="/notificaciones" className="text-primary text-decoration-none small">Ver todas</a>
                </div>
              </div>
            )}
          </div>

          <div className="dropdown ms-2">
            <a href="#" className="d-flex align-items-center text-decoration-none dropdown-toggle usuario-dropdown" data-bs-toggle="dropdown">
              <div className="bg-primary text-white rounded-circle d-flex align-items-center justify-content-center me-2 shadow-sm" style={{ width: 40, height: 40, fontWeight: 'bold' }}>
                {getIniciales(username)}
              </div>
              <strong className="fw-semibold d-none d-lg-block">{username}</strong>
            </a>
            <ul className="dropdown-menu dropdown-menu-end shadow border-0 mt-2">
              <li><a className="dropdown-item" href="#"><i className="fa-solid fa-user me-2 text-muted"></i>Mi Perfil</a></li>
              <li><hr className="dropdown-divider" /></li>
              <li><a className="dropdown-item text-danger" href="#" onClick={logout}><i className="fa-solid fa-right-from-bracket me-2"></i>Cerrar Sesión</a></li>
            </ul>
          </div>
        </div>
      </div>
    </nav>
  );
}