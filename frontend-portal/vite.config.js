import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

const apiProxy = {
  '/api': {
    target: 'http://localhost:8080',
    changeOrigin: true,
  },
};

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5174,
    strictPort: true,
    // El proxy permite ejecutar staff y portal simultáneamente sin ampliar CORS.
    proxy: apiProxy,
  },
  preview: {
    port: 4174,
    strictPort: true,
    proxy: apiProxy,
  },
});
