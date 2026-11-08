import { describe, it, expect } from 'vitest'
import { http, HttpResponse } from 'msw'
import { server } from '../mocks/server'
import { closeIncident, getAdminIncidents, openIncident } from './adminIncidents'

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

// Apertura y cierre de incidencias del administrador (HU50, T305) contra el contrato del backend (PR #233):
// PATCH /api/admin/incidencias/{id}/abrir con { respuesta } y DELETE /api/admin/incidencias/{id}.

const ABIERTA = {
  id: 'inc-1',
  asunto: 'No me llega el pedido',
  descripcion: 'Lo pedí hace una semana',
  estado: 'ABIERTA',
  respuesta: 'Lo estamos revisando',
  fechaCreacion: '2026-11-06T10:15:30Z',
}

describe('openIncident', () => {
  it('HU50-CP18: pide PATCH /api/admin/incidencias/{id}/abrir con la respuesta y devuelve la incidencia abierta', async () => {
    const peticion = {}
    server.use(
      http.patch('/api/admin/incidencias/:id/abrir', async ({ request, params }) => {
        peticion.id = params.id
        peticion.cuerpo = await request.json()
        return HttpResponse.json(ABIERTA)
      }),
    )

    const respuesta = await openIncident('inc-1', 'Lo estamos revisando')

    expect(peticion).toEqual({ id: 'inc-1', cuerpo: { respuesta: 'Lo estamos revisando' } })
    expect(respuesta).toEqual(ABIERTA)
  })

  it('HU50-CP18: un 409 llega como error con su estado y su código', async () => {
    server.use(
      http.patch('/api/admin/incidencias/:id/abrir', () =>
        HttpResponse.json({ mensaje: 'La incidencia ya está abierta', code: 'INCIDENT_ALREADY_OPEN' }, { status: 409 }),
      ),
    )

    await expect(openIncident('inc-1', 'Respuesta')).rejects.toMatchObject({
      status: 409,
      code: 'INCIDENT_ALREADY_OPEN',
    })
  })
})

describe('closeIncident', () => {
  it('HU50-CP18: pide DELETE /api/admin/incidencias/{id} y un 204 sin cuerpo no es un error', async () => {
    const ids = []
    server.use(
      http.delete('/api/admin/incidencias/:id', ({ params }) => {
        ids.push(params.id)
        return new HttpResponse(null, { status: 204 })
      }),
    )

    await expect(closeIncident('inc-1')).resolves.toBeNull()
    expect(ids).toEqual(['inc-1'])
  })

  it('HU50-CP18: un 404 llega como error con su estado y su código', async () => {
    server.use(
      http.delete('/api/admin/incidencias/:id', () =>
        HttpResponse.json({ mensaje: 'La incidencia no existe', code: 'INCIDENT_NOT_FOUND' }, { status: 404 }),
      ),
    )

    await expect(closeIncident('inc-1')).rejects.toMatchObject({ status: 404, code: 'INCIDENT_NOT_FOUND' })
  })
})
