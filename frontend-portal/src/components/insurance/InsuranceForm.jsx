import { useState } from 'react';
import {
  AlertCircle,
  ChevronDown,
  LoaderCircle,
  ShieldCheck,
} from 'lucide-react';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { getLocalDateInputValue } from '../../utils/dates.js';
import {
  buildInsurancePayload,
  POLICY_RELATIONSHIPS,
  validateInsuranceDates,
} from '../../utils/insurance.js';

const INITIAL_FORM = {
  insuranceId: '',
  policyNumber: '',
  policyHolderName: '',
  relationshipToHolder: 'SELF',
  effectiveDate: '',
  expirationDate: '',
};

const SELECT_CLASS = 'h-11 w-full appearance-none rounded-lg border border-input bg-background px-3 pr-10 text-sm text-foreground shadow-xs outline-none transition-[color,box-shadow,border-color] focus-visible:border-ring focus-visible:ring-2 focus-visible:ring-ring/25 disabled:cursor-not-allowed disabled:opacity-50';

export default function InsuranceForm({ catalog, isSubmitting, error, onSubmit }) {
  const [form, setForm] = useState(INITIAL_FORM);
  const [formError, setFormError] = useState('');
  const today = getLocalDateInputValue();
  const errorMessage = formError || error?.message || '';

  function updateField(event) {
    const { name, value } = event.target;
    setForm((current) => ({ ...current, [name]: value }));
    setFormError('');
  }

  function handleSubmit(event) {
    event.preventDefault();
    const dateError = validateInsuranceDates(form.effectiveDate, form.expirationDate, today);
    if (dateError) {
      setFormError(dateError);
      return;
    }
    onSubmit(buildInsurancePayload(form));
  }

  return (
    <form className="space-y-6" onSubmit={handleSubmit} aria-busy={isSubmitting}>
      <div className="grid gap-5 sm:grid-cols-2">
        <div className="space-y-2 sm:col-span-2">
          <Label htmlFor="insuranceId">Aseguradora o plan</Label>
          <div className="relative">
            <select
              id="insuranceId"
              name="insuranceId"
              className={SELECT_CLASS}
              value={form.insuranceId}
              onChange={updateField}
              required
            >
              <option value="">Selecciona una opción</option>
              {(catalog || []).map((insurance) => (
                <option key={insurance.id} value={insurance.id}>{insurance.name}</option>
              ))}
            </select>
            <ChevronDown className="pointer-events-none absolute right-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" aria-hidden="true" />
          </div>
        </div>

        <div className="space-y-2 sm:col-span-2">
          <Label htmlFor="policyNumber">Número de póliza</Label>
          <Input
            id="policyNumber"
            name="policyNumber"
            className="h-11"
            value={form.policyNumber}
            onChange={updateField}
            minLength="3"
            maxLength="50"
            autoComplete="off"
            required
          />
        </div>

        <div className="space-y-2">
          <Label htmlFor="policyHolderName">
            Nombre del titular <span className="font-normal text-muted-foreground">(opcional)</span>
          </Label>
          <Input
            id="policyHolderName"
            name="policyHolderName"
            className="h-11"
            value={form.policyHolderName}
            onChange={updateField}
            maxLength="100"
            autoComplete="name"
          />
          <p className="text-xs leading-5 text-muted-foreground">Si eres el titular, puedes dejarlo vacío.</p>
        </div>

        <div className="space-y-2">
          <Label htmlFor="relationshipToHolder">Relación con el titular</Label>
          <div className="relative">
            <select
              id="relationshipToHolder"
              name="relationshipToHolder"
              className={SELECT_CLASS}
              value={form.relationshipToHolder}
              onChange={updateField}
              required
            >
              {POLICY_RELATIONSHIPS.map((relationship) => (
                <option key={relationship.value} value={relationship.value}>{relationship.label}</option>
              ))}
            </select>
            <ChevronDown className="pointer-events-none absolute right-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" aria-hidden="true" />
          </div>
        </div>

        <div className="space-y-2">
          <Label htmlFor="effectiveDate">Inicio de vigencia</Label>
          <Input
            id="effectiveDate"
            name="effectiveDate"
            type="date"
            className="h-11"
            value={form.effectiveDate}
            onChange={updateField}
            aria-invalid={Boolean(formError)}
            aria-describedby={formError ? 'insurance-form-error' : undefined}
            required
          />
        </div>

        <div className="space-y-2">
          <Label htmlFor="expirationDate">Fin de vigencia</Label>
          <Input
            id="expirationDate"
            name="expirationDate"
            type="date"
            className="h-11"
            min={today}
            value={form.expirationDate}
            onChange={updateField}
            aria-invalid={Boolean(formError)}
            aria-describedby={formError ? 'insurance-form-error' : undefined}
            required
          />
        </div>
      </div>

      {errorMessage && (
        <Alert id="insurance-form-error" variant="destructive">
          <AlertCircle aria-hidden="true" />
          <AlertTitle>No pudimos guardar el seguro</AlertTitle>
          <AlertDescription>{errorMessage}</AlertDescription>
        </Alert>
      )}

      <Button type="submit" size="lg" className="w-full" disabled={isSubmitting || !catalog?.length}>
        {isSubmitting
          ? <LoaderCircle className="animate-spin" aria-hidden="true" />
          : <ShieldCheck aria-hidden="true" />}
        {isSubmitting ? 'Guardando seguro…' : 'Guardar seguro'}
      </Button>
    </form>
  );
}
