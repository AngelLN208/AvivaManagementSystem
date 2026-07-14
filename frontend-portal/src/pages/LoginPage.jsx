import { useState } from 'react';
import { Link, useLocation, useNavigate, useSearchParams } from 'react-router-dom';
import {
  ArrowRight,
  CalendarCheck2,
  CircleAlert,
  Eye,
  EyeOff,
  HeartPulse,
  LoaderCircle,
  LockKeyhole,
  ShieldCheck,
} from 'lucide-react';
import AvivaLogo from '@/components/brand/AvivaLogo.jsx';
import { Alert, AlertDescription } from '@/components/ui/alert.jsx';
import { Button } from '@/components/ui/button.jsx';
import { Card, CardContent, CardDescription, CardHeader } from '@/components/ui/card.jsx';
import { Input } from '@/components/ui/input.jsx';
import { Label } from '@/components/ui/label.jsx';
import { useAuth } from '../hooks/useAuth.js';

export default function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [searchParams] = useSearchParams();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  async function handleSubmit(event) {
    event.preventDefault();
    setError('');

    if (!username.trim() || !password) {
      setError('Ingresa tu usuario y contraseña.');
      return;
    }

    setIsSubmitting(true);
    try {
      await login(username.trim(), password);
      navigate(location.state?.from || '/', { replace: true });
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <main className="grid min-h-dvh bg-background lg:grid-cols-[minmax(22rem,0.9fr)_minmax(32rem,1.1fr)]">
      <section className="relative hidden overflow-hidden bg-primary px-10 py-12 text-primary-foreground lg:flex xl:px-16" aria-label="Portal de pacientes de Clínica Aviva">
        <div className="relative z-10 flex w-full max-w-xl flex-col">
          <Link to="/login" className="w-fit rounded-xl bg-white p-2.5 shadow-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-white/80" aria-label="Clínica Aviva">
            <AvivaLogo alt="" className="h-11 w-auto" />
          </Link>

          <div className="my-auto py-14">
            <span className="mb-5 inline-flex items-center gap-2 rounded-full border border-white/20 bg-white/10 px-3 py-1.5 text-sm font-semibold">
              <HeartPulse className="size-4" aria-hidden="true" />
              Tu salud, en un solo lugar
            </span>
            <h2 className="max-w-lg text-4xl font-semibold leading-tight tracking-tight xl:text-5xl">
              Cuida tu tiempo con una atención más simple.
            </h2>
            <p className="mt-5 max-w-lg text-base leading-7 text-primary-foreground/80 xl:text-lg">
              Agenda consultas, revisa tus citas y gestiona tus pagos con la tranquilidad de estar en Clínica Aviva.
            </p>

            <ul className="mt-9 grid gap-4 text-sm text-primary-foreground/90" aria-label="Beneficios del portal">
              <li className="flex items-center gap-3">
                <span className="grid size-9 place-items-center rounded-lg bg-white/10"><CalendarCheck2 className="size-5" aria-hidden="true" /></span>
                Consulta y organiza tus próximas citas.
              </li>
              <li className="flex items-center gap-3">
                <span className="grid size-9 place-items-center rounded-lg bg-white/10"><ShieldCheck className="size-5" aria-hidden="true" /></span>
                Acceso personal y protegido.
              </li>
            </ul>
          </div>

          <p className="text-xs text-primary-foreground/65">Portal exclusivo para pacientes de Clínica Aviva.</p>
        </div>
        <div className="pointer-events-none absolute -bottom-40 -right-36 size-[28rem] rounded-full border-[5rem] border-white/[0.04]" aria-hidden="true" />
        <div className="pointer-events-none absolute -right-14 top-16 size-48 rounded-full bg-white/[0.04] blur-sm" aria-hidden="true" />
      </section>

      <section className="flex min-h-dvh items-center justify-center bg-muted/30 px-4 py-8 sm:px-8 lg:px-12" aria-labelledby="login-title">
        <div className="w-full max-w-md">
          <Link to="/login" className="mx-auto mb-8 block w-fit rounded-lg focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring lg:hidden" aria-label="Clínica Aviva">
            <AvivaLogo alt="" className="h-12 w-auto" />
          </Link>

          <Card className="gap-0 border-border/80 py-0 shadow-lg shadow-primary/5">
            <CardHeader className="px-5 pb-5 pt-7 sm:px-8 sm:pt-8">
              <span className="mb-3 grid size-11 place-items-center rounded-xl bg-primary/10 text-primary" aria-hidden="true">
                <LockKeyhole className="size-5" />
              </span>
              <h1 id="login-title" className="text-2xl font-semibold tracking-tight sm:text-3xl">Inicia sesión</h1>
              <CardDescription className="pt-1 text-sm sm:text-base">
                Ingresa con la cuenta asociada a tu perfil de paciente.
              </CardDescription>
            </CardHeader>

            <CardContent className="space-y-5 px-5 pb-7 sm:px-8 sm:pb-8">
              {searchParams.get('sesion') === 'expirada' && (
                <AuthAlert>Tu sesión venció. Inicia sesión nuevamente para continuar.</AuthAlert>
              )}
              {searchParams.get('registro') === 'exitoso' && (
                <AuthAlert variant="success">Tu cuenta fue creada. Inicia sesión para continuar.</AuthAlert>
              )}
              {error && <AuthAlert variant="destructive" live>{error}</AuthAlert>}

              <form className="space-y-5" onSubmit={handleSubmit}>
                <div className="space-y-2">
                  <Label htmlFor="login-username">Usuario</Label>
                  <Input
                    id="login-username"
                    name="username"
                    type="text"
                    className="h-12"
                    autoComplete="username"
                    minLength="3"
                    maxLength="50"
                    value={username}
                    onChange={(event) => setUsername(event.target.value)}
                    placeholder="Tu nombre de usuario"
                    required
                    autoFocus
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="login-password">Contraseña</Label>
                  <div className="relative">
                    <Input
                      id="login-password"
                      name="password"
                      type={showPassword ? 'text' : 'password'}
                      className="h-12 pr-12"
                      autoComplete="current-password"
                      minLength="4"
                      maxLength="100"
                      value={password}
                      onChange={(event) => setPassword(event.target.value)}
                      placeholder="Tu contraseña"
                      required
                    />
                    <Button
                      type="button"
                      variant="ghost"
                      size="icon"
                      className="absolute right-1 top-1 size-10 text-muted-foreground"
                      aria-label={showPassword ? 'Ocultar contraseña' : 'Mostrar contraseña'}
                      aria-pressed={showPassword}
                      onClick={() => setShowPassword((current) => !current)}
                    >
                      {showPassword ? <EyeOff className="size-5" aria-hidden="true" /> : <Eye className="size-5" aria-hidden="true" />}
                    </Button>
                  </div>
                </div>

                <Button type="submit" size="lg" className="h-12 w-full" disabled={isSubmitting}>
                  {isSubmitting ? (
                    <>
                      <LoaderCircle className="size-5 animate-spin" aria-hidden="true" />
                      Ingresando…
                    </>
                  ) : (
                    <>
                      Ingresar al portal
                      <ArrowRight className="size-5" aria-hidden="true" />
                    </>
                  )}
                </Button>
              </form>

              <p className="border-t border-border pt-5 text-center text-sm text-muted-foreground">
                ¿Aún no tienes una cuenta?{' '}
                <Link to="/registro" className="font-semibold text-primary underline-offset-4 hover:underline focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring">
                  Regístrate como paciente
                </Link>
              </p>
            </CardContent>
          </Card>

          <p className="mt-6 text-center text-xs leading-5 text-muted-foreground">
            Tus datos están protegidos y solo se usan para brindarte atención.
          </p>
        </div>
      </section>
    </main>
  );
}

function AuthAlert({ variant = 'info', live = false, children }) {
  return (
    <Alert variant={variant} role={live ? 'alert' : 'status'} aria-live={live ? 'assertive' : 'polite'}>
      {variant === 'destructive' ? <CircleAlert aria-hidden="true" /> : <ShieldCheck aria-hidden="true" />}
      <AlertDescription>{children}</AlertDescription>
    </Alert>
  );
}
