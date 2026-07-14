import { useMemo, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth.js';
import InlineAlert from '../components/ui/InlineAlert.jsx';
import { getLocalDateInputValue } from '../utils/dates.js';

const INITIAL_FORM = {
  firstName: '',
  lastName: '',
  dni: '',
  dateOfBirth: '',
  gender: '',
  phone: '',
  email: '',
  address: '',
  username: '',
  password: '',
  confirmPassword: '',
};

export default function RegisterPage() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState(INITIAL_FORM);
  const [error, setError] = useState('');
  const [fieldErrors, setFieldErrors] = useState({});
  const [isSubmitting, setIsSubmitting] = useState(false);
  const maxBirthDate = useMemo(() => getLocalDateInputValue(), []);

  const errorAttributes = (name) => ({
    'aria-invalid': fieldErrors[name] ? 'true' : undefined,
    'aria-describedby': fieldErrors[name] ? `${name}-error` : undefined,
  });

  const focusField = (name) => {
    window.requestAnimationFrame(() => document.getElementById(name)?.focus());
  };

  function updateField(event) {
    const { name, value } = event.target;
    setForm((current) => ({ ...current, [name]: value }));
    setFieldErrors((current) => ({ ...current, [name]: '' }));
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setError('');
    setFieldErrors({});

    if (form.password !== form.confirmPassword) {
      setFieldErrors({ confirmPassword: 'Las contraseñas no coinciden.' });
      focusField('confirmPassword');
      return;
    }
    if (form.dateOfBirth > maxBirthDate) {
      setFieldErrors({ dateOfBirth: 'La fecha de nacimiento no puede ser futura.' });
      focusField('dateOfBirth');
      return;
    }

    const { confirmPassword: _confirmPassword, ...payload } = form;
    void _confirmPassword;
    setIsSubmitting(true);
    try {
      await register({
        ...payload,
        firstName: payload.firstName.trim(),
        lastName: payload.lastName.trim(),
        username: payload.username.trim(),
        email: payload.email.trim(),
        address: payload.address.trim() || null,
      });
      navigate('/', { replace: true });
    } catch (requestError) {
      if (requestError.accountCreated) {
        navigate('/login?registro=exitoso', { replace: true });
        return;
      }
      setError(requestError.message);
      const nextFieldErrors = requestError.fieldErrors || {};
      setFieldErrors(nextFieldErrors);
      const firstInvalidField = Object.keys(nextFieldErrors)[0];
      if (firstInvalidField) focusField(firstInvalidField);
    } finally {
      setIsSubmitting(false);
    }
  }

  const fieldError = (name) => fieldErrors[name]
    ? <span className="field-error" id={`${name}-error`} role="alert">{fieldErrors[name]}</span>
    : null;

  return (
    <main className="register-page">
      <header className="register-header">
        <Link to="/login" className="brand" aria-label="Clínica Aviva, volver al acceso">
          <span className="brand__mark" aria-hidden="true"><span>+</span></span>
          <span className="brand__copy"><strong>aviva</strong><small>Portal paciente</small></span>
        </Link>
        <p>¿Ya tienes cuenta? <Link to="/login">Inicia sesión</Link></p>
      </header>

      <section className="register-card">
        <div className="register-card__heading">
          <p className="eyebrow">Crea tu acceso personal</p>
          <h1>Regístrate como paciente</h1>
          <p>Usaremos estos datos únicamente para crear tu cuenta y gestionar tus citas.</p>
        </div>

        {error && <InlineAlert tone="danger">{error}</InlineAlert>}

        <form className="register-form" onSubmit={handleSubmit}>
          <fieldset>
            <legend>Datos personales</legend>
            <div className="form-grid">
              <div className="form-field">
                <label htmlFor="firstName">Nombres</label>
                <input id="firstName" name="firstName" value={form.firstName} onChange={updateField} minLength="2" maxLength="50" autoComplete="given-name" required {...errorAttributes('firstName')} />
                {fieldError('firstName')}
              </div>
              <div className="form-field">
                <label htmlFor="lastName">Apellidos</label>
                <input id="lastName" name="lastName" value={form.lastName} onChange={updateField} minLength="2" maxLength="50" autoComplete="family-name" required {...errorAttributes('lastName')} />
                {fieldError('lastName')}
              </div>
              <div className="form-field">
                <label htmlFor="dni">DNI o documento</label>
                <input id="dni" name="dni" value={form.dni} onChange={updateField} inputMode="numeric" pattern="[0-9]{8,12}" minLength="8" maxLength="12" placeholder="8 a 12 dígitos" required {...errorAttributes('dni')} />
                {fieldError('dni')}
              </div>
              <div className="form-field">
                <label htmlFor="dateOfBirth">Fecha de nacimiento</label>
                <input id="dateOfBirth" name="dateOfBirth" type="date" max={maxBirthDate} value={form.dateOfBirth} onChange={updateField} autoComplete="bday" required {...errorAttributes('dateOfBirth')} />
                {fieldError('dateOfBirth')}
              </div>
              <div className="form-field">
                <label htmlFor="gender">Género</label>
                <select id="gender" name="gender" value={form.gender} onChange={updateField} required {...errorAttributes('gender')}>
                  <option value="">Selecciona una opción</option>
                  <option value="FEMALE">Femenino</option>
                  <option value="MALE">Masculino</option>
                  <option value="OTHER">Otro / Prefiero otra denominación</option>
                </select>
                {fieldError('gender')}
              </div>
              <div className="form-field">
                <label htmlFor="phone">Teléfono</label>
                <input id="phone" name="phone" type="tel" value={form.phone} onChange={updateField} pattern="[0-9+\-\s]{7,15}" minLength="7" maxLength="15" autoComplete="tel" placeholder="Ej. 999 999 999" required {...errorAttributes('phone')} />
                {fieldError('phone')}
              </div>
              <div className="form-field form-field--wide">
                <label htmlFor="email">Correo electrónico</label>
                <input id="email" name="email" type="email" value={form.email} onChange={updateField} autoComplete="email" placeholder="nombre@correo.com" required {...errorAttributes('email')} />
                {fieldError('email')}
              </div>
              <div className="form-field form-field--wide">
                <label htmlFor="address">Dirección <span>(opcional)</span></label>
                <input id="address" name="address" value={form.address} onChange={updateField} autoComplete="street-address" {...errorAttributes('address')} />
                {fieldError('address')}
              </div>
            </div>
          </fieldset>

          <fieldset>
            <legend>Datos de acceso</legend>
            <div className="form-grid">
              <div className="form-field form-field--wide">
                <label htmlFor="username">Nombre de usuario</label>
                <input id="username" name="username" value={form.username} onChange={updateField} minLength="3" maxLength="50" autoComplete="username" placeholder="Elige un usuario fácil de recordar" required {...errorAttributes('username')} />
                {fieldError('username')}
              </div>
              <div className="form-field">
                <label htmlFor="password">Contraseña</label>
                <input id="password" name="password" type="password" value={form.password} onChange={updateField} minLength="4" maxLength="100" autoComplete="new-password" required {...errorAttributes('password')} />
                <small className="helper-text">Mínimo 4 caracteres.</small>
                {fieldError('password')}
              </div>
              <div className="form-field">
                <label htmlFor="confirmPassword">Repite la contraseña</label>
                <input id="confirmPassword" name="confirmPassword" type="password" value={form.confirmPassword} onChange={updateField} minLength="4" maxLength="100" autoComplete="new-password" required {...errorAttributes('confirmPassword')} />
                {fieldError('confirmPassword')}
              </div>
            </div>
          </fieldset>

          <button type="submit" className="button button--primary button--wide" disabled={isSubmitting}>
            {isSubmitting ? 'Creando tu cuenta…' : 'Crear cuenta e ingresar'}
          </button>
        </form>
      </section>
    </main>
  );
}
