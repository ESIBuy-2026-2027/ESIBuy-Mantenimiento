import { describe, it, expect } from 'vitest'
import { http, HttpResponse } from 'msw'
import { server } from '../mocks/server'
import { deleteAdminCategory, getAdminCategories } from './adminCategories'

const CATEGORIAS = [
  { id: 'cat-1', nombre: 'Hogar', descripcion: 'Muebles', estado: 'ACTIVA', numeroProductos: 4 },
  { id: 'cat-2', nombre: 'Juguetes', descripcion: '', estado: 'INACTIVA', numeroProductos: 0 },
]

describe('getAdminCategories', () => {
  it('HU39-CP06: pide GET /api/admin/categorias y devuelve el array de categorías', async () => {
    const peticion = {}
    server.use(
      http.get('/api/admin/categorias', ({ request }) => {
        peticion.metodo = request.method
        return HttpResponse.json(CATEGORIAS)
      }),
    )

    const respuesta = await getAdminCategories()

    expect(peticion.metodo).toBe('GET')
    expect(respuesta).toEqual(CATEGORIAS)
  })

  it('HU39-CP06: un 403 llega como error con su estado y su código', async () => {
    server.use(
      http.get('/api/admin/categorias', () => HttpResponse.json({ code: 'ACCESS_DENIED' }, { status: 403 })),
    )

    await expect(getAdminCategories()).rejects.toMatchObject({ status: 403, code: 'ACCESS_DENIED' })
  })
})

describe('deleteAdminCategory', () => {
  it('HU42-CP16: pide DELETE /api/admin/categorias/{id} con el id de la categoría', async () => {
    const peticion = {}
    server.use(
      http.delete('/api/admin/categorias/:id', ({ request, params }) => {
        peticion.metodo = request.method
        peticion.id = params.id
        return new HttpResponse(null, { status: 204 })
      }),
    )

    await deleteAdminCategory('cat-9')

    expect(peticion).toEqual({ metodo: 'DELETE', id: 'cat-9' })
  })

  it('HU42-CP16: un 409 llega como error con su estado y su código', async () => {
    server.use(
      http.delete('/api/admin/categorias/:id', () =>
        HttpResponse.json({ code: 'CATEGORY_HAS_PRODUCTS' }, { status: 409 }),
      ),
    )

    await expect(deleteAdminCategory('cat-1')).rejects.toMatchObject({ status: 409, code: 'CATEGORY_HAS_PRODUCTS' })
  })
})
