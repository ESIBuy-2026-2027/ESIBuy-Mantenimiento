import { describe, it, expect } from 'vitest'
import { http, HttpResponse } from 'msw'
import { server } from '../mocks/server'
import { addFavoriteCategory, getPreferences, removeFavoriteCategory } from './preferences'

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

describe('addFavoriteCategory', () => {
  it('HU528-CP14: pide POST /api/customer/preferences/categories con la categoría y devuelve las favoritas', async () => {
    const peticion = {}
    server.use(
      http.post('/api/customer/preferences/categories', async ({ request }) => {
        peticion.metodo = request.method
        peticion.cuerpo = await request.json()
        return HttpResponse.json({ categorias: ['cat-2', 'cat-1'] })
      }),
    )

    const respuesta = await addFavoriteCategory('cat-1')

    expect(peticion).toEqual({ metodo: 'POST', cuerpo: { categoriaId: 'cat-1' } })
    expect(respuesta).toEqual({ categorias: ['cat-2', 'cat-1'] })
  })

  it('HU528-CP14: un 404 llega como error con su estado y su código', async () => {
    server.use(
      http.post('/api/customer/preferences/categories', () =>
        HttpResponse.json({ code: 'CATEGORY_NOT_FOUND' }, { status: 404 }),
      ),
    )

    await expect(addFavoriteCategory('cat-9')).rejects.toMatchObject({ status: 404, code: 'CATEGORY_NOT_FOUND' })
  })
})

describe('removeFavoriteCategory', () => {
  it('HU528-CP14: pide DELETE /api/customer/preferences/categories/{id} y devuelve las favoritas que quedan', async () => {
    const peticion = {}
    server.use(
      http.delete('/api/customer/preferences/categories/:categoriaId', ({ request, params }) => {
        peticion.metodo = request.method
        peticion.categoriaId = params.categoriaId
        return HttpResponse.json({ categorias: ['cat-2'] })
      }),
    )

    const respuesta = await removeFavoriteCategory('cat/1')

    expect(peticion).toEqual({ metodo: 'DELETE', categoriaId: 'cat/1' })
    expect(respuesta).toEqual({ categorias: ['cat-2'] })
  })
})
