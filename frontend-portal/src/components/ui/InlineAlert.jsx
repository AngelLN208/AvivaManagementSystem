export default function InlineAlert({ tone = 'info', children, onDismiss }) {
  return (
    <div
      className={`inline-alert inline-alert--${tone}`}
      role={tone === 'danger' ? 'alert' : 'status'}
      aria-live="polite"
    >
      <span>{children}</span>
      {onDismiss && (
        <button type="button" aria-label="Cerrar mensaje" onClick={onDismiss}>×</button>
      )}
    </div>
  );
}
