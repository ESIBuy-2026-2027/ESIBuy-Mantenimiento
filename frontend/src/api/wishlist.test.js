import { describe, it, expect } from 'vitest'
import { http, HttpResponse } from 'msw'
import { server } from '../mocks/server'
import {
  addItemToWishlist,
  addWishlistItem,
  createWishlist,
  deleteWishlist,
  getWishlist,
  getWishlists,
  removeItemFromWishlist,
  removeWishlistItem,
  renameWishlist,
} from './wishlist'

const URL_LISTA = '/api/customer/wishlist'
const URL_LISTAS = '/api/customer/wishlists'

describe('wishlist api', () => {
  it('HU14-CP20: addWishlistItem pide POST /api/customer/wishlist/items', async () => {
    let body
    server.use(
      http.post(`${URL_LISTA}/items`, async ({ request }) => {
        body = await request.json()
        return HttpResponse.json({ items: [{ productoId: 'p-7', addedAt: '2026-11-04T10:00:00Z' }] })
      }),
    )

    const respuesta = await addWishlistItem('p-7')
    expect(body).toEqual({ productoId: 'p-7' })
    expect(respuesta.items).toHaveLength(1)
  })

  it('HU14-CP31: getWishlist y removeWishlistItem llaman a /api/customer/wishlist', async () => {
    server.use(
      http.get(URL_LISTA, () => HttpResponse.json({ items: [] })),
      http.delete(`${URL_LISTA}/items/:productoId`, ({ params }) =>
        HttpResponse.json({ items: [], eliminado: params.productoId }),
      ),
    )

    expect(await getWishlist()).toEqual({ items: [] })
    expect(await removeWishlistItem('p-1')).toMatchObject({ items: [], eliminado: 'p-1' })
  })

  it('PBI534-CP05: getWishlists, createWishlist, renameWishlist, deleteWishlist y gestión de productos', async () => {
    server.use(
      http.get(URL_LISTAS, () => HttpResponse.json([{ id: 'wl-1', nombre: 'Regalos', items: [] }])),
      http.post(URL_LISTAS, async ({ request }) =>
        HttpResponse.json({ id: 'wl-2', nombre: (await request.json()).nombre, items: [] }, { status: 201 }),
      ),
      http.patch(`${URL_LISTAS}/:id`, async ({ params, request }) =>
        HttpResponse.json({ id: params.id, nombre: (await request.json()).nombre, items: [] }),
      ),
      http.delete(`${URL_LISTAS}/:id`, () => new HttpResponse(null, { status: 204 })),
      http.post(`${URL_LISTAS}/:id/items`, async ({ params, request }) =>
        HttpResponse.json({ id: params.id, nombre: 'Regalos', items: [{ productoId: (await request.json()).productoId }] }),
      ),
      http.delete(`${URL_LISTAS}/:id/items/:productoId`, ({ params }) =>
        HttpResponse.json({ id: params.id, nombre: 'Regalos', items: [] }),
      ),
    )

    expect(await getWishlists()).toEqual([{ id: 'wl-1', nombre: 'Regalos', items: [] }])
    expect(await createWishlist('Oficina')).toEqual({ id: 'wl-2', nombre: 'Oficina', items: [] })
    expect(await renameWishlist('wl-1', 'Cumple')).toEqual({ id: 'wl-1', nombre: 'Cumple', items: [] })
    expect(await deleteWishlist('wl-1')).toBeNull()
    expect(await addItemToWishlist('wl-1', 'p-1')).toMatchObject({ items: [{ productoId: 'p-1' }] })
    expect(await removeItemFromWishlist('wl-1', 'p-1')).toMatchObject({ items: [] })
  })
})
