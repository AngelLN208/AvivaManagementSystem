import { Link } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth.js';

export default function NotFoundPage() {
  const { isAuthenticated } = useAuth();

  return (
    <main className="not-found-page">
      <div className="not-found-page__mark">404</div>
      <p className="eyebrow">Página no encontrada</p>
      <h1>Esta dirección no existe</h1>
      <p>Revisa el enlace o vuelve a una sección disponible del portal.</p>
      <Link to={isAuthenticated ? '/' : '/login'} className="button button--primary">
        {isAuthenticated ? 'Volver al inicio' : 'Ir a iniciar sesión'}
      </Link>
    </main>
  );
}
