export default function FiltrosHorarios({ filtros, setFiltros, especialidades, onLimpiar }) {
  return (
    <div className="row g-2 mb-3">
      <div className="col-12 col-md-5">
        <div className="input-group barra-busqueda rounded-pill overflow-hidden">
          <span className="input-group-text bg-white border-0 text-muted ps-3">
            <i className="fa-solid fa-search"></i>
          </span>
          <input
            type="text"
            className="form-control border-0 bg-white shadow-none"
            placeholder="Buscar médico por nombre..."
            autoComplete="off"
            value={filtros.nombre}
            onChange={(e) => setFiltros((f) => ({ ...f, nombre: e.target.value }))}
          />
        </div>
      </div>
      <div className="col-6 col-md-4">
        <select
          className="form-select rounded-pill"
          value={filtros.especialidad}
          onChange={(e) => setFiltros((f) => ({ ...f, especialidad: e.target.value }))}
        >
          <option value="">Todas las especialidades</option>
          {especialidades.map((esp) => <option key={esp} value={esp}>{esp}</option>)}
        </select>
      </div>
      <div className="col-6 col-md-3">
        <button className="btn btn-outline-secondary rounded-pill w-100" onClick={onLimpiar}>
          <i className="fa-solid fa-xmark me-1"></i> Limpiar
        </button>
      </div>
    </div>
  );
}