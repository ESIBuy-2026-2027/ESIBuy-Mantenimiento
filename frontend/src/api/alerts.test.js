import { describe, it, expect } from 'vitest'
import { http, HttpResponse } from 'msw'
import { server } from '../mocks/server'
import { getAlerts, markAlertAsRead } from './alerts'

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

// PENDIENTE DE CONFIRMAR CON EL EQUIPO: caso HU531-CP07, propuesto.
describe('markAlertAsRead', () => {
  it('HU531-CP07: pide PATCH /api/customer/alerts/{id}/read y devuelve el mensaje y la alerta ya leída', async () => {
    const alerta = {
      id: 'a-1',
      tipo: 'DISPONIBILIDAD',
      fecha: '2026-11-26T10:00:00Z',
      leida: true,
      producto: { id: 'p-1', nombre: 'Teclado', precioAnterior: null, precioNuevo: null },
    }
    const peticion = {}
    server.use(
      http.patch('/api/customer/alerts/:id/read', ({ request, params }) => {
        peticion.metodo = request.method
        peticion.id = params.id
        return HttpResponse.json({ mensaje: 'Alerta marcada como leída', alerta })
      }),
    )

    const respuesta = await markAlertAsRead('a-1')

    expect(peticion).toEqual({ metodo: 'PATCH', id: 'a-1' })
    expect(respuesta).toEqual({ mensaje: 'Alerta marcada como leída', alerta })
  })

  it('HU531-CP07: un 404 llega como error con su estado y el código ALERT_NOT_FOUND', async () => {
    server.use(
      http.patch('/api/customer/alerts/:id/read', () =>
        HttpResponse.json({ mensaje: 'La alerta no existe', code: 'ALERT_NOT_FOUND' }, { status: 404 }),
      ),
    )

    await expect(markAlertAsRead('a-9')).rejects.toMatchObject({ status: 404, code: 'ALERT_NOT_FOUND' })
  })
})
