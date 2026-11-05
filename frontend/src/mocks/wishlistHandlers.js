import { http, HttpResponse } from 'msw'
import { getUsuarioEnSesion } from './authHandlers'
import { PRODUCTOS, estaDisponible } from './productsHandlers'

// Lista de deseos de cada cliente de ejemplo (HU14): identificador del cliente → [{ productoId, addedAt }], en el
// orden en que se añadieron. Como el backend real, vive en el servidor: se pierde al reiniciar los mocks.
const listas = new Map()

export function resetWishlistMock() {
  listas.clear()
}

const listaDe = (usuario) => {
  if (!listas.has(usuario.id)) listas.set(usuario.id, [])
  return listas.get(usuario.id)
}

export const wishlistHandlers = [
  // Añade un producto a la lista del cliente en sesión. Uno repetido no es un error y la lista no cambia; uno que no
  // existe o no está disponible responde 404 (uno agotado sí se puede añadir).
  http.post('/api/customer/wishlist/items', async ({ request }) => {
    const usuario = getUsuarioEnSesion()
    if (!usuario) return new HttpResponse(null, { status: 401 })
    if (usuario.rol !== 'CLIENTE') return new HttpResponse(null, { status: 403 })

    const { productoId } = await request.json()
    if (typeof productoId !== 'string' || !productoId.trim()) {
      return HttpResponse.json({ code: 'VALIDATION_ERROR' }, { status: 400 })
    }
    if (!PRODUCTOS.some((producto) => producto.id === productoId && estaDisponible(producto))) {
      return HttpResponse.json({ code: 'PRODUCT_NOT_FOUND' }, { status: 404 })
    }

    const lista = listaDe(usuario)
    if (!lista.some((item) => item.productoId === productoId)) {
      lista.push({ productoId, addedAt: new Date().toISOString() })
    }
    return HttpResponse.json({ items: lista })
  }),
]
