import { useState } from 'react';
import {
  AlertCircle,
  Check,
  CreditCard,
  LoaderCircle,
  WalletCards,
} from 'lucide-react';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import { Button } from '@/components/ui/button';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { formatCurrency, PAYMENT_METHODS } from '../../utils/payments.js';

export default function ProcessPaymentDialog({ payment, isSubmitting, error, onConfirm, onClose }) {
  const [method, setMethod] = useState('DEBIT_CARD');

  if (!payment) return null;

  function handleSubmit(event) {
    event.preventDefault();
    onConfirm(method);
  }

  function handleOpenChange(open) {
    if (!open && !isSubmitting) onClose();
  }

  return (
    <Dialog open onOpenChange={handleOpenChange}>
      <DialogContent
        className="max-w-lg overflow-hidden p-0"
        showCloseButton={!isSubmitting}
        onEscapeKeyDown={(event) => {
          if (isSubmitting) event.preventDefault();
        }}
        onInteractOutside={(event) => {
          if (isSubmitting) event.preventDefault();
        }}
      >
        <form onSubmit={handleSubmit} aria-busy={isSubmitting}>
          <DialogHeader className="border-b border-border bg-muted/30 px-6 py-5 text-left">
            <div className="mb-2 flex size-11 items-center justify-center rounded-xl bg-primary/10 text-primary">
              <WalletCards className="size-5" aria-hidden="true" />
            </div>
            <DialogTitle className="text-2xl">Registrar pago</DialogTitle>
            <DialogDescription className="leading-6">
              Confirma el importe y selecciona el medio de pago que deseas registrar.
            </DialogDescription>
          </DialogHeader>

          <div className="space-y-5 px-6 py-5">
            <div className="rounded-2xl border border-primary/20 bg-primary/5 p-4">
              <p className="text-xs font-bold uppercase tracking-[0.14em] text-primary">Importe total</p>
              <p className="mt-1 text-3xl font-bold tracking-tight text-foreground">{formatCurrency(payment.amount)}</p>
              <p className="mt-1 text-sm text-muted-foreground">Pago asociado a la cita #{payment.appointmentId}</p>
            </div>

            <fieldset disabled={isSubmitting}>
              <legend className="mb-3 text-sm font-semibold text-foreground">Medio de pago</legend>
              <div className="grid gap-3 sm:grid-cols-2">
                {PAYMENT_METHODS.map((item) => {
                  const isSelected = method === item.value;
                  return (
                    <label key={item.value} className="group cursor-pointer">
                      <input
                        className="peer sr-only"
                        type="radio"
                        name="payment-method"
                        value={item.value}
                        checked={isSelected}
                        onChange={() => setMethod(item.value)}
                      />
                      <span className="flex min-h-20 items-center gap-3 rounded-xl border border-input bg-background p-4 text-sm font-semibold text-foreground transition-colors group-hover:border-primary/50 peer-focus-visible:ring-2 peer-focus-visible:ring-ring/35 peer-focus-visible:ring-offset-2 peer-checked:border-primary peer-checked:bg-primary/5">
                        <span className="grid size-9 shrink-0 place-items-center rounded-lg bg-muted text-muted-foreground peer-checked:text-primary">
                          <CreditCard className="size-5" aria-hidden="true" />
                        </span>
                        <span className="flex-1">{item.label}</span>
                        <span className={isSelected
                          ? 'grid size-5 place-items-center rounded-full bg-primary text-primary-foreground'
                          : 'size-5 rounded-full border border-input'}
                          aria-hidden="true"
                        >
                          {isSelected && <Check className="size-3.5" />}
                        </span>
                      </span>
                    </label>
                  );
                })}
              </div>
            </fieldset>

            {error && (
              <Alert variant="destructive">
                <AlertCircle aria-hidden="true" />
                <AlertTitle>No pudimos registrar el pago</AlertTitle>
                <AlertDescription>
                  Intenta nuevamente. Si el problema continúa, vuelve a cargar la página.
                </AlertDescription>
              </Alert>
            )}
          </div>

          <DialogFooter className="border-t border-border bg-muted/20 px-6 py-4">
            <Button type="button" variant="outline" disabled={isSubmitting} onClick={onClose}>
              Volver
            </Button>
            <Button type="submit" disabled={isSubmitting}>
              {isSubmitting ? <LoaderCircle className="animate-spin" aria-hidden="true" /> : <Check aria-hidden="true" />}
              {isSubmitting ? 'Registrando…' : 'Registrar pago'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
