import { APPOINTMENT_STATUS } from '../../utils/appointments.js';
import { Badge } from './badge.jsx';

const TONE_VARIANTS = {
  success: 'success',
  warning: 'warning',
  danger: 'destructive',
  info: 'info',
  neutral: 'outline',
};

export default function StatusBadge({ status }) {
  const metadata = APPOINTMENT_STATUS[status] || { label: status || 'Sin estado', tone: 'neutral' };
  return <Badge variant={TONE_VARIANTS[metadata.tone] || 'outline'}>{metadata.label}</Badge>;
}
