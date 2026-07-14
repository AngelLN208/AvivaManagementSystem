import { useState, useMemo } from 'react';
import { useNotificaciones } from '../hooks/useNotificaciones';
import FiltrosNotificaciones from '../components/notificaciones/FiltrosNotificaciones';
import TablaNotificaciones from '../components/notificaciones/TablaNotificaciones';

export default function Notificaciones() {
  const { notificaciones, isLoading, isError } = useNotificaciones();
  const [filtros, setFiltros] = useState({ tipo: '', canal: '', estado: '' });

  const stats = useMemo(() => ({
    pendientes: notificaciones.filter((n) => n.status === 'PENDING').length,
    fallidas: notificaciones.filter((n) => n.status === 'FAILED').length,
    enviadas: notificaciones.filter(
      (n) => n.status === 'SENT' || n.status === 'DELIVERED'
    ).length,
  }), [notificaciones]);

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
        <div className="card tarjeta-estadistica border-0 rounded-4 p-3 shadow-sm">
          <div className="card-body">
            <div className="d-flex justify-content-between align-items-center mb-2">
              <h6 className="text-muted mb-0"><i className="fa-solid fa-clock me-2"></i> Pendientes</h6>
              <span className="badge bg-warning rounded-pill px-2 py-1">En cola</span>
            </div>
            <h2 className="fw-bold mb-0 text-warning">{isLoading ? '—' : stats.pendientes}</h2>
            <small className="text-muted">Esperando procesamiento</small>
          </div>
        </div>
        <div className="card tarjeta-estadistica border-0 rounded-4 p-3 shadow-sm">
          <div className="card-body">
            <div className="d-flex justify-content-between align-items-center mb-2">
              <h6 className="text-muted mb-0"><i className="fa-solid fa-circle-check me-2"></i> Enviadas</h6>
              <span className="badge bg-success rounded-pill px-2 py-1">Procesadas</span>
            </div>
            <h2 className="fw-bold mb-0 text-success">{isLoading ? '—' : stats.enviadas}</h2>
            <small className="text-muted">Correos enviados e internas disponibles</small>
          </div>
        </div>
        <div className="card tarjeta-estadistica border-0 rounded-4 p-3 shadow-sm">
          <div className="card-body">
            <div className="d-flex justify-content-between align-items-center mb-2">
              <h6 className="text-muted mb-0"><i className="fa-solid fa-triangle-exclamation me-2"></i> Fallidas</h6>
              <span className="badge bg-danger rounded-pill px-2 py-1">Revisar</span>
            </div>
            <h2 className="fw-bold mb-0 text-danger">{isLoading ? '—' : stats.fallidas}</h2>
            <small className="text-muted">Superaron los reintentos</small>
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
