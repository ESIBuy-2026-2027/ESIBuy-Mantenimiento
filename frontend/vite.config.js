import { defineConfig } from 'vitest/config'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
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