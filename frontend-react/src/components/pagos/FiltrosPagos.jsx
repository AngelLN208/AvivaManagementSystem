export default function FiltrosPagos({ filtros, setFiltros, onLimpiar }) {
  return (
    <div className="row g-2 mb-3">
      <div className="col-12 col-md-4">
        <div className="input-group barra-busqueda rounded-pill overflow-hidden">
          <span className="input-group-text bg-white border-0 text-muted ps-3">
            <i className="fa-solid fa-id-card"></i>
          </span>
          <input
            type="text"
            className="form-control border-0 bg-white shadow-none"
            placeholder="DNI del paciente..."
            autoComplete="off"
            value={filtros.dni}
            onChange={(e) => setFiltros((f) => ({ ...f, dni: e.target.value }))}
          />
        </div>
      </div>
      <div className="col-6 col-md-3">
        <select
          className="form-select rounded-pill"
          value={filtros.estado}
          onChange={(e) => setFiltros((f) => ({ ...f, estado: e.target.value }))}
        >
          <option value="">Todos los estados</option>
          <option value="PENDING">Pendiente</option>
          <option value="PAID">Pagado</option>
          <option value="CANCELLED">Cancelado</option>
          <option value="REFUNDED">Reembolsado</option>
        </select>
      </div>
      <div className="col-6 col-md-2">
        <button className="btn btn-outline-secondary rounded-pill w-100" onClick={onLimpiar}>
          <i className="fa-solid fa-xmark me-1"></i> Limpiar
        </button>
      </div>
    </div>
  );
}