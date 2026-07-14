import { AlertTriangle, CircleAlert, LoaderCircle } from 'lucide-react';
import { Alert, AlertDescription } from '@/components/ui/alert.jsx';
import { Button } from '@/components/ui/button.jsx';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog.jsx';
import { doctorDisplayName } from '../../utils/appointments.js';
import { formatLongDate, formatTime } from '../../utils/dates.js';

export default function CancelAppointmentDialog({ appointment, isSubmitting, error, onConfirm, onClose }) {
  if (!appointment) return null;

  function requestClose() {
    if (!isSubmitting) onClose();
  }

  return (
    <Dialog open onOpenChange={(open) => !open && requestClose()}>
      <DialogContent
        className="max-h-[calc(100dvh-2rem)] overflow-y-auto sm:max-w-lg"
        showCloseButton={!isSubmitting}
        onEscapeKeyDown={(event) => isSubmitting && event.preventDefault()}
        onPointerDownOutside={(event) => isSubmitting && event.preventDefault()}
      >
        <DialogHeader>
          <span className="mb-2 grid size-12 place-items-center rounded-xl bg-destructive/10 text-destructive" aria-hidden="true">
            <AlertTriangle className="size-6" />
          </span>
          <p className="text-xs font-bold uppercase tracking-[0.16em] text-destructive">Confirmar cancelación</p>
          <DialogTitle className="text-xl sm:text-2xl">¿Deseas cancelar esta cita?</DialogTitle>
          <DialogDescription className="pt-1">
            {doctorDisplayName(appointment.doctor)} · {formatLongDate(appointment.appointmentDateTime)} a las {formatTime(appointment.appointmentDateTime)}.
          </DialogDescription>
        </DialogHeader>

        <Alert variant="warning" role="status">
          <AlertTriangle aria-hidden="true" />
          <AlertDescription>La cita pasará a tu historial como cancelada.</AlertDescription>
        </Alert>

        {error && (
          <Alert variant="destructive" role="alert">
            <CircleAlert aria-hidden="true" />
            <AlertDescription>{error.message}</AlertDescription>
          </Alert>
        )}

        <DialogFooter>
          <Button type="button" variant="outline" disabled={isSubmitting} onClick={requestClose} autoFocus>
            Volver
          </Button>
          <Button type="button" variant="destructive" disabled={isSubmitting} onClick={onConfirm}>
            {isSubmitting && <LoaderCircle className="size-4 animate-spin" aria-hidden="true" />}
            {isSubmitting ? 'Cancelando…' : 'Sí, cancelar cita'}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
