import { http, HttpResponse } from 'msw'
import { authHandlers } from './authHandlers'

export const handlers = [
  ...authHandlers,
  http.get('/api/categorias', () =>
    HttpResponse.json([
      { id: 'cat-1', nombre: 'Tecnología' },
      { id: 'cat-2', nombre: 'Hogar' },
    ]),
  ),
]
