import { NavLink, Link } from 'react-router-dom';
import {
  CalendarPlus,
  LogOut,
  Moon,
  Sun,
} from 'lucide-react';
import AvivaLogo from '@/components/brand/AvivaLogo.jsx';
import { Button } from '@/components/ui/button.jsx';
import { cn } from '@/lib/utils.js';
import { PORTAL_NAVIGATION } from './portalNavigation.js';

export function PortalNavigationLinks({ onNavigate, mobile = false, notificationCount = 0 }) {
  return PORTAL_NAVIGATION.map(({ to, label, icon: Icon, end }) => (
    <NavLink
      key={to}
      to={to}
      end={end}
      onClick={onNavigate}
      className={({ isActive }) => cn(
        'group flex min-h-11 items-center gap-3 rounded-xl px-3 text-sm font-semibold transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 focus-visible:ring-offset-background',
        isActive
          ? 'bg-primary/10 text-primary'
          : 'text-muted-foreground hover:bg-muted hover:text-foreground',
        mobile && 'min-h-12 text-base',
      )}
    >
      {({ isActive }) => (
        <>
          <Icon
            className={cn(
              'size-5 shrink-0 transition-colors',
              isActive ? 'text-primary' : 'text-muted-foreground group-hover:text-foreground',
            )}
            aria-hidden="true"
          />
          <span>{label}</span>
          {to === '/notificaciones' && notificationCount > 0 && (
            <span
              className="ml-auto grid min-w-5 place-items-center rounded-full bg-primary px-1.5 py-0.5 text-[0.68rem] font-bold leading-none text-primary-foreground"
              aria-label={`${notificationCount} notificaciones no leídas`}
            >
              {notificationCount > 99 ? '99+' : notificationCount}
            </span>
          )}
        </>
      )}
    </NavLink>
  ));
}

export default function AppSidebar({
  displayName,
  notificationCount,
  theme,
  onToggleTheme,
  onLogout,
}) {
  const initial = displayName.charAt(0).toUpperCase();

  return (
    <aside className="fixed inset-y-0 left-0 z-30 hidden w-64 flex-col border-r border-border/80 bg-card lg:flex">
      <div className="flex h-20 items-center border-b border-border/70 px-6">
        <Link to="/" aria-label="Clínica Aviva, ir al resumen" className="rounded-md focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring">
          <AvivaLogo alt="" className="h-11 w-auto" />
        </Link>
      </div>

      <div className="flex min-h-0 flex-1 flex-col px-4 py-5">
        <Button asChild className="h-12 w-full justify-start gap-3 rounded-xl shadow-sm">
          <Link to="/agendar">
            <CalendarPlus className="size-5" aria-hidden="true" />
            Agendar cita
          </Link>
        </Button>

        <nav className="mt-6 grid gap-1" aria-label="Navegación principal">
          <PortalNavigationLinks notificationCount={notificationCount} />
        </nav>

        <div className="mt-auto space-y-3 border-t border-border/70 pt-4">
          <div className="flex items-center gap-3 rounded-xl bg-muted/60 p-3" aria-label={`Sesión de ${displayName}`}>
            <span className="grid size-10 shrink-0 place-items-center rounded-full bg-primary/10 text-sm font-bold text-primary" aria-hidden="true">
              {initial}
            </span>
            <span className="min-w-0">
              <small className="block text-xs text-muted-foreground">Paciente</small>
              <strong className="block truncate text-sm font-semibold text-foreground">{displayName}</strong>
            </span>
          </div>

          <Button type="button" variant="ghost" className="h-11 w-full justify-start gap-3 text-muted-foreground" onClick={onToggleTheme}>
            {theme === 'dark' ? <Sun className="size-5" aria-hidden="true" /> : <Moon className="size-5" aria-hidden="true" />}
            {theme === 'dark' ? 'Usar modo claro' : 'Usar modo oscuro'}
          </Button>
          <Button type="button" variant="ghost" className="h-11 w-full justify-start gap-3 text-muted-foreground hover:text-destructive" onClick={onLogout}>
            <LogOut className="size-5" aria-hidden="true" />
            Cerrar sesión
          </Button>
        </div>
      </div>
    </aside>
  );
}
