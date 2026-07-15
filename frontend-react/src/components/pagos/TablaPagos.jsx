import BadgeEstado from '../ui/BadgeEstado';
import { formatFechaHora, METODO_PAGO_MAP } from '../../utils/formatters';

export default function TablaPagos({ pagos, isLoading, isError, onProcesar, onVerComprobante }) {
  if (isLoading) {
    return <tr><td colSpan={6} className="text-center text-muted py-4">Cargando...</td></tr>;
  }
  if (isError) {
    return <tr><td colSpan={6} className="text-center text-danger py-4">Error al cargar los pagos.</td></tr>;
  }
  if (!pagos.length) {
    return <tr><td colSpan={6} className="text-center text-muted py-4">No hay pagos registrados.</td></tr>;
  }

  return pagos.map((p) => {
    const paciente = `${p.appointment?.patient?.firstName || ''} ${p.appointment?.patient?.lastName || ''}`.trim() || '—';
    const fechaCita = formatFechaHora(p.appointment?.appointmentDateTime);
    const monto = `S/ ${parseFloat(p.amount || 0).toFixed(2)}`;

    return (
      <tr key={p.id}>
        <td className="fw-semibold">{paciente}</td>
        <td>{fechaCita}</td>
        <td>{METODO_PAGO_MAP[p.method] || p.method || '—'}</td>
        <td className="fw-semibold">
          {monto}
          {p.insuranceName && (
            <><br /><small className="texto-exito"><i className="fa-solid fa-shield-halved me-1"></i>{p.insuranceName}</small></>
          )}
        </td>
        <td><BadgeEstado estado={p.status} /></td>
        <td className="text-end">
          {p.status === 'PENDING' && (
            <button className="btn btn-sm btn-light text-success rounded-circle" title="Procesar pago" onClick={() => onProcesar(p)}>
              <i className="fa-solid fa-check"></i>
            </button>
          )}
          {p.status === 'PAID' && (
            <button className="btn btn-sm btn-light text-primary rounded-circle" title="Ver comprobante" onClick={() => onVerComprobante(p)}>
              <i className="fa-solid fa-receipt"></i>
            </button>
          )}
        </td>
      </tr>
    );
  });
}