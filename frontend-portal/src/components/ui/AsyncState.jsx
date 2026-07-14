import { AlertCircle, Inbox, LoaderCircle } from 'lucide-react';
import { Button } from './button.jsx';
import { Card, CardContent } from './card.jsx';

export function LoadingState({ message = 'Cargando información…' }) {
  return (
    <Card role="status" aria-live="polite" className="min-h-48 items-center justify-center">
      <CardContent className="flex flex-col items-center gap-3 text-center text-muted-foreground">
        <LoaderCircle className="size-7 animate-spin text-primary" aria-hidden="true" />
        <p className="m-0 text-sm font-medium">{message}</p>
      </CardContent>
    </Card>
  );
}

export function ErrorState({ message, onRetry }) {
  return (
    <Card role="alert" className="border-destructive/25 bg-destructive/5">
      <CardContent className="flex min-h-48 flex-col items-center justify-center gap-3 text-center">
        <span className="flex size-11 items-center justify-center rounded-full bg-destructive/10 text-destructive">
          <AlertCircle className="size-5" aria-hidden="true" />
        </span>
        <h2 className="m-0 text-lg font-semibold text-foreground">No pudimos cargar esta información</h2>
        <p className="m-0 max-w-xl text-sm text-muted-foreground">{message || 'Intenta nuevamente en unos momentos.'}</p>
        {onRetry && <Button type="button" variant="outline" onClick={onRetry}>Reintentar</Button>}
      </CardContent>
    </Card>
  );
}

export function EmptyState({ title, description, action }) {
  return (
    <div className="flex min-h-64 flex-col items-center justify-center rounded-2xl border border-dashed border-border bg-muted/35 px-6 py-12 text-center">
      <span className="mb-4 flex size-12 items-center justify-center rounded-full bg-background text-primary shadow-sm ring-1 ring-border">
        <Inbox className="size-5" aria-hidden="true" />
      </span>
      <h2 className="m-0 text-xl font-semibold text-foreground">{title}</h2>
      <p className="mb-0 mt-2 max-w-lg text-sm leading-6 text-muted-foreground">{description}</p>
      {action && <div className="mt-5">{action}</div>}
    </div>
  );
}
