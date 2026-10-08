import { http, HttpResponse } from 'msw'
import { adminSellersHandlers } from './adminSellersHandlers'
import { authHandlers } from './authHandlers'
import { productsHandlers } from './productsHandlers'
import { registroHandlers } from './registroHandlers'

export const handlers = [
  ...authHandlers,
  ...productsHandlers,
  ...adminSellersHandlers,
  ...registroHandlers,
  http.get('/api/categorias', () =>
    HttpResponse.json([
      { id: 'cat-1', nombre: 'Tecnología' },
      { id: 'cat-2', nombre: 'Hogar' },
    ]),
  ),
]
