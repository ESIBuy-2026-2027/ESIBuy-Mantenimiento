import '@testing-library/jest-dom/vitest'
import { afterAll, afterEach, beforeAll, vi } from 'vitest'
import { cleanup } from '@testing-library/react'
import { resetAddressesMock } from '../mocks/addressesHandlers'
import { resetAdminCategoriesMock } from '../mocks/adminCategoriesHandlers'
import { resetAdminSellersMock } from '../mocks/adminSellersHandlers'
import { resetAdminsMock } from '../mocks/adminsHandlers'
import { resetAuthMock } from '../mocks/authHandlers'
import { resetCartMock } from '../mocks/cartHandlers'
import { resetOrdersMock } from '../mocks/ordersHandlers'
import { resetPaymentMethodsMock } from '../mocks/paymentMethodsHandlers'
import { resetProductsMock } from '../mocks/productsHandlers'
import { resetReviewsMock } from '../mocks/reviewsHandlers'
import { resetUsersMock } from '../mocks/usersHandlers'
import { resetWishlistMock } from '../mocks/wishlistHandlers'
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
  resetCartMock()
  resetOrdersMock()
  resetPaymentMethodsMock()
  resetAddressesMock()
  resetAdminCategoriesMock()
  resetAdminSellersMock()
  resetAdminsMock()
  resetProductsMock()
  resetReviewsMock()
  resetUsersMock()
  resetWishlistMock()
})

afterAll(() => {
  server.close()
  vi.unstubAllGlobals()
})
