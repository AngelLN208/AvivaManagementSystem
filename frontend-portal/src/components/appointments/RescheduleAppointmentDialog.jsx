import { useMemo, useState } from 'react';
import {
  CalendarClock,
  CircleAlert,
  Clock3,
  LoaderCircle,
  RefreshCw,
} from 'lucide-react';
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
import { Input } from '@/components/ui/input.jsx';
import { Label } from '@/components/ui/label.jsx';
import { useAvailableSlots } from '../../hooks/useCatalogs.js';
import { cn } from '@/lib/utils.js';
import { doctorDisplayName } from '../../utils/appointments.js';
import {
  combineLocalDateTime,
  formatSlotTime,
  getLocalDateInputValue,
  isPastSlot,
} from '../../utils/dates.js';

export default function RescheduleAppointmentDialog({ appointment, isSubmitting, error, onConfirm, onClose }) {
  if (!appointment) return null;

  return (
    <RescheduleAppointmentDialogContent
      key={`${appointment.id}-${error?.status === 409 ? 'conflict' : 'ready'}`}
      appointment={appointment}
      isSubmitting={isSubmitting}
      error={error}
      onConfirm={onConfirm}
      onClose={onClose}
    />
  );
}

function RescheduleAppointmentDialogContent({ appointment, isSubmitting, error, onConfirm, onClose }) {
  const [date, setDate] = useState('');
  const [time, setTime] = useState('');
  const doctorId = appointment.doctor?.id;
  const slotsQuery = useAvailableSlots(doctorId, date);
  const minimumDate = getLocalDateInputValue();

  const availableSlots = useMemo(
    () => (slotsQuery.data || []).filter((slot) => !isPastSlot(date, slot.startTime)),
    [slotsQuery.data, date],
  );

  function handleSubmit(event) {
    event.preventDefault();
    if (!date || !time) return;
    onConfirm(combineLocalDateTime(date, time));
  }

  function requestClose() {
    if (!isSubmitting) onClose();
  }

  return (
    <Dialog open onOpenChange={(open) => !open && requestClose()}>
      <DialogContent
        className="max-h-[calc(100dvh-2rem)] overflow-y-auto sm:max-w-xl"
        showCloseButton={!isSubmitting}
        onEscapeKeyDown={(event) => isSubmitting && event.preventDefault()}
        onPointerDownOutside={(event) => isSubmitting && event.preventDefault()}
      >
        <form className="space-y-5" onSubmit={handleSubmit}>
          <DialogHeader>
            <span className="mb-2 grid size-12 place-items-center rounded-xl bg-primary/10 text-primary" aria-hidden="true">
              <CalendarClock className="size-6" />
            </span>
            <p className="text-xs font-bold uppercase tracking-[0.16em] text-primary">Nueva fecha y hora</p>
            <DialogTitle className="text-xl sm:text-2xl">Reprogramar cita</DialogTitle>
            <DialogDescription className="pt-1">
              El médico se mantiene: <strong className="font-semibold text-foreground">{doctorDisplayName(appointment.doctor)}</strong>.
            </DialogDescription>
          </DialogHeader>

          <div className="space-y-2">
            <Label htmlFor="reschedule-date">Fecha</Label>
            <Input
              id="reschedule-date"
              className="h-11"
              type="date"
              min={minimumDate}
              value={date}
              onChange={(event) => {
                setDate(event.target.value);
                setTime('');
              }}
              disabled={isSubmitting}
              required
              autoFocus
            />
          </div>

          <fieldset className="min-w-0 space-y-3 disabled:opacity-60" disabled={!date || slotsQuery.isLoading || isSubmitting}>
            <legend className="mb-2 text-sm font-semibold text-foreground">Horario disponible</legend>

            {date && slotsQuery.isLoading && (
              <p className="flex items-center gap-2 text-sm text-muted-foreground" role="status">
                <LoaderCircle className="size-4 animate-spin" aria-hidden="true" />
                Consultando horarios…
              </p>
            )}
            {date && slotsQuery.isError && (
              <Alert variant="destructive">
                <CircleAlert aria-hidden="true" />
                <AlertDescription className="space-y-3">
                  <p>{slotsQuery.error.message}</p>
                  <Button type="button" variant="outline" size="sm" onClick={() => slotsQuery.refetch()}>
                    <RefreshCw className="size-4" aria-hidden="true" />
                    Reintentar
                  </Button>
                </AlertDescription>
              </Alert>
            )}
            {date && !slotsQuery.isLoading && !slotsQuery.isError && availableSlots.length === 0 && (
              <Alert variant="info" role="status">
                <Clock3 aria-hidden="true" />
                <AlertDescription>No hay horarios disponibles para esta fecha.</AlertDescription>
              </Alert>
            )}
            {date && !slotsQuery.isLoading && !slotsQuery.isError && availableSlots.length > 0 && (
              <p className="text-sm text-muted-foreground" role="status" aria-live="polite">
                {availableSlots.length} {availableSlots.length === 1 ? 'horario disponible' : 'horarios disponibles'}.
              </p>
            )}

            <div className="grid grid-cols-2 gap-2.5 sm:grid-cols-3">
              {availableSlots.map((slot) => {
                const isSelected = time === slot.startTime;
                return (
                  <label
                    key={slot.startTime}
                    className={cn(
                      'flex min-h-11 cursor-pointer items-center justify-center gap-2 rounded-lg border border-input bg-background px-3 py-2 text-sm font-semibold transition hover:border-primary/60 hover:bg-accent/50 focus-within:ring-2 focus-within:ring-ring/30',
                      isSelected && 'border-primary bg-primary/10 text-primary ring-1 ring-primary/20',
                    )}
                  >
                    <input
                      className="size-4 shrink-0 accent-primary"
                      type="radio"
                      name="reschedule-time"
                      value={slot.startTime}
                      checked={isSelected}
                      onChange={() => setTime(slot.startTime)}
                    />
                    {formatSlotTime(slot.startTime)}
                  </label>
                );
              })}
            </div>
          </fieldset>

          {error && (
            <Alert variant="destructive" role="alert">
              <CircleAlert aria-hidden="true" />
              <AlertDescription>{error.message}</AlertDescription>
            </Alert>
          )}

          <DialogFooter>
            <Button type="button" variant="outline" disabled={isSubmitting} onClick={requestClose}>Volver</Button>
            <Button type="submit" disabled={!date || !time || isSubmitting}>
              {isSubmitting && <LoaderCircle className="size-4 animate-spin" aria-hidden="true" />}
              {isSubmitting ? 'Guardando…' : 'Confirmar cambio'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
