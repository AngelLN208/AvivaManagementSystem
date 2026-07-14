import { Link } from 'react-router-dom';
import {
  Bell,
  CalendarPlus,
  LogOut,
  Menu,
  Moon,
  Sun,
} from 'lucide-react';
import AvivaLogo from '@/components/brand/AvivaLogo.jsx';
import { Button } from '@/components/ui/button.jsx';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '@/components/ui/dialog.jsx';
import { PortalNavigationLinks } from './AppSidebar.jsx';

export default function MobileNavigation({
  displayName,
  menuButtonRef,
  open,
  onOpenChange,
  notificationCount,
  theme,
  onToggleTheme,
  onLogout,
}) {
  const initial = displayName.charAt(0).toUpperCase();

  return (
    <header className="sticky top-0 z-40 flex h-16 items-center justify-between border-b border-border/80 bg-background/95 px-4 backdrop-blur supports-[backdrop-filter]:bg-background/85 sm:px-6 lg:hidden">
      <Link to="/" aria-label="Clínica Aviva, ir al resumen" className="rounded-md focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring">
        <AvivaLogo alt="" className="h-9 w-auto" />
      </Link>

      <div className="flex items-center gap-2">
        <Button asChild variant="ghost" size="icon" className="relative size-11">
          <Link
            to="/notificaciones"
            aria-label={notificationCount > 0
              ? `Abrir notificaciones, ${notificationCount} no leídas`
              : 'Abrir notificaciones'}
          >
            <Bell className="size-5" aria-hidden="true" />
            {notificationCount > 0 && (
              <span className="absolute right-1.5 top-1.5 min-w-4 rounded-full bg-primary px-1 text-center text-[0.62rem] font-bold leading-4 text-primary-foreground" aria-hidden="true">
                {notificationCount > 9 ? '9+' : notificationCount}
              </span>
            )}
          </Link>
        </Button>

        <Dialog open={open} onOpenChange={onOpenChange}>
          <DialogTrigger asChild>
            <Button ref={menuButtonRef} type="button" variant="outline" size="icon" className="size-11" aria-label="Abrir menú principal">
              <Menu className="size-5" aria-hidden="true" />
            </Button>
          </DialogTrigger>
          <DialogContent className="left-0 top-0 flex h-dvh w-[min(88vw,22rem)] max-w-none translate-x-0 translate-y-0 flex-col gap-0 rounded-none border-y-0 border-l-0 p-0 sm:max-w-sm" aria-describedby="mobile-navigation-description">
            <DialogHeader className="border-b border-border/70 px-5 py-5 text-left">
              <DialogTitle>Menú del portal</DialogTitle>
              <DialogDescription id="mobile-navigation-description" className="sr-only">
                Navega por las secciones de tu portal de paciente.
              </DialogDescription>
            </DialogHeader>

          <div className="flex min-h-0 flex-1 flex-col overflow-y-auto px-4 py-5">
            <Button asChild className="h-12 w-full justify-start gap-3 rounded-xl">
              <Link to="/agendar" onClick={() => onOpenChange(false)}>
                <CalendarPlus className="size-5" aria-hidden="true" />
                Agendar cita
              </Link>
            </Button>

            <nav className="mt-5 grid gap-1" aria-label="Navegación principal">
              <PortalNavigationLinks
                mobile
                notificationCount={notificationCount}
                onNavigate={() => onOpenChange(false)}
              />
            </nav>

            <div className="mt-auto space-y-3 border-t border-border/70 pt-5">
              <div className="flex items-center gap-3 rounded-xl bg-muted/60 p-3" aria-label={`Sesión de ${displayName}`}>
                <span className="grid size-10 shrink-0 place-items-center rounded-full bg-primary/10 text-sm font-bold text-primary" aria-hidden="true">
                  {initial}
                </span>
                <span className="min-w-0">
                  <small className="block text-xs text-muted-foreground">Paciente</small>
                  <strong className="block truncate text-sm font-semibold">{displayName}</strong>
                </span>
              </div>
              <Button type="button" variant="outline" className="h-11 w-full justify-start gap-3" onClick={onToggleTheme}>
                {theme === 'dark' ? <Sun className="size-5" aria-hidden="true" /> : <Moon className="size-5" aria-hidden="true" />}
                {theme === 'dark' ? 'Usar modo claro' : 'Usar modo oscuro'}
              </Button>
              <Button type="button" variant="ghost" className="h-11 w-full justify-start gap-3 text-muted-foreground hover:text-destructive" onClick={onLogout}>
                <LogOut className="size-5" aria-hidden="true" />
                Cerrar sesión
              </Button>
            </div>
          </div>
          </DialogContent>
        </Dialog>
      </div>
    </header>
  );
}
