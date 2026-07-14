import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { AuthProvider } from './context/AuthContext.jsx';
import App from './App.jsx';
import './styles.css';

// Aplica el tema antes del primer render para evitar un destello de color en
// las rutas públicas y mantener la preferencia en todo el portal.
const storedTheme = window.localStorage.getItem('aviva.portal.theme');
const initialTheme = storedTheme === 'dark' || storedTheme === 'light'
  ? storedTheme
  : window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
document.documentElement.dataset.theme = initialTheme;
document.documentElement.classList.toggle('dark', initialTheme === 'dark');

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 30_000,
      retry: (failureCount, error) => error?.status >= 500 && failureCount < 1,
      refetchOnWindowFocus: false,
    },
  },
});

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <AuthProvider>
          <App />
        </AuthProvider>
      </BrowserRouter>
    </QueryClientProvider>
  </StrictMode>,
);
