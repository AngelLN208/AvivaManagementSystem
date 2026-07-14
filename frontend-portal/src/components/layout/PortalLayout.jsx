import { useEffect, useRef, useState } from 'react';
import { Outlet, useLocation } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth.js';
import { useMyNotifications } from '../../hooks/useNotifications.js';
import { unreadNotificationCount } from '../../utils/notifications.js';
import AppSidebar from './AppSidebar.jsx';
import MobileNavigation from './MobileNavigation.jsx';
import { PAGE_TITLES } from './portalNavigation.js';

function getInitialTheme() {
  const saved = window.localStorage.getItem('aviva.portal.theme');
  if (saved === 'dark' || saved === 'light') return saved;
  return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
}

export default function PortalLayout() {
  const { session, logout } = useAuth();
  const notificationsQuery = useMyNotifications();
  const location = useLocation();
  const menuButtonRef = useRef(null);
  const hasNavigated = useRef(false);
  const [menuOpen, setMenuOpen] = useState(false);
  const [theme, setTheme] = useState(getInitialTheme);

  useEffect(() => {
    document.documentElement.dataset.theme = theme;
    document.documentElement.classList.toggle('dark', theme === 'dark');
    document.querySelector('meta[name="theme-color"]')
      ?.setAttribute('content', theme === 'dark' ? '#0b1821' : '#0b6b61');
    window.localStorage.setItem('aviva.portal.theme', theme);
  }, [theme]);

  useEffect(() => {
    document.title = `${PAGE_TITLES[location.pathname] || 'Portal paciente'} | Clínica Aviva`;
    if (hasNavigated.current) {
      document.getElementById('contenido-principal')?.focus();
    } else {
      hasNavigated.current = true;
    }
  }, [location.pathname]);

  const displayName = session?.firstName || session?.username || 'Paciente';
  const notificationCount = unreadNotificationCount(notificationsQuery.data);
  const toggleTheme = () => setTheme((current) => current === 'dark' ? 'light' : 'dark');

  return (
    <div className="min-h-dvh bg-background text-foreground">
      <a
        className="fixed left-4 top-3 z-[100] -translate-y-20 rounded-lg bg-primary px-4 py-3 text-sm font-semibold text-primary-foreground shadow-lg transition-transform focus:translate-y-0 focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2"
        href="#contenido-principal"
      >
        Saltar al contenido
      </a>

      <AppSidebar
        displayName={displayName}
        notificationCount={notificationCount}
        theme={theme}
        onToggleTheme={toggleTheme}
        onLogout={logout}
      />

      <div className="flex min-h-dvh min-w-0 flex-col lg:pl-64">
        <MobileNavigation
          displayName={displayName}
          notificationCount={notificationCount}
          menuButtonRef={menuButtonRef}
          open={menuOpen}
          onOpenChange={setMenuOpen}
          theme={theme}
          onToggleTheme={toggleTheme}
          onLogout={logout}
        />

        <main
          id="contenido-principal"
          className="min-h-[calc(100dvh-8rem)] flex-1 scroll-mt-20 bg-muted/30 px-4 py-6 outline-none sm:px-6 sm:py-8 lg:px-8 lg:py-10"
          tabIndex="-1"
        >
          <div className="mx-auto w-full max-w-7xl">
            <Outlet />
          </div>
        </main>

        <footer className="border-t border-border/70 bg-background px-4 py-5 text-center text-xs text-muted-foreground sm:px-6 lg:px-8">
          <p>Clínica Aviva · Tu información, protegida.</p>
        </footer>
      </div>
    </div>
  );
}
