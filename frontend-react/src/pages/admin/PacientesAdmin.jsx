import { useState, useEffect } from 'react';
import { getPacientes, actualizarPaciente, eliminarPaciente, crearPaciente } from '../../api/pacientesApi';
import { getSeguros, crearSeguro } from '../../api/segurosApi';

const SEGURO_FORM_INICIAL = {
  name: '',
  description: '',
  coveragePercentage: '',
  deductible: '',
  maxCoveragePerConsultation: '',
  maxAnnualCoverage: '',
};

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

  // --- Seguros (creación desde el apartado Pacientes) ---
  const [showSeguroForm, setShowSeguroForm] = useState(false);
  const [seguroForm, setSeguroForm] = useState(SEGURO_FORM_INICIAL);
  const [seguros, setSeguros] = useState([]);
  const [seguroError, setSeguroError] = useState(null);
  const [seguroSuccess, setSeguroSuccess] = useState(null);

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

  // --- Seguros ---

  async function loadSeguros() {
    try {
      const data = await getSeguros();
      setSeguros(Array.isArray(data) ? data : []);
    } catch (err) {
      setSeguroError(err.message);
    }
  }

  async function handleToggleSeguroForm() {
    const abrir = !showSeguroForm;
    setShowSeguroForm(abrir);
    setSeguroForm(SEGURO_FORM_INICIAL);
    setSeguroError(null);
    setSeguroSuccess(null);
    if (abrir) {
      await loadSeguros();
    }
  }

  async function handleSubmitSeguro(e) {
    e.preventDefault();
    try {
      setSeguroError(null);
      const payload = {
        name: seguroForm.name,
        description: seguroForm.description,
        coveragePercentage: parseFloat(seguroForm.coveragePercentage),
        deductible: parseFloat(seguroForm.deductible),
        maxCoveragePerConsultation: parseFloat(seguroForm.maxCoveragePerConsultation),
        maxAnnualCoverage: parseFloat(seguroForm.maxAnnualCoverage),
      };
      await crearSeguro(payload);
      setSeguroForm(SEGURO_FORM_INICIAL);
      setSeguroSuccess('Seguro creado exitosamente. Ya está disponible para el recepcionista.');
      await loadSeguros();
    } catch (err) {
      setSeguroError(err.message);
    }
  }

  if (error) return <div className="alert alert-danger m-4">{error}</div>;

  return (
    <div className="container-fluid p-4">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h3 className="fw-semibold">Pacientes</h3>
        <div className="d-flex gap-2">
          <button className="btn btn-outline-primary" onClick={handleToggleSeguroForm}>
            <i className="fa-solid fa-shield-heart me-2"></i>
            {showSeguroForm ? 'Cerrar' : 'Crear Seguro'}
          </button>
          <button className="btn btn-primary" onClick={() => { setShowForm(!showForm); setEditingId(null); setForm({ dni: '', firstName: '', lastName: '', gender: 'MALE', dateOfBirth: '', phone: '', email: '', address: '' }); }}>
            {showForm ? 'Cerrar' : '+ Nuevo'}
          </button>
        </div>
      </div>

      {showSeguroForm && (
        <div className="card mb-4 border-primary">
          <div className="card-body">
            <h5 className="fw-semibold mb-3"><i className="fa-solid fa-shield-heart me-2"></i>Nuevo Seguro</h5>

            {seguroError && <div className="alert alert-danger">{seguroError}</div>}
            {seguroSuccess && <div className="alert alert-success">{seguroSuccess}</div>}

            <form onSubmit={handleSubmitSeguro}>
              <div className="row">
                <div className="col-md-6 mb-3">
                  <label className="form-label">Nombre</label>
                  <input
                    type="text"
                    className="form-control"
                    required
                    minLength={3}
                    maxLength={100}
                    value={seguroForm.name}
                    onChange={(e) => setSeguroForm({ ...seguroForm, name: e.target.value })}
                  />
                </div>
                <div className="col-md-6 mb-3">
                  <label className="form-label">% de Cobertura</label>
                  <input
                    type="number"
                    step="0.01"
                    min="0"
                    className="form-control"
                    required
                    value={seguroForm.coveragePercentage}
                    onChange={(e) => setSeguroForm({ ...seguroForm, coveragePercentage: e.target.value })}
                  />
                </div>
                <div className="col-md-6 mb-3">
                  <label className="form-label">Deducible (S/)</label>
                  <input
                    type="number"
                    step="0.01"
                    min="0"
                    className="form-control"
                    required
                    value={seguroForm.deductible}
                    onChange={(e) => setSeguroForm({ ...seguroForm, deductible: e.target.value })}
                  />
                </div>
                <div className="col-md-6 mb-3">
                  <label className="form-label">Máximo por Consulta (S/)</label>
                  <input
                    type="number"
                    step="0.01"
                    min="0.01"
                    className="form-control"
                    required
                    value={seguroForm.maxCoveragePerConsultation}
                    onChange={(e) => setSeguroForm({ ...seguroForm, maxCoveragePerConsultation: e.target.value })}
                  />
                </div>
                <div className="col-md-6 mb-3">
                  <label className="form-label">Máximo Anual (S/)</label>
                  <input
                    type="number"
                    step="0.01"
                    min="0.01"
                    className="form-control"
                    required
                    value={seguroForm.maxAnnualCoverage}
                    onChange={(e) => setSeguroForm({ ...seguroForm, maxAnnualCoverage: e.target.value })}
                  />
                </div>
                <div className="col-md-12 mb-3">
                  <label className="form-label">Descripción</label>
                  <textarea
                    className="form-control"
                    maxLength={500}
                    value={seguroForm.description}
                    onChange={(e) => setSeguroForm({ ...seguroForm, description: e.target.value })}
                  />
                </div>
              </div>
              <button type="submit" className="btn btn-success">Guardar Seguro</button>
            </form>

            {seguros.length > 0 && (
              <div className="table-responsive mt-4">
                <h6 className="fw-semibold">Seguros ya registrados</h6>
                <table className="table table-sm table-striped">
                  <thead>
                    <tr>
                      <th>Nombre</th>
                      <th>% Cobertura</th>
                      <th>Deducible</th>
                      <th>Estado</th>
                    </tr>
                  </thead>
                  <tbody>
                    {seguros.map((seg) => (
                      <tr key={seg.id}>
                        <td>{seg.name}</td>
                        <td>{seg.coveragePercentage}%</td>
                        <td>S/ {seg.deductible}</td>
                        <td>
                          <span className={`badge ${seg.active ? 'bg-success' : 'bg-secondary'}`}>
                            {seg.active ? 'Activo' : 'Inactivo'}
                          </span>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </div>
      )}

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