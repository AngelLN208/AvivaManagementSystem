import {
  Bell,
  CalendarDays,
  CreditCard,
  LayoutDashboard,
  ShieldCheck,
  Stethoscope,
} from 'lucide-react';

export const PORTAL_NAVIGATION = [
  { to: '/', label: 'Resumen', icon: LayoutDashboard, end: true },
  { to: '/notificaciones', label: 'Notificaciones', icon: Bell },
  { to: '/citas', label: 'Citas', icon: CalendarDays },
  { to: '/pagos', label: 'Pagos', icon: CreditCard },
  { to: '/seguro', label: 'Seguro', icon: ShieldCheck },
  { to: '/medicos', label: 'Médicos', icon: Stethoscope },
];

export const PAGE_TITLES = {
  '/': 'Resumen',
  '/notificaciones': 'Notificaciones',
  '/citas': 'Mis citas',
  '/agendar': 'Agendar cita',
  '/pagos': 'Mis pagos y constancias',
  '/seguro': 'Mi seguro',
  '/medicos': 'Médicos',
};
