import { useState, useMemo } from 'react';
import { useNotificacionesPagina } from '../hooks/useNotificacionesPagina';
import FiltrosNotificaciones from '../components/notificaciones/FiltrosNotificaciones';
import TablaNotificaciones from '../components/notificaciones/TablaNotificaciones';

export default function Notificaciones() {
  const { notificaciones, isLoading, isError } = useNotificacionesPagina();
  const [filtros, setFiltros] = useState({ tipo: '', canal: '', estado: '' });

  const notificacionesFiltradas = useMemo(() => {
    return notificaciones.filter((n) => {
      const okTipo = !filtros.tipo || n.type === filtros.tipo;
      const okCanal = !filtros.canal || n.channel === filtros.canal;
      const okEstado = !filtros.estado || n.status === filtros.estado;
      return okTipo && okCanal && okEstado;
    });
  }, [notificaciones, filtros]);

  return (
    <>
      <div className="panel-titulo d-flex align-items-center gap-2 mb-4 mt-2">
        <i className="fa-solid fa-bell texto-primario-personalizado fa-lg"></i>
        <h4 className="fw-bold mb-0">Centro de Notificaciones</h4>
      </div>

      <div className="grid-estadisticas mb-4">
        <div className="card tarjeta-estadistica border-0 rounded-4 p-3 shadow-sm opacity-50">
          <div className="card-body">
            <div className="d-flex justify-content-between align-items-center mb-2">
              <h6 className="text-muted mb-0"><i className="fa-solid fa-bell me-2"></i> Sin Leer</h6>
              <span className="badge bg-secondary rounded-pill px-2 py-1">Próximamente</span>
            </div>
            <h2 className="fw-bold mb-0 text-muted">—</h2>
            <small className="text-muted">Requieren atención</small>
          </div>
        </div>
        <div className="card tarjeta-estadistica border-0 rounded-4 p-3 shadow-sm opacity-50">
          <div className="card-body">
            <div className="d-flex justify-content-between align-items-center mb-2">
              <h6 className="text-muted mb-0"><i className="fa-solid fa-check-double me-2"></i> Leídas</h6>
              <span className="badge bg-secondary rounded-pill px-2 py-1">Próximamente</span>
            </div>
            <h2 className="fw-bold mb-0 text-muted">—</h2>
            <small className="text-muted">Ya revisadas</small>
          </div>
        </div>
        <div className="card tarjeta-estadistica border-0 rounded-4 p-3 shadow-sm">
          <div className="card-body">
            <div className="d-flex justify-content-between align-items-center mb-2">
              <h6 className="text-muted mb-0"><i className="fa-solid fa-list me-2"></i> Total</h6>
              <span className="badge fondo-info-sutil texto-info rounded-pill px-2 py-1">Registro</span>
            </div>
            <h2 className="fw-bold mb-0">{isLoading ? '—' : notificaciones.length}</h2>
            <small className="text-muted">En el sistema</small>
          </div>
        </div>
      </div>

      <div className="card border-0 rounded-4 shadow-sm p-4">
        <div className="d-flex justify-content-between align-items-center mb-3 flex-wrap gap-2">
          <h5 className="fw-bold mb-0">Notificaciones</h5>
        </div>

        <FiltrosNotificaciones
          filtros={filtros}
          setFiltros={setFiltros}
          onLimpiar={() => setFiltros({ tipo: '', canal: '', estado: '' })}
        />

        <div className="table-responsive">
          <table className="table table-borderless align-middle tabla-personalizada m-0">
            <thead className="border-bottom">
              <tr>
                <th>Tipo</th><th>Destinatario</th><th>Asunto</th><th>Canal</th><th>Estado</th><th>Fecha</th>
              </tr>
            </thead>
            <tbody>
              <TablaNotificaciones
                notificaciones={notificacionesFiltradas}
                isLoading={isLoading}
                isError={isError}
              />
            </tbody>
          </table>
        </div>
      </div>
    </>
  );
}