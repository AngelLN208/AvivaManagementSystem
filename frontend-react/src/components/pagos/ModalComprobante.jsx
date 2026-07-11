import { useEffect } from 'react';
import { useQuery } from '@tanstack/react-query';
import { getRecibos } from '../../api/pagosApi';
import { formatFecha, formatFechaHora, METODO_PAGO_MAP } from '../../utils/formatters';

export default function ModalComprobante({ pago, onClosed }) {
  const { data: recibos = [] } = useQuery({
    queryKey: ['recibos'],
    queryFn: getRecibos,
    enabled: !!pago,
  });

  useEffect(() => {
    if (pago) {
      window.bootstrap?.Modal.getOrCreateInstance(document.getElementById('modalComprobante')).show();
    }
  }, [pago]);

  useEffect(() => {
    const el = document.getElementById('modalComprobante');
    const handleHidden = () => onClosed?.();
    el?.addEventListener('hidden.bs.modal', handleHidden);
    return () => el?.removeEventListener('hidden.bs.modal', handleHidden);
  }, [onClosed]);

  const recibo = pago ? recibos.find((r) => r.payment?.id === pago.id) : null;
  const paciente = pago ? `${pago.appointment?.patient?.firstName || ''} ${pago.appointment?.patient?.lastName || ''}`.trim() : '—';
  const medico = pago?.appointment?.doctor ? `${pago.appointment.doctor.firstName} ${pago.appointment.doctor.lastName}` : '—';

  return (
    <div className="modal fade" id="modalComprobante" tabIndex="-1" aria-hidden="true">
      <div className="modal-dialog modal-dialog-centered">
        <div className="modal-content border-0 rounded-4 shadow">
          <div className="modal-header border-bottom-0 pt-4 px-4">
            <h5 className="modal-title fw-bold"><i className="fa-solid fa-receipt me-2 text-success"></i> Comprobante de Pago</h5>
            <button type="button" className="btn-close" data-bs-dismiss="modal"></button>
          </div>
          <div className="modal-body px-4 pb-4">
            <div className="border rounded-3 p-4">
              <div className="d-flex justify-content-between align-items-start mb-3">
                <div>
                  <h6 className="fw-bold mb-0">Clínica Aviva</h6>
                  <small className="text-muted">N° {recibo?.receiptNumber || '—'}</small>
                </div>
                <span className="badge fondo-exito-sutil texto-exito rounded-pill px-3">Pagado</span>
              </div>
              <hr />
              <div className="row g-2 mb-3">
                <div className="col-6">
                  <small className="text-muted d-block">Paciente</small>
                  <span className="fw-semibold">{paciente}</span>
                </div>
                <div className="col-6">
                  <small className="text-muted d-block">Médico</small>
                  <span className="fw-semibold">{medico}</span>
                </div>
                <div className="col-6">
                  <small className="text-muted d-block">Fecha de cita</small>
                  <span className="fw-semibold">{formatFechaHora(pago?.appointment?.appointmentDateTime)}</span>
                </div>
                <div className="col-6">
                  <small className="text-muted d-block">Método</small>
                  <span className="fw-semibold">{METODO_PAGO_MAP[pago?.method] || pago?.method || '—'}</span>
                </div>
                <div className="col-6">
                  <small className="text-muted d-block">Fecha de pago</small>
                  <span className="fw-semibold">{formatFecha(pago?.paymentDate || pago?.updatedAt)}</span>
                </div>
              </div>
              <hr />
              <div className="d-flex justify-content-between align-items-center">
                <span className="fw-bold">Total</span>
                <span className="fw-bold fs-5 texto-primario-personalizado">S/ {parseFloat(pago?.amount || 0).toFixed(2)}</span>
              </div>
            </div>
            <button className="btn btn-outline-secondary rounded-pill w-100 mt-3" data-bs-dismiss="modal">Cerrar</button>
          </div>
        </div>
      </div>
    </div>
  );
}