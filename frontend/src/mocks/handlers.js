import { http, HttpResponse } from 'msw'
import { authHandlers } from './authHandlers'
import { registroHandlers } from './registroHandlers'

export const handlers = [
  ...authHandlers,
  ...registroHandlers,
  http.get('/api/categorias', () =>
    HttpResponse.json([
      { id: 'cat-1', nombre: 'Tecnología' },
      { id: 'cat-2', nombre: 'Hogar' },
    ]),
  ),
]
