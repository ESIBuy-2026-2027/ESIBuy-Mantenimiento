import { http, HttpResponse } from 'msw'
import { getUsuarioEnSesion } from './authHandlers'
import { PRODUCTOS, aCatalogo, datosComerciales, estaDisponible } from './productsHandlers'

// Favoritos (HU14) y listas de deseos (PBI 534) de cada cliente de ejemplo, por separado (AB#528):
// usuarioId -> { items } y usuarioId -> [{ id, nombre, items }]
const favoritos = new Map()
const listas = new Map()
const LISTA_POR_DEFECTO = 'Mi lista de deseos'
let siguienteListaId = 1

export function resetWishlistMock() {
  favoritos.clear()
  listas.clear()
  siguienteListaId = 1
}

const listasDe = (usuario) => {
  if (!listas.has(usuario.id)) listas.set(usuario.id, [])
  return listas.get(usuario.id)
}

// Un cliente siempre tiene al menos una lista: si no hay ninguna, se crea la de por defecto
const conListaPorDefecto = (todas) => {
  if (todas.length === 0) todas.push({ id: `wl-${siguienteListaId++}`, nombre: LISTA_POR_DEFECTO, items: [] })
  return todas
}

const favoritosDe = (usuario) => {
  if (!favoritos.has(usuario.id)) favoritos.set(usuario.id, { id: `fav-${usuario.id}`, nombre: 'Favoritos', items: [] })
  return favoritos.get(usuario.id)
}

const rechazarSiNoEsCliente = () => {
  const usuario = getUsuarioEnSesion()
  if (!usuario) return new HttpResponse(null, { status: 401 })
  if (usuario.rol !== 'CLIENTE') return new HttpResponse(null, { status: 403 })
  return null
}

const aListaDetallada = (lista) => ({
  id: lista.id,
  nombre: lista.nombre,
  items: lista.items.map(({ productoId, addedAt, cantidad }) => {
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
      cantidad: cantidad ?? 1,
    }
  }),
})

const nombreValido = (nombre) => typeof nombre === 'string' && nombre.trim().length > 0 && nombre.trim().length <= 80

export const wishlistHandlers = [
  http.get('/api/customer/wishlist', () => {
    const rechazo = rechazarSiNoEsCliente()
    if (rechazo) return rechazo
    return HttpResponse.json(aListaDetallada(favoritosDe(getUsuarioEnSesion())))
  }),

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

    const lista = favoritosDe(getUsuarioEnSesion())
    if (!lista.items.some((item) => item.productoId === productoId)) {
      lista.items.push({ productoId, addedAt: new Date().toISOString() })
    }
    return HttpResponse.json({ items: lista.items })
  }),

  http.delete('/api/customer/wishlist/items/:productoId', ({ params }) => {
    const rechazo = rechazarSiNoEsCliente()
    if (rechazo) return rechazo

    const lista = favoritosDe(getUsuarioEnSesion())
    const posicion = lista.items.findIndex((item) => item.productoId === params.productoId)
    if (posicion >= 0) lista.items.splice(posicion, 1)
    return HttpResponse.json(aListaDetallada(lista))
  }),

  // Endpoints multi-lista (PBI 534)
  http.get('/api/customer/wishlists', () => {
    const rechazo = rechazarSiNoEsCliente()
    if (rechazo) return rechazo
    return HttpResponse.json(conListaPorDefecto(listasDe(getUsuarioEnSesion())).map(aListaDetallada))
  }),

  http.post('/api/customer/wishlists', async ({ request }) => {
    const rechazo = rechazarSiNoEsCliente()
    if (rechazo) return rechazo

    const { nombre } = await request.json()
    if (!nombreValido(nombre)) {
      return HttpResponse.json({ code: 'VALIDATION_ERROR' }, { status: 400 })
    }
    const nueva = { id: `wl-${siguienteListaId++}`, nombre: nombre.trim(), items: [] }
    listasDe(getUsuarioEnSesion()).push(nueva)
    return HttpResponse.json(aListaDetallada(nueva), { status: 201 })
  }),

  http.patch('/api/customer/wishlists/:listaId', async ({ request, params }) => {
    const rechazo = rechazarSiNoEsCliente()
    if (rechazo) return rechazo

    const { nombre } = await request.json()
    if (!nombreValido(nombre)) {
      return HttpResponse.json({ code: 'VALIDATION_ERROR' }, { status: 400 })
    }
    const lista = listasDe(getUsuarioEnSesion()).find((item) => item.id === params.listaId)
    if (!lista) return HttpResponse.json({ code: 'WISHLIST_NOT_FOUND' }, { status: 404 })
    lista.nombre = nombre.trim()
    return HttpResponse.json(aListaDetallada(lista))
  }),

  http.delete('/api/customer/wishlists/:listaId', ({ params }) => {
    const rechazo = rechazarSiNoEsCliente()
    if (rechazo) return rechazo

    const todas = listasDe(getUsuarioEnSesion())
    const idx = todas.findIndex((item) => item.id === params.listaId)
    if (idx < 0) return HttpResponse.json({ code: 'WISHLIST_NOT_FOUND' }, { status: 404 })
    todas.splice(idx, 1)
    conListaPorDefecto(todas)
    return new HttpResponse(null, { status: 204 })
  }),

  http.post('/api/customer/wishlists/:listaId/items', async ({ request, params }) => {
    const rechazo = rechazarSiNoEsCliente()
    if (rechazo) return rechazo

    const { productoId } = await request.json()
    if (typeof productoId !== 'string' || !productoId.trim()) {
      return HttpResponse.json({ code: 'VALIDATION_ERROR' }, { status: 400 })
    }
    if (!PRODUCTOS.some((producto) => producto.id === productoId && estaDisponible(producto))) {
      return HttpResponse.json({ code: 'PRODUCT_NOT_FOUND' }, { status: 404 })
    }
    const lista = listasDe(getUsuarioEnSesion()).find((item) => item.id === params.listaId)
    if (!lista) return HttpResponse.json({ code: 'WISHLIST_NOT_FOUND' }, { status: 404 })
    if (!lista.items.some((item) => item.productoId === productoId)) {
      lista.items.push({ productoId, addedAt: new Date().toISOString() })
    }
    return HttpResponse.json(aListaDetallada(lista))
  }),

  http.delete('/api/customer/wishlists/:listaId/items/:productoId', ({ params }) => {
    const rechazo = rechazarSiNoEsCliente()
    if (rechazo) return rechazo

    const lista = listasDe(getUsuarioEnSesion()).find((item) => item.id === params.listaId)
    if (!lista) return HttpResponse.json({ code: 'WISHLIST_NOT_FOUND' }, { status: 404 })
    const idx = lista.items.findIndex((item) => item.productoId === params.productoId)
    if (idx >= 0) lista.items.splice(idx, 1)
    return HttpResponse.json(aListaDetallada(lista))
  }),

  http.patch('/api/customer/wishlists/:listaId/items/:productoId', async ({ request, params }) => {
    const rechazo = rechazarSiNoEsCliente()
    if (rechazo) return rechazo

    const lista = listasDe(getUsuarioEnSesion()).find((item) => item.id === params.listaId)
    if (!lista) return HttpResponse.json({ code: 'WISHLIST_NOT_FOUND' }, { status: 404 })

    const producto = PRODUCTOS.find((candidato) => candidato.id === params.productoId && estaDisponible(candidato))
    if (!producto) return HttpResponse.json({ code: 'PRODUCT_NOT_FOUND' }, { status: 404 })

    const { cantidad } = await request.json()
    const { stock } = datosComerciales(producto)
    if (!Number.isInteger(cantidad) || cantidad < 1 || cantidad > stock) {
      return HttpResponse.json({ code: 'VALIDATION_ERROR' }, { status: 400 })
    }

    const item = lista.items.find((entrada) => entrada.productoId === params.productoId)
    if (item) item.cantidad = cantidad
    return HttpResponse.json(aListaDetallada(lista))
  }),
]
