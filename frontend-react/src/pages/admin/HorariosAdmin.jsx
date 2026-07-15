import { useState, useEffect, useCallback } from 'react';
import { getDoctores } from '../../api/doctoresApi';
import { getHorariosPorDoctor, crearHorario, actualizarHorario, eliminarHorario } from '../../api/horariosAdminApi';

const DIAS_SEMANA = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'];
const DIAS_NOMBRES = ['Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes', 'Sábado', 'Domingo'];

export default function HorariosAdmin() {
  const [doctores, setDoctores] = useState([]);
  const [horarios, setHorarios] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);
  const [showForm, setShowForm] = useState(false);
  const [selectedDoctorId, setSelectedDoctorId] = useState('');
  const [editingId, setEditingId] = useState(null);
  const [form, setForm] = useState({
    dayOfWeek: 'MONDAY',
    startTime: '09:00',
    endTime: '17:00',
    appointmentDurationMinutes: 30,
    maxAppointmentsPerDay: 8,
    notes: '',
  });

  const loadHorarios = useCallback(async () => {
    try {
      setIsLoading(true);
      const data = await getHorariosPorDoctor(selectedDoctorId);
      setHorarios(Array.isArray(data) ? data : []);
    } catch (err) {
      setError(err.message);
    } finally {
      setIsLoading(false);
    }
  }, [selectedDoctorId]);

  useEffect(() => {
    let cancelled = false;

    getDoctores()
      .then((data) => {
        if (cancelled) return;
        setDoctores(Array.isArray(data) ? data : []);
        if (data?.length > 0) {
          setSelectedDoctorId(data[0].id);
        }
      })
      .catch((err) => {
        if (!cancelled) setError(err.message);
      });

    return () => {
      cancelled = true;
    };
  }, []);

  useEffect(() => {
    if (!selectedDoctorId) return undefined;

    let cancelled = false;
    getHorariosPorDoctor(selectedDoctorId)
      .then((data) => {
        if (!cancelled) setHorarios(Array.isArray(data) ? data : []);
      })
      .catch((err) => {
        if (!cancelled) setError(err.message);
      })
      .finally(() => {
        if (!cancelled) setIsLoading(false);
      });

    return () => {
      cancelled = true;
    };
  }, [selectedDoctorId]);

  async function handleSubmit(e) {
    e.preventDefault();
    try {
      if (editingId) {
        await actualizarHorario(editingId, form);
      } else {
        await crearHorario(selectedDoctorId, form);
      }
      setForm({ dayOfWeek: 'MONDAY', startTime: '09:00', endTime: '17:00', appointmentDurationMinutes: 30, maxAppointmentsPerDay: 8, notes: '' });
      setEditingId(null);
      setShowForm(false);
      await loadHorarios();
    } catch (err) {
      setError(err.message);
    }
  }

  async function handleDelete(id) {
    if (window.confirm('¿Eliminar horario?')) {
      try {
        await eliminarHorario(id);
        await loadHorarios();
      } catch (err) {
        setError(err.message);
      }
    }
  }

  function handleEdit(horario) {
    setForm({
      dayOfWeek: horario.dayOfWeek,
      startTime: horario.startTime,
      endTime: horario.endTime,
      appointmentDurationMinutes: horario.appointmentDurationMinutes,
      maxAppointmentsPerDay: horario.maxAppointmentsPerDay,
      notes: horario.notes,
    });
    setEditingId(horario.id);
    setShowForm(true);
  }

  if (error) return <div className="alert alert-danger m-4">{error}</div>;

  return (
    <div className="container-fluid p-4">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h3 className="fw-semibold">Horarios Médicos</h3>
      </div>

      <div className="mb-4">
        <label className="form-label">Seleccionar Doctor</label>
        <select className="form-select" value={selectedDoctorId} onChange={(e) => setSelectedDoctorId(e.target.value)}>
          {doctores.map((doc) => (
            <option key={doc.id} value={doc.id}>{doc.firstName} {doc.lastName}</option>
          ))}
        </select>
      </div>

      <div className="mb-4">
        <button className="btn btn-primary" onClick={() => { setShowForm(!showForm); setEditingId(null); setForm({ dayOfWeek: 'MONDAY', startTime: '09:00', endTime: '17:00', appointmentDurationMinutes: 30, maxAppointmentsPerDay: 8, notes: '' }); }}>
          {showForm ? 'Cerrar' : '+ Nuevo Horario'}
        </button>
      </div>

      {showForm && (
        <div className="card mb-4">
          <div className="card-body">
            <form onSubmit={handleSubmit}>
              <div className="row">
                <div className="col-md-6 mb-3">
                  <label className="form-label">Día de la Semana</label>
                  <select className="form-select" value={form.dayOfWeek} onChange={(e) => setForm({ ...form, dayOfWeek: e.target.value })}>
                    {DIAS_SEMANA.map((dia, idx) => (
                      <option key={dia} value={dia}>{DIAS_NOMBRES[idx]}</option>
                    ))}
                  </select>
                </div>
                <div className="col-md-3 mb-3">
                  <label className="form-label">Hora Inicio</label>
                  <input type="time" className="form-control" value={form.startTime} onChange={(e) => setForm({ ...form, startTime: e.target.value })} />
                </div>
                <div className="col-md-3 mb-3">
                  <label className="form-label">Hora Fin</label>
                  <input type="time" className="form-control" value={form.endTime} onChange={(e) => setForm({ ...form, endTime: e.target.value })} />
                </div>
                <div className="col-md-6 mb-3">
                  <label className="form-label">Duración Cita (minutos)</label>
                  <input type="number" className="form-control" min="15" value={form.appointmentDurationMinutes} onChange={(e) => setForm({ ...form, appointmentDurationMinutes: parseInt(e.target.value) })} />
                </div>
                <div className="col-md-6 mb-3">
                  <label className="form-label">Max Citas por Día</label>
                  <input type="number" className="form-control" min="1" value={form.maxAppointmentsPerDay} onChange={(e) => setForm({ ...form, maxAppointmentsPerDay: parseInt(e.target.value) })} />
                </div>
                <div className="col-12 mb-3">
                  <label className="form-label">Notas</label>
                  <textarea className="form-control" rows="2" value={form.notes} onChange={(e) => setForm({ ...form, notes: e.target.value })}></textarea>
                </div>
              </div>
              <button type="submit" className="btn btn-success">Guardar</button>
            </form>
          </div>
        </div>
      )}

      {isLoading ? (
        <div className="text-center">
          <div className="spinner-border" role="status"></div>
        </div>
      ) : (
        <div className="table-responsive">
          <table className="table table-striped">
            <thead>
              <tr>
                <th>Día</th>
                <th>Hora Inicio</th>
                <th>Hora Fin</th>
                <th>Duración</th>
                <th>Max Citas</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              {horarios.map((hor) => (
                <tr key={hor.id}>
                  <td>{DIAS_NOMBRES[DIAS_SEMANA.indexOf(hor.dayOfWeek)]}</td>
                  <td>{hor.startTime}</td>
                  <td>{hor.endTime}</td>
                  <td>{hor.appointmentDurationMinutes} min</td>
                  <td>{hor.maxAppointmentsPerDay}</td>
                  <td>
                    <button className="btn btn-sm btn-warning me-2" onClick={() => handleEdit(hor)}>Editar</button>
                    <button className="btn btn-sm btn-danger" onClick={() => handleDelete(hor.id)}>Eliminar</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
