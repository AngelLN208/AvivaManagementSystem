import { useState, useEffect } from 'react';
import { getDoctores, crearDoctor, actualizarDoctor, eliminarDoctor } from '../../api/doctoresApi';
import { getEspecialidades } from '../../api/especialidadesApi';

export default function DoctoresAdmin() {
  const [doctores, setDoctores] = useState([]);
  const [especialidades, setEspecialidades] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);
  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [form, setForm] = useState({
    firstName: '',
    lastName: '',
    licenseNumber: '',
    phone: '',
    email: '',
    specialtyId: '',
  });

  useEffect(() => {
    loadData();
  }, []);

  async function loadData() {
    try {
      setIsLoading(true);
      const [doctoresData, especialidadesData] = await Promise.all([
        getDoctores(),
        getEspecialidades(),
      ]);
      setDoctores(Array.isArray(doctoresData) ? doctoresData : []);
      setEspecialidades(Array.isArray(especialidadesData) ? especialidadesData : []);
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
        await actualizarDoctor(editingId, form);
      } else {
        await crearDoctor(form);
      }
      setForm({ firstName: '', lastName: '', licenseNumber: '', phone: '', email: '', specialtyId: '' });
      setEditingId(null);
      setShowForm(false);
      await loadData();
    } catch (err) {
      setError(err.message);
    }
  }

  async function handleDelete(id) {
    if (window.confirm('¿Eliminar doctor?')) {
      try {
        await eliminarDoctor(id);
        await loadData();
      } catch (err) {
        setError(err.message);
      }
    }
  }

  function handleEdit(doctor) {
    setForm({
      firstName: doctor.firstName,
      lastName: doctor.lastName,
      licenseNumber: doctor.licenseNumber,
      phone: doctor.phone,
      email: doctor.email,
      specialtyId: doctor.specialty?.id || '',
    });
    setEditingId(doctor.id);
    setShowForm(true);
  }

  if (error) return <div className="alert alert-danger m-4">{error}</div>;

  return (
    <div className="container-fluid p-4">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h3 className="fw-semibold">Doctores</h3>
        <button className="btn btn-primary" onClick={() => { setShowForm(!showForm); setEditingId(null); setForm({ firstName: '', lastName: '', licenseNumber: '', phone: '', email: '', specialtyId: '' }); }}>
          {showForm ? 'Cerrar' : '+ Nuevo'}
        </button>
      </div>

      {showForm && (
        <div className="card mb-4">
          <div className="card-body">
            <form onSubmit={handleSubmit}>
              <div className="row">
                <div className="col-md-6 mb-3">
                  <label className="form-label">Nombre</label>
                  <input type="text" className="form-control" required value={form.firstName} onChange={(e) => setForm({ ...form, firstName: e.target.value })} />
                </div>
                <div className="col-md-6 mb-3">
                  <label className="form-label">Apellido</label>
                  <input type="text" className="form-control" required value={form.lastName} onChange={(e) => setForm({ ...form, lastName: e.target.value })} />
                </div>
                <div className="col-md-6 mb-3">
                  <label className="form-label">Número de Licencia</label>
                  <input type="text" className="form-control" required value={form.licenseNumber} onChange={(e) => setForm({ ...form, licenseNumber: e.target.value })} />
                </div>
                <div className="col-md-6 mb-3">
                  <label className="form-label">Especialidad</label>
                  <select className="form-select" required value={form.specialtyId} onChange={(e) => setForm({ ...form, specialtyId: e.target.value })}>
                    <option value="">Seleccionar</option>
                    {especialidades.map((e) => (
                      <option key={e.id} value={e.id}>{e.name}</option>
                    ))}
                  </select>
                </div>
                <div className="col-md-6 mb-3">
                  <label className="form-label">Teléfono</label>
                  <input type="tel" className="form-control" value={form.phone} onChange={(e) => setForm({ ...form, phone: e.target.value })} />
                </div>
                <div className="col-md-6 mb-3">
                  <label className="form-label">Email</label>
                  <input type="email" className="form-control" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} />
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
                <th>Nombre</th>
                <th>Licencia</th>
                <th>Especialidad</th>
                <th>Email</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              {doctores.map((doc) => (
                <tr key={doc.id}>
                  <td>{doc.firstName} {doc.lastName}</td>
                  <td>{doc.licenseNumber}</td>
                  <td>{doc.specialty?.name}</td>
                  <td>{doc.email}</td>
                  <td>
                    <button className="btn btn-sm btn-warning me-2" onClick={() => handleEdit(doc)}>Editar</button>
                    <button className="btn btn-sm btn-danger" onClick={() => handleDelete(doc.id)}>Eliminar</button>
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
