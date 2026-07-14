import { useEffect, useState } from 'react';
import ThemeContext from './themeContext';

export function ThemeProvider({ children }) {
  const [tema, setTema] = useState(() => localStorage.getItem('temaAviva') || 'claro');

  useEffect(() => {
    document.body.setAttribute('data-tema', tema);
    localStorage.setItem('temaAviva', tema);
  }, [tema]);

  const toggleTema = () => setTema(prev => (prev === 'claro' ? 'oscuro' : 'claro'));

  return (
    <ThemeContext.Provider value={{ tema, toggleTema }}>
      {children}
    </ThemeContext.Provider>
  );
}
