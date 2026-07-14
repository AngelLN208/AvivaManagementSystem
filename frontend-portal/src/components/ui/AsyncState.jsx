export function LoadingState({ message = 'Cargando información…' }) {
  return (
    <div className="state-card" role="status" aria-live="polite">
      <span className="spinner" aria-hidden="true" />
      <p>{message}</p>
    </div>
  );
}

export function ErrorState({ message, onRetry }) {
  return (
    <div className="state-card state-card--error" role="alert">
      <span className="state-card__symbol" aria-hidden="true">!</span>
      <h2>No pudimos cargar esta información</h2>
      <p>{message || 'Intenta nuevamente en unos momentos.'}</p>
      {onRetry && (
        <button type="button" className="button button--secondary" onClick={onRetry}>
          Reintentar
        </button>
      )}
    </div>
  );
}

export function EmptyState({ title, description, action }) {
  return (
    <div className="state-card state-card--empty">
      <span className="state-card__symbol" aria-hidden="true">+</span>
      <h2>{title}</h2>
      <p>{description}</p>
      {action}
    </div>
  );
}
