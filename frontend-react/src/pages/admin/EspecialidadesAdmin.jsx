import { useState, useEffect } from 'react';
import { getEspecialidades, crearEspecialidad, actualizarEspecialidad, eliminarEspecialidad } from '../../api/especialidadesApi';

export default function EspecialidadesAdmin() {
  const [especialidades, setEspecialidades] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);
  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [form, setForm] = useState({
    name: '',
    description: '',
  });

  useEffect(() => {
    loadData();
  }, []);

  async function loadData() {
    try {
      setIsLoading(true);
      const data = await getEspecialidades();
      setEspecialidades(Array.isArray(data) ? data : []);
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
        await actualizarEspecialidad(editingId, form);
      } else {
        await crearEspecialidad(form);
      }
      setForm({ name: '', description: '' });
      setEditingId(null);
      setShowForm(false);
      await loadData();
    } catch (err) {
      setError(err.message);
    }
  }

  async function handleDelete(id) {
    if (window.confirm('¿Eliminar especialidad?')) {
      try {
        await eliminarEspecialidad(id);
        await loadData();
      } catch (err) {
        setError(err.message);
      }
    }
  }

  function handleEdit(spec) {
    setForm({ name: spec.name, description: spec.description });
    setEditingId(spec.id);
    setShowForm(true);
  }

  if (error) return <div className="alert alert-danger m-4">{error}</div>;

  return (
    <div className="container-fluid p-4">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h3 className="fw-semibold">Especialidades</h3>
        <button className="btn btn-primary" onClick={() => { setShowForm(!showForm); setEditingId(null); setForm({ name: '', description: '' }); }}>
          {showForm ? 'Cerrar' : '+ Nueva'}
        </button>
      </div>

      {showForm && (
        <div className="card mb-4">
          <div className="card-body">
            <form onSubmit={handleSubmit}>
              <div className="mb-3">
                <label className="form-label">Nombre</label>
                <input type="text" className="form-control" required value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} />
              </div>
              <div className="mb-3">
                <label className="form-label">Descripción</label>
                <textarea className="form-control" rows="3" value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })}></textarea>
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
                <th>Descripción</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              {especialidades.map((spec) => (
                <tr key={spec.id}>
                  <td>{spec.name}</td>
                  <td>{spec.description}</td>
                  <td>
                    <button className="btn btn-sm btn-warning me-2" onClick={() => handleEdit(spec)}>Editar</button>
                    <button className="btn btn-sm btn-danger" onClick={() => handleDelete(spec.id)}>Eliminar</button>
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
