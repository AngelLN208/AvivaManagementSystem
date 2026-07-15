import { useState } from 'react';
import { useNavigate } from 'react-router-dom';

export default function CitasDoctor() {
  const navigate = useNavigate();
  
  // Estado para controlar el Modal
  const [showModal, setShowModal] = useState(false);
  const [pacienteSeleccionado, setPacienteSeleccionado] = useState(null);

  const primaryColor = "#00a4e4";

  // Datos extendidos
  const citasMock = [
    { id: 1, hora: '08:00 AM', paciente: 'Carlos Mendoza', dni: '74839201', motivo: 'Chequeo general', estado: 'Atendida', edad: 45 },
    { id: 2, hora: '09:00 AM', paciente: 'María González', dni: '48392011', motivo: 'Dolor de cabeza persistente', estado: 'En Espera', edad: 32 },
    { id: 3, hora: '10:00 AM', paciente: 'Luis Ramírez', dni: '09384722', motivo: 'Revisión de exámenes', estado: 'No llega', edad: 50 },
    { id: 4, hora: '10:30 AM', paciente: 'Ana Sofía Vargas', dni: '73829103', motivo: 'Fiebre alta', estado: 'Adicional', edad: 28 },
  ];

  const abrirHistorial = (paciente) => {
    setPacienteSeleccionado(paciente);
    setShowModal(true);
  };

  return (
    <div className="container-fluid p-0">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h2 className="h4 fw-bold mb-0" style={{ color: '#004b87' }}>Visión General</h2>
        <p className="text-muted mb-0"><i className="bi bi-calendar3 me-2"></i>Hoy, 15 Jul 2026</p>
      </div>

      {/* Tarjetas de Estadísticas */}
      <div className="row g-3 mb-4">
        <div className="col-md-4">
          <div className="card border-0 shadow-sm rounded-3">
            <div className="card-body p-4 d-flex align-items-center">
              <div className="rounded p-3 me-3" style={{ backgroundColor: 'rgba(0, 164, 228, 0.1)', color: primaryColor }}>
                <i className="bi bi-people-fill fs-3"></i>
              </div>
              <div>
                <p className="text-muted mb-1 small text-uppercase fw-bold">Pacientes del Día</p>
                <h3 className="fw-bold mb-0">12</h3>
              </div>
            </div>
          </div>
        </div>
        <div className="col-md-4">
          <div className="card border-0 shadow-sm rounded-3">
            <div className="card-body p-4 d-flex align-items-center">
              <div className="rounded p-3 me-3 bg-success bg-opacity-10 text-success">
                <i className="bi bi-check-circle-fill fs-3"></i>
              </div>
              <div>
                <p className="text-muted mb-1 small text-uppercase fw-bold">Atendidos</p>
                <h3 className="fw-bold mb-0">4</h3>
              </div>
            </div>
          </div>
        </div>
        <div className="col-md-4">
          <div className="card border-0 shadow-sm rounded-3">
            <div className="card-body p-4 d-flex align-items-center">
              <div className="rounded p-3 me-3 bg-warning bg-opacity-10 text-warning">
                <i className="bi bi-clock-fill fs-3"></i>
              </div>
              <div>
                <p className="text-muted mb-1 small text-uppercase fw-bold">En Espera</p>
                <h3 className="fw-bold mb-0">8</h3>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Tabla de Citas */}
      <div className="card border-0 shadow-sm rounded-3">
        <div className="card-header bg-white border-bottom p-4">
          <h5 className="fw-bold mb-0"><i className="bi bi-list-task me-2" style={{ color: primaryColor }}></i> Citas Programadas</h5>
        </div>
        <div className="card-body p-0">
          <div className="table-responsive">
            <table className="table table-hover align-middle mb-0">
              <thead className="table-light">
                <tr>
                  <th className="ps-4 py-3">Hora</th>
                  <th>Paciente</th>
                  <th>Motivo</th>
                  <th>Estado</th>
                  <th className="text-end pe-4">Acción</th>
                </tr>
              </thead>
              <tbody>
                {citasMock.map((cita) => (
                  <tr key={cita.id}>
                    <td className="ps-4 fw-bold text-muted">{cita.hora}</td>
                    <td>
                      <p className="mb-0 fw-bold text-dark">{cita.paciente}</p>
                      <small className="text-muted">DNI: {cita.dni}</small>
                    </td>
                    <td className="text-secondary">{cita.motivo}</td>
                    <td>
                      <span className={`badge ${
                        cita.estado === 'Atendida' ? 'bg-success' :
                        cita.estado === 'En Espera' ? 'bg-info text-dark' :
                        cita.estado === 'Adicional' ? 'bg-dark' : 'bg-danger'
                      }`}>
                        {cita.estado}
                      </span>
                    </td>
                    <td className="text-end pe-4">
                      <button 
                        onClick={() => abrirHistorial(cita)}
                        className="btn btn-sm btn-outline-secondary me-2"
                      >
                        <i className="bi bi-journal-medical"></i> Historial
                      </button>

                      {cita.estado === 'En Espera' || cita.estado === 'Adicional' ? (
                        <button 
                          onClick={() => navigate(`/doctor/atender/${cita.id}`)}
                          className="btn btn-sm text-white"
                          style={{ backgroundColor: primaryColor }}
                        >
                          <i className="bi bi-stethoscope me-1"></i> Atender
                        </button>
                      ) : (
                        <button className="btn btn-sm btn-light" disabled>Completada</button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>

      {/* MODAL DE BOOTSTRAP (Controlado por React State) */}
      {showModal && (
        <>
          <div className="modal fade show d-block" tabIndex="-1" style={{ backgroundColor: 'rgba(0,0,0,0.5)' }}>
            <div className="modal-dialog modal-lg modal-dialog-centered">
              <div className="modal-content border-0 shadow">
                <div className="modal-header bg-light">
                  <h5 className="modal-title fw-bold text-dark">
                    <i className="bi bi-folder2-open me-2" style={{ color: primaryColor }}></i> 
                    Expediente: {pacienteSeleccionado?.paciente}
                  </h5>
                  <button type="button" className="btn-close" onClick={() => setShowModal(false)}></button>
                </div>
                <div className="modal-body p-4">
                  <div className="row mb-4 bg-light p-3 rounded mx-1">
                    <div className="col-md-6">
                      <p className="mb-1"><span className="fw-bold">DNI:</span> {pacienteSeleccionado?.dni}</p>
                      <p className="mb-1"><span className="fw-bold">Edad:</span> {pacienteSeleccionado?.edad} años</p>
                    </div>
                    <div className="col-md-6">
                      <p className="mb-1"><span className="fw-bold text-danger">Alergias:</span> Ninguna conocida</p>
                      <p className="mb-1"><span className="fw-bold text-primary">Grupo Sanguíneo:</span> O+</p>
                    </div>
                  </div>
                  
                  <h6 className="fw-bold border-bottom pb-2">Consultas Previas</h6>
                  <div className="list-group list-group-flush">
                    <div className="list-group-item px-0">
                      <div className="d-flex justify-content-between">
                        <strong className="text-dark">Medicina General</strong>
                        <small className="text-muted">10 Mayo 2026</small>
                      </div>
                      <p className="mb-1 text-secondary small">Paciente acudió con síntomas de resfriado común. Se recetó paracetamol.</p>
                    </div>
                    <div className="list-group-item px-0">
                      <div className="d-flex justify-content-between">
                        <strong className="text-dark">Gastroenterología</strong>
                        <small className="text-muted">02 Febrero 2026</small>
                      </div>
                      <p className="mb-1 text-secondary small">Acidez estomacal. Se indicaron antiácidos y dieta blanda.</p>
                    </div>
                  </div>
                </div>
                <div className="modal-footer border-0 bg-light">
                  <button className="btn btn-secondary" onClick={() => setShowModal(false)}>Cerrar</button>
                  <button className="btn text-white" style={{ backgroundColor: primaryColor }}>Imprimir Resumen</button>
                </div>
              </div>
            </div>
          </div>
        </>
      )}
    </div>
  );
}