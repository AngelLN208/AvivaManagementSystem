import { useState, useMemo } from 'react';
import { usePagos, usePagosMutations } from '../hooks/usePagos';
import { esHoy } from '../utils/formatters';
import FiltrosPagos from '../components/pagos/FiltrosPagos';
import TablaPagos from '../components/pagos/TablaPagos';
import ModalProcesarPago from '../components/pagos/ModalProcesarPago';
import ModalComprobante from '../components/pagos/ModalComprobante';

export default function Pagos() {
  const { pagos, isLoading, isError } = usePagos();
  const { procesar } = usePagosMutations();

  const [filtros, setFiltros] = useState({ dni: '', estado: '' });
  const [pagoAProcesar, setPagoAProcesar] = useState(null);
  const [pagoAVerComprobante, setPagoAVerComprobante] = useState(null);

  const pagosFiltrados = useMemo(() => {
    return pagos.filter((p) => {
      const dni = p.appointment?.patient?.dni || '';
      const okDni = !filtros.dni || dni.includes(filtros.dni);
      const okEstado = !filtros.estado || p.status === filtros.estado;
      return okDni && okEstado;
    });
  }, [pagos, filtros]);

  const stats = useMemo(() => {
    const pendientes = pagos.filter((p) => p.status === 'PENDING');
    const realizadosHoy = pagos.filter((p) => p.status === 'PAID' && esHoy(p.paymentDate || p.updatedAt));
    const cancelacionesHoy = pagos.filter((p) => (p.status === 'CANCELLED' || p.status === 'REFUNDED') && esHoy(p.updatedAt));
    const totalHoy = realizadosHoy.reduce((s, p) => s + parseFloat(p.amount || 0), 0);
    return {
      totalHoy: `S/ ${totalHoy.toFixed(2)}`,
      pendientes: pendientes.length,
      realizadosHoy: realizadosHoy.length,
      cancelacionesHoy: cancelacionesHoy.length,
    };
  }, [pagos]);

  return (
    <>
      <div className="panel-titulo d-flex align-items-center gap-2 mb-4 mt-2">
        <i className="fa-solid fa-money-bill-wave texto-primario-personalizado fa-lg"></i>
        <h4 className="fw-bold mb-0">Pagos y Facturación</h4>
      </div>

      <div className="grid-estadisticas mb-4">
        <div className="card tarjeta-estadistica border-0 rounded-4 p-3 shadow-sm">
          <div className="card-body">
            <h6 className="text-muted mb-2"><i className="fa-solid fa-sack-dollar me-2"></i> Pagos del Día</h6>
            <h2 className="fw-bold mb-0">{isLoading ? '—' : stats.totalHoy}</h2>
            <small className="text-muted">Total ingresado hoy</small>
          </div>
        </div>
        <div className="card tarjeta-estadistica border-0 rounded-4 p-3 shadow-sm">
          <div className="card-body">
            <h6 className="text-muted mb-2"><i className="fa-solid fa-file-invoice-dollar me-2"></i> Pendientes de Pago</h6>
            <h2 className="fw-bold mb-0 text-warning">{isLoading ? '—' : stats.pendientes}</h2>
            <small className="text-muted">Citas sin confirmar pago</small>
          </div>
        </div>
        <div className="card tarjeta-estadistica border-0 rounded-4 p-3 shadow-sm">
          <div className="card-body">
            <h6 className="text-muted mb-2"><i className="fa-solid fa-circle-check me-2"></i> Pagos Realizados Hoy</h6>
            <h2 className="fw-bold mb-0 texto-exito">{isLoading ? '—' : stats.realizadosHoy}</h2>
            <small className="text-muted">Transacciones exitosas</small>
          </div>
        </div>
        <div className="card tarjeta-estadistica border-0 rounded-4 p-3 shadow-sm">
          <div className="card-body">
            <h6 className="text-muted mb-2"><i className="fa-solid fa-ban me-2"></i> Cancelaciones Hoy</h6>
            <h2 className="fw-bold mb-0 text-danger">{isLoading ? '—' : stats.cancelacionesHoy}</h2>
            <small className="text-muted">Pagos cancelados o reembolsados</small>
          </div>
        </div>
      </div>

      <div className="card border-0 rounded-4 shadow-sm p-4">
        <div className="d-flex justify-content-between align-items-center mb-3 flex-wrap gap-2">
          <h5 className="fw-bold mb-0">Historial de Pagos</h5>
        </div>

        <FiltrosPagos
          filtros={filtros}
          setFiltros={setFiltros}
          onLimpiar={() => setFiltros({ dni: '', estado: '' })}
        />

        <div className="table-responsive">
          <table className="table table-borderless align-middle tabla-personalizada m-0">
            <thead className="border-bottom">
              <tr>
                <th>Paciente</th><th>Fecha Cita</th><th>Método de Pago</th><th>Monto</th><th>Estado</th><th className="text-end">Acciones</th>
              </tr>
            </thead>
            <tbody>
              <TablaPagos
                pagos={pagosFiltrados}
                isLoading={isLoading}
                isError={isError}
                onProcesar={setPagoAProcesar}
                onVerComprobante={setPagoAVerComprobante}
              />
            </tbody>
          </table>
        </div>
      </div>

      <ModalProcesarPago
        pago={pagoAProcesar}
        onConfirmar={(paymentId, method, opts) => procesar.mutate({ paymentId, method }, opts)}
        onClosed={() => setPagoAProcesar(null)}
        isSubmitting={procesar.isPending}
      />

      <ModalComprobante
        pago={pagoAVerComprobante}
        onClosed={() => setPagoAVerComprobante(null)}
      />
    </>
  );
}
