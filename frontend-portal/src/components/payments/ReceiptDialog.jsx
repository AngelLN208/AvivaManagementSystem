import {
  Building2,
  CalendarDays,
  Download,
  FileText,
  Hash,
  Printer,
  ReceiptText,
  Stethoscope,
} from 'lucide-react';
import { Button } from '@/components/ui/button';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogTitle,
} from '@/components/ui/dialog';
import { formatShortDate } from '../../utils/dates.js';
import {
  formatCurrency,
  formatReceiptNumberForDisplay,
  getReceiptDescriptionForDisplay,
} from '../../utils/payments.js';

export default function ReceiptDialog({
  receipt,
  isDownloading = false,
  downloadError,
  onDownload,
  onClose,
}) {
  if (!receipt) return null;

  const receiptNumber = formatReceiptNumberForDisplay(receipt.receiptNumber);
  const description = getReceiptDescriptionForDisplay(receipt);

  return (
    <Dialog open onOpenChange={(open) => !open && onClose()}>
      <DialogContent className="max-h-[90vh] max-w-2xl overflow-y-auto p-0 print:fixed print:inset-0 print:max-h-none print:w-full print:max-w-none print:translate-x-0 print:translate-y-0 print:overflow-visible print:border-0 print:bg-white print:shadow-none">
        <article className="print-receipt bg-card p-6 text-card-foreground print:bg-white print:text-black sm:p-8">
          <header className="flex items-center gap-4 border-b border-border pb-5 print:border-neutral-300">
            <span className="grid size-12 shrink-0 place-items-center rounded-2xl bg-gradient-to-br from-primary to-primary-strong text-primary-foreground shadow-sm" aria-hidden="true">
              <Building2 className="size-6" />
            </span>
            <div>
              <strong className="block text-lg font-bold text-foreground print:text-black">Clínica Aviva</strong>
              <span className="text-sm text-muted-foreground print:text-neutral-600">Portal de pacientes</span>
            </div>
          </header>

          <div className="py-6">
            <p className="mb-2 flex items-center gap-2 text-xs font-bold uppercase tracking-[0.16em] text-primary print:text-neutral-700">
              <ReceiptText className="size-4" aria-hidden="true" />
              Constancia de pago
            </p>
            <DialogTitle asChild>
              <h2 className="text-2xl font-bold tracking-tight text-foreground print:text-black sm:text-3xl">
                {receiptNumber ? `Constancia de pago ${receiptNumber}` : 'Constancia de pago'}
              </h2>
            </DialogTitle>
          </div>

          <dl className="grid gap-3 sm:grid-cols-2">
            <ReceiptDetail icon={CalendarDays} label="Fecha" value={formatShortDate(receipt.createdAt)} />
            <ReceiptDetail icon={Hash} label="Pago" value={'#' + receipt.paymentId} />
            <ReceiptDetail icon={Stethoscope} label="Cita" value={'#' + receipt.appointmentId} />
            <ReceiptDetail icon={FileText} label="Total registrado" value={formatCurrency(receipt.totalAmount)} emphasized />
          </dl>

          <DialogDescription asChild>
            <p className="mt-6 rounded-xl border-l-4 border-primary bg-primary/5 p-4 text-sm leading-6 text-foreground print:border-neutral-500 print:bg-white print:text-black">
              {description}
            </p>
          </DialogDescription>

          <p className="mt-6 border-t border-border pt-4 text-xs leading-5 text-muted-foreground print:border-neutral-300 print:text-neutral-600">
            Documento emitido desde el portal de pacientes de Clínica Aviva.
          </p>
          {downloadError && (
            <p className="no-print mt-4 rounded-lg bg-destructive/10 px-4 py-3 text-sm font-medium text-destructive" role="alert">
              {downloadError.message || 'No pudimos descargar la constancia.'}
            </p>
          )}
        </article>

        <DialogFooter className="no-print border-t border-border bg-muted/20 px-6 py-4">
          <Button type="button" variant="outline" onClick={onClose}>Cerrar</Button>
          <Button type="button" variant="outline" disabled={isDownloading} onClick={() => onDownload(receipt)}>
            <Download aria-hidden="true" />
            {isDownloading ? 'Descargando…' : 'Descargar PDF'}
          </Button>
          <Button type="button" onClick={() => window.print()}>
            <Printer aria-hidden="true" />
            Imprimir constancia
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

function ReceiptDetail({ icon: Icon, label, value, emphasized = false }) {
  return (
    <div className={emphasized
      ? 'rounded-xl border border-primary/20 bg-primary/5 p-4 print:border-neutral-300 print:bg-white'
      : 'rounded-xl border border-border bg-muted/30 p-4 print:border-neutral-300 print:bg-white'}
    >
      <dt className="flex items-center gap-2 text-xs font-bold uppercase tracking-[0.1em] text-muted-foreground print:text-neutral-600">
        <Icon className="size-4" aria-hidden="true" />
        {label}
      </dt>
      <dd className={emphasized
        ? 'mt-2 text-lg font-bold text-primary print:text-black'
        : 'mt-2 font-semibold text-foreground print:text-black'}
      >
        {value}
      </dd>
    </div>
  );
}
