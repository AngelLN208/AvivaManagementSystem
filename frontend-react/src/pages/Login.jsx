import { useState } from 'react';
import { useAuth } from '../hooks/useAuth';
import './Login.css';

export default function Login() {
  const { login } = useAuth();
  const [usuario, setUsuario] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [cargando, setCargando] = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();
    setError('');

    if (!usuario.trim() || !password.trim()) {
      setError('Ingresa usuario y contraseña');
      return;
    }

    setCargando(true);
    try {
      await login(usuario.trim(), password.trim());
    } catch (err) {
      setError(err.message || 'Usuario o contraseña incorrectos');
    } finally {
      setCargando(false);
    }
  }

  return (
    <div className="login-page container-fluid vh-100">
      <div className="row h-100">

        <div className="col-lg-7 d-none d-lg-flex left-panel">
          <div className="overlay-content">
            <div className="logo-box">
              <h1>AVIVA+</h1>
              <p>Plataforma Empresarial</p>
            </div>
            <div className="image-area">
              <img
                src="https://cloudfront-us-east-1.images.arcpublishing.com/elcomercio/3FV7XNCCHBGM3JIEEPO333JI7U.jpeg"
                alt="Trabajador"
              />
            </div>
          </div>
        </div>

        <div className="col-lg-5 col-12 right-panel">
          <div className="login-container">
            <form className="login-form" onSubmit={handleSubmit}>
              <h2>Iniciar sesión</h2>

              <div className="mb-3">
                <label className="form-label">Usuario</label>
                <input
                  type="text"
                  className="form-control custom-input"
                  placeholder="Ingresa tu usuario"
                  value={usuario}
                  onChange={(e) => setUsuario(e.target.value)}
                />
              </div>

              <div className="mb-4">
                <label className="form-label">Contraseña</label>
                <input
                  type="password"
                  className="form-control custom-input"
                  placeholder="Ingresa tu contraseña"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                />
              </div>

              <button type="submit" className="btn login-btn w-100" disabled={cargando}>
                {cargando ? 'Ingresando...' : 'Ingresar'}
              </button>

              {error && <p className="text-danger mt-3 text-center">{error}</p>}
            </form>
          </div>
        </div>

      </div>
    </div>
  );
}
