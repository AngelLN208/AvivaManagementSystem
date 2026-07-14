export default function FiltrosNotificaciones({ filtros, setFiltros, onLimpiar }) {
  return (
    <div className="row g-2 mb-3">
      <div className="col-12 col-md-4">
        <select
          className="form-select rounded-pill"
          value={filtros.tipo}
          onChange={(e) => setFiltros((f) => ({ ...f, tipo: e.target.value }))}
        >
          <option value="">Todos los tipos</option>
          <option value="APPOINTMENT_CREATED">Cita creada</option>
          <option value="APPOINTMENT_RESCHEDULED">Cita reprogramada</option>
          <option value="APPOINTMENT_CANCELLED">Cita cancelada</option>
          <option value="APPOINTMENT_REMINDER">Recordatorio de cita</option>
          <option value="APPOINTMENT_CONFIRMED">Cita confirmada</option>
          <option value="PAYMENT_RECEIVED">Pago recibido</option>
          <option value="PAYMENT_FAILED">Fallo en pago</option>
          <option value="TREATMENT_PLAN">Plan de tratamiento</option>
        </select>
      </div>
      <div className="col-6 col-md-3">
        <select
          className="form-select rounded-pill"
          value={filtros.canal}
          onChange={(e) => setFiltros((f) => ({ ...f, canal: e.target.value }))}
        >
          <option value="">Todos los canales</option>
          <option value="EMAIL">Email</option>
          <option value="SMS">SMS</option>
          <option value="IN_APP">In-App</option>
        </select>
      </div>
      <div className="col-6 col-md-3">
        <select
          className="form-select rounded-pill"
          value={filtros.estado}
          onChange={(e) => setFiltros((f) => ({ ...f, estado: e.target.value }))}
        >
          <option value="">Todos los estados</option>
          <option value="PENDING">Pendiente</option>
          <option value="SENT">Enviada</option>
          <option value="DELIVERED">Entregada</option>
          <option value="FAILED">Fallida</option>
        </select>
      </div>
      <div className="col-12 col-md-2">
        <button className="btn btn-outline-secondary rounded-pill w-100" onClick={onLimpiar}>
          <i className="fa-solid fa-xmark me-1"></i> Limpiar
        </button>
      </div>
    </div>
  );
}