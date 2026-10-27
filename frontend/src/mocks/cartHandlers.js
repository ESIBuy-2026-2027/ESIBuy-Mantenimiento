import { http, HttpResponse } from 'msw'
import { getUsuarioEnSesion } from './authHandlers'
import { PRODUCTOS, datosComerciales } from './productsHandlers'

// Carrito de cada cliente de ejemplo: identificador del cliente → (identificador del producto → cantidad). Como el
// backend real, vive en el servidor: no se pierde al recargar la página, pero sí al reiniciar los mocks.
const carritos = new Map()

export function resetCartMock() {
  carritos.clear()
}

const redondear = (importe) => Math.round(importe * 100) / 100

// El carrito completo, agrupado por proveedor y con el total de cada grupo calculado aquí, como lo hará el servidor.
function aCarrito(lineasPorProducto) {
  const grupos = new Map()
  let totalProductos = 0

  for (const [productoId, cantidad] of lineasPorProducto) {
    const producto = PRODUCTOS.find((candidato) => candidato.id === productoId)
    const { vendedor, precioFinal, stock } = datosComerciales(producto)
    if (!grupos.has(vendedor.id)) grupos.set(vendedor.id, { vendedor, total: 0, lineas: [] })

    const subtotal = redondear(precioFinal * cantidad)
    const grupo = grupos.get(vendedor.id)
    grupo.lineas.push({
      productoId,
      nombre: producto.nombre,
      imagen: '',
      cantidad,
      precioUnitario: precioFinal,
      subtotal,
      stock,
    })
    grupo.total = redondear(grupo.total + subtotal)
    totalProductos += cantidad
  }

  const lista = [...grupos.values()]
  return { grupos: lista, totalProductos, total: redondear(lista.reduce((suma, grupo) => suma + grupo.total, 0)) }
}

// Quita del carrito los productos de un proveedor y devuelve ese grupo tal y como estaba (o null si no había nada).
// Lo usa la creación de pedidos (HU10), que se lleva del carrito lo que compra.
export function retirarGrupoDelCarrito(usuarioId, vendedorId) {
  const carrito = carritos.get(usuarioId)
  const grupo = carrito && aCarrito(carrito).grupos.find((candidato) => candidato.vendedor.id === vendedorId)
  if (!grupo) return null
  for (const linea of grupo.lineas) carrito.delete(linea.productoId)
  return grupo
}

// Devuelve la respuesta de error si quien pregunta no es un cliente, o null si puede continuar.
export function rechazarSiNoEsCliente() {
  const usuario = getUsuarioEnSesion()
  if (!usuario) return new HttpResponse(null, { status: 401 })
  if (usuario.rol !== 'CLIENTE') return new HttpResponse(null, { status: 403 })
  return null
}

const carritoDe = (usuario) => {
  if (!carritos.has(usuario.id)) carritos.set(usuario.id, new Map())
  return carritos.get(usuario.id)
}

const productoDisponible = (id) => PRODUCTOS.find((candidato) => candidato.id === id && candidato.disponible)

const esCantidadValida = (cantidad) => Number.isInteger(cantidad) && cantidad >= 1

const sinExistencias = () => HttpResponse.json({ code: 'INSUFFICIENT_STOCK' }, { status: 409 })
const peticionIncorrecta = () => HttpResponse.json({ code: 'VALIDATION_ERROR' }, { status: 400 })

export const cartHandlers = [
  http.get('/api/cart', () => {
    const rechazo = rechazarSiNoEsCliente()
    if (rechazo) return rechazo
    return HttpResponse.json(aCarrito(carritoDe(getUsuarioEnSesion())))
  }),

  // Añade unidades a las que ya hubiera (HU9).
  http.post('/api/cart/items', async ({ request }) => {
    const rechazo = rechazarSiNoEsCliente()
    if (rechazo) return rechazo

    const { productoId, cantidad } = await request.json()
    if (!esCantidadValida(cantidad)) return peticionIncorrecta()
    const producto = productoDisponible(productoId)
    if (!producto) return new HttpResponse(null, { status: 404 })

    const carrito = carritoDe(getUsuarioEnSesion())
    const total = (carrito.get(productoId) ?? 0) + cantidad
    if (total > producto.stock) return sinExistencias()

    carrito.set(productoId, total)
    return HttpResponse.json(aCarrito(carrito))
  }),

  // Fija la cantidad de un producto que ya está en el carrito.
  http.put('/api/cart/items/:id', async ({ params, request }) => {
    const rechazo = rechazarSiNoEsCliente()
    if (rechazo) return rechazo

    const { cantidad } = await request.json()
    if (!esCantidadValida(cantidad)) return peticionIncorrecta()
    const carrito = carritoDe(getUsuarioEnSesion())
    const producto = productoDisponible(params.id)
    if (!producto || !carrito.has(params.id)) return new HttpResponse(null, { status: 404 })
    if (cantidad > producto.stock) return sinExistencias()

    carrito.set(params.id, cantidad)
    return HttpResponse.json(aCarrito(carrito))
  }),

  http.delete('/api/cart/items/:id', ({ params }) => {
    const rechazo = rechazarSiNoEsCliente()
    if (rechazo) return rechazo

    const carrito = carritoDe(getUsuarioEnSesion())
    carrito.delete(params.id)
    return HttpResponse.json(aCarrito(carrito))
  }),

  http.delete('/api/cart', () => {
    const rechazo = rechazarSiNoEsCliente()
    if (rechazo) return rechazo

    const carrito = carritoDe(getUsuarioEnSesion())
    carrito.clear()
    return HttpResponse.json(aCarrito(carrito))
  }),
]
