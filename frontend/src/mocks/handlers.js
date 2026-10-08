import { http, HttpResponse } from 'msw'
import { authHandlers } from './authHandlers'
import { productsHandlers } from './productsHandlers'
import { usersHandlers } from './usersHandlers'

export const handlers = [
  ...authHandlers,
  ...productsHandlers,
  ...usersHandlers,
  http.get('/api/categorias', () =>
    HttpResponse.json([
      { id: 'cat-1', nombre: 'Tecnología' },
      { id: 'cat-2', nombre: 'Hogar' },
    ]),
  ),
]
