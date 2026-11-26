import { describe, it, expect } from 'vitest'
import { http, HttpResponse } from 'msw'
import { server } from '../mocks/server'
import { getAlerts } from './alerts'

describe('getAlerts', () => {
  it('HU529-CP25: pide GET /api/customer/alerts y devuelve el mensaje y las alertas del cliente', async () => {
    const alerta = {
      id: 'a-1',
      tipo: 'BAJADA_PRECIO',
      fecha: '2026-11-26T10:00:00Z',
      leida: false,
      producto: { id: 'p-1', nombre: 'Teclado', precioAnterior: 50, precioNuevo: 40 },
    }
    const peticion = {}
    server.use(
      http.get('/api/customer/alerts', ({ request }) => {
        peticion.metodo = request.method
        return HttpResponse.json({ mensaje: 'Alertas consultadas correctamente', alertas: [alerta] })
      }),
    )

    const respuesta = await getAlerts()

    expect(peticion.metodo).toBe('GET')
    expect(respuesta).toEqual({ mensaje: 'Alertas consultadas correctamente', alertas: [alerta] })
  })

  it('HU529-CP25: un 403 llega como error con su estado y su código', async () => {
    server.use(
      http.get('/api/customer/alerts', () => HttpResponse.json({ code: 'ACCESS_DENIED' }, { status: 403 })),
    )

    await expect(getAlerts()).rejects.toMatchObject({ status: 403, code: 'ACCESS_DENIED' })
  })
})
