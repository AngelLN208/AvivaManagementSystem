import BadgeEstado from '../ui/BadgeEstado';
import { calcularEdad } from '../../utils/formatters';

export default function TablaPacientes({ pacientes, isLoading, isError, onVer }) {
  if (isLoading) {
    return <tr><td colSpan={7} className="text-center text-muted py-4">Cargando...</td></tr>;
  }
  if (isError) {
    return <tr><td colSpan={7} className="text-center text-danger py-4">Error al cargar los pacientes.</td></tr>;
  }
  if (!pacientes.length) {
    return <tr><td colSpan={7} className="text-center text-muted py-4">No hay pacientes registrados.</td></tr>;
  }

  return pacientes.map((p) => {
    const nombre = `${p.firstName || ''} ${p.lastName || ''}`.trim();
    return (
      <tr key={p.id}>
        <td className="fw-semibold">{nombre}</td>
        <td>{p.dni || '—'}</td>
        <td>{calcularEdad(p.dateOfBirth)}</td>
        <td>{p.phone || '—'}</td>
        <td>{p.email || '—'}</td>
        <td><BadgeEstado estado={p.status} /></td>
        <td className="text-end">
          <button className="btn btn-sm btn-light text-primary rounded-circle" title="Ver paciente" onClick={() => onVer(p)}>
            <i className="fa-solid fa-eye"></i>
          </button>
        </td>
      </tr>
    );
  });
}