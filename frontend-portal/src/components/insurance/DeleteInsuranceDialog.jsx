import {
  AlertCircle,
  LoaderCircle,
  Trash2,
  TriangleAlert,
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

export default function DeleteInsuranceDialog({ insurance, isSubmitting, error, onConfirm, onClose }) {
  if (!insurance) return null;

  function handleOpenChange(open) {
    if (!open && !isSubmitting) onClose();
  }

  return (
    <Dialog open onOpenChange={handleOpenChange}>
      <DialogContent
        className="max-w-md"
        showCloseButton={!isSubmitting}
        onEscapeKeyDown={(event) => {
          if (isSubmitting) event.preventDefault();
        }}
        onInteractOutside={(event) => {
          if (isSubmitting) event.preventDefault();
        }}
      >
        <DialogHeader className="text-left">
          <span className="mb-2 grid size-12 place-items-center rounded-2xl bg-destructive/10 text-destructive" aria-hidden="true">
            <TriangleAlert className="size-6" />
          </span>
          <DialogTitle className="text-2xl">¿Retirar este seguro?</DialogTitle>
          <DialogDescription className="text-sm leading-6">
            {insurance.insuranceName} dejará de aplicarse a las citas nuevas. Los pagos ya calculados no cambiarán.
          </DialogDescription>
        </DialogHeader>

        {error && (
          <Alert variant="destructive">
            <AlertCircle aria-hidden="true" />
            <AlertTitle>No pudimos retirar el seguro</AlertTitle>
            <AlertDescription>{error.message}</AlertDescription>
          </Alert>
        )}

        <DialogFooter>
          <Button type="button" variant="outline" disabled={isSubmitting} onClick={onClose}>
            Volver
          </Button>
          <Button type="button" variant="destructive" disabled={isSubmitting} onClick={onConfirm}>
            {isSubmitting
              ? <LoaderCircle className="animate-spin" aria-hidden="true" />
              : <Trash2 aria-hidden="true" />}
            {isSubmitting ? 'Retirando…' : 'Sí, retirar'}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
