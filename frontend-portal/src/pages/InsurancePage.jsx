import { useEffect, useRef, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import {
  AlertCircle,
  CheckCircle2,
  Info,
  LoaderCircle,
  RefreshCw,
  ShieldCheck,
  X,
} from 'lucide-react';
import DeleteInsuranceDialog from '../components/insurance/DeleteInsuranceDialog.jsx';
import InsuranceForm from '../components/insurance/InsuranceForm.jsx';
import InsuranceSummary from '../components/insurance/InsuranceSummary.jsx';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import { Button } from '@/components/ui/button';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card';
import {
  useInsuranceCatalog,
  useInsuranceMutations,
  useMyInsurances,
} from '../hooks/useInsurances.js';
import { getPrimaryInsurance } from '../utils/insurance.js';

export default function InsurancePage() {
  const [searchParams] = useSearchParams();
  const isOnboarding = searchParams.get('onboarding') === '1';
  const catalogQuery = useInsuranceCatalog();
  const insurancesQuery = useMyInsurances();
  const { createInsurance, deleteInsurance } = useInsuranceMutations();
  const deleteTriggerRef = useRef(null);
  const noticeRef = useRef(null);
  const [insuranceToDelete, setInsuranceToDelete] = useState(null);
  const [notice, setNotice] = useState('');
  const insurance = getPrimaryInsurance(insurancesQuery.data);

  useEffect(() => {
    if (notice) noticeRef.current?.focus();
  }, [notice]);

  function openDeleteDialog() {
    deleteTriggerRef.current = document.activeElement;
    deleteInsurance.reset();
    setInsuranceToDelete(insurance);
  }

  function closeDeleteDialog() {
    setInsuranceToDelete(null);
    window.requestAnimationFrame(() => deleteTriggerRef.current?.focus());
  }

  function handleCreate(payload) {
    createInsurance.mutate(payload, {
      onSuccess: () => setNotice('Tu seguro fue registrado. Se aplicará a las citas nuevas.'),
    });
  }

  function confirmDelete() {
    deleteInsurance.mutate(insuranceToDelete.id, {
      onSuccess: () => {
        closeDeleteDialog();
        setNotice('El seguro fue retirado correctamente.');
      },
    });
  }

  if (catalogQuery.isLoading || insurancesQuery.isLoading) {
    return (
      <Card className="mx-auto max-w-3xl border-primary/15">
        <CardContent className="flex min-h-72 flex-col items-center justify-center gap-4 text-center" role="status" aria-live="polite">
          <span className="grid size-12 place-items-center rounded-2xl bg-primary/10 text-primary">
            <LoaderCircle className="size-6 animate-spin" aria-hidden="true" />
          </span>
          <div>
            <p className="font-semibold text-foreground">Consultando tu cobertura</p>
            <p className="mt-1 text-sm text-muted-foreground">Estamos preparando la información de tu seguro.</p>
          </div>
        </CardContent>
      </Card>
    );
  }

  if (insurancesQuery.isError) {
    return (
      <Alert variant="destructive" className="mx-auto max-w-3xl">
        <AlertCircle aria-hidden="true" />
        <AlertTitle>No pudimos cargar tu seguro</AlertTitle>
        <AlertDescription className="space-y-4">
          <p>{insurancesQuery.error.message}</p>
          <Button type="button" variant="outline" size="sm" onClick={() => insurancesQuery.refetch()}>
            <RefreshCw aria-hidden="true" />
            Reintentar
          </Button>
        </AlertDescription>
      </Alert>
    );
  }

  return (
    <div className="space-y-6">
      <header className="flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
        <div className="max-w-3xl">
          <p className="mb-2 text-xs font-bold uppercase tracking-[0.18em] text-primary">
            {isOnboarding ? 'Último paso' : 'Cobertura personal'}
          </p>
          <h1 className="text-3xl font-bold tracking-tight text-foreground sm:text-4xl">
            {isOnboarding ? 'Seguro médico (opcional)' : 'Mi seguro'}
          </h1>
          <p className="mt-3 text-base leading-7 text-muted-foreground">
            Puedes atenderte como particular o registrar una póliza activa para tus próximas citas.
          </p>
        </div>
        <span className="hidden size-14 place-items-center rounded-2xl bg-primary/10 text-primary sm:grid" aria-hidden="true">
          <ShieldCheck className="size-7" />
        </span>
      </header>

      {notice && (
        <div ref={noticeRef} className="relative outline-none" tabIndex={-1}>
          <Alert className="border-emerald-200 bg-emerald-50 pr-12 text-emerald-950 dark:border-emerald-900 dark:bg-emerald-950/40 dark:text-emerald-100">
            <CheckCircle2 className="text-emerald-700 dark:text-emerald-300" aria-hidden="true" />
            <AlertTitle>Cobertura actualizada</AlertTitle>
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

      <Alert className="border-sky-200 bg-sky-50 text-sky-950 dark:border-sky-900 dark:bg-sky-950/40 dark:text-sky-100">
        <Info className="text-sky-700 dark:text-sky-300" aria-hidden="true" />
        <AlertTitle>Cómo se aplica tu cobertura</AlertTitle>
        <AlertDescription>
          El seguro se calcula al crear cada cita. Agregarlo o retirarlo no modifica pagos que ya fueron generados.
        </AlertDescription>
      </Alert>

      {insurance ? (
        <InsuranceSummary
          insurance={insurance}
          onDelete={openDeleteDialog}
          isDeleting={deleteInsurance.isPending}
        />
      ) : (
        <Card className="overflow-hidden">
          <CardHeader className="border-b border-border bg-muted/30">
            <div className="mb-2 flex size-11 items-center justify-center rounded-xl bg-primary/10 text-primary">
              <ShieldCheck className="size-5" aria-hidden="true" />
            </div>
            <CardTitle className="text-2xl">Registra tu póliza</CardTitle>
            <CardDescription>
              Completa la información de tu aseguradora y la vigencia de la cobertura.
            </CardDescription>
          </CardHeader>
          <CardContent className="py-6">
            {catalogQuery.isError ? (
              <Alert variant="destructive">
                <AlertCircle aria-hidden="true" />
                <AlertTitle>No pudimos cargar las aseguradoras</AlertTitle>
                <AlertDescription className="space-y-4">
                  <p>{catalogQuery.error.message}</p>
                  <Button type="button" variant="outline" size="sm" onClick={() => catalogQuery.refetch()}>
                    <RefreshCw aria-hidden="true" />
                    Reintentar
                  </Button>
                </AlertDescription>
              </Alert>
            ) : catalogQuery.data?.length ? (
              <InsuranceForm
                catalog={catalogQuery.data}
                isSubmitting={createInsurance.isPending}
                error={createInsurance.error}
                onSubmit={handleCreate}
              />
            ) : (
              <Alert>
                <Info aria-hidden="true" />
                <AlertTitle>Aseguradoras no disponibles</AlertTitle>
                <AlertDescription>No hay aseguradoras activas disponibles en este momento.</AlertDescription>
              </Alert>
            )}
          </CardContent>
        </Card>
      )}

      {isOnboarding && (
        <Card className="border-primary/20 bg-primary/5">
          <CardContent className="flex flex-col gap-5 py-6 sm:flex-row sm:items-center sm:justify-between">
            <div>
              <h2 className="text-xl font-semibold text-foreground">
                {insurance ? 'Tu cuenta está lista' : 'Puedes hacerlo más adelante'}
              </h2>
              <p className="mt-1 text-sm leading-6 text-muted-foreground">
                El seguro no es obligatorio para usar el portal ni para agendar una cita.
              </p>
            </div>
            <Button asChild className="shrink-0">
              <Link to="/">{insurance ? 'Ir a mi portal' : 'Omitir por ahora'}</Link>
            </Button>
          </CardContent>
        </Card>
      )}

      <DeleteInsuranceDialog
        insurance={insuranceToDelete}
        isSubmitting={deleteInsurance.isPending}
        error={deleteInsurance.error}
        onConfirm={confirmDelete}
        onClose={() => !deleteInsurance.isPending && closeDeleteDialog()}
      />
    </div>
  );
}
