import '@testing-library/jest-dom/vitest'
import { afterAll, afterEach, beforeAll, vi } from 'vitest'
import { cleanup } from '@testing-library/react'
import { resetAdminSellersMock } from '../mocks/adminSellersHandlers'
import { resetAdminsMock } from '../mocks/adminsHandlers'
import { resetAuthMock } from '../mocks/authHandlers'
import { resetUsersMock } from '../mocks/usersHandlers'
import { server } from '../mocks/server'

class ResizeObserverStub {
  observe = vi.fn()
  unobserve = vi.fn()
  disconnect = vi.fn()
}

// jsdom no trae IntersectionObserver: por defecto no avisa nunca (la vista de productos cargaría solo la primera
// página). Los tests del scroll infinito ponen uno propio que dispara a mano.
class IntersectionObserverStub {
  observe = vi.fn()
  unobserve = vi.fn()
  disconnect = vi.fn()
}

beforeAll(() => {
  vi.stubGlobal('ResizeObserver', ResizeObserverStub)
  vi.stubGlobal('IntersectionObserver', IntersectionObserverStub)
  server.listen({ onUnhandledRequest: 'error' })
})

afterEach(() => {
  cleanup()
  server.resetHandlers()
  resetAuthMock()
  resetAdminSellersMock()
  resetAdminsMock()
  resetUsersMock()
})

afterAll(() => {
  server.close()
  vi.unstubAllGlobals()
})
