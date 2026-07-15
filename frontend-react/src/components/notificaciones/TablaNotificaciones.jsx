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

  return notificaciones.map((notification) => {
    const status = ESTADO_NOTIF_MAP[notification.status] || {
      clase: 'fondo-info-sutil texto-info',
      label: notification.status,
    };

    return (
      <tr key={notification.id}>
        <td>{TIPO_NOTIF_MAP[notification.type] || notification.type || '—'}</td>
        <td>{notification.recipientName || notification.recipientEmail || '—'}</td>
        <td className="text-truncate" style={{ maxWidth: 200 }} title={notification.subject || ''}>
          {notification.subject || '—'}
        </td>
        <td>{notification.channel || '—'}</td>
        <td><span className={`badge ${status.clase} rounded-pill px-3 py-1`}>{status.label}</span></td>
        <td>{formatFechaHora(notification.createdAt)}</td>
      </tr>
    );
  });
}
