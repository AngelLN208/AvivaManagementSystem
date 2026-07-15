import { useMemo, useState } from 'react';
import { useNotificaciones } from '../hooks/useNotificaciones';
import FiltrosNotificaciones from '../components/notificaciones/FiltrosNotificaciones';
import TablaNotificaciones from '../components/notificaciones/TablaNotificaciones';

export default function Notificaciones() {
  const { notificaciones, isLoading, isError } = useNotificaciones();
  const [filtros, setFiltros] = useState({ tipo: '', canal: '', estado: '' });

  const stats = useMemo(() => ({
    pendientes: notificaciones.filter((notification) => notification.status === 'PENDING').length,
    fallidas: notificaciones.filter((notification) => notification.status === 'FAILED').length,
    enviadas: notificaciones.filter(
      (notification) => notification.status === 'SENT' || notification.status === 'DELIVERED',
    ).length,
  }), [notificaciones]);

  const notificacionesFiltradas = useMemo(() => notificaciones.filter((notification) => {
    const matchesType = !filtros.tipo || notification.type === filtros.tipo;
    const matchesChannel = !filtros.canal || notification.channel === filtros.canal;
    const matchesStatus = !filtros.estado || notification.status === filtros.estado;
    return matchesType && matchesChannel && matchesStatus;
  }), [notificaciones, filtros]);

  return (
    <>
      <div className="panel-titulo d-flex align-items-center gap-2 mb-4 mt-2">
        <i className="fa-solid fa-bell texto-primario-personalizado fa-lg"></i>
        <h4 className="fw-bold mb-0">Centro de Notificaciones</h4>
      </div>

      <div className="grid-estadisticas mb-4">
        <NotificationStat title="Pendientes" value={stats.pendientes} loading={isLoading} icon="fa-clock" color="warning" />
        <NotificationStat title="Enviadas" value={stats.enviadas} loading={isLoading} icon="fa-circle-check" color="success" />
        <NotificationStat title="Fallidas" value={stats.fallidas} loading={isLoading} icon="fa-triangle-exclamation" color="danger" />
      </div>

      <div className="card border-0 rounded-4 shadow-sm p-4">
        <h5 className="fw-bold mb-3">Notificaciones</h5>
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

function NotificationStat({ title, value, loading, icon, color }) {
  return (
    <div className="card tarjeta-estadistica border-0 rounded-4 p-3 shadow-sm">
      <div className="card-body">
        <h6 className="text-muted mb-2"><i className={`fa-solid ${icon} me-2`}></i>{title}</h6>
        <h2 className={`fw-bold mb-0 text-${color}`}>{loading ? '—' : value}</h2>
      </div>
    </div>
  );
}
