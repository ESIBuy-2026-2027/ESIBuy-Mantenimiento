import { describe, it, expect } from 'vitest'
import { http, HttpResponse } from 'msw'
import { server } from '../mocks/server'
import { getPreferences, savePreferences } from './preferences'

describe('getPreferences', () => {
  it('HU46-CP19: pide GET /api/customer/preferences y devuelve las categorías del cliente', async () => {
    const peticion = {}
    server.use(
      http.get('/api/customer/preferences', ({ request }) => {
        peticion.metodo = request.method
        return HttpResponse.json({ categorias: ['cat-2', 'cat-1'] })
      }),
    )

    const respuesta = await getPreferences()

    expect(peticion.metodo).toBe('GET')
    expect(respuesta).toEqual({ categorias: ['cat-2', 'cat-1'] })
  })
})

describe('savePreferences', () => {
  it('HU46-CP19: pide PUT /api/customer/preferences con las categorías y devuelve las guardadas', async () => {
    const peticion = {}
    server.use(
      http.put('/api/customer/preferences', async ({ request }) => {
        peticion.metodo = request.method
        peticion.cuerpo = await request.json()
        return HttpResponse.json({ categorias: ['cat-1'] })
      }),
    )

    const respuesta = await savePreferences(['cat-1'])

    expect(peticion).toEqual({ metodo: 'PUT', cuerpo: { categorias: ['cat-1'] } })
    expect(respuesta).toEqual({ categorias: ['cat-1'] })
  })

  it('HU46-CP19: un 400 llega como error con su estado y su código', async () => {
    server.use(
      http.put('/api/customer/preferences', () => HttpResponse.json({ code: 'VALIDATION_ERROR' }, { status: 400 })),
    )

    await expect(savePreferences(['cat-9'])).rejects.toMatchObject({ status: 400, code: 'VALIDATION_ERROR' })
  })
})
