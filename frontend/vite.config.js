import { defineConfig } from 'vitest/config'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  // Proxy de desarrollo (T230, D7): /api va al backend de Spring Boot con el mismo origen que el
  // frontend, así las cookies de sesión y CSRF funcionan sin CORS. Con los mocks activos (MSW) las
  // peticiones no llegan a salir del navegador.
  server: {
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
  test: {
    environment: 'jsdom',
    setupFiles: './src/test/setup.js',
    coverage: {
      provider: 'v8',
      reporter: ['text-summary', 'lcov'],
      reportOnFailure: true,
      include: ['src/**/*.{js,jsx}'],
      exclude: ['src/mocks/**', 'src/test/**', 'src/**/*.test.{js,jsx}'],
    },
  },
})