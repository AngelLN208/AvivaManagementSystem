import { useState, useEffect } from 'react';
import { getPacientes, actualizarPaciente, eliminarPaciente, crearPaciente } from '../../api/pacientesApi';

export default function PacientesAdmin() {
  const [pacientes, setPacientes] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);
  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [form, setForm] = useState({
    dni: '',
    firstName: '',
    lastName: '',
    gender: 'MALE',
    dateOfBirth: '',
    phone: '',
    email: '',
    address: '',
  });

  useEffect(() => {
    loadData();
  }, []);

  async function loadData() {
    try {
      setIsLoading(true);
      const data = await getPacientes();
      setPacientes(Array.isArray(data) ? data : []);
    } catch (err) {
      setError(err.message);
    } finally {
      setIsLoading(false);
    }
  }

  async function handleSubmit(e) {
    e.preventDefault();
    try {
      if (editingId) {
        await actualizarPaciente(editingId, form);
      } else {
        await crearPaciente(form);
      }
      setForm({ dni: '', firstName: '', lastName: '', gender: 'MALE', dateOfBirth: '', phone: '', email: '', address: '' });
      setEditingId(null);
      setShowForm(false);
      await loadData();
    } catch (err) {
      setError(err.message);
    }
  }

  async function handleDelete(id) {
    if (window.confirm('¿Eliminar paciente?')) {
      try {
        await eliminarPaciente(id);
        await loadData();
      } catch (err) {
        setError(err.message);
      }
    }
  }

  function handleEdit(paciente) {
    setForm({
      dni: paciente.dni,
      firstName: paciente.firstName,
      lastName: paciente.lastName,
      gender: paciente.gender,
      dateOfBirth: paciente.dateOfBirth?.split('T')[0] || '',
      phone: paciente.phone,
      email: paciente.email,
      address: paciente.address,
    });
    setEditingId(paciente.id);
    setShowForm(true);
  }

  if (error) return <div className="alert alert-danger m-4">{error}</div>;

  return (
    <div className="container-fluid p-4">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h3 className="fw-semibold">Pacientes</h3>
        <button className="btn btn-primary" onClick={() => { setShowForm(!showForm); setEditingId(null); setForm({ dni: '', firstName: '', lastName: '', gender: 'MALE', dateOfBirth: '', phone: '', email: '', address: '' }); }}>
          {showForm ? 'Cerrar' : '+ Nuevo'}
        </button>
      </div>

      {showForm && (
        <div className="card mb-4">
          <div className="card-body">
            <form onSubmit={handleSubmit}>
              <div className="row">
                <div className="col-md-6 mb-3">
                  <label className="form-label">DNI</label>
                  <input type="text" className="form-control" required value={form.dni} onChange={(e) => setForm({ ...form, dni: e.target.value })} />
                </div>
                <div className="col-md-6 mb-3">
                  <label className="form-label">Género</label>
                  <select className="form-select" value={form.gender} onChange={(e) => setForm({ ...form, gender: e.target.value })}>
                    <option value="MALE">Masculino</option>
                    <option value="FEMALE">Femenino</option>
                    <option value="OTHER">Otro</option>
                  </select>
                </div>
                <div className="col-md-6 mb-3">
                  <label className="form-label">Nombre</label>
                  <input type="text" className="form-control" required value={form.firstName} onChange={(e) => setForm({ ...form, firstName: e.target.value })} />
                </div>
                <div className="col-md-6 mb-3">
                  <label className="form-label">Apellido</label>
                  <input type="text" className="form-control" required value={form.lastName} onChange={(e) => setForm({ ...form, lastName: e.target.value })} />
                </div>
                <div className="col-md-6 mb-3">
                  <label className="form-label">Fecha de Nacimiento</label>
                  <input type="date" className="form-control" value={form.dateOfBirth} onChange={(e) => setForm({ ...form, dateOfBirth: e.target.value })} />
                </div>
                <div className="col-md-6 mb-3">
                  <label className="form-label">Teléfono</label>
                  <input type="tel" className="form-control" value={form.phone} onChange={(e) => setForm({ ...form, phone: e.target.value })} />
                </div>
                <div className="col-md-6 mb-3">
                  <label className="form-label">Email</label>
                  <input type="email" className="form-control" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} />
                </div>
                <div className="col-md-6 mb-3">
                  <label className="form-label">Dirección</label>
                  <input type="text" className="form-control" value={form.address} onChange={(e) => setForm({ ...form, address: e.target.value })} />
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
                <th>DNI</th>
                <th>Nombre</th>
                <th>Email</th>
                <th>Teléfono</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              {pacientes.map((pac) => (
                <tr key={pac.id}>
                  <td>{pac.dni}</td>
                  <td>{pac.firstName} {pac.lastName}</td>
                  <td>{pac.email}</td>
                  <td>{pac.phone}</td>
                  <td>
                    <button className="btn btn-sm btn-warning me-2" onClick={() => handleEdit(pac)}>Editar</button>
                    <button className="btn btn-sm btn-danger" onClick={() => handleDelete(pac.id)}>Eliminar</button>
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
