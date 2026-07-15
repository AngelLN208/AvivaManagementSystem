import { useQuery } from '@tanstack/react-query';
import { getCitas } from '../../api/dashboardApi';

const DIAS = ['Dom', 'Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb'];

function agruparUltimos7Dias(citas) {
  const hoy = new Date();
  hoy.setHours(0, 0, 0, 0);

  const dias = Array.from({ length: 7 }, (_, i) => {
    const fecha = new Date(hoy);
    fecha.setDate(hoy.getDate() - (6 - i));
    return { fecha, total: 0 };
  });

  citas.forEach((cita) => {
    const f = new Date(cita.appointmentDateTime);
    if (isNaN(f)) return;
    f.setHours(0, 0, 0, 0);
    const dia = dias.find((d) => d.fecha.getTime() === f.getTime());
    if (dia) dia.total += 1;
  });

  return dias.map((d) => ({ label: DIAS[d.fecha.getDay()], total: d.total }));
}

export default function CitasSemana() {
  const { data, isLoading, isError, dataUpdatedAt } = useQuery({
    queryKey: ['citas-semana'],
    queryFn: getCitas,
    refetchInterval: 60000, // se actualiza solo cada 60s
  });

  const citas = Array.isArray(data) ? data : [];
  const dias = agruparUltimos7Dias(citas);
  const max = Math.max(1, ...dias.map((d) => d.total));

  return (
    <div className="card border-0 rounded-4 shadow-sm p-4 h-100">
      <div className="d-flex justify-content-between align-items-start mb-3">
        <div>
          <h6 className="fw-bold mb-0">Citas de la semana</h6>
          <small className="text-muted">Últimos 7 días</small>
        </div>
        {!isLoading && !isError && (
          <span className="badge fondo-exito-sutil texto-exito rounded-pill px-2 py-1">
            <i className="fa-solid fa-circle me-1" style={{ fontSize: '0.5rem' }}></i>
            En vivo
          </span>
        )}
      </div>

      {isLoading ? (
        <div className="text-center py-4 text-muted">
          <small>Cargando...</small>
        </div>
      ) : isError ? (
        <div className="text-center py-4 text-muted">
          <small>No se pudo cargar la gráfica</small>
        </div>
      ) : (
        <>
          <div className="d-flex align-items-end justify-content-between" style={{ height: 120, gap: 8 }}>
            {dias.map((d, i) => (
              <div key={i} className="d-flex flex-column align-items-center flex-grow-1" style={{ height: '100%' }}>
                <div className="flex-grow-1 d-flex align-items-end w-100">
                  <div
                    title={`${d.total} citas`}
                    style={{
                      width: '100%',
                      height: `${Math.max(6, (d.total / max) * 100)}%`,
                      background: d.total > 0 ? 'var(--color-primario-oscuro)' : 'var(--borde-claro)',
                      borderRadius: '6px 6px 0 0',
                      transition: 'height 0.3s ease',
                    }}
                  ></div>
                </div>
                <small className="text-muted mt-2" style={{ fontSize: '0.72rem' }}>{d.label}</small>
              </div>
            ))}
          </div>

          {dataUpdatedAt && (
            <small className="text-muted d-block text-end mt-3" style={{ fontSize: '0.7rem' }}>
              Actualizado {new Date(dataUpdatedAt).toLocaleTimeString('es-PE', { hour: '2-digit', minute: '2-digit' })}
            </small>
          )}
        </>
      )}
    </div>
  );
}