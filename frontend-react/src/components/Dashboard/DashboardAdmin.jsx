import "./DashboardAdmin.css";

function DashboardAdmin() {
  return (
    <div className="container-fluid p-4">
      <h3 className="fw-semibold mb-4">Dashboard</h3>

      <div className="row g-4">
        <div className="col-md-4">
          <div className="dashboard-card">
            <div className="d-flex justify-content-between align-items-center">
              <div>
                <h6 className="text-muted">Pacientes</h6>

                <h2>120</h2>

                <small className="text-success">+10% desde el mes pasado</small>
              </div>

              <div className="dashboard-icon bg-card-primary">
                <i className="bi bi-people-fill"></i>
              </div>
            </div>
          </div>
        </div>

        <div className="col-md-4">
          <div className="dashboard-card">
            <div className="d-flex justify-content-between align-items-center">
              <div>
                <h6 className="text-muted">Citas Hoy</h6>

                <h2>15</h2>

                <small className="text-danger">-5% desde ayer</small>
              </div>

              <div className="dashboard-icon bg-card-success">
                <i className="bi bi-calendar-check-fill"></i>
              </div>
            </div>
          </div>
        </div>

        <div className="col-md-4">
          <div className="dashboard-card">
            <div className="d-flex justify-content-between align-items-center">
              <div>
                <h6 className="text-muted">Pagos Pendientes</h6>

                <h2>8</h2>

                <small className="text-danger">3 sin confirmar</small>
              </div>

              <div className="dashboard-icon bg-card-warning">
                <i className="bi bi-credit-card-fill"></i>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default DashboardAdmin;
