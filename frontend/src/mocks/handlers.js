import { http, HttpResponse } from 'msw'
import { adminSellersHandlers } from './adminSellersHandlers'
import { adminsHandlers } from './adminsHandlers'
import { authHandlers } from './authHandlers'
import { cartHandlers } from './cartHandlers'
import { productsHandlers } from './productsHandlers'
import { registroHandlers } from './registroHandlers'
import { usersHandlers } from './usersHandlers'

export const handlers = [
  ...authHandlers,
  ...productsHandlers,
  ...cartHandlers,
  ...usersHandlers,
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
