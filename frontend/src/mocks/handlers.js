import { http, HttpResponse } from 'msw'
import { adminSellersHandlers } from './adminSellersHandlers'
import { adminsHandlers } from './adminsHandlers'
import { authHandlers } from './authHandlers'
import { cartHandlers } from './cartHandlers'
import { ordersHandlers } from './ordersHandlers'
import { productsHandlers } from './productsHandlers'
import { profileHandlers } from './profileHandlers'
import { registroHandlers } from './registroHandlers'
import { usersHandlers } from './usersHandlers'

export const handlers = [
  ...authHandlers,
  ...productsHandlers,
  ...cartHandlers,
  ...ordersHandlers,
  ...usersHandlers,
  ...profileHandlers,
  ...adminSellersHandlers,
  ...adminsHandlers,
  ...registroHandlers,
  http.get('/api/categorias', () =>
    HttpResponse.json([
      { id: 'cat-1', nombre: 'Tecnología' },
      { id: 'cat-2', nombre: 'Hogar' },
    ]),
  ),
]
