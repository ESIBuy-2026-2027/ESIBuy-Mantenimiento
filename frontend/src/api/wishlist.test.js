import { describe, it, expect } from 'vitest'
import { http, HttpResponse } from 'msw'
import { server } from '../mocks/server'
import { addWishlistItem, getWishlist, removeWishlistItem } from './wishlist'

const LISTA = {
  items: [
    { productoId: 'p-1', addedAt: '2026-11-04T10:00:00Z' },
    { productoId: 'p-7', addedAt: '2026-11-04T11:30:00Z' },
  ],
}

describe('addWishlistItem', () => {
  it('HU14-CP20: pide POST /api/customer/wishlist/items con el producto y devuelve la lista del cliente', async () => {
    const peticion = {}
    server.use(
      http.post('/api/customer/wishlist/items', async ({ request }) => {
        peticion.metodo = request.method
        peticion.cuerpo = await request.json()
        return HttpResponse.json(LISTA)
      }),
    )

    const respuesta = await addWishlistItem('p-7')

    expect(peticion).toEqual({ metodo: 'POST', cuerpo: { productoId: 'p-7' } })
    expect(respuesta).toEqual(LISTA)
  })

  it('HU14-CP20: un 404 llega como error con su estado y su código', async () => {
    server.use(
      http.post('/api/customer/wishlist/items', () =>
        HttpResponse.json({ code: 'PRODUCT_NOT_FOUND' }, { status: 404 }),
      ),
    )

    await expect(addWishlistItem('p-9')).rejects.toMatchObject({ status: 404, code: 'PRODUCT_NOT_FOUND' })
  })
})

const LISTA_DETALLADA = {
  items: [
    {
      productoId: 'p-1',
      nombre: 'Auriculares inalámbricos',
      imagen: 'https://tienda.test/auriculares.png',
      precioFinal: 53.91,
      stock: 34,
      disponible: true,
      addedAt: '2026-11-04T10:00:00Z',
    },
  ],
}

describe('getWishlist', () => {
  it('HU14-CP31: pide GET /api/customer/wishlist y devuelve la lista con los datos de cada producto', async () => {
    const peticion = {}
    server.use(
      http.get('/api/customer/wishlist', ({ request }) => {
        peticion.metodo = request.method
        return HttpResponse.json(LISTA_DETALLADA)
      }),
    )

    const respuesta = await getWishlist()

    expect(peticion.metodo).toBe('GET')
    expect(respuesta).toEqual(LISTA_DETALLADA)
  })

  it('HU14-CP31: un 403 llega como error con su estado y su código', async () => {
    server.use(http.get('/api/customer/wishlist', () => HttpResponse.json({ code: 'ACCESS_DENIED' }, { status: 403 })))

    await expect(getWishlist()).rejects.toMatchObject({ status: 403, code: 'ACCESS_DENIED' })
  })
})

describe('removeWishlistItem', () => {
  it('HU14-CP31: pide DELETE /api/customer/wishlist/items/{productoId} y devuelve la lista actualizada', async () => {
    const peticion = {}
    server.use(
      http.delete('/api/customer/wishlist/items/:productoId', ({ request, params }) => {
        peticion.metodo = request.method
        peticion.productoId = params.productoId
        return HttpResponse.json({ items: [] })
      }),
    )

    const respuesta = await removeWishlistItem('a/b c')

    expect(peticion).toEqual({ metodo: 'DELETE', productoId: 'a/b c' })
    expect(respuesta).toEqual({ items: [] })
  })
})
