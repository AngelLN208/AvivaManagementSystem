const DIAS_ES = {
  MONDAY: 'Lunes', TUESDAY: 'Martes', WEDNESDAY: 'Miércoles',
  THURSDAY: 'Jueves', FRIDAY: 'Viernes', SATURDAY: 'Sábado', SUNDAY: 'Domingo',
};

export default function TablaHorarios({ horarios, isLoading, isError, onVerDisponibilidad }) {
  if (isLoading) {
    return <tr><td colSpan={8} className="text-center text-muted py-4">Cargando...</td></tr>;
  }
  if (isError) {
    return <tr><td colSpan={8} className="text-center text-danger py-4">Error al cargar los horarios.</td></tr>;
  }
  if (!horarios.length) {
    return <tr><td colSpan={8} className="text-center text-muted py-4">No hay horarios registrados.</td></tr>;
  }

  return horarios.map(({ doctor: d, schedule: s }) => {
    const nombre = `Dr. ${d.firstName} ${d.lastName}`;
    const especialidad = d.specialty?.name || '—';
    const dia = DIAS_ES[s.dayOfWeek] || s.dayOfWeek;
    const horario = `${s.startTime?.substring(0, 5)} — ${s.endTime?.substring(0, 5)}`;

    return (
      <tr key={`${d.id}-${s.id ?? s.dayOfWeek}`}>
        <td className="fw-semibold">{nombre}</td>
        <td>{especialidad}</td>
        <td>{dia}</td>
        <td>{horario}</td>
        <td>{s.appointmentDurationMinutes} min</td>
        <td>{s.maxAppointmentsPerDay}</td>
        <td>
          {s.active
            ? <span className="badge fondo-exito-sutil texto-exito rounded-pill px-3 py-1">Activo</span>
            : <span className="badge fondo-error-sutil texto-error rounded-pill px-3 py-1">Inactivo</span>}
        </td>
        <td className="text-end">
          <button className="btn btn-sm btn-light text-primary rounded-circle" title="Ver disponibilidad"
            onClick={() => onVerDisponibilidad(d)}>
            <i className="fa-solid fa-eye"></i>
          </button>
        </td>
      </tr>
    );
  });
}