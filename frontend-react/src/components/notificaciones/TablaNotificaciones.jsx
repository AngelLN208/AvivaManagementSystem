import { formatFechaHora, TIPO_NOTIF_MAP, ESTADO_NOTIF_MAP } from '../../utils/formatters';

export default function TablaNotificaciones({ notificaciones, isLoading, isError }) {
  if (isLoading) {
    return <tr><td colSpan={6} className="text-center text-muted py-4">Cargando...</td></tr>;
  }
  if (isError) {
    return <tr><td colSpan={6} className="text-center text-danger py-4">Error al cargar las notificaciones.</td></tr>;
  }
  if (!notificaciones.length) {
    return <tr><td colSpan={6} className="text-center text-muted py-4">No hay notificaciones.</td></tr>;
  }

  return notificaciones.map((n) => {
    const estadoInfo = ESTADO_NOTIF_MAP[n.status] || { clase: 'fondo-info-sutil texto-info', label: n.status };
    return (
      <tr key={n.id}>
        <td>{TIPO_NOTIF_MAP[n.type] || n.type || '—'}</td>
        <td>{n.recipientName || n.recipientEmail || '—'}</td>
        <td className="text-truncate" style={{ maxWidth: 200 }} title={n.subject || ''}>{n.subject || '—'}</td>
        <td>{n.channel || '—'}</td>
        <td><span className={`badge ${estadoInfo.clase} rounded-pill px-3 py-1`}>{estadoInfo.label}</span></td>
        <td>{formatFechaHora(n.createdAt)}</td>
      </tr>
    );
  });
}