import { CalendarDays, CheckCircle2, ChevronRight, Clock3, Plus, Stethoscope } from 'lucide-react';
import { Link } from 'react-router-dom';
import AppointmentCard from '../components/appointments/AppointmentCard.jsx';
import { ErrorState, LoadingState } from '../components/ui/AsyncState.jsx';
import { Button } from '../components/ui/button.jsx';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '../components/ui/card.jsx';
import { useAuth } from '../hooks/useAuth.js';
import { useMyAppointments } from '../hooks/useAppointments.js';
import { useCurrentTime } from '../hooks/useCurrentTime.js';
import { splitAppointments } from '../utils/appointments.js';

export default function DashboardPage() {
  const { session } = useAuth();
  const appointmentsQuery = useMyAppointments();
  const now = useCurrentTime();
  const { upcoming, history } = splitAppointments(appointmentsQuery.data || [], now);
  const completedCount = history.filter((appointment) => appointment.status === 'COMPLETED').length;
  const displayName = session?.firstName || session?.username;
  const today = new Date();
  const dayLabel = new Intl.DateTimeFormat('es-PE', { weekday: 'long', day: 'numeric', month: 'long' })
    .format(today);

  return (
    <div className="space-y-6 lg:space-y-8">
      <section className="relative overflow-hidden rounded-3xl border border-primary/15 bg-card px-6 py-7 shadow-sm sm:px-8 sm:py-9">
        <div className="absolute -right-16 -top-20 size-64 rounded-full bg-primary/8 blur-3xl" aria-hidden="true" />
        <div className="relative grid items-center gap-8 lg:grid-cols-[1fr_auto]">
          <div className="max-w-2xl">
            <p className="mb-2 text-xs font-bold uppercase tracking-[0.18em] text-primary">Portal del paciente</p>
            <h1 className="text-3xl font-bold tracking-tight text-foreground sm:text-4xl">
              Hola, {displayName}
            </h1>
            <p className="mb-0 mt-3 max-w-xl text-base leading-7 text-muted-foreground">
              Consulta tu agenda, encuentra a tu especialista y administra tus citas desde un solo lugar.
            </p>
            <div className="mt-6 flex flex-col gap-3 sm:flex-row">
              <Button asChild size="lg">
                <Link to="/agendar"><Plus />Agendar una cita</Link>
              </Button>
              <Button asChild size="lg" variant="outline">
                <Link to="/citas">Ver mis citas<ChevronRight /></Link>
              </Button>
            </div>
          </div>

          <div className="hidden min-w-52 rounded-2xl border border-border bg-background/90 p-5 shadow-sm lg:block" aria-hidden="true">
            <div className="flex items-center gap-3 text-primary">
              <span className="flex size-10 items-center justify-center rounded-xl bg-primary/10"><CalendarDays className="size-5" /></span>
              <span className="text-sm font-semibold capitalize">{dayLabel}</span>
            </div>
            <p className="mb-0 mt-4 text-sm leading-6 text-muted-foreground">Tu información se mantiene organizada y disponible cuando la necesites.</p>
          </div>
        </div>
      </section>

      {appointmentsQuery.isLoading && <LoadingState message="Consultando tus citas…" />}
      {appointmentsQuery.isError && (
        <ErrorState message={appointmentsQuery.error.message} onRetry={appointmentsQuery.refetch} />
      )}

      {appointmentsQuery.isSuccess && (
        <>
          <section className="grid gap-4 sm:grid-cols-2 xl:grid-cols-3" aria-label="Resumen de citas">
            <MetricCard
              icon={CalendarDays}
              label="Próximas citas"
              value={upcoming.length}
              detail={upcoming.length === 1 ? 'cita programada' : 'citas programadas'}
            />
            <MetricCard
              icon={CheckCircle2}
              label="Atenciones completadas"
              value={completedCount}
              detail="en tu historial"
            />
            <Card className="border-primary/15 bg-primary text-primary-foreground">
              <CardHeader>
                <span className="mb-2 flex size-10 items-center justify-center rounded-xl bg-white/12"><Clock3 className="size-5" /></span>
                <CardDescription className="font-semibold text-primary-foreground/75">Disponibilidad en línea</CardDescription>
                <CardTitle className="text-xl">Elige el día y la hora</CardTitle>
              </CardHeader>
              <CardContent>
                <Link to="/agendar" className="inline-flex items-center gap-1 text-sm font-bold text-primary-foreground hover:underline">
                  Revisar horarios <ChevronRight className="size-4" />
                </Link>
              </CardContent>
            </Card>
          </section>

          <Card>
            <CardHeader className="sm:grid-cols-[1fr_auto]">
              <div>
                <p className="mb-2 text-xs font-bold uppercase tracking-[0.16em] text-primary">Agenda personal</p>
                <CardTitle className="text-xl sm:text-2xl">
                  {upcoming.length ? 'Tu próxima cita' : 'Aún no tienes citas próximas'}
                </CardTitle>
                <CardDescription className="mt-2">Mantén a la vista la información más importante de tu atención.</CardDescription>
              </div>
              {upcoming.length > 0 && (
                <Button asChild variant="ghost" className="mt-3 justify-start sm:mt-0">
                  <Link to="/citas">Ver todas<ChevronRight /></Link>
                </Button>
              )}
            </CardHeader>
            <CardContent>
              {upcoming.length > 0 ? (
                <AppointmentCard appointment={upcoming[0]} compact />
              ) : (
                <div className="flex flex-col items-start gap-4 rounded-2xl border border-dashed border-border bg-muted/40 p-6 sm:flex-row sm:items-center sm:justify-between">
                  <div>
                    <p className="m-0 font-semibold text-foreground">Tu agenda está disponible</p>
                    <p className="mb-0 mt-1 text-sm text-muted-foreground">Agenda una cita y podrás consultarla aquí rápidamente.</p>
                  </div>
                  <Button asChild><Link to="/agendar"><Plus />Agendar cita</Link></Button>
                </div>
              )}
            </CardContent>
          </Card>

          <section className="flex flex-col gap-5 rounded-2xl border border-border bg-muted/45 p-6 sm:flex-row sm:items-center sm:justify-between">
            <div className="flex gap-4">
              <span className="flex size-11 shrink-0 items-center justify-center rounded-xl bg-background text-primary shadow-sm ring-1 ring-border">
                <Stethoscope className="size-5" aria-hidden="true" />
              </span>
              <div>
                <h2 className="m-0 text-lg font-semibold text-foreground">Prepárate para tu próxima atención</h2>
                <p className="mb-0 mt-1 text-sm text-muted-foreground">Llega 15 minutos antes y ten a la mano tu documento de identidad.</p>
              </div>
            </div>
            <Button asChild variant="outline"><Link to="/medicos">Conocer especialistas</Link></Button>
          </section>
        </>
      )}
    </div>
  );
}

function MetricCard({ icon: Icon, label, value, detail }) {
  return (
    <Card>
      <CardHeader>
        <span className="mb-2 flex size-10 items-center justify-center rounded-xl bg-primary/10 text-primary">
          <Icon className="size-5" aria-hidden="true" />
        </span>
        <CardDescription className="font-semibold">{label}</CardDescription>
        <CardTitle className="text-3xl">{value}</CardTitle>
      </CardHeader>
      <CardContent><p className="m-0 text-sm text-muted-foreground">{detail}</p></CardContent>
    </Card>
  );
}
