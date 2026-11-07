import { describe, it, expect } from 'vitest'
import { http, HttpResponse } from 'msw'
import { server } from '../mocks/server'
import {
  changeAdminCategoryStatus,
  createAdminCategory,
  deleteAdminCategory,
  getAdminCategories,
  updateAdminCategory,
} from './adminCategories'

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

describe('createAdminCategory', () => {
  it('HU40-CP10: pide POST /api/admin/categorias con el nombre, la descripción y el estado, y devuelve la categoría creada', async () => {
    const peticion = {}
    const creada = { id: 'cat-11', nombre: 'Jardín', descripcion: 'Plantas', estado: 'INACTIVA', numeroProductos: 0 }
    server.use(
      http.post('/api/admin/categorias', async ({ request }) => {
        peticion.metodo = request.method
        peticion.cuerpo = await request.json()
        return HttpResponse.json(creada, { status: 201 })
      }),
    )

    const respuesta = await createAdminCategory({ nombre: 'Jardín', descripcion: 'Plantas', estado: 'INACTIVA' })

    expect(peticion).toEqual({
      metodo: 'POST',
      cuerpo: { nombre: 'Jardín', descripcion: 'Plantas', estado: 'INACTIVA' },
    })
    expect(respuesta).toEqual(creada)
  })

  it('HU40-CP10: un 409 llega como error con su estado y su código', async () => {
    server.use(
      http.post('/api/admin/categorias', () =>
        HttpResponse.json({ code: 'CATEGORY_NAME_ALREADY_EXISTS' }, { status: 409 }),
      ),
    )

    await expect(createAdminCategory({ nombre: 'Hogar', descripcion: '', estado: 'ACTIVA' })).rejects.toMatchObject({
      status: 409,
      code: 'CATEGORY_NAME_ALREADY_EXISTS',
    })
  })
})

describe('updateAdminCategory', () => {
  it('HU41-CP15: pide PUT /api/admin/categorias/{id} con el nombre y la descripción, y devuelve la categoría editada', async () => {
    const peticion = {}
    const editada = { id: 'cat-2', nombre: 'Casa', descripcion: 'Todo para el hogar', estado: 'ACTIVA', numeroProductos: 14 }
    server.use(
      http.put('/api/admin/categorias/:id', async ({ request, params }) => {
        peticion.metodo = request.method
        peticion.id = params.id
        peticion.cuerpo = await request.json()
        return HttpResponse.json(editada)
      }),
    )

    const respuesta = await updateAdminCategory('cat-2', { nombre: 'Casa', descripcion: 'Todo para el hogar' })

    expect(peticion).toEqual({
      metodo: 'PUT',
      id: 'cat-2',
      cuerpo: { nombre: 'Casa', descripcion: 'Todo para el hogar' },
    })
    expect(respuesta).toEqual(editada)
  })

  it('HU41-CP15: un 404 llega como error con su estado y su código', async () => {
    server.use(
      http.put('/api/admin/categorias/:id', () => HttpResponse.json({ code: 'CATEGORY_NOT_FOUND' }, { status: 404 })),
    )

    await expect(updateAdminCategory('cat-99', { nombre: 'Casa', descripcion: '' })).rejects.toMatchObject({
      status: 404,
      code: 'CATEGORY_NOT_FOUND',
    })
  })
})

describe('changeAdminCategoryStatus', () => {
  it('HU41-CP27: pide PATCH /api/admin/categorias/{id}/status con el estado nuevo y devuelve la fila actualizada', async () => {
    const peticion = {}
    const desactivada = { id: 'a/b', nombre: 'Hogar', descripcion: '', estado: 'INACTIVA', numeroProductos: 4 }
    server.use(
      http.patch('/api/admin/categorias/:id/status', async ({ request, params }) => {
        peticion.metodo = request.method
        peticion.id = params.id
        peticion.cuerpo = await request.json()
        return HttpResponse.json(desactivada)
      }),
    )

    const respuesta = await changeAdminCategoryStatus('a/b', 'INACTIVA')

    expect(peticion).toEqual({ metodo: 'PATCH', id: 'a/b', cuerpo: { estado: 'INACTIVA' } })
    expect(respuesta).toEqual(desactivada)
  })

  it('HU41-CP27: un 404 llega como error con su estado y su código', async () => {
    server.use(
      http.patch('/api/admin/categorias/:id/status', () =>
        HttpResponse.json({ code: 'CATEGORY_NOT_FOUND' }, { status: 404 }),
      ),
    )

    await expect(changeAdminCategoryStatus('cat-99', 'ACTIVA')).rejects.toMatchObject({
      status: 404,
      code: 'CATEGORY_NOT_FOUND',
    })
  })
})
