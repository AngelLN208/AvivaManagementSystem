const ACENTOS = {
  primario: 'fondo-primario-sutil texto-primario-personalizado',
  info: 'fondo-info-sutil texto-info',
  exito: 'fondo-exito-sutil texto-exito',
  advertencia: 'fondo-advertencia-sutil texto-advertencia',
};

export default function StatCard({ icon, title, value, subtitle, badge, accent = 'primario', isLoading }) {
  return (
    <div className="card tarjeta-estadistica border-0 p-3 shadow-sm h-100">
      <div className="card-body p-0 d-flex justify-content-between align-items-start">
        <div className="flex-grow-1">
          <div className="d-flex justify-content-between align-items-center mb-2">
            <h6 className="text-muted mb-0">{title}</h6>
            {badge && (
              <span className="badge fondo-info-sutil texto-info rounded-pill px-2 py-1">
                {badge}
              </span>
            )}
          </div>
          <h2 className="fw-bold mb-1">{isLoading ? '—' : value}</h2>
          <small className="text-muted">{isLoading ? 'Cargando...' : subtitle}</small>
        </div>

        <div className={`icono-estadistica ${ACENTOS[accent]}`}>
          <i className={icon}></i>
        </div>
      </div>
    </div>
  );
}