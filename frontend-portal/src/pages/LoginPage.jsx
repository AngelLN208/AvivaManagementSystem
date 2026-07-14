import { useState } from 'react';
import { Link, useLocation, useNavigate, useSearchParams } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth.js';
import InlineAlert from '../components/ui/InlineAlert.jsx';

export default function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [searchParams] = useSearchParams();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  async function handleSubmit(event) {
    event.preventDefault();
    setError('');

    if (!username.trim() || !password) {
      setError('Ingresa tu usuario y contraseña.');
      return;
    }

    setIsSubmitting(true);
    try {
      await login(username.trim(), password);
      navigate(location.state?.from || '/', { replace: true });
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <main className="auth-page">
      <section className="auth-story" aria-label="Portal de pacientes de Clínica Aviva">
        <div className="auth-story__content">
          <Link to="/login" className="brand brand--light" aria-label="Clínica Aviva">
            <span className="brand__mark" aria-hidden="true"><span>+</span></span>
            <span className="brand__copy"><strong>aviva</strong><small>Portal paciente</small></span>
          </Link>
          <div>
            <p className="eyebrow eyebrow--light">Tu tiempo también importa</p>
            <h1>Gestiona tus citas con tranquilidad.</h1>
            <p>Consulta horarios, agenda con tu especialista y administra tus próximas citas desde un solo lugar.</p>
          </div>
          <div className="auth-story__trust">
            <span aria-hidden="true">✓</span>
            Acceso personal y seguro
          </div>
        </div>
        <div className="auth-story__orb auth-story__orb--one" />
        <div className="auth-story__orb auth-story__orb--two" />
      </section>

      <section className="auth-panel">
        <div className="auth-card">
          <div className="auth-card__mobile-brand">
            <span className="brand__mark" aria-hidden="true"><span>+</span></span>
            <strong>aviva</strong>
          </div>
          <p className="eyebrow">Bienvenido de nuevo</p>
          <h2>Inicia sesión</h2>
          <p className="auth-card__intro">Ingresa con la cuenta asociada a tu perfil de paciente.</p>

          {searchParams.get('sesion') === 'expirada' && (
            <InlineAlert>Tu sesión venció. Inicia sesión nuevamente para continuar.</InlineAlert>
          )}
          {searchParams.get('registro') === 'exitoso' && (
            <InlineAlert tone="success">Tu cuenta fue creada. Inicia sesión para continuar.</InlineAlert>
          )}
          {error && <InlineAlert tone="danger">{error}</InlineAlert>}

          <form className="auth-form" onSubmit={handleSubmit}>
            <div className="form-field">
              <label htmlFor="login-username">Usuario</label>
              <input
                id="login-username"
                name="username"
                type="text"
                autoComplete="username"
                minLength="3"
                maxLength="50"
                value={username}
                onChange={(event) => setUsername(event.target.value)}
                placeholder="Tu nombre de usuario"
                required
                autoFocus
              />
            </div>

            <div className="form-field">
              <label htmlFor="login-password">Contraseña</label>
              <div className="password-field">
                <input
                  id="login-password"
                  name="password"
                  type={showPassword ? 'text' : 'password'}
                  autoComplete="current-password"
                  minLength="4"
                  maxLength="100"
                  value={password}
                  onChange={(event) => setPassword(event.target.value)}
                  placeholder="Tu contraseña"
                  required
                />
                <button
                  type="button"
                  className="password-toggle"
                  aria-label={showPassword ? 'Ocultar contraseña' : 'Mostrar contraseña'}
                  onClick={() => setShowPassword((current) => !current)}
                >
                  {showPassword ? 'Ocultar' : 'Ver'}
                </button>
              </div>
            </div>

            <button type="submit" className="button button--primary button--wide" disabled={isSubmitting}>
              {isSubmitting ? 'Ingresando…' : 'Ingresar al portal'}
            </button>
          </form>

          <p className="auth-switch">¿Aún no tienes una cuenta? <Link to="/registro">Regístrate como paciente</Link></p>
        </div>
      </section>
    </main>
  );
}
