import { useState, useEffect } from 'react';
import { getPagos, procesarPago } from '../../api/pagosApi';

export default function PagosAdmin() {
  const [pagos, setPagos] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);
  const [processingId, setProcessingId] = useState(null);
  const [selectedMethod, setSelectedMethod] = useState('CREDIT_CARD');

  useEffect(() => {
    loadData();
  }, []);

  async function loadData() {
    try {
      setIsLoading(true);
      const data = await getPagos();
      setPagos(Array.isArray(data) ? data : []);
    } catch (err) {
      setError(err.message);
    } finally {
      setIsLoading(false);
    }
  }

  async function handleProcesar(id) {
    if (window.confirm(`¿Procesar pago con ${selectedMethod}?`)) {
      try {
        setProcessingId(id);
        await procesarPago(id, selectedMethod);
        await loadData();
      } catch (err) {
        setError(err.message);
      } finally {
        setProcessingId(null);
      }
    }
  }

  if (error) return <div className="alert alert-danger m-4">{error}</div>;

  return (
    <div className="container-fluid p-4">
      <h3 className="fw-semibold mb-4">Pagos</h3>

      {isLoading ? (
        <div className="text-center">
          <div className="spinner-border" role="status"></div>
        </div>
      ) : (
        <div className="table-responsive">
          <table className="table table-striped">
            <thead>
              <tr>
                <th>ID</th>
                <th>Monto</th>
                <th>Estado</th>
                <th>Método</th>
                <th>Fecha</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              {pagos.map((pago) => (
                <tr key={pago.id}>
                  <td>{pago.id}</td>
                  <td>S/ {pago.amount}</td>
                  <td>
                    <span className={`badge bg-${pago.status === 'COMPLETED' ? 'success' : pago.status === 'PENDING' ? 'warning' : 'danger'}`}>
                      {pago.status}
                    </span>
                  </td>
                  <td>{pago.method}</td>
                  <td>{new Date(pago.paymentDate).toLocaleDateString()}</td>
                  <td>
                    {pago.status === 'PENDING' && (
                      <div className="d-flex gap-2">
                        <select 
                          className="form-select form-select-sm" 
                          value={selectedMethod} 
                          onChange={(e) => setSelectedMethod(e.target.value)}
                          style={{ maxWidth: '150px' }}
                        >
                          <option value="CREDIT_CARD">Tarjeta</option>
                          <option value="BANK_TRANSFER">Transferencia</option>
                          <option value="CASH">Efectivo</option>
                        </select>
                        <button 
                          className="btn btn-sm btn-success" 
                          onClick={() => handleProcesar(pago.id)}
                          disabled={processingId === pago.id}
                        >
                          {processingId === pago.id ? 'Procesando...' : 'Procesar'}
                        </button>
                      </div>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
