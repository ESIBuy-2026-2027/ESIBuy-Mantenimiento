import { http, HttpResponse } from 'msw'
import { addressesHandlers } from './addressesHandlers'
import { adminCategoriesHandlers } from './adminCategoriesHandlers'
import { adminSellersHandlers } from './adminSellersHandlers'
import { adminsHandlers } from './adminsHandlers'
import { authHandlers } from './authHandlers'
import { cartHandlers } from './cartHandlers'
import { incidentsHandlers } from './incidentsHandlers'
import { ordersHandlers } from './ordersHandlers'
import { paymentMethodsHandlers } from './paymentMethodsHandlers'
import { preferencesHandlers } from './preferencesHandlers'
import { productsHandlers } from './productsHandlers'
import { profileHandlers } from './profileHandlers'
import { registroHandlers } from './registroHandlers'
import { reviewsHandlers } from './reviewsHandlers'
import { statisticsHandlers } from './statisticsHandlers'
import { usersHandlers } from './usersHandlers'
import { wishlistHandlers } from './wishlistHandlers'
import { securityHandlers } from './securityHandlers'

export const handlers = [
  ...authHandlers,
  ...reviewsHandlers,
  ...productsHandlers,
  ...cartHandlers,
  ...wishlistHandlers,
  ...ordersHandlers,
  ...paymentMethodsHandlers,
  ...addressesHandlers,
  ...usersHandlers,
  ...profileHandlers,
  ...preferencesHandlers,
  ...incidentsHandlers,
  ...statisticsHandlers,
  ...adminCategoriesHandlers,
  ...adminSellersHandlers,
  ...securityHandlers,
  ...adminsHandlers,
  ...registroHandlers,
  http.get('/api/categorias', () =>
    HttpResponse.json([
      { id: 'cat-1', nombre: 'Tecnología' },
      { id: 'cat-2', nombre: 'Hogar' },
    ]),
  ),
]
