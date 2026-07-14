import {
  CalendarDays,
  CheckCircle2,
  FileClock,
  ReceiptText,
  ShieldCheck,
  WalletCards,
} from 'lucide-react';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import {
  Card,
  CardContent,
  CardFooter,
  CardHeader,
  CardTitle,
} from '@/components/ui/card';
import { doctorDisplayName } from '../../utils/appointments.js';
import { formatLongDate, formatShortDate, formatTime } from '../../utils/dates.js';
import {
  formatCurrency,
  PAYMENT_METHOD_LABELS,
  PAYMENT_STATUS,
} from '../../utils/payments.js';

const STATUS_CLASSES = {
  success: 'border-emerald-200 bg-emerald-50 text-emerald-800 dark:border-emerald-900 dark:bg-emerald-950/50 dark:text-emerald-200',
  warning: 'border-amber-200 bg-amber-50 text-amber-800 dark:border-amber-900 dark:bg-amber-950/50 dark:text-amber-200',
  danger: 'border-rose-200 bg-rose-50 text-rose-800 dark:border-rose-900 dark:bg-rose-950/50 dark:text-rose-200',
  info: 'border-sky-200 bg-sky-50 text-sky-800 dark:border-sky-900 dark:bg-sky-950/50 dark:text-sky-200',
  neutral: 'border-border bg-muted text-muted-foreground',
};

export default function PaymentCard({ payment, appointment, receipt, receiptLoadFailed = false, onPay, onViewReceipt }) {
  const status = PAYMENT_STATUS[payment.status] || {
    label: 'Estado no disponible',
    tone: 'neutral',
  };
  const statusLabel = payment.status === 'PAID' ? 'Pago registrado' : status.label;
  const appointmentDateTime = appointment?.appointmentDateTime || payment.appointmentDateTime;
  const doctorName = appointment
    ? doctorDisplayName(appointment.doctor)
    : payment.doctorName || 'Cita médica';

  return (
    <article>
      <Card className="gap-0 overflow-hidden border-border/80 shadow-none transition-shadow hover:shadow-sm">
        <CardHeader className="gap-4 border-b border-border/70 bg-card pb-5 sm:grid-cols-[1fr_auto]">
          <div className="min-w-0">
            <p className="mb-2 text-xs font-bold uppercase tracking-[0.14em] text-primary">Pago #{payment.id}</p>
            <CardTitle asChild>
              <h2 className="truncate text-lg font-semibold text-foreground sm:text-xl">{doctorName}</h2>
            </CardTitle>
            <p className="mt-2 flex items-start gap-2 text-sm leading-6 text-muted-foreground">
              <CalendarDays className="mt-1 size-4 shrink-0 text-primary" aria-hidden="true" />
              <span>
                {appointmentDateTime
                  ? formatLongDate(appointmentDateTime) + ' · ' + formatTime(appointmentDateTime)
                  : 'Cita #' + payment.appointmentId}
              </span>
            </p>
          </div>
          <Badge variant="outline" className={'h-fit justify-self-start px-3 py-1 ' + (STATUS_CLASSES[status.tone] || STATUS_CLASSES.neutral)}>
            {statusLabel}
          </Badge>
        </CardHeader>

        <CardContent className="py-5">
          <dl className="overflow-hidden rounded-xl border border-border/70 bg-muted/30">
            <div className="flex items-center justify-between gap-5 border-b border-border/70 px-4 py-3">
              <dt className="text-sm text-muted-foreground">Precio de consulta</dt>
              <dd className="m-0 font-medium text-foreground">{formatCurrency(payment.baseAmount)}</dd>
            </div>
            {Number(payment.insuranceCoveredAmount) > 0 && (
              <div className="flex items-center justify-between gap-5 border-b border-border/70 px-4 py-3">
                <dt className="flex items-center gap-2 text-sm text-muted-foreground">
                  <ShieldCheck className="size-4 text-emerald-600 dark:text-emerald-300" aria-hidden="true" />
                  Cobertura de {payment.insuranceName || 'seguro'}
                </dt>
                <dd className="m-0 font-medium text-emerald-700 dark:text-emerald-300">
                  − {formatCurrency(payment.insuranceCoveredAmount)}
                </dd>
              </div>
            )}
            {Number(payment.deductibleApplied) > 0 && (
              <div className="flex items-center justify-between gap-5 border-b border-border/70 px-4 py-3">
                <dt className="text-sm text-muted-foreground">Deducible aplicado</dt>
                <dd className="m-0 font-medium text-foreground">{formatCurrency(payment.deductibleApplied)}</dd>
              </div>
            )}
            <div className="flex items-center justify-between gap-5 bg-primary/5 px-4 py-4">
              <dt className="font-semibold text-foreground">Total del paciente</dt>
              <dd className="m-0 text-lg font-bold text-primary">{formatCurrency(payment.amount)}</dd>
            </div>
          </dl>

          {payment.status === 'PAID' && (
            <p className="mt-4 flex items-center gap-2 text-sm text-muted-foreground">
              <CheckCircle2 className="size-4 text-emerald-600 dark:text-emerald-300" aria-hidden="true" />
              <span>
                Registrado {payment.paymentDate ? 'el ' + formatShortDate(payment.paymentDate) : ''}
                {payment.method ? ' · ' + (PAYMENT_METHOD_LABELS[payment.method] || 'Medio registrado') : ''}
              </span>
            </p>
          )}
        </CardContent>

        <CardFooter className="flex flex-wrap gap-2 border-t border-border/70 bg-muted/20 py-4">
          {payment.status === 'PENDING' && (
            <Button type="button" onClick={() => onPay(payment)}>
              <WalletCards aria-hidden="true" />
              Registrar pago
            </Button>
          )}
          {receipt && (
            <Button type="button" variant="outline" onClick={() => onViewReceipt(receipt)}>
              <ReceiptText aria-hidden="true" />
              Ver constancia
            </Button>
          )}
          {payment.status === 'PAID' && !receipt && (
            <span className="inline-flex items-center gap-2 text-sm text-muted-foreground" role="status">
              <FileClock className="size-4" aria-hidden="true" />
              {receiptLoadFailed ? 'Constancia no disponible en este momento' : 'Preparando constancia de pago…'}
            </span>
          )}
        </CardFooter>
      </Card>
    </article>
  );
}
