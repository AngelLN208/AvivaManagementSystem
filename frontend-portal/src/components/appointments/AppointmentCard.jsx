import { CalendarDays, FileText, Stethoscope } from 'lucide-react';
import StatusBadge from '../ui/StatusBadge.jsx';
import { Button } from '../ui/button.jsx';
import { Card, CardContent } from '../ui/card.jsx';
import { doctorDisplayName } from '../../utils/appointments.js';
import { formatLongDate, formatTime } from '../../utils/dates.js';

export default function AppointmentCard({ appointment, canManage, onReschedule, onCancel, compact = false }) {
  const doctor = appointment.doctor;
  const dateLabel = formatLongDate(appointment.appointmentDateTime);
  const timeLabel = formatTime(appointment.appointmentDateTime);
  const actionContext = `${doctorDisplayName(doctor)}, ${dateLabel} a las ${timeLabel}`;

  return (
    <Card className={compact ? 'gap-0 border-0 bg-muted/35 py-0 shadow-none' : 'gap-0 py-0'}>
      <CardContent className="grid gap-5 p-5 sm:grid-cols-[9.5rem_1fr] sm:p-6">
        <div
          className="flex items-center gap-3 rounded-xl border border-primary/15 bg-primary/6 p-4 text-primary sm:flex-col sm:items-start sm:justify-center"
          aria-label={`${dateLabel}, ${timeLabel}`}
        >
          <CalendarDays className="size-5" aria-hidden="true" />
          <div>
            <strong className="block text-lg leading-tight text-foreground">{timeLabel}</strong>
            <span className="mt-1 block text-xs font-semibold capitalize leading-5 text-muted-foreground">{dateLabel}</span>
          </div>
        </div>

        <div className="min-w-0">
          <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
            <div>
              <p className="mb-1 flex items-center gap-1.5 text-xs font-bold uppercase tracking-[0.12em] text-primary">
                <Stethoscope className="size-3.5" aria-hidden="true" />
                {doctor?.specialty?.name || 'Consulta general'}
              </p>
              <h3 className="m-0 text-lg font-semibold text-foreground">{doctorDisplayName(doctor)}</h3>
            </div>
            <StatusBadge status={appointment.status} />
          </div>

          {appointment.reason && !compact && (
            <div className="mt-4 flex gap-3 rounded-xl bg-muted/60 p-3 text-sm text-muted-foreground">
              <FileText className="mt-0.5 size-4 shrink-0 text-primary" aria-hidden="true" />
              <p className="m-0"><span className="font-semibold text-foreground">Motivo: </span>{appointment.reason}</p>
            </div>
          )}

          {canManage && !compact && (
            <div className="mt-5 flex flex-col gap-2 border-t border-border pt-4 sm:flex-row">
              <Button type="button" variant="outline" size="sm" aria-label={`Reprogramar cita con ${actionContext}`} onClick={() => onReschedule(appointment)}>
                Reprogramar
              </Button>
              <Button type="button" variant="ghost" size="sm" className="text-destructive hover:bg-destructive/8 hover:text-destructive" aria-label={`Cancelar cita con ${actionContext}`} onClick={() => onCancel(appointment)}>
                Cancelar cita
              </Button>
            </div>
          )}
        </div>
      </CardContent>
    </Card>
  );
}
