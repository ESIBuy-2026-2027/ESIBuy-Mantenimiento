import { describe, it, expect } from 'vitest'
import { http, HttpResponse } from 'msw'
import { server } from '../mocks/server'
import { getAdminIncidents } from './adminIncidents'

// Supervisión de incidencias del administrador (HU49, T304) contra el contrato del backend (PR #232):
// GET /api/admin/incidencias?page&size, con el autor de cada incidencia ({email, rol} o null).

const PAGINA = {
  items: [
    {
      id: 'inc-1',
      asunto: 'No me llega el pedido',
      descripcion: 'Lo pedí hace una semana',
      estado: 'PENDIENTE',
      respuesta: '',
      fechaCreacion: '2026-11-06T10:15:30Z',
      autor: { email: 'cliente@esibuy.test', rol: 'CLIENTE' },
    },
    {
      id: 'inc-2',
      asunto: 'Sin stock',
      descripcion: 'Un producto aparece sin stock',
      estado: 'PENDIENTE',
      respuesta: '',
      fechaCreacion: '2026-11-06T11:00:00Z',
      autor: null,
    },
  ],
  page: 2,
  size: 5,
  totalItems: 7,
  totalPages: 2,
}

describe('getAdminIncidents', () => {
  it('HU49-CP15: pide GET /api/admin/incidencias con la página y el tamaño y devuelve la página con los autores', async () => {
    const peticion = {}
    server.use(
      http.get('/api/admin/incidencias', ({ request }) => {
        const params = new URL(request.url).searchParams
        peticion.metodo = request.method
        peticion.page = params.get('page')
        peticion.size = params.get('size')
        return HttpResponse.json(PAGINA)
      }),
    )

    const respuesta = await getAdminIncidents({ page: 2, size: 5 })

    expect(peticion).toEqual({ metodo: 'GET', page: '2', size: '5' })
    expect(respuesta).toEqual(PAGINA)
  })

  it('HU49-CP15: un 403 llega como error con su estado y su código', async () => {
    server.use(
      http.get('/api/admin/incidencias', () => HttpResponse.json({ code: 'ACCESS_DENIED' }, { status: 403 })),
    )

    await expect(getAdminIncidents({ page: 1, size: 10 })).rejects.toMatchObject({
      status: 403,
      code: 'ACCESS_DENIED',
    })
  })
})
