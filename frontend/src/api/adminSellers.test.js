import { describe, it, expect } from 'vitest'
import { http, HttpResponse } from 'msw'
import { server } from '../mocks/server'
import { updateAdminSellerStatus } from './adminSellers'
import { ApiError } from './client'

describe('updateAdminSellerStatus', () => {
  it('updateAdminSellerStatus_enviaElNuevoEstadoPorPatchALaRutaDelVendedor', async () => {
    const peticion = {}
    server.use(
      http.patch('/api/admin/sellers/:id/status', async ({ params, request }) => {
        peticion.id = params.id
        peticion.metodo = request.method
        peticion.cuerpo = await request.json()
        return HttpResponse.json({ id: params.id, estado: 'ACTIVO' })
      }),
    )

    const respuesta = await updateAdminSellerStatus('s-05', 'ACTIVO')

    expect(peticion).toEqual({ id: 's-05', metodo: 'PATCH', cuerpo: { estado: 'ACTIVO' } })
    expect(respuesta).toEqual({ id: 's-05', estado: 'ACTIVO' })
  })

  it('updateAdminSellerStatus_codificaElIdentificadorEnLaRuta', async () => {
    let ruta = null
    server.use(
      http.patch('/api/admin/sellers/:id/status', ({ request }) => {
        ruta = new URL(request.url).pathname
        return HttpResponse.json({})
      }),
    )

    await updateAdminSellerStatus('id con/barra', 'DESACTIVADO')

    expect(ruta).toBe('/api/admin/sellers/id%20con%2Fbarra/status')
  })

  it('updateAdminSellerStatus_conErrorDelServidor_lanzaApiError', async () => {
    server.use(http.patch('/api/admin/sellers/:id/status', () => new HttpResponse(null, { status: 500 })))

    const error = await updateAdminSellerStatus('s-05', 'ACTIVO').catch((excepcion) => excepcion)

    expect(error).toBeInstanceOf(ApiError)
    expect(error.status).toBe(500)
  })
})
