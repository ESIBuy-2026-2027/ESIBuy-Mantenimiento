import { describe, it, expect } from 'vitest'
import { http, HttpResponse } from 'msw'
import { server } from '../mocks/server'
import { addWishlistItem } from './wishlist'

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
