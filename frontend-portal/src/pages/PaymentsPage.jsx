import { useEffect, useMemo, useRef, useState } from 'react';
import {
  CheckCircle2,
  CircleDollarSign,
  FileText,
  LoaderCircle,
  ReceiptText,
  RefreshCw,
  WalletCards,
  X,
} from 'lucide-react';
import PaymentCard from '../components/payments/PaymentCard.jsx';
import ProcessPaymentDialog from '../components/payments/ProcessPaymentDialog.jsx';
import ReceiptDialog from '../components/payments/ReceiptDialog.jsx';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import { Button } from '@/components/ui/button';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card';
import { useMyAppointments } from '../hooks/useAppointments.js';
import { useMyPayments, useMyReceipts, usePaymentMutations } from '../hooks/usePayments.js';
import {
  filterPayments,
  receiptPdfFilename,
  receiptsByPaymentId,
} from '../utils/payments.js';

const FILTERS = [
  { id: 'pending', label: 'Pendientes' },
  { id: 'paid', label: 'Registrados' },
  { id: 'all', label: 'Todos' },
];

export default function PaymentsPage() {
  const paymentsQuery = useMyPayments();
  const receiptsQuery = useMyReceipts();
  const appointmentsQuery = useMyAppointments();
  const { payPayment, downloadReceiptPdf } = usePaymentMutations();
  const lastDialogTrigger = useRef(null);
  const noticeRef = useRef(null);
  const [filter, setFilter] = useState('pending');
  const [paymentToProcess, setPaymentToProcess] = useState(null);
  const [receiptToView, setReceiptToView] = useState(null);
  const [notice, setNotice] = useState('');

  const payments = paymentsQuery.data || [];
  const visiblePayments = useMemo(
    () => filterPayments(paymentsQuery.data, filter),
    [paymentsQuery.data, filter],
  );
  const receiptMap = useMemo(
    () => receiptsByPaymentId(receiptsQuery.data),
    [receiptsQuery.data],
  );
  const appointmentMap = useMemo(
    () => new Map((appointmentsQuery.data || []).map((appointment) => [String(appointment.id), appointment])),
    [appointmentsQuery.data],
  );

  useEffect(() => {
    if (notice) noticeRef.current?.focus();
  }, [notice]);

  function rememberDialogTrigger() {
    lastDialogTrigger.current = document.activeElement;
  }

  function restoreDialogFocus() {
    window.requestAnimationFrame(() => lastDialogTrigger.current?.focus());
  }

  function openPaymentDialog(payment) {
    rememberDialogTrigger();
    payPayment.reset();
    setPaymentToProcess(payment);
  }

  function closePaymentDialog() {
    setPaymentToProcess(null);
    restoreDialogFocus();
  }

  function openReceiptDialog(receipt) {
    rememberDialogTrigger();
    downloadReceiptPdf.reset();
    setReceiptToView(receipt);
  }

  function closeReceiptDialog() {
    setReceiptToView(null);
    restoreDialogFocus();
  }

  function confirmPayment(method) {
    payPayment.mutate({ paymentId: paymentToProcess.id, method }, {
      onSuccess: () => {
        closePaymentDialog();
        setNotice('Pago registrado. Tu constancia de pago ya está disponible.');
        setFilter('paid');
      },
    });
  }

  function downloadReceipt(receipt) {
    downloadReceiptPdf.mutate(receipt.id, {
      onSuccess: (blob) => {
        const objectUrl = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = objectUrl;
        link.download = receiptPdfFilename(receipt.receiptNumber);
        document.body.appendChild(link);
        link.click();
        link.remove();
        window.setTimeout(() => window.URL.revokeObjectURL(objectUrl), 0);
      },
    });
  }

  if (paymentsQuery.isLoading) {
    return (
      <Card className="mx-auto max-w-3xl border-primary/15">
        <CardContent className="flex min-h-72 flex-col items-center justify-center gap-4 text-center" role="status" aria-live="polite">
          <span className="grid size-12 place-items-center rounded-2xl bg-primary/10 text-primary">
            <LoaderCircle className="size-6 animate-spin" aria-hidden="true" />
          </span>
          <div>
            <p className="font-semibold text-foreground">Consultando tus pagos</p>
            <p className="mt-1 text-sm text-muted-foreground">Estamos preparando tu información financiera.</p>
          </div>
        </CardContent>
      </Card>
    );
  }

  if (paymentsQuery.isError) {
    return (
      <Alert variant="destructive" className="mx-auto max-w-3xl">
        <FileText aria-hidden="true" />
        <AlertTitle>No pudimos cargar tus pagos</AlertTitle>
        <AlertDescription className="space-y-4">
          <p>Intenta nuevamente. Si el problema continúa, vuelve a cargar la página.</p>
          <Button type="button" variant="outline" size="sm" onClick={() => paymentsQuery.refetch()}>
            <RefreshCw aria-hidden="true" />
            Reintentar
          </Button>
        </AlertDescription>
      </Alert>
    );
  }

  const pendingCount = payments.filter((payment) => payment.status === 'PENDING').length;
  const paidCount = payments.filter((payment) => payment.status === 'PAID').length;

  return (
    <div className="space-y-6">
      <header className="flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
        <div className="max-w-3xl">
          <p className="mb-2 text-xs font-bold uppercase tracking-[0.18em] text-primary">Finanzas personales</p>
          <h1 className="text-3xl font-bold tracking-tight text-foreground sm:text-4xl">Pagos y constancias</h1>
          <p className="mt-3 text-base leading-7 text-muted-foreground">
            Consulta los importes asociados a tus citas, registra un pago y revisa tus constancias.
          </p>
        </div>
        <span className="hidden size-14 place-items-center rounded-2xl bg-primary/10 text-primary sm:grid" aria-hidden="true">
          <CircleDollarSign className="size-7" />
        </span>
      </header>

      <Alert className="border-primary/20 bg-primary/5">
        <WalletCards className="text-primary" aria-hidden="true" />
        <AlertTitle>Todo en un solo lugar</AlertTitle>
        <AlertDescription>
          Revisa cada importe antes de registrarlo. La constancia de pago quedará disponible al finalizar.
        </AlertDescription>
      </Alert>

      {notice && (
        <div ref={noticeRef} className="relative outline-none" tabIndex={-1}>
          <Alert className="border-emerald-200 bg-emerald-50 pr-12 text-emerald-950 dark:border-emerald-900 dark:bg-emerald-950/40 dark:text-emerald-100">
            <CheckCircle2 className="text-emerald-700 dark:text-emerald-300" aria-hidden="true" />
            <AlertTitle>Pago registrado</AlertTitle>
            <AlertDescription>{notice}</AlertDescription>
          </Alert>
          <Button
            type="button"
            variant="ghost"
            size="icon-sm"
            className="absolute right-2 top-2 text-emerald-900 hover:bg-emerald-100 dark:text-emerald-100 dark:hover:bg-emerald-900"
            aria-label="Cerrar mensaje"
            onClick={() => setNotice('')}
          >
            <X aria-hidden="true" />
          </Button>
        </div>
      )}

      {receiptsQuery.isError && (
        <Alert variant="destructive">
          <ReceiptText aria-hidden="true" />
          <AlertTitle>No pudimos cargar las constancias</AlertTitle>
          <AlertDescription>Intenta actualizar la página dentro de unos momentos.</AlertDescription>
        </Alert>
      )}

      <Card className="overflow-hidden">
        <CardHeader className="border-b border-border bg-muted/30">
          <CardTitle className="text-xl">Movimientos</CardTitle>
          <CardDescription>Filtra tus pagos por estado y consulta el detalle de cada cita.</CardDescription>
          <div className="mt-4 flex w-full flex-wrap gap-2" role="group" aria-label="Filtrar pagos">
            {FILTERS.map((item) => {
              const count = item.id === 'pending'
                ? pendingCount
                : item.id === 'paid' ? paidCount : payments.length;
              const isActive = filter === item.id;
              return (
                <Button
                  key={item.id}
                  type="button"
                  size="sm"
                  variant={isActive ? 'default' : 'ghost'}
                  aria-pressed={isActive}
                  onClick={() => setFilter(item.id)}
                >
                  {item.label}
                  <span className={isActive
                    ? 'rounded-full bg-primary-foreground/15 px-2 py-0.5 text-xs'
                    : 'rounded-full bg-muted px-2 py-0.5 text-xs text-muted-foreground'}
                  >
                    {count}
                  </span>
                </Button>
              );
            })}
          </div>
        </CardHeader>

        <CardContent className="py-6">
          {visiblePayments.length === 0 ? (
            <div className="flex min-h-64 flex-col items-center justify-center px-4 text-center">
              <span className="grid size-14 place-items-center rounded-2xl bg-muted text-muted-foreground">
                <ReceiptText className="size-7" aria-hidden="true" />
              </span>
              <h2 className="mt-5 text-xl font-semibold text-foreground">
                {filter === 'pending' ? 'No tienes pagos pendientes' : 'No hay pagos en esta sección'}
              </h2>
              <p className="mt-2 max-w-md text-sm leading-6 text-muted-foreground">
                {filter === 'pending'
                  ? 'Cuando agendes una cita, el importe correspondiente aparecerá aquí.'
                  : 'Tus pagos registrados aparecerán aquí cuando estén disponibles.'}
              </p>
            </div>
          ) : (
            <div className="space-y-4">
              {visiblePayments.map((payment) => (
                <PaymentCard
                  key={payment.id}
                  payment={payment}
                  appointment={appointmentMap.get(String(payment.appointmentId))}
                  receipt={receiptMap.get(String(payment.id))}
                  receiptLoadFailed={receiptsQuery.isError}
                  onPay={openPaymentDialog}
                  onViewReceipt={openReceiptDialog}
                />
              ))}
            </div>
          )}
        </CardContent>
      </Card>

      <ProcessPaymentDialog
        payment={paymentToProcess}
        isSubmitting={payPayment.isPending}
        error={payPayment.error}
        onConfirm={confirmPayment}
        onClose={() => !payPayment.isPending && closePaymentDialog()}
      />
      <ReceiptDialog
        receipt={receiptToView}
        isDownloading={downloadReceiptPdf.isPending}
        downloadError={downloadReceiptPdf.error}
        onDownload={downloadReceipt}
        onClose={closeReceiptDialog}
      />
    </div>
  );
}
