import '@testing-library/jest-dom/vitest'
import { afterAll, afterEach, beforeAll, vi } from 'vitest'
import { cleanup } from '@testing-library/react'
import { resetAdminSellersMock } from '../mocks/adminSellersHandlers'
import { resetAuthMock } from '../mocks/authHandlers'
import { server } from '../mocks/server'

class ResizeObserverStub {
  observe = vi.fn()
  unobserve = vi.fn()
  disconnect = vi.fn()
}

beforeAll(() => {
  vi.stubGlobal('ResizeObserver', ResizeObserverStub)
  server.listen({ onUnhandledRequest: 'error' })
})

afterEach(() => {
  cleanup()
  server.resetHandlers()
  resetAuthMock()
  resetAdminSellersMock()
})

afterAll(() => {
  server.close()
  vi.unstubAllGlobals()
})
