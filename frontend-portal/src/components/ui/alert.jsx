import * as React from 'react';
import { cva } from 'class-variance-authority';
import { cn } from '@/lib/utils';

const alertVariants = cva(
  'relative grid w-full grid-cols-[0_1fr] items-start gap-y-1 rounded-xl border px-4 py-3.5 text-sm leading-relaxed has-[>svg]:grid-cols-[1.125rem_1fr] has-[>svg]:gap-x-3 [&>svg]:size-[1.125rem] [&>svg]:translate-y-0.5',
  {
    variants: {
      variant: {
        default: 'border-border bg-card text-card-foreground [&>svg]:text-primary',
        destructive: 'border-destructive/25 bg-destructive/8 text-destructive [&>svg]:text-current',
        success: 'border-success/25 bg-success-soft text-success [&>svg]:text-current',
        warning: 'border-warning/25 bg-warning-soft text-warning [&>svg]:text-current',
        info: 'border-info/25 bg-info-soft text-info [&>svg]:text-current',
      },
    },
    defaultVariants: {
      variant: 'default',
    },
  },
);

const Alert = React.forwardRef(function Alert({ className, role, variant, ...props }, ref) {
  return (
    <div
      ref={ref}
      data-slot="alert"
      role={role ?? (variant === 'destructive' ? 'alert' : 'status')}
      className={cn(alertVariants({ variant }), className)}
      {...props}
    />
  );
});

const AlertTitle = React.forwardRef(function AlertTitle({ className, ...props }, ref) {
  return (
    <div
      ref={ref}
      data-slot="alert-title"
      className={cn('col-start-2 font-semibold leading-none tracking-tight', className)}
      {...props}
    />
  );
});

const AlertDescription = React.forwardRef(function AlertDescription({ className, ...props }, ref) {
  return (
    <div
      ref={ref}
      data-slot="alert-description"
      className={cn('col-start-2 text-sm opacity-90 [&_p]:leading-relaxed', className)}
      {...props}
    />
  );
});

export { Alert, AlertDescription, AlertTitle };
