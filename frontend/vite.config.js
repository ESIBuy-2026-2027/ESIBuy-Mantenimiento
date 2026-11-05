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
    // 15 s por test en lugar de los 5 s por defecto: en el agente self-hosted de Sonar, con la batería
    // completa y la cobertura, los tests de las páginas superan los 5 s y fallan sin estar rotos.
    testTimeout: 15000,
    coverage: {
      provider: 'v8',
      reporter: ['text-summary', 'lcov'],
      reportOnFailure: true,
      include: ['src/**/*.{js,jsx}'],
      exclude: ['src/mocks/**', 'src/test/**', 'src/**/*.test.{js,jsx}'],
    },
  },
})