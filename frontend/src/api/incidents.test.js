import { describe, it, expect } from 'vitest'
import { http, HttpResponse } from 'msw'
import { server } from '../mocks/server'
import { createIncident, getIncidents } from './incidents'

const INCIDENCIA = {
  id: 'inc-1',
  asunto: 'No me llega el pedido',
  descripcion: 'Lo pedí hace una semana',
  estado: 'PENDIENTE',
  respuesta: '',
  fechaCreacion: '2026-11-06T10:15:30Z',
}

describe('getIncidents', () => {
  it('HU44-CP23: pide GET /api/perfil/incidencias con la página y el tamaño y devuelve la página', async () => {
    const peticion = {}
    const pagina = { items: [INCIDENCIA], page: 2, size: 5, totalItems: 6, totalPages: 2 }
    server.use(
      http.get('/api/perfil/incidencias', ({ request }) => {
        const params = new URL(request.url).searchParams
        peticion.metodo = request.method
        peticion.page = params.get('page')
        peticion.size = params.get('size')
        return HttpResponse.json(pagina)
      }),
    )

    const respuesta = await getIncidents({ page: 2, size: 5 })

    expect(peticion).toEqual({ metodo: 'GET', page: '2', size: '5' })
    expect(respuesta).toEqual(pagina)
  })

  it('HU44-CP23: un 403 llega como error con su estado y su código', async () => {
    server.use(
      http.get('/api/perfil/incidencias', () => HttpResponse.json({ code: 'ACCESS_DENIED' }, { status: 403 })),
    )

    await expect(getIncidents({ page: 1, size: 10 })).rejects.toMatchObject({ status: 403, code: 'ACCESS_DENIED' })
  })
})

describe('createIncident', () => {
  it('HU44-CP23: pide POST /api/perfil/incidencias con el asunto y la descripción y devuelve la incidencia', async () => {
    const peticion = {}
    server.use(
      http.post('/api/perfil/incidencias', async ({ request }) => {
        peticion.metodo = request.method
        peticion.cuerpo = await request.json()
        return HttpResponse.json(INCIDENCIA, { status: 201 })
      }),
    )

    const respuesta = await createIncident({ asunto: 'No me llega el pedido', descripcion: 'Lo pedí hace una semana' })

    expect(peticion).toEqual({
      metodo: 'POST',
      cuerpo: { asunto: 'No me llega el pedido', descripcion: 'Lo pedí hace una semana' },
    })
    expect(respuesta).toEqual(INCIDENCIA)
  })

  it('HU44-CP23: un 400 llega como error con su estado y su código', async () => {
    server.use(
      http.post('/api/perfil/incidencias', () =>
        HttpResponse.json({ mensaje: 'La incidencia es inválida', code: 'VALIDATION_ERROR' }, { status: 400 }),
      ),
    )

    await expect(createIncident({ asunto: 'x', descripcion: 'y' })).rejects.toMatchObject({
      status: 400,
      code: 'VALIDATION_ERROR',
    })
  })
})
