import BadgeEstado from './BadgeEstado';

export default function TablaCitasHoy({ citas, isLoading }) {
  if (isLoading) {
    return (
      <tr>
        <td colSpan={5} className="text-center text-muted py-4">Cargando citas...</td>
      </tr>
    );
  }

  if (!citas.length) {
    return (
      <tr>
        <td colSpan={5} className="text-center text-muted py-4">No hay citas para hoy.</td>
      </tr>
    );
  }

  return citas.map(c => {
    const hora = c.appointmentDateTime
      ? new Date(c.appointmentDateTime).toLocaleTimeString('es-PE', { hour: '2-digit', minute: '2-digit' })
      : '—';
    const paciente = `${c.patient?.firstName || ''} ${c.patient?.lastName || ''}`.trim() || '—';
    const medico = `${c.doctor?.firstName || ''} ${c.doctor?.lastName || ''}`.trim() || '—';
    const especialidad = c.doctor?.specialty?.name || '—';

    return (
      <tr key={c.id}>
        <td className="fw-semibold">{hora}</td>
        <td>{paciente}</td>
        <td>{especialidad}</td>
        <td>{medico}</td>
        <td><BadgeEstado estado={c.status} /></td>
      </tr>
    );
  });
}