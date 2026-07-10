export default function StatCard({ icon, title, value, subtitle, badge, isLoading }) {
  return (
    <div className="card tarjeta-estadistica border-0 rounded-4 p-3 shadow-sm">
      <div className="card-body">
        <div className="d-flex justify-content-between align-items-center mb-2">
          <h6 className="text-muted mb-0">
            <i className={`${icon} me-2`}></i> {title}
          </h6>
          {badge && (
            <span className="badge fondo-info-sutil texto-info rounded-pill px-2 py-1">
              {badge}
            </span>
          )}
        </div>
        <h2 className="fw-bold mb-0">{isLoading ? '—' : value}</h2>
        <small className="text-muted">{isLoading ? 'Cargando...' : subtitle}</small>
      </div>
    </div>
  );
}