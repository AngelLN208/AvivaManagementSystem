import BadgeEstado from '../ui/BadgeEstado';

export default function TablaCitas({ citas, isLoading, isError, onReprogramar, onCancelar }) {
  if (isLoading) {
    return <tr><td colSpan={7} className="text-center text-muted py-4">Cargando...</td></tr>;
  }
  if (isError) {
    return <tr><td colSpan={7} className="text-center text-danger py-4">Error al cargar las citas.</td></tr>;
  }
  if (!citas.length) {
    return <tr><td colSpan={7} className="text-center text-muted py-4">No hay citas registradas.</td></tr>;
  }

  return citas.map((c) => {
    const hora = c.appointmentDateTime
      ? new Date(c.appointmentDateTime).toLocaleTimeString('es-PE', { hour: '2-digit', minute: '2-digit' })
      : '—';
    const fecha = c.appointmentDateTime
      ? new Date(c.appointmentDateTime).toLocaleDateString('es-PE', { day: '2-digit', month: '2-digit', year: 'numeric' })
      : '';
    const paciente = `${c.patient?.firstName || ''} ${c.patient?.lastName || ''}`.trim() || '—';
    const dni = c.patient?.dni || '—';
    const medico = `Dr. ${c.doctor?.firstName || ''} ${c.doctor?.lastName || ''}`.trim();
    const especialidad = c.doctor?.specialty?.name || '—';
    const cancelada = c.status === 'CANCELLED' || c.status === 'COMPLETED';

    return (
      <tr key={c.id}>
        <td className="fw-semibold">{hora}<br /><small className="text-muted">{fecha}</small></td>
        <td>{paciente}</td>
        <td>{dni}</td>
        <td>{especialidad}</td>
        <td>{medico}</td>
        <td><BadgeEstado estado={c.status} /></td>
        <td className="text-end">
          {!cancelada && (
            <>
              <button className="btn btn-sm btn-light text-warning rounded-circle me-1" title="Reprogramar" onClick={() => onReprogramar(c)}>
                <i className="fa-solid fa-clock"></i>
              </button>
              <button className="btn btn-sm btn-light text-danger rounded-circle" title="Cancelar" onClick={() => onCancelar(c)}>
                <i className="fa-solid fa-ban"></i>
              </button>
            </>
          )}
        </td>
      </tr>
    );
  });
}