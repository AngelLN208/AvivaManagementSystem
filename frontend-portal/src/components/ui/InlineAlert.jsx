import { AlertCircle, CheckCircle2, Info, X } from 'lucide-react';
import { Alert, AlertDescription } from './alert.jsx';
import { Button } from './button.jsx';

const TONE_CONFIG = {
  danger: { variant: 'destructive', Icon: AlertCircle },
  success: { variant: 'success', Icon: CheckCircle2 },
  warning: { variant: 'warning', Icon: AlertCircle },
  info: { variant: 'info', Icon: Info },
};

export default function InlineAlert({ tone = 'info', children, onDismiss }) {
  const { variant, Icon } = TONE_CONFIG[tone] || TONE_CONFIG.info;

  return (
    <Alert
      variant={variant}
      role={tone === 'danger' ? 'alert' : 'status'}
      aria-live="polite"
      className={onDismiss ? 'grid-cols-[1.125rem_1fr_auto]' : undefined}
    >
      <Icon aria-hidden="true" />
      <AlertDescription>{children}</AlertDescription>
      {onDismiss && (
        <Button
          type="button"
          variant="ghost"
          size="icon-sm"
          className="col-start-3 row-start-1 -mr-2 -mt-1"
          aria-label="Cerrar mensaje"
          onClick={onDismiss}
        >
          <X className="size-4" />
        </Button>
      )}
    </Alert>
  );
}
