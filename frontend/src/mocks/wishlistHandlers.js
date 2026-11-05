import { http, HttpResponse } from 'msw'
import { getUsuarioEnSesion } from './authHandlers'
import { PRODUCTOS, aCatalogo, datosComerciales, estaDisponible } from './productsHandlers'

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

const rechazarSiNoEsCliente = () => {
  const usuario = getUsuarioEnSesion()
  if (!usuario) return new HttpResponse(null, { status: 401 })
  if (usuario.rol !== 'CLIENTE') return new HttpResponse(null, { status: 403 })
  return null
}

// La lista con los datos actuales de cada producto, como la devuelve el backend al consultarla.
const aListaDetallada = (lista) => ({
  items: lista.map(({ productoId, addedAt }) => {
    const producto = PRODUCTOS.find((candidato) => candidato.id === productoId)
    const { precioFinal, stock } = datosComerciales(producto)
    return {
      productoId,
      nombre: producto.nombre,
      imagen: aCatalogo(producto).imagen,
      precioFinal,
      stock,
      disponible: estaDisponible(producto),
      addedAt,
    }
  }),
})

export const wishlistHandlers = [
  // La lista del cliente en sesión, con los datos actuales de cada producto y en el orden en que se añadieron.
  http.get('/api/customer/wishlist', () => {
    const rechazo = rechazarSiNoEsCliente()
    if (rechazo) return rechazo
    return HttpResponse.json(aListaDetallada(listaDe(getUsuarioEnSesion())))
  }),

  // Añade un producto a la lista del cliente en sesión. Uno repetido no es un error y la lista no cambia; uno que no
  // existe o no está disponible responde 404 (uno agotado sí se puede añadir).
  http.post('/api/customer/wishlist/items', async ({ request }) => {
    const rechazo = rechazarSiNoEsCliente()
    if (rechazo) return rechazo

    const { productoId } = await request.json()
    if (typeof productoId !== 'string' || !productoId.trim()) {
      return HttpResponse.json({ code: 'VALIDATION_ERROR' }, { status: 400 })
    }
    if (!PRODUCTOS.some((producto) => producto.id === productoId && estaDisponible(producto))) {
      return HttpResponse.json({ code: 'PRODUCT_NOT_FOUND' }, { status: 404 })
    }

    const lista = listaDe(getUsuarioEnSesion())
    if (!lista.some((item) => item.productoId === productoId)) {
      lista.push({ productoId, addedAt: new Date().toISOString() })
    }
    return HttpResponse.json({ items: lista })
  }),

  // Quita un producto de la lista, esté o no disponible, y responde con la lista detallada. Quitar uno que no está no
  // es un error.
  http.delete('/api/customer/wishlist/items/:productoId', ({ params }) => {
    const rechazo = rechazarSiNoEsCliente()
    if (rechazo) return rechazo

    const lista = listaDe(getUsuarioEnSesion())
    const posicion = lista.findIndex((item) => item.productoId === params.productoId)
    if (posicion >= 0) lista.splice(posicion, 1)
    return HttpResponse.json(aListaDetallada(lista))
  }),
]
