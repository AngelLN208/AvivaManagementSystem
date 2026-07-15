import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { getInsurances } from '../../api/insurancesApi';

const ESTADO_INICIAL = {
  nombre: '', apellido: '', dni: '', fechaNac: '', genero: '',
  codigoPais: '+51', telefono: '', email: '', direccion: '',
};

const SEGURO_INICIAL = {
  tieneSeguro: false,
  insuranceId: '',
  policyNumber: '',
  policyHolderName: '',
  relationshipToHolder: '',
  isPrimary: true,
  effectiveDate: '',
  expirationDate: '',
};

export default function ModalNuevoPaciente({ onCrear, isCreating }) {
  const [form, setForm] = useState(ESTADO_INICIAL);
  const [seguro, setSeguro] = useState(SEGURO_INICIAL);

  const { data: seguros = [] } = useQuery({ queryKey: ['insurances'], queryFn: getInsurances });

  function set(campo, valor) {
    setForm((f) => ({ ...f, [campo]: valor }));
  }

  function setSeg(campo, valor) {
    setSeguro((s) => ({ ...s, [campo]: valor }));
  }

  function handleSubmit(e) {
    e.preventDefault();
    const { nombre, apellido, dni, fechaNac, genero, codigoPais, telefono, email, direccion } = form;

    if (!nombre || !apellido || !dni || !fechaNac || !genero || !telefono || !email) {
      alert('❌ Completa nombre, apellido, DNI, fecha de nacimiento, género, teléfono y correo.');
      return;
    }

    if (!/^\d{8}$/.test(dni)) {
      alert('❌ El DNI debe contener exactamente 8 dígitos.');
      return;
    }

    if (seguro.tieneSeguro) {
      if (!seguro.insuranceId || !seguro.policyNumber || !seguro.effectiveDate || !seguro.expirationDate) {
        alert('❌ Completa los datos del seguro: aseguradora, número de póliza y fechas de vigencia.');
        return;
      }
      if (seguro.expirationDate < seguro.effectiveDate) {
        alert('❌ La fecha de vencimiento no puede ser anterior a la fecha de inicio.');
        return;
      }
    }

    const phone = (codigoPais && telefono) ? `${codigoPais}${telefono}` : '';

    const datosPaciente = {
      firstName: nombre, lastName: apellido, dni,
      dateOfBirth: fechaNac, gender: genero,
      phone, email, address: direccion,
    };

    const datosSeguro = seguro.tieneSeguro ? {
      insuranceId: parseInt(seguro.insuranceId),
      policyNumber: seguro.policyNumber,
      policyHolderName: seguro.policyHolderName || nombre + ' ' + apellido,
      relationshipToHolder: seguro.relationshipToHolder || 'Titular',
      isPrimary: seguro.isPrimary,
      effectiveDate: `${seguro.effectiveDate}T00:00:00`,
      expirationDate: `${seguro.expirationDate}T00:00:00`,
    } : null;

    onCrear(
      { datosPaciente, datosSeguro },
      {
        onSuccess: () => {
          setForm(ESTADO_INICIAL);
          setSeguro(SEGURO_INICIAL);
          window.bootstrap?.Modal.getInstance(document.getElementById('modalPaciente'))?.hide();
        },
        onError: (err) => {
          if (err.patientCreated) {
            setForm(ESTADO_INICIAL);
            setSeguro(SEGURO_INICIAL);
            window.bootstrap?.Modal.getInstance(document.getElementById('modalPaciente'))?.hide();
          }
          alert(`❌ ${err.message}`);
        },
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
                  <input type="text" className="form-control rounded-3" placeholder="Ej: 12345678" maxLength={8}
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
                  <label className="form-label small fw-bold">Teléfono <span className="text-danger">*</span></label>
                  <div className="input-group">
                    <input type="text" className="form-control rounded-start-3" style={{ maxWidth: 70 }}
                      value={form.codigoPais} onChange={(e) => set('codigoPais', e.target.value)} />
                    <input type="tel" className="form-control rounded-end-3" placeholder="999888777"
                      value={form.telefono} onChange={(e) => set('telefono', e.target.value)} required />
                  </div>
                </div>
              </div>

              <div className="mb-3">
                <label className="form-label small fw-bold">Correo electrónico <span className="text-danger">*</span></label>
                <input type="email" className="form-control rounded-3" placeholder="correo@ejemplo.com"
                  value={form.email} onChange={(e) => set('email', e.target.value)} required />
              </div>

              <div className="mb-3">
                <label className="form-label small fw-bold">Dirección</label>
                <input type="text" className="form-control rounded-3" placeholder="Ej: Av. Principal 123"
                  value={form.direccion} onChange={(e) => set('direccion', e.target.value)} />
              </div>

              {/* SEGURO MÉDICO */}
              <div className="mb-3 p-3 rounded-3 border">
                <div className="form-check form-switch mb-2">
                  <input
                    className="form-check-input"
                    type="checkbox"
                    id="tieneSeguro"
                    checked={seguro.tieneSeguro}
                    onChange={(e) => setSeg('tieneSeguro', e.target.checked)}
                  />
                  <label className="form-check-label fw-bold" htmlFor="tieneSeguro">
                    <i className="fa-solid fa-shield-halved me-1"></i> Vincular Seguro Médico
                  </label>
                </div>

                {seguro.tieneSeguro && (
                  <div className="mt-3">
                    <div className="mb-3">
                      <label className="form-label small fw-bold">Aseguradora <span className="text-danger">*</span></label>
                      <select className="form-select rounded-3" value={seguro.insuranceId}
                        onChange={(e) => setSeg('insuranceId', e.target.value)} required>
                        <option value="" disabled>Seleccionar aseguradora...</option>
                        {seguros.map((s) => <option key={s.id} value={s.id}>{s.name}</option>)}
                      </select>
                    </div>

                    <div className="row">
                      <div className="col-md-6 mb-3">
                        <label className="form-label small fw-bold">Número de Póliza <span className="text-danger">*</span></label>
                        <input type="text" className="form-control rounded-3" placeholder="Ej: POL-123456"
                          value={seguro.policyNumber} onChange={(e) => setSeg('policyNumber', e.target.value)} required />
                      </div>
                      <div className="col-md-6 mb-3">
                        <label className="form-label small fw-bold">Nombre del Titular</label>
                        <input type="text" className="form-control rounded-3" placeholder="Si es distinto al paciente"
                          value={seguro.policyHolderName} onChange={(e) => setSeg('policyHolderName', e.target.value)} />
                      </div>
                    </div>

                    <div className="row">
                      <div className="col-md-6 mb-3">
                        <label className="form-label small fw-bold">Relación con el Titular</label>
                        <input type="text" className="form-control rounded-3" placeholder="Ej: Titular, Hijo/a, Cónyuge"
                          value={seguro.relationshipToHolder} onChange={(e) => setSeg('relationshipToHolder', e.target.value)} />
                      </div>
                      <div className="col-md-6 mb-3 d-flex align-items-end">
                        <div className="form-check">
                          <input
                            className="form-check-input"
                            type="checkbox"
                            id="esPrimario"
                            checked={seguro.isPrimary}
                            onChange={(e) => setSeg('isPrimary', e.target.checked)}
                          />
                          <label className="form-check-label" htmlFor="esPrimario">
                            Seguro primario
                          </label>
                        </div>
                      </div>
                    </div>

                    <div className="row">
                      <div className="col-md-6 mb-3">
                        <label className="form-label small fw-bold">Fecha de Inicio <span className="text-danger">*</span></label>
                        <input type="date" className="form-control rounded-3"
                          value={seguro.effectiveDate} onChange={(e) => setSeg('effectiveDate', e.target.value)} required />
                      </div>
                      <div className="col-md-6 mb-3">
                        <label className="form-label small fw-bold">Fecha de Vencimiento <span className="text-danger">*</span></label>
                        <input type="date" className="form-control rounded-3"
                          value={seguro.expirationDate} onChange={(e) => setSeg('expirationDate', e.target.value)} required />
                      </div>
                    </div>
                  </div>
                )}
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
