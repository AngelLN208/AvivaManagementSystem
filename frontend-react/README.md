# Clinica Aviva - Frontend de recepcion

Aplicacion React/Vite para el personal de recepcion de Clinica Aviva.

## Desarrollo local

1. Copiar `.env.example` como `.env`.
2. Verificar que el backend se ejecute en `http://localhost:8080`.
3. Instalar dependencias con `npm ci`.
4. Iniciar con `npm run dev`.

## Despliegue en Vercel

Configurar la variable del proyecto con la URL publica del backend:

```text
VITE_API_URL=https://tu-backend.onrender.com/api
```

El backend debe incluir el dominio de Vercel dentro de
`CORS_ALLOWED_ORIGINS` en Render.

## Verificacion

```bash
npm run lint
npm run build
```

<!-- Documentacion original de la plantilla Vite -->

# React + Vite

This template provides a minimal setup to get React working in Vite with HMR and some ESLint rules.

Currently, two official plugins are available:

- [@vitejs/plugin-react](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react) uses [Oxc](https://oxc.rs)
- [@vitejs/plugin-react-swc](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react-swc) uses [SWC](https://swc.rs/)

## React Compiler

The React Compiler is not enabled on this template because of its impact on dev & build performances. To add it, see [this documentation](https://react.dev/learn/react-compiler/installation).

## Expanding the ESLint configuration

If you are developing a production application, we recommend using TypeScript with type-aware lint rules enabled. Check out the [TS template](https://github.com/vitejs/vite/tree/main/packages/create-vite/template-react-ts) for information on how to integrate TypeScript and [`typescript-eslint`](https://typescript-eslint.io) in your project.
