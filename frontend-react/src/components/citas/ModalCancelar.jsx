import { useEffect } from 'react';

export default function ModalCancelar({ cita, onConfirmar, onClosed, isSubmitting }) {
  useEffect(() => {
    if (cita) {
      window.bootstrap?.Modal.getOrCreateInstance(document.getElementById('modalCancelar')).show();
    }
  }, [cita]);

  useEffect(() => {
    const el = document.getElementById('modalCancelar');
    const handleHidden = () => onClosed?.();
    el?.addEventListener('hidden.bs.modal', handleHidden);
    return () => el?.removeEventListener('hidden.bs.modal', handleHidden);
  }, [onClosed]);

  const nombrePaciente = cita ? `${cita.patient?.firstName || ''} ${cita.patient?.lastName || ''}`.trim() : '';

  function handleConfirmar() {
    onConfirmar(cita.id, {
      onSuccess: () => window.bootstrap?.Modal.getInstance(document.getElementById('modalCancelar'))?.hide(),
      onError: (err) => alert(`❌ ${err.message}`),
    });
  }

  return (
    <div className="modal fade" id="modalCancelar" tabIndex="-1" aria-hidden="true">
      <div className="modal-dialog modal-dialog-centered modal-sm">
        <div className="modal-content border-0 rounded-4 shadow">
          <div className="modal-body p-4 text-center">
            <div className="mb-3"><i className="fa-solid fa-circle-exclamation fa-3x text-danger"></i></div>
            <h5 className="fw-bold">¿Cancelar esta cita?</h5>
            <p className="text-muted small mb-4">Esta acción cancelará la cita de <strong>{nombrePaciente}</strong>.</p>
            <div className="d-flex gap-2">
              <button className="btn btn-light rounded-pill flex-grow-1 fw-semibold" data-bs-dismiss="modal">No, volver</button>
              <button className="btn btn-danger rounded-pill flex-grow-1 fw-semibold" onClick={handleConfirmar} disabled={isSubmitting}>
                {isSubmitting ? 'Cancelando...' : 'Sí, cancelar'}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}