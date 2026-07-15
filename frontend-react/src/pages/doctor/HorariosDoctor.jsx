export default function HorariosDoctor() {
  const primaryColor = "#00a4e4"; // Celeste corporativo Aviva

  // Componente auxiliar para renderizar las tarjetas de citas en el calendario
  const CitaCard = ({ paciente, tipo }) => {
    // Definimos los colores según el tipo de cita usando clases Bootstrap
    let colorClass = "";
    let iconClass = "";
    let borderClass = "";

    switch (tipo) {
      case 'Normal':
        colorClass = "bg-primary bg-opacity-10 text-dark";
        borderClass = "border-start border-primary border-4";
        iconClass = "bi-calendar-check text-primary";
        break;
      case 'Reprogramada':
        colorClass = "bg-warning bg-opacity-10 text-dark";
        borderClass = "border-start border-warning border-4";
        iconClass = "bi-clock-history text-warning";
        break;
      case 'Resultados':
        colorClass = "bg-success bg-opacity-10 text-dark";
        borderClass = "border-start border-success border-4";
        iconClass = "bi-file-earmark-medical text-success";
        break;
      case 'Urgente':
        colorClass = "bg-danger bg-opacity-10 text-dark";
        borderClass = "border-start border-danger border-4";
        iconClass = "bi-exclamation-triangle-fill text-danger";
        break;
      default:
        colorClass = "bg-light text-dark";
        borderClass = "border-start border-secondary border-4";
        iconClass = "bi-calendar";
    }

    return (
      <div className={`p-2 rounded mb-1 shadow-sm ${colorClass} ${borderClass}`} style={{ minHeight: '60px' }}>
        <div className="fw-bold" style={{ fontSize: '0.85rem' }}>{paciente}</div>
        <div className="text-muted mt-1" style={{ fontSize: '0.75rem' }}>
          <i className={`bi ${iconClass} me-1`}></i>{tipo}
        </div>
      </div>
    );
  };

  return (
    <div className="container-fluid p-0 animation-fade-in">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div>
          <h2 className="h4 fw-bold mb-1" style={{ color: '#004b87' }}>Calendario de Citas</h2>
          <p className="text-muted mb-0 small">Horarios de atención programados</p>
        </div>
        
        {/* Paginación de Semanas */}
        <div className="btn-group shadow-sm">
          <button className="btn btn-white border bg-white"><i className="bi bi-chevron-left text-muted"></i></button>
          <button className="btn btn-white border bg-white fw-bold" style={{ color: '#004b87' }}>
            <i className="bi bi-calendar3 me-2"></i>13 Jul - 17 Jul, 2026
          </button>
          <button className="btn btn-white border bg-white"><i className="bi bi-chevron-right text-muted"></i></button>
        </div>
      </div>

      <div className="card border-0 shadow-sm rounded-3">
        <div className="card-header bg-white border-bottom p-3 d-flex justify-content-between align-items-center">
          
          {/* Leyenda de colores */}
          <div className="d-flex gap-3">
            <span className="badge bg-primary bg-opacity-10 text-primary border border-primary"><i className="bi bi-calendar-check me-1"></i> Normal</span>
            <span className="badge bg-warning bg-opacity-25 text-dark border border-warning"><i className="bi bi-clock-history me-1"></i> Reprogramada</span>
            <span className="badge bg-success bg-opacity-10 text-success border border-success"><i className="bi bi-file-earmark-medical me-1"></i> Resultados</span>
            <span className="badge bg-danger bg-opacity-10 text-danger border border-danger"><i className="bi bi-exclamation-triangle-fill me-1"></i> Urgente</span>
          </div>

          <button className="btn btn-sm btn-outline-secondary">
            <i className="bi bi-printer me-1"></i> Imprimir Semana
          </button>
        </div>

        <div className="card-body p-0">
          <div className="table-responsive">
            <table className="table table-bordered align-middle mb-0" style={{ minWidth: '1000px', tableLayout: 'fixed' }}>
              <thead className="table-light text-center">
                <tr>
                  <th style={{ width: '8%' }} className="py-3 text-muted"><i className="bi bi-clock"></i> Hora</th>
                  <th style={{ width: '18.4%' }} className="py-3" style={{ color: '#004b87' }}>Lunes 13</th>
                  <th style={{ width: '18.4%' }} className="py-3" style={{ color: '#004b87' }}>Martes 14</th>
                  <th style={{ width: '18.4%' }} className="py-3" style={{ color: '#004b87' }}>Miércoles 15</th>
                  <th style={{ width: '18.4%' }} className="py-3" style={{ color: '#004b87' }}>Jueves 16</th>
                  <th style={{ width: '18.4%' }} className="py-3" style={{ color: '#004b87' }}>Viernes 17</th>
                </tr>
              </thead>
              <tbody>
                {/* 08:00 AM */}
                <tr>
                  <td className="text-center fw-bold text-muted bg-light">08:00 AM</td>
                  <td className="p-2"><CitaCard paciente="Carlos Mendoza" tipo="Normal" /></td>
                  <td className="p-2"><CitaCard paciente="Raúl Gutiérrez" tipo="Urgente" /></td>
                  <td className="p-2"><CitaCard paciente="Lucía Paz" tipo="Normal" /></td>
                  <td className="p-2"><CitaCard paciente="Roberto Gómez" tipo="Reprogramada" /></td>
                  <td className="p-2"><CitaCard paciente="Elena Castro" tipo="Resultados" /></td>
                </tr>

                {/* 09:00 AM */}
                <tr>
                  <td className="text-center fw-bold text-muted bg-light">09:00 AM</td>
                  <td className="p-2"><CitaCard paciente="Ana Sofía Vargas" tipo="Resultados" /></td>
                  <td className="p-2"><CitaCard paciente="María González" tipo="Normal" /></td>
                  <td className="p-2 bg-light bg-opacity-50"></td>
                  <td className="p-2"><CitaCard paciente="Diego Torres" tipo="Normal" /></td>
                  <td className="p-2"><CitaCard paciente="Sofía Miranda" tipo="Urgente" /></td>
                </tr>

                {/* 10:00 AM */}
                <tr>
                  <td className="text-center fw-bold text-muted bg-light">10:00 AM</td>
                  <td className="p-2"><CitaCard paciente="Jorge Rojas" tipo="Reprogramada" /></td>
                  <td className="p-2"><CitaCard paciente="Martín Silva" tipo="Resultados" /></td>
                  <td className="p-2"><CitaCard paciente="Camila Ríos" tipo="Normal" /></td>
                  <td className="p-2"><CitaCard paciente="Andrés Núñez" tipo="Normal" /></td>
                  <td className="p-2 bg-light bg-opacity-50"></td>
                </tr>

                {/* 11:00 AM */}
                <tr>
                  <td className="text-center fw-bold text-muted bg-light">11:00 AM</td>
                  <td className="p-2"><CitaCard paciente="Valeria Silva" tipo="Normal" /></td>
                  <td className="p-2 bg-light bg-opacity-50"></td>
                  <td className="p-2"><CitaCard paciente="Fernando López" tipo="Reprogramada" /></td>
                  <td className="p-2"><CitaCard paciente="Julio César" tipo="Urgente" /></td>
                  <td className="p-2"><CitaCard paciente="Diana Paredes" tipo="Resultados" /></td>
                </tr>

                {/* 12:00 PM (Mediodía) */}
                <tr>
                  <td className="text-center fw-bold text-muted bg-light">12:00 PM</td>
                  <td className="p-2"><CitaCard paciente="Renato Tapia" tipo="Urgente" /></td>
                  <td className="p-2"><CitaCard paciente="Carmen Soto" tipo="Normal" /></td>
                  <td className="p-2"><CitaCard paciente="Javier Ríos" tipo="Resultados" /></td>
                  <td className="p-2"><CitaCard paciente="Pedro Sánchez" tipo="Normal" /></td>
                  <td className="p-2"><CitaCard paciente="Luisa Espinoza" tipo="Reprogramada" /></td>
                </tr>

                {/* DESCANSO MÉDICO */}
                <tr>
                  <td className="text-center fw-bold text-muted border-end-0 bg-light">01:00 PM</td>
                  <td colSpan="5" className="text-center bg-light text-muted p-3 border-start-0" style={{ backgroundImage: 'repeating-linear-gradient(45deg, #f8f9fa, #f8f9fa 10px, #e9ecef 10px, #e9ecef 20px)' }}>
                    <h6 className="mb-0 fw-bold"><i className="bi bi-cup-hot me-2"></i>Horario de Descanso / Almuerzo Médico</h6>
                  </td>
                </tr>

                {/* 02:00 PM */}
                <tr>
                  <td className="text-center fw-bold text-muted bg-light">02:00 PM</td>
                  <td className="p-2"><CitaCard paciente="Miguel Ángel" tipo="Normal" /></td>
                  <td className="p-2"><CitaCard paciente="Teresa Vidal" tipo="Resultados" /></td>
                  <td className="p-2 bg-light bg-opacity-50"></td>
                  <td className="p-2"><CitaCard paciente="Hugo Chávez" tipo="Normal" /></td>
                  <td className="p-2"><CitaCard paciente="Daniela Vega" tipo="Normal" /></td>
                </tr>

                {/* 03:00 PM */}
                <tr>
                  <td className="text-center fw-bold text-muted bg-light">03:00 PM</td>
                  <td className="p-2"><CitaCard paciente="Patricia Reyes" tipo="Reprogramada" /></td>
                  <td className="p-2"><CitaCard paciente="Gustavo Lima" tipo="Normal" /></td>
                  <td className="p-2"><CitaCard paciente="Claudio Pizarro" tipo="Urgente" /></td>
                  <td className="p-2"><CitaCard paciente="Rosa Alarcón" tipo="Resultados" /></td>
                  <td className="p-2 bg-light bg-opacity-50"></td>
                </tr>

                {/* 04:00 PM */}
                <tr>
                  <td className="text-center fw-bold text-muted bg-light border-bottom-0">04:00 PM</td>
                  <td className="p-2 bg-light bg-opacity-50 border-bottom-0"></td>
                  <td className="p-2 border-bottom-0"><CitaCard paciente="Marcos León" tipo="Reprogramada" /></td>
                  <td className="p-2 border-bottom-0"><CitaCard paciente="Fiorella Díaz" tipo="Normal" /></td>
                  <td className="p-2 border-bottom-0"><CitaCard paciente="Gabriel Ortiz" tipo="Normal" /></td>
                  <td className="p-2 border-bottom-0"><CitaCard paciente="Alonso Meza" tipo="Urgente" /></td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
        
        {/* Footer info */}
        <div className="card-footer bg-white border-top p-3 text-center">
          <small className="text-muted"><i className="bi bi-info-circle me-1"></i> Los espacios vacíos indican disponibilidad en su agenda. Sincronizado en tiempo real con recepción.</small>
        </div>
      </div>
    </div>
  );
}