import {
  CalendarRange,
  FileText,
  LoaderCircle,
  ShieldCheck,
  Trash2,
  UserRound,
  UsersRound,
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
import { formatShortDate } from '../../utils/dates.js';
import { POLICY_RELATIONSHIPS } from '../../utils/insurance.js';

export default function InsuranceSummary({ insurance, onDelete, isDeleting }) {
  const relationship = POLICY_RELATIONSHIPS.find(
    (item) => item.value === insurance.relationshipToHolder,
  )?.label || insurance.relationshipToHolder || 'No indicada';

  return (
    <article>
      <Card className="overflow-hidden border-primary/15">
        <CardHeader className="gap-4 border-b border-border bg-gradient-to-r from-primary/10 via-primary/5 to-transparent pb-6 sm:grid-cols-[auto_1fr_auto] sm:items-center">
          <span className="grid size-14 place-items-center rounded-2xl bg-primary text-primary-foreground shadow-sm" aria-hidden="true">
            <ShieldCheck className="size-7" />
          </span>
          <div>
            <p className="mb-1 text-xs font-bold uppercase tracking-[0.16em] text-primary">Seguro principal</p>
            <CardTitle asChild>
              <h2 className="text-2xl font-semibold text-foreground">{insurance.insuranceName}</h2>
            </CardTitle>
          </div>
          <Badge variant="outline" className="w-fit border-emerald-200 bg-emerald-50 px-3 py-1 text-emerald-800 dark:border-emerald-900 dark:bg-emerald-950/50 dark:text-emerald-200">
            <ShieldCheck aria-hidden="true" />
            Activo
          </Badge>
        </CardHeader>

        <CardContent className="py-6">
          <dl className="grid gap-3 sm:grid-cols-2">
            <InsuranceDetail icon={FileText} label="Número de póliza" value={insurance.policyNumber} />
            <InsuranceDetail icon={UserRound} label="Titular" value={insurance.policyHolderName || 'Paciente'} />
            <InsuranceDetail icon={UsersRound} label="Relación" value={relationship} />
            <InsuranceDetail
              icon={CalendarRange}
              label="Vigencia"
              value={formatShortDate(insurance.effectiveDate) + ' – ' + formatShortDate(insurance.expirationDate)}
            />
          </dl>
        </CardContent>

        <CardFooter className="border-t border-border bg-muted/20 py-4">
          <Button
            type="button"
            variant="outline"
            className="border-destructive/30 text-destructive hover:border-destructive/50 hover:bg-destructive/10 hover:text-destructive"
            onClick={onDelete}
            disabled={isDeleting}
          >
            {isDeleting
              ? <LoaderCircle className="animate-spin" aria-hidden="true" />
              : <Trash2 aria-hidden="true" />}
            {isDeleting ? 'Retirando seguro…' : 'Retirar seguro'}
          </Button>
        </CardFooter>
      </Card>
    </article>
  );
}

function InsuranceDetail({ icon: Icon, label, value }) {
  return (
    <div className="rounded-xl border border-border bg-muted/30 p-4">
      <dt className="flex items-center gap-2 text-xs font-bold uppercase tracking-[0.1em] text-muted-foreground">
        <Icon className="size-4 text-primary" aria-hidden="true" />
        {label}
      </dt>
      <dd className="mt-2 font-semibold text-foreground">{value}</dd>
    </div>
  );
}
