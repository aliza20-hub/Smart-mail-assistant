import { defineConfig } from 'vite';
import { svelte } from '@sveltejs/vite-plugin-svelte';

// Proxies /api/* to the Spring Boot backend during `npm run dev` so the
// browser never needs to know the backend's port, and CORS is a non-issue
// for local development.
export default defineConfig({
  plugins: [svelte()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
});
