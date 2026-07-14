import { APPOINTMENT_STATUS } from '../../utils/appointments.js';

export default function StatusBadge({ status }) {
  const metadata = APPOINTMENT_STATUS[status] || { label: status || 'Sin estado', tone: 'neutral' };
  return <span className={`status-badge status-badge--${metadata.tone}`}>{metadata.label}</span>;
}
