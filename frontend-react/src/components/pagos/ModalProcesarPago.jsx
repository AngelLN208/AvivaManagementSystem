import { useEffect, useState } from 'react';
import { formatFechaHora } from '../../utils/formatters';

export default function ModalProcesarPago({ pago, onConfirmar, onClosed, isSubmitting }) {
  if (!pago) return null;

  return (
    <ModalProcesarPagoContent
      key={pago.id}
      pago={pago}
      onConfirmar={onConfirmar}
      onClosed={onClosed}
      isSubmitting={isSubmitting}
    />
  );
}

function ModalProcesarPagoContent({ pago, onConfirmar, onClosed, isSubmitting }) {
  const [metodo, setMetodo] = useState('');

  useEffect(() => {
    window.bootstrap?.Modal.getOrCreateInstance(document.getElementById('modalProcesarPago')).show();
  }, []);

  useEffect(() => {
    const el = document.getElementById('modalProcesarPago');
    const handleHidden = () => onClosed?.();
    el?.addEventListener('hidden.bs.modal', handleHidden);
    return () => el?.removeEventListener('hidden.bs.modal', handleHidden);
  }, [onClosed]);

  function handleConfirmar() {
    if (!metodo) { alert('❌ Selecciona un método de pago.'); return; }
    onConfirmar(pago.id, metodo, {
      onSuccess: () => window.bootstrap?.Modal.getInstance(document.getElementById('modalProcesarPago'))?.hide(),
      onError: (err) => alert(`❌ ${err.message}`),
    });
  }

  const paciente = pago ? `${pago.appointment?.patient?.firstName || ''} ${pago.appointment?.patient?.lastName || ''}`.trim() : '—';
  const medico = pago?.appointment?.doctor ? `${pago.appointment.doctor.firstName} ${pago.appointment.doctor.lastName}` : '—';
  const fechaCita = pago ? formatFechaHora(pago.appointment?.appointmentDateTime) : '—';
  const monto = pago ? `S/ ${parseFloat(pago.amount || 0).toFixed(2)}` : '—';

  return (
    <div className="modal fade" id="modalProcesarPago" tabIndex="-1" aria-hidden="true">
      <div className="modal-dialog modal-dialog-centered">
        <div className="modal-content border-0 rounded-4 shadow">
          <div className="modal-header border-bottom-0 pt-4 px-4">
            <h5 className="modal-title fw-bold"><i className="fa-solid fa-money-bill-wave me-2 text-primary"></i> Procesar Pago</h5>
            <button type="button" className="btn-close" data-bs-dismiss="modal"></button>
          </div>
          <div className="modal-body px-4 pb-4">
            <div className="alert alert-light border rounded-3 p-3 mb-4">
              <div className="row g-2">
                <div className="col-12">
                  <small className="text-muted d-block">Paciente</small>
                  <span className="fw-bold">{paciente}</span>
                </div>
                <div className="col-6">
                  <small className="text-muted d-block">Fecha y hora de cita</small>
                  <span className="fw-semibold">{fechaCita}</span>
                </div>
                <div className="col-6">
                  <small className="text-muted d-block">Médico</small>
                  <span className="fw-semibold">{medico}</span>
                </div>
                {pago?.insuranceName && (
                  <div className="col-12">
                    <div className="alert alert-light border rounded-3 py-2 px-3 mb-2">
                      <small className="text-muted d-block mb-1">
                        <i className="fa-solid fa-shield-halved me-1 texto-exito"></i>
                        Cubierto por <strong>{pago.insuranceName}</strong>
                      </small>
                      <div className="d-flex justify-content-between small">
                        <span className="text-muted">Costo base:</span>
                        <span>S/ {parseFloat(pago.baseAmount || 0).toFixed(2)}</span>
                      </div>
                      <div className="d-flex justify-content-between small">
                        <span className="text-muted">Deducible aplicado:</span>
                        <span>S/ {parseFloat(pago.deductibleApplied || 0).toFixed(2)}</span>
                      </div>
                      <div className="d-flex justify-content-between small texto-exito fw-semibold">
                        <span>Cubierto por seguro:</span>
                        <span>- S/ {parseFloat(pago.insuranceCoveredAmount || 0).toFixed(2)}</span>
                      </div>
                    </div>
                  </div>
                )}
                <div className="col-12">
                  <small className="text-muted d-block">Monto a cobrar al paciente</small>
                  <span className="fw-bold fs-5 texto-primario-personalizado">{monto}</span>
                </div>
              </div>
            </div>

            <div className="mb-4">
              <label className="form-label small fw-bold">Método de Pago <span className="text-danger">*</span></label>
              <select className="form-select rounded-3" value={metodo} onChange={(e) => setMetodo(e.target.value)}>
                <option value="" disabled>Seleccionar método...</option>
                <option value="CASH">Efectivo</option>
                <option value="CREDIT_CARD">Tarjeta de Crédito</option>
                <option value="DEBIT_CARD">Tarjeta de Débito</option>
                <option value="TRANSFER">Transferencia Bancaria</option>
              </select>
            </div>

            <div className="d-flex gap-2">
              <button className="btn btn-light rounded-pill flex-grow-1 fw-semibold" data-bs-dismiss="modal">Cancelar</button>
              <button className="btn btn-primary rounded-pill flex-grow-1 fw-semibold text-white" onClick={handleConfirmar} disabled={isSubmitting}>
                <i className="fa-solid fa-check me-1"></i> {isSubmitting ? 'Procesando...' : 'Confirmar Pago'}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
