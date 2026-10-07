import { http, HttpResponse } from 'msw'
import { authHandlers } from './authHandlers'
import { productsHandlers } from './productsHandlers'

export const handlers = [
  ...authHandlers,
  ...productsHandlers,
  http.get('/api/categorias', () =>
    HttpResponse.json([
      { id: 'cat-1', nombre: 'Tecnología' },
      { id: 'cat-2', nombre: 'Hogar' },
    ]),
  ),
]
