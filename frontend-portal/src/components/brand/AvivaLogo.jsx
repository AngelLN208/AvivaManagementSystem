import avivaLogo from '@/assets/aviva.png';
import { cn } from '@/lib/utils';

const SIZE_CLASSES = {
  sm: 'h-8 w-auto',
  md: 'h-12 w-auto',
  lg: 'h-16 w-auto',
};

export default function AvivaLogo({
  alt = 'Clínica Aviva',
  className,
  imageClassName,
  size = 'md',
  ...props
}) {
  return (
    <span
      className={cn('aviva-logo', SIZE_CLASSES[size] || SIZE_CLASSES.md, className)}
      {...props}
    >
      <img
        src={avivaLogo}
        alt={alt}
        className={cn('aviva-logo__image', imageClassName)}
        draggable="false"
      />
    </span>
  );
}
