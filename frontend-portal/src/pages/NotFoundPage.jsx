import { Link } from 'react-router-dom';
import { ArrowLeft, MapPinOff } from 'lucide-react';
import AvivaLogo from '@/components/brand/AvivaLogo.jsx';
import { Button } from '@/components/ui/button.jsx';
import { Card, CardContent } from '@/components/ui/card.jsx';
import { useAuth } from '../hooks/useAuth.js';

export default function NotFoundPage() {
  const { isAuthenticated } = useAuth();

  return (
    <main className="grid min-h-dvh place-items-center bg-muted/30 px-4 py-10 text-foreground">
      <div className="w-full max-w-lg text-center">
        <Link to={isAuthenticated ? '/' : '/login'} className="mx-auto mb-8 block w-fit rounded-lg focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring" aria-label="Clínica Aviva">
          <AvivaLogo alt="" className="h-12 w-auto" />
        </Link>

        <Card className="gap-0 border-border/80 py-0 shadow-lg shadow-primary/5">
          <CardContent className="px-6 py-9 sm:px-10 sm:py-12">
            <span className="mx-auto grid size-16 place-items-center rounded-2xl bg-primary/10 text-primary" aria-hidden="true">
              <MapPinOff className="size-8" />
            </span>
            <p className="mt-6 text-sm font-semibold text-primary">Error 404</p>
            <h1 className="mt-2 text-2xl font-semibold tracking-tight sm:text-3xl">Esta dirección no existe</h1>
            <p className="mx-auto mt-3 max-w-md text-sm leading-6 text-muted-foreground sm:text-base">
              Revisa el enlace o vuelve a una sección disponible del portal.
            </p>
            <Button asChild size="lg" className="mt-7">
              <Link to={isAuthenticated ? '/' : '/login'}>
                <ArrowLeft className="size-5" aria-hidden="true" />
                {isAuthenticated ? 'Volver al resumen' : 'Ir a iniciar sesión'}
              </Link>
            </Button>
          </CardContent>
        </Card>
      </div>
    </main>
  );
}
