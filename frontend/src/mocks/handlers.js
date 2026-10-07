import { http, HttpResponse } from 'msw'

export const handlers = [
  http.get('/api/auth/me', () => new HttpResponse(null, { status: 401 })),
  http.get('/api/categorias', () =>
    HttpResponse.json([
      { id: 'cat-1', nombre: 'Tecnología' },
      { id: 'cat-2', nombre: 'Hogar' },
    ]),
  ),
]
