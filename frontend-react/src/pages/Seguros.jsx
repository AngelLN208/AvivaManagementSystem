import { useSeguros } from '../hooks/useSeguros';

export default function Seguros() {
  const { data: seguros = [], isLoading, isError } = useSeguros();

  return (
    <>
      <div className="panel-titulo d-flex align-items-center gap-2 mb-4 mt-2">
        <i className="fa-solid fa-shield-heart texto-primario-personalizado fa-lg"></i>
        <h4 className="fw-bold mb-0">Seguros y Convenios</h4>
      </div>

      <div className="card border-0 rounded-4 shadow-sm p-4">
        <div className="d-flex justify-content-between align-items-center mb-4 flex-wrap gap-2">
          <h5 className="fw-bold mb-0">Aseguradoras Disponibles</h5>
        </div>

        <div className="table-responsive">
          <table className="table table-borderless align-middle tabla-personalizada m-0">
            <thead className="border-bottom">
              <tr>
                <th>Nombre</th>
                <th>Descripción</th>
                <th>% Cobertura</th>
                <th>Deducible</th>
                <th>Máx. por Consulta</th>
                <th>Máx. Anual</th>
                <th>Estado</th>
              </tr>
            </thead>
            <tbody>
              {isLoading && (
                <tr>
                  <td colSpan={7} className="text-center py-4">
                    <div className="spinner-border spinner-border-sm" role="status"></div>
                  </td>
                </tr>
              )}
              {isError && !isLoading && (
                <tr>
                  <td colSpan={7} className="text-center text-danger py-4">
                    No se pudieron cargar los seguros
                  </td>
                </tr>
              )}
              {!isLoading && !isError && seguros.length === 0 && (
                <tr>
                  <td colSpan={7} className="text-center text-muted py-4">
                    No hay seguros registrados
                  </td>
                </tr>
              )}
              {!isLoading &&
                !isError &&
                seguros.map((seg) => (
                  <tr key={seg.id}>
                    <td className="fw-semibold">{seg.name}</td>
                    <td>{seg.description || '—'}</td>
                    <td>{seg.coveragePercentage}%</td>
                    <td>S/ {seg.deductible}</td>
                    <td>S/ {seg.maxCoveragePerConsultation}</td>
                    <td>S/ {seg.maxAnnualCoverage}</td>
                    <td>
                      <span className={`badge ${seg.active ? 'bg-success' : 'bg-secondary'}`}>
                        {seg.active ? 'Activo' : 'Inactivo'}
                      </span>
                    </td>
                  </tr>
                ))}
            </tbody>
          </table>
        </div>
      </div>
    </>
  );
}