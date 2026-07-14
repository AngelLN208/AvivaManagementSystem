import { useEffect, useMemo, useRef, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import {
  BadgeCheck,
  Check,
  CircleAlert,
  Fingerprint,
  Info,
  LoaderCircle,
  Minus,
  ShieldCheck,
  UserRoundPlus,
} from 'lucide-react';
import {
  requestPatientActivation,
  verifyPatientActivationCode,
} from '../api/authApi.js';
import AvivaLogo from '@/components/brand/AvivaLogo.jsx';
import { Alert, AlertDescription } from '@/components/ui/alert.jsx';
import { Button } from '@/components/ui/button.jsx';
import { Card, CardContent, CardHeader } from '@/components/ui/card.jsx';
import { Input } from '@/components/ui/input.jsx';
import { Label } from '@/components/ui/label.jsx';
import { useAuth } from '../hooks/useAuth.js';
import { cn } from '@/lib/utils.js';
import { getLocalDateInputValue } from '../utils/dates.js';
import {
  isValidDni,
  getActivationGrant,
  isInvalidActivationGrantError,
  normalizeActivationStep,
  normalizeDni,
  normalizeVerificationCode,
  REGISTRATION_STEP,
} from '../utils/registration.js';

const INITIAL_FORM = {
  firstName: '',
  lastName: '',
  dateOfBirth: '',
  gender: '',
  phone: '',
  email: '',
  address: '',
  username: '',
  password: '',
  confirmPassword: '',
};

const STEP_COPY = {
  [REGISTRATION_STEP.DNI]: {
    eyebrow: 'Empecemos por tu identidad',
    title: 'Crea o activa tu cuenta',
    description: 'Ingresa tu DNI para saber si ya tienes un perfil registrado en la clínica.',
  },
  [REGISTRATION_STEP.NEW_PATIENT]: {
    eyebrow: 'Nuevo paciente',
    title: 'Completa tus datos',
    description: 'Crearemos tu perfil de paciente y tus credenciales de acceso en un solo paso.',
  },
  [REGISTRATION_STEP.VERIFICATION_REQUIRED]: {
    eyebrow: 'Activa tu acceso',
    title: 'Verifica que eres tú',
    description: 'Tu perfil clínico ya existe. Solo crearemos una cuenta para que puedas ingresar.',
  },
  [REGISTRATION_STEP.CREATE_ACCESS]: {
    eyebrow: 'Correo verificado',
    title: 'Crea tus datos de acceso',
    description: 'La verificación fue correcta. Ahora elige el usuario y la contraseña de tu nueva cuenta.',
  },
  [REGISTRATION_STEP.ACCOUNT_EXISTS]: {
    eyebrow: 'Cuenta encontrada',
    title: 'Ya tienes acceso al portal',
    description: 'No crearemos otra cuenta ni duplicaremos tu perfil de paciente.',
  },
  [REGISTRATION_STEP.CONTACT_STAFF]: {
    eyebrow: 'Necesitamos ayudarte',
    title: 'Comunícate con la clínica',
    description: 'El personal debe revisar tu perfil antes de habilitar el acceso al portal.',
  },
};

const FIELD_CLASS = 'space-y-2';
const FIELDSET_CLASS = 'space-y-5 rounded-xl border border-border bg-muted/20 p-4 sm:p-5';
const LEGEND_CLASS = 'px-1 text-base font-semibold text-foreground';
const SELECT_CLASS = 'flex h-11 w-full rounded-lg border border-input bg-background px-3 py-2 text-sm text-foreground shadow-xs outline-none transition focus-visible:border-ring focus-visible:ring-2 focus-visible:ring-ring/25 aria-invalid:border-destructive aria-invalid:ring-2 aria-invalid:ring-destructive/20';

export default function RegisterPage() {
  const { register, completeActivation } = useAuth();
  const navigate = useNavigate();
  const stepHeadingRef = useRef(null);
  const [step, setStep] = useState(REGISTRATION_STEP.DNI);
  const [dni, setDni] = useState('');
  const [form, setForm] = useState(INITIAL_FORM);
  const [verificationCode, setVerificationCode] = useState('');
  const [activation, setActivation] = useState(null);
  const [activationGrant, setActivationGrant] = useState(null);
  const [codeExpired, setCodeExpired] = useState(false);
  const [error, setError] = useState('');
  const [fieldErrors, setFieldErrors] = useState({});
  const [isCheckingDni, setIsCheckingDni] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const maxBirthDate = useMemo(() => getLocalDateInputValue(), []);
  const copy = STEP_COPY[step] || STEP_COPY[REGISTRATION_STEP.DNI];

  useEffect(() => {
    if (step !== REGISTRATION_STEP.DNI) stepHeadingRef.current?.focus();
  }, [step]);

  useEffect(() => {
    if (!activation?.expiresAt) return undefined;
    const remaining = activation.expiresAt - Date.now();
    const timerId = window.setTimeout(
      () => setCodeExpired(true),
      Math.max(0, remaining),
    );
    return () => window.clearTimeout(timerId);
  }, [activation]);

  const errorAttributes = (name, helperId) => {
    const describedBy = [
      helperId,
      fieldErrors[name] ? `${name}-error` : '',
    ].filter(Boolean).join(' ');

    return {
      'aria-invalid': fieldErrors[name] ? 'true' : undefined,
      'aria-describedby': describedBy || undefined,
    };
  };

  const fieldError = (name) => fieldErrors[name]
    ? <span className="flex items-start gap-1.5 text-sm font-medium text-destructive" id={`${name}-error`} role="alert"><CircleAlert className="mt-0.5 size-4 shrink-0" aria-hidden="true" />{fieldErrors[name]}</span>
    : null;

  const focusField = (name) => {
    window.requestAnimationFrame(() => document.getElementById(name)?.focus());
  };

  function updateField(event) {
    const { name, value } = event.target;
    setForm((current) => ({ ...current, [name]: value }));
    setFieldErrors((current) => ({ ...current, [name]: '' }));
  }

  function clearMessages() {
    setError('');
    setFieldErrors({});
  }

  function changeDni() {
    clearMessages();
    setActivation(null);
    setActivationGrant(null);
    setVerificationCode('');
    setCodeExpired(false);
    setStep(REGISTRATION_STEP.DNI);
    window.requestAnimationFrame(() => document.getElementById('dni')?.focus());
  }

  async function resolveDni() {
    clearMessages();
    // Toda solicitud nueva invalida cualquier grant OTP que solo vivía en React.
    setActivation(null);
    setActivationGrant(null);
    setVerificationCode('');
    setCodeExpired(false);
    if (!isValidDni(dni)) {
      setFieldErrors({ dni: 'El DNI debe contener entre 8 y 12 dígitos.' });
      focusField('dni');
      return;
    }

    setIsCheckingDni(true);
    try {
      const response = await requestPatientActivation(dni);
      const nextStep = normalizeActivationStep(response);
      if (!Object.values(REGISTRATION_STEP).includes(nextStep) || nextStep === REGISTRATION_STEP.DNI) {
        throw new Error('No pudimos determinar el siguiente paso del registro. Intenta nuevamente.');
      }

      if (nextStep === REGISTRATION_STEP.VERIFICATION_REQUIRED) {
        if (!response?.challengeId) {
          throw new Error('No recibimos un código de verificación válido. Solicítalo nuevamente.');
        }
        const expiresInSeconds = Number(response.expiresInSeconds) || 600;
        setActivation({
          challengeId: response.challengeId,
          deliveryHint: response.deliveryHint || '',
          expiresAt: Date.now() + expiresInSeconds * 1000,
        });
        setCodeExpired(false);
      }
      setStep(nextStep);
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setIsCheckingDni(false);
    }
  }

  async function handleDniSubmit(event) {
    event.preventDefault();
    await resolveDni();
  }

  function validateAccessFields() {
    if (form.password !== form.confirmPassword) {
      setFieldErrors({ confirmPassword: 'Las contraseñas no coinciden.' });
      focusField('confirmPassword');
      return false;
    }
    return true;
  }

  async function handleNewPatientSubmit(event) {
    event.preventDefault();
    clearMessages();
    if (!validateAccessFields()) return;
    if (form.dateOfBirth > maxBirthDate) {
      setFieldErrors({ dateOfBirth: 'La fecha de nacimiento no puede ser futura.' });
      focusField('dateOfBirth');
      return;
    }

    const { confirmPassword: _confirmPassword, ...payload } = form;
    void _confirmPassword;
    setIsSubmitting(true);
    try {
      await register({
        ...payload,
        dni,
        firstName: payload.firstName.trim(),
        lastName: payload.lastName.trim(),
        username: payload.username.trim(),
        email: payload.email.trim(),
        address: payload.address.trim() || null,
      });
      navigate('/seguro?onboarding=1', { replace: true });
    } catch (requestError) {
      if (requestError.accountCreated) {
        navigate('/login?registro=exitoso', { replace: true });
        return;
      }
      setError(requestError.message);
      const nextFieldErrors = requestError.fieldErrors || {};
      setFieldErrors(nextFieldErrors);
      const firstInvalidField = Object.keys(nextFieldErrors)[0];
      if (firstInvalidField) focusField(firstInvalidField);
    } finally {
      setIsSubmitting(false);
    }
  }

  async function handleCodeSubmit(event) {
    event.preventDefault();
    clearMessages();
    if (codeExpired) {
      setError('El código venció. Solicita uno nuevo para continuar.');
      return;
    }
    if (!activation?.challengeId) {
      setError('Solicita un código nuevo antes de continuar.');
      return;
    }
    if (verificationCode.length !== 6) {
      setFieldErrors({ code: 'Ingresa el código de 6 dígitos.' });
      focusField('activation-code');
      return;
    }
    setIsSubmitting(true);
    try {
      const challengeId = activation.challengeId;
      const response = await verifyPatientActivationCode({
        challengeId,
        code: verificationCode,
      });
      const grant = getActivationGrant(response, challengeId);
      if (!grant) {
        throw new Error('No recibimos una autorización válida para crear tu acceso. Solicita otro código.');
      }
      setActivationGrant(grant);
      setActivation(null);
      setVerificationCode('');
      setCodeExpired(false);
      setStep(REGISTRATION_STEP.CREATE_ACCESS);
    } catch (requestError) {
      if (requestError.status === 410 || requestError.code?.includes('EXPIRED')) {
        setCodeExpired(true);
      }
      setError(requestError.message);
      const nextFieldErrors = requestError.fieldErrors || {};
      setFieldErrors(nextFieldErrors);
      const firstInvalidField = Object.keys(nextFieldErrors)[0];
      if (firstInvalidField) focusField(firstInvalidField === 'code' ? 'activation-code' : firstInvalidField);
    } finally {
      setIsSubmitting(false);
    }
  }

  async function handleCompleteActivation(event) {
    event.preventDefault();
    clearMessages();
    if (!activationGrant?.activationToken || !activationGrant?.challengeId) {
      setError('La verificación ya no es válida. Solicita un código nuevo.');
      setActivation(null);
      setActivationGrant(null);
      setVerificationCode('');
      setCodeExpired(false);
      setStep(REGISTRATION_STEP.VERIFICATION_REQUIRED);
      return;
    }
    if (!validateAccessFields()) return;

    setIsSubmitting(true);
    try {
      await completeActivation({
        activationToken: activationGrant.activationToken,
        challengeId: activationGrant.challengeId,
        username: form.username.trim(),
        password: form.password,
      });
      // Destruir el grant antes de abandonar el registro.
      setActivationGrant(null);
      navigate('/seguro?onboarding=1', { replace: true });
    } catch (requestError) {
      const grantInvalid = isInvalidActivationGrantError(requestError);
      if (grantInvalid) {
        setActivation(null);
        setActivationGrant(null);
        setVerificationCode('');
        setStep(REGISTRATION_STEP.VERIFICATION_REQUIRED);
        setCodeExpired(false);
        setError('La verificación ya no es válida. Solicita un código nuevo para continuar.');
      } else {
        setError(requestError.message);
      }
      const nextFieldErrors = requestError.fieldErrors || {};
      setFieldErrors(nextFieldErrors);
      const firstInvalidField = Object.keys(nextFieldErrors)[0];
      if (firstInvalidField && firstInvalidField !== 'activationToken') focusField(firstInvalidField);
    } finally {
      setIsSubmitting(false);
    }
  }

  async function resendCode() {
    // El reenvío revoca el grant local aunque la llamada de red falle.
    setActivation(null);
    setActivationGrant(null);
    setVerificationCode('');
    setCodeExpired(false);
    setStep(REGISTRATION_STEP.VERIFICATION_REQUIRED);
    await resolveDni();
    window.requestAnimationFrame(() => document.getElementById('activation-code')?.focus());
  }

  return (
    <main className="min-h-dvh bg-muted/30 text-foreground">
      <header className="border-b border-border/80 bg-background">
        <div className="mx-auto flex h-16 max-w-7xl items-center justify-between px-4 sm:h-20 sm:px-6 lg:px-8">
          <Link to="/login" className="rounded-lg focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring" aria-label="Clínica Aviva, volver al acceso">
            <AvivaLogo alt="" className="h-10 w-auto sm:h-11" />
          </Link>
          <p className="text-sm text-muted-foreground">
            <span className="hidden sm:inline">¿Ya tienes cuenta? </span>
            <Link to="/login" className="font-semibold text-primary underline-offset-4 hover:underline focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring">Inicia sesión</Link>
          </p>
        </div>
      </header>

      <div className="mx-auto grid w-full max-w-7xl items-start gap-8 px-4 py-6 sm:px-6 sm:py-10 lg:grid-cols-[minmax(15rem,0.7fr)_minmax(0,2fr)] lg:px-8 lg:py-12">
        <aside className="sticky top-8 hidden rounded-2xl bg-primary p-7 text-primary-foreground shadow-sm lg:block" aria-label="Información del registro">
          <span className="grid size-11 place-items-center rounded-xl bg-white/10" aria-hidden="true"><ShieldCheck className="size-6" /></span>
          <h2 className="mt-6 text-2xl font-semibold tracking-tight">Tu acceso a Aviva</h2>
          <p className="mt-3 text-sm leading-6 text-primary-foreground/80">Validaremos tu identidad antes de crear una cuenta. Tu información clínica seguirá protegida.</p>
          <ul className="mt-7 space-y-4 text-sm text-primary-foreground/90">
            <li className="flex gap-3"><Check className="mt-0.5 size-5 shrink-0" aria-hidden="true" /><span>Si tu paciente ya existe, solo crearemos el acceso.</span></li>
            <li className="flex gap-3"><Check className="mt-0.5 size-5 shrink-0" aria-hidden="true" /><span>No duplicaremos tu perfil, citas ni seguros.</span></li>
            <li className="flex gap-3"><Check className="mt-0.5 size-5 shrink-0" aria-hidden="true" /><span>Agregar un seguro será opcional al finalizar.</span></li>
          </ul>
        </aside>

        <Card className="w-full gap-0 overflow-hidden border-border/80 py-0 shadow-lg shadow-primary/5">
          <CardHeader className="border-b border-border/70 px-5 pb-6 pt-7 sm:px-8 sm:pb-7 sm:pt-8">
            <span className="mb-3 grid size-11 place-items-center rounded-xl bg-primary/10 text-primary" aria-hidden="true">
              {step === REGISTRATION_STEP.DNI ? <Fingerprint className="size-6" /> : <UserRoundPlus className="size-6" />}
            </span>
            <p className="text-sm font-semibold text-primary">{copy.eyebrow}</p>
            <h1 className="text-2xl font-semibold tracking-tight sm:text-3xl" ref={stepHeadingRef} tabIndex={step === REGISTRATION_STEP.DNI ? undefined : -1}>{copy.title}</h1>
            <p className="max-w-2xl text-sm leading-6 text-muted-foreground sm:text-base">{copy.description}</p>
          </CardHeader>

          <CardContent className="space-y-7 px-5 py-6 sm:px-8 sm:py-8">

        <RegistrationProgress step={step} />

        {error && <RegistrationAlert variant="destructive" live>{error}</RegistrationAlert>}

        {step === REGISTRATION_STEP.DNI && (
          <form className="mx-auto w-full max-w-lg space-y-6" onSubmit={handleDniSubmit}>
            <div className={FIELD_CLASS}>
              <Label htmlFor="dni">DNI o documento de identidad</Label>
              <Input
                id="dni"
                name="dni"
                className="h-12 text-base"
                value={dni}
                onChange={(event) => {
                  setDni(normalizeDni(event.target.value));
                  setFieldErrors((current) => ({ ...current, dni: '' }));
                }}
                inputMode="numeric"
                pattern="[0-9]{8,12}"
                minLength="8"
                maxLength="12"
                autoComplete="off"
                placeholder="8 a 12 dígitos"
                autoFocus
                required
                {...errorAttributes('dni', 'dni-help')}
              />
              <small className="block text-sm leading-5 text-muted-foreground" id="dni-help">No mostraremos tus datos personales durante esta búsqueda.</small>
              {fieldError('dni')}
            </div>
            <Button type="submit" size="lg" className="h-12 w-full" disabled={isCheckingDni}>
              {isCheckingDni && <LoaderCircle className="size-5 animate-spin" aria-hidden="true" />}
              {isCheckingDni ? 'Comprobando…' : 'Continuar'}
            </Button>
          </form>
        )}

        {step === REGISTRATION_STEP.NEW_PATIENT && (
          <form className="space-y-6" onSubmit={handleNewPatientSubmit}>
            <DniSummary dni={dni} onChange={changeDni} />
            <fieldset className={FIELDSET_CLASS}>
              <legend className={LEGEND_CLASS}>Datos personales</legend>
              <div className="grid gap-5 sm:grid-cols-2">
                <Field label="Nombres" name="firstName" form={form} updateField={updateField} errorAttributes={errorAttributes} fieldError={fieldError} autoComplete="given-name" minLength="2" maxLength="50" />
                <Field label="Apellidos" name="lastName" form={form} updateField={updateField} errorAttributes={errorAttributes} fieldError={fieldError} autoComplete="family-name" minLength="2" maxLength="50" />
                <div className={FIELD_CLASS}>
                  <Label htmlFor="dateOfBirth">Fecha de nacimiento</Label>
                  <Input className="h-11" id="dateOfBirth" name="dateOfBirth" type="date" max={maxBirthDate} value={form.dateOfBirth} onChange={updateField} autoComplete="bday" required {...errorAttributes('dateOfBirth')} />
                  {fieldError('dateOfBirth')}
                </div>
                <div className={FIELD_CLASS}>
                  <Label htmlFor="gender">Género</Label>
                  <select className={SELECT_CLASS} id="gender" name="gender" value={form.gender} onChange={updateField} required {...errorAttributes('gender')}>
                    <option value="">Selecciona una opción</option>
                    <option value="FEMALE">Femenino</option>
                    <option value="MALE">Masculino</option>
                    <option value="OTHER">Otro / Prefiero otra denominación</option>
                  </select>
                  {fieldError('gender')}
                </div>
                <Field label="Teléfono" name="phone" type="tel" form={form} updateField={updateField} errorAttributes={errorAttributes} fieldError={fieldError} autoComplete="tel" minLength="7" maxLength="15" pattern="[0-9+\-\s]{7,15}" placeholder="Ej. 999 999 999" />
                <Field label="Correo electrónico" name="email" type="email" form={form} updateField={updateField} errorAttributes={errorAttributes} fieldError={fieldError} autoComplete="email" placeholder="nombre@correo.com" />
                <Field label="Dirección" optional name="address" wide form={form} updateField={updateField} errorAttributes={errorAttributes} fieldError={fieldError} autoComplete="street-address" required={false} />
              </div>
            </fieldset>
            <AccessFields form={form} updateField={updateField} errorAttributes={errorAttributes} fieldError={fieldError} />
            <Button type="submit" size="lg" className="h-12 w-full" disabled={isSubmitting}>
              {isSubmitting && <LoaderCircle className="size-5 animate-spin" aria-hidden="true" />}
              {isSubmitting ? 'Creando tu cuenta…' : 'Crear cuenta y continuar'}
            </Button>
          </form>
        )}

        {step === REGISTRATION_STEP.VERIFICATION_REQUIRED && (
          <form className="space-y-6" onSubmit={handleCodeSubmit}>
            <DniSummary dni={dni} onChange={changeDni} />
            <RegistrationAlert>
              {activation?.challengeId
                ? <>Enviamos un código al correo registrado{activation.deliveryHint ? ` (${activation.deliveryHint})` : ''}. En esta pantalla solo verificaremos el código.</>
                : <>Solicita un código nuevo para volver a verificar tu identidad.</>}
            </RegistrationAlert>
            {codeExpired && <RegistrationAlert variant="destructive" live>El código venció. Solicita uno nuevo.</RegistrationAlert>}
            <fieldset className={FIELDSET_CLASS}>
              <legend className={LEGEND_CLASS}>Verificación</legend>
              <div className="mx-auto max-w-sm space-y-2 text-center">
                <Label className="justify-center" htmlFor="activation-code">Código de 6 dígitos</Label>
                <Input
                  id="activation-code"
                  name="code"
                  className="h-14 text-center text-xl font-semibold tracking-[0.35em]"
                  value={verificationCode}
                  onChange={(event) => {
                    setVerificationCode(normalizeVerificationCode(event.target.value));
                    setFieldErrors((current) => ({ ...current, code: '' }));
                  }}
                  inputMode="numeric"
                  autoComplete="one-time-code"
                  pattern="[0-9]{6}"
                  minLength="6"
                  maxLength="6"
                  disabled={!activation?.challengeId || codeExpired || isSubmitting}
                  required
                  aria-invalid={fieldErrors.code ? 'true' : undefined}
                  aria-describedby={fieldErrors.code ? 'code-help code-error' : 'code-help'}
                />
                <small className="block text-sm leading-5 text-muted-foreground" id="code-help">El código tiene vigencia limitada y solo puede usarse una vez.</small>
                {fieldError('code')}
              </div>
              <Button type="button" variant="link" className="mx-auto flex h-auto p-0" onClick={resendCode} disabled={isCheckingDni || isSubmitting}>
                {isCheckingDni ? 'Enviando…' : 'Enviar un código nuevo'}
              </Button>
            </fieldset>
            <Button type="submit" size="lg" className="h-12 w-full" disabled={isSubmitting || codeExpired || !activation?.challengeId}>
              {isSubmitting && <LoaderCircle className="size-5 animate-spin" aria-hidden="true" />}
              {isSubmitting ? 'Verificando código…' : 'Verificar código'}
            </Button>
          </form>
        )}

        {step === REGISTRATION_STEP.CREATE_ACCESS && (
          <form className="space-y-6" onSubmit={handleCompleteActivation}>
            <DniSummary dni={dni} onChange={changeDni} />
            <RegistrationAlert variant="success">
              Tu correo fue verificado. Elige tus credenciales; no modificaremos el perfil, las citas ni los seguros que ya existen.
            </RegistrationAlert>
            <AccessFields form={form} updateField={updateField} errorAttributes={errorAttributes} fieldError={fieldError} />
            <div className="grid gap-3 sm:grid-cols-2">
              <Button type="submit" size="lg" className="h-12" disabled={isSubmitting || !activationGrant?.activationToken || !activationGrant?.challengeId}>
                {isSubmitting && <LoaderCircle className="size-5 animate-spin" aria-hidden="true" />}
                {isSubmitting ? 'Creando tu acceso…' : 'Crear acceso y continuar'}
              </Button>
              <Button type="button" size="lg" variant="outline" className="h-12" onClick={resendCode} disabled={isCheckingDni || isSubmitting}>
                {isCheckingDni ? 'Enviando…' : 'Verificar con un código nuevo'}
              </Button>
            </div>
          </form>
        )}

        {step === REGISTRATION_STEP.ACCOUNT_EXISTS && (
          <div className="mx-auto max-w-xl py-4 text-center">
            <span className="mx-auto grid size-16 place-items-center rounded-full bg-success-soft text-success" aria-hidden="true"><BadgeCheck className="size-8" /></span>
            <p className="mx-auto mt-5 max-w-md leading-7 text-muted-foreground">El DNI ingresado ya tiene una cuenta. Usa tus credenciales habituales para acceder.</p>
            <div className="mt-6 flex flex-col justify-center gap-3 sm:flex-row">
              <Button asChild size="lg"><Link to="/login">Ir a iniciar sesión</Link></Button>
              <Button type="button" size="lg" variant="outline" onClick={changeDni}>Usar otro DNI</Button>
            </div>
          </div>
        )}

        {step === REGISTRATION_STEP.CONTACT_STAFF && (
          <div className="mx-auto max-w-xl py-4 text-center">
            <span className="mx-auto grid size-16 place-items-center rounded-full bg-warning-soft text-warning" aria-hidden="true"><CircleAlert className="size-8" /></span>
            <p className="mx-auto mt-5 max-w-md leading-7 text-muted-foreground">Por seguridad no podemos activar este perfil en línea. Comunícate con recepción para validar tus datos.</p>
            <Button type="button" size="lg" variant="outline" className="mt-6" onClick={changeDni}>Usar otro DNI</Button>
          </div>
        )}
          </CardContent>
        </Card>
      </div>
    </main>
  );
}

function RegistrationProgress({ step }) {
  if (step === REGISTRATION_STEP.ACCOUNT_EXISTS || step === REGISTRATION_STEP.CONTACT_STAFF) {
    return null;
  }

  const currentIndex = {
    [REGISTRATION_STEP.DNI]: 0,
    [REGISTRATION_STEP.VERIFICATION_REQUIRED]: 1,
    [REGISTRATION_STEP.NEW_PATIENT]: 2,
    [REGISTRATION_STEP.CREATE_ACCESS]: 2,
    [REGISTRATION_STEP.ACCOUNT_EXISTS]: 0,
    [REGISTRATION_STEP.CONTACT_STAFF]: 0,
  }[step] ?? 0;
  const isNewPatient = step === REGISTRATION_STEP.NEW_PATIENT;
  const items = [
    { label: 'Identidad' },
    { label: isNewPatient ? 'No requerido' : 'Código' },
    { label: 'Acceso' },
    { label: 'Seguro' },
  ];

  return (
    <div className="relative">
      <span className="absolute left-[12.5%] right-[12.5%] top-4 h-px bg-border" aria-hidden="true" />
      <ol className="relative grid grid-cols-4 gap-1" aria-label="Progreso del registro">
        {items.map((item, index) => {
          const skipped = isNewPatient && index === 1;
          const current = index === currentIndex;
          const completed = index < currentIndex && !skipped;
          return (
            <li key={item.label} className="relative flex min-w-0 flex-col items-center gap-2 text-center" aria-current={current ? 'step' : undefined}>
              <span
                className={cn(
                  'relative z-10 grid size-8 place-items-center rounded-full border bg-background text-xs font-bold text-muted-foreground',
                  completed && 'border-primary bg-primary text-primary-foreground',
                  current && 'border-2 border-primary bg-primary/10 text-primary',
                  skipped && 'border-dashed bg-muted text-muted-foreground',
                )}
                aria-hidden="true"
              >
                {completed ? <Check className="size-4" /> : skipped ? <Minus className="size-4" /> : index + 1}
              </span>
              <span className={cn('text-[0.68rem] font-medium leading-4 text-muted-foreground sm:text-xs', current && 'font-semibold text-foreground')}>
                {item.label}{index === 3 && <span className="hidden sm:inline"> opcional</span>}
              </span>
            </li>
          );
        })}
      </ol>
    </div>
  );
}

function DniSummary({ dni, onChange }) {
  return (
    <div className="flex flex-wrap items-center justify-between gap-3 rounded-xl border border-border bg-muted/50 px-4 py-3">
      <span>
        <small className="block text-xs text-muted-foreground">Documento ingresado</small>
        <strong className="block text-sm font-semibold tracking-wide text-foreground">DNI {dni}</strong>
      </span>
      <Button type="button" variant="link" className="h-auto p-0" onClick={onChange}>Cambiar DNI</Button>
    </div>
  );
}

function AccessFields({ form, updateField, errorAttributes, fieldError }) {
  return (
    <fieldset className={FIELDSET_CLASS}>
      <legend className={LEGEND_CLASS}>Datos de acceso</legend>
      <div className="grid gap-5 sm:grid-cols-2">
        <Field label="Nombre de usuario" name="username" wide form={form} updateField={updateField} errorAttributes={errorAttributes} fieldError={fieldError} autoComplete="username" minLength="3" maxLength="50" placeholder="Elige un usuario fácil de recordar" />
        <div className={FIELD_CLASS}>
          <Label htmlFor="password">Contraseña</Label>
          <Input className="h-11" id="password" name="password" type="password" value={form.password} onChange={updateField} minLength="8" maxLength="100" autoComplete="new-password" required {...errorAttributes('password', 'password-help')} />
          <small className="block text-sm text-muted-foreground" id="password-help">Mínimo 8 caracteres.</small>
          {fieldError('password')}
        </div>
        <div className={FIELD_CLASS}>
          <Label htmlFor="confirmPassword">Repite la contraseña</Label>
          <Input className="h-11" id="confirmPassword" name="confirmPassword" type="password" value={form.confirmPassword} onChange={updateField} minLength="8" maxLength="100" autoComplete="new-password" required {...errorAttributes('confirmPassword')} />
          {fieldError('confirmPassword')}
        </div>
      </div>
    </fieldset>
  );
}

function Field({
  label,
  name,
  type = 'text',
  wide = false,
  optional = false,
  form,
  updateField,
  errorAttributes,
  fieldError,
  required = true,
  ...inputProps
}) {
  return (
    <div className={cn(FIELD_CLASS, wide && 'sm:col-span-2')}>
      <Label htmlFor={name}>{label} {optional && <span className="font-normal text-muted-foreground">(opcional)</span>}</Label>
      <Input className="h-11" id={name} name={name} type={type} value={form[name]} onChange={updateField} required={required} {...inputProps} {...errorAttributes(name)} />
      {fieldError(name)}
    </div>
  );
}

function RegistrationAlert({ variant = 'info', live = false, children }) {
  const Icon = variant === 'destructive'
    ? CircleAlert
    : variant === 'success'
      ? BadgeCheck
      : Info;

  return (
    <Alert variant={variant} role={live ? 'alert' : 'status'} aria-live={live ? 'assertive' : 'polite'}>
      <Icon aria-hidden="true" />
      <AlertDescription>{children}</AlertDescription>
    </Alert>
  );
}
