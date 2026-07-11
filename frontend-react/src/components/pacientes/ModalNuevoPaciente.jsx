import { useState } from 'react';

const ESTADO_INICIAL = {
  nombre: '', apellido: '', dni: '', fechaNac: '', genero: '',
  codigoPais: '+51', telefono: '', email: '', direccion: '',
};

export default function ModalNuevoPaciente({ onCrear, isCreating }) {
  const [form, setForm] = useState(ESTADO_INICIAL);

  function set(campo, valor) {
    setForm((f) => ({ ...f, [campo]: valor }));
  }

  function handleSubmit(e) {
    e.preventDefault();
    const { nombre, apellido, dni, fechaNac, genero, codigoPais, telefono, email, direccion } = form;

    if (!nombre || !apellido || !dni || !fechaNac || !genero) {
      alert('❌ Nombre, apellido, DNI, fecha de nacimiento y género son obligatorios.');
      return;
    }

    const phone = (codigoPais && telefono) ? `${codigoPais}${telefono}` : '';

    onCrear(
      {
        firstName: nombre, lastName: apellido, dni,
        dateOfBirth: fechaNac, gender: genero,
        phone, email, address: direccion,
      },
      {
        onSuccess: () => {
          setForm(ESTADO_INICIAL);
          window.bootstrap?.Modal.getInstance(document.getElementById('modalPaciente'))?.hide();
        },
        onError: (err) => alert(`❌ ${err.message}`),
      }
    );
  }

  return (
    <div className="modal fade" id="modalPaciente" tabIndex="-1" aria-hidden="true">
      <div className="modal-dialog modal-dialog-centered modal-lg">
        <div className="modal-content border-0 rounded-4 shadow">
          <div className="modal-header border-0 pt-4 px-4">
            <h5 className="modal-title fw-bold"><i className="fa-solid fa-user-plus me-2 text-primary"></i> Nuevo Paciente</h5>
            <button type="button" className="btn-close" data-bs-dismiss="modal"></button>
          </div>
          <div className="modal-body px-4 pb-4">
            <form onSubmit={handleSubmit} noValidate>

              <div className="row">
                <div className="col-md-6 mb-3">
                  <label className="form-label small fw-bold">Nombre <span className="text-danger">*</span></label>
                  <input type="text" className="form-control rounded-3" placeholder="Ej: Juan"
                    value={form.nombre} onChange={(e) => set('nombre', e.target.value)} required />
                </div>
                <div className="col-md-6 mb-3">
                  <label className="form-label small fw-bold">Apellido <span className="text-danger">*</span></label>
                  <input type="text" className="form-control rounded-3" placeholder="Ej: Pérez"
                    value={form.apellido} onChange={(e) => set('apellido', e.target.value)} required />
                </div>
              </div>

              <div className="row">
                <div className="col-md-6 mb-3">
                  <label className="form-label small fw-bold">DNI <span className="text-danger">*</span></label>
                  <input type="text" className="form-control rounded-3" placeholder="Ej: 12345678" maxLength={12}
                    value={form.dni} onChange={(e) => set('dni', e.target.value)} required />
                </div>
                <div className="col-md-6 mb-3">
                  <label className="form-label small fw-bold">Fecha de Nacimiento <span className="text-danger">*</span></label>
                  <input type="date" className="form-control rounded-3"
                    value={form.fechaNac} onChange={(e) => set('fechaNac', e.target.value)} required />
                </div>
              </div>

              <div className="row">
                <div className="col-md-6 mb-3">
                  <label className="form-label small fw-bold">Género <span className="text-danger">*</span></label>
                  <select className="form-select rounded-3" value={form.genero}
                    onChange={(e) => set('genero', e.target.value)} required>
                    <option value="" disabled>Seleccionar...</option>
                    <option value="MALE">Masculino</option>
                    <option value="FEMALE">Femenino</option>
                    <option value="OTHER">Otro</option>
                  </select>
                </div>
                <div className="col-md-6 mb-3">
                  <label className="form-label small fw-bold">Teléfono</label>
                  <div className="input-group">
                    <input type="text" className="form-control rounded-start-3" style={{ maxWidth: 70 }}
                      value={form.codigoPais} onChange={(e) => set('codigoPais', e.target.value)} />
                    <input type="tel" className="form-control rounded-end-3" placeholder="999888777"
                      value={form.telefono} onChange={(e) => set('telefono', e.target.value)} />
                  </div>
                </div>
              </div>

              <div className="mb-3">
                <label className="form-label small fw-bold">Correo electrónico</label>
                <input type="email" className="form-control rounded-3" placeholder="correo@ejemplo.com"
                  value={form.email} onChange={(e) => set('email', e.target.value)} />
              </div>

              <div className="mb-3">
                <label className="form-label small fw-bold">Dirección</label>
                <input type="text" className="form-control rounded-3" placeholder="Ej: Av. Principal 123"
                  value={form.direccion} onChange={(e) => set('direccion', e.target.value)} />
              </div>

              <div className="mb-3">
                <label className="form-label small fw-bold text-muted">
                  <i className="fa-solid fa-shield-halved me-1"></i> Seguro Médico
                  <span className="badge fondo-advertencia-sutil texto-advertencia rounded-pill ms-2 px-2" style={{ fontSize: '0.7rem' }}>Próximamente</span>
                </label>
                <select className="form-select rounded-3" disabled>
                  <option>No disponible aún</option>
                </select>
                <small className="text-muted">La vinculación de seguros estará disponible próximamente.</small>
              </div>

              <div className="d-grid mt-4">
                <button type="submit" className="btn btn-primary rounded-pill py-2 text-white fw-bold" disabled={isCreating}>
                  <i className="fa-solid fa-floppy-disk me-2"></i> {isCreating ? 'Guardando...' : 'Guardar Paciente'}
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  );
}