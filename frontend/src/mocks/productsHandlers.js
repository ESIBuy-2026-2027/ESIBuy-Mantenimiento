import { http, HttpResponse } from 'msw'
import { getUsuarioEnSesion } from './authHandlers'
import { leerPagina, normalizar, paginar } from './paginar'

export const VENDEDOR_CON_PRODUCTOS = 'u-2'
export const OTRO_VENDEDOR = 'u-99'

export const CATEGORIAS = new Map([
  ['cat-1', 'Tecnología'],
  ['cat-2', 'Hogar'],
])

// [nombre, precio, stock, disponible, categorías]. El vendedor u-2 tiene 23 (3 páginas de 10);
// u-5 (vendedor-vacio@esibuy.test) no tiene ninguno; u-99 tiene los últimos 3, que u-2 nunca debe ver.
export const PRODUCTOS = [
  ['Auriculares inalámbricos', 59.9, 34, true, ['cat-1']],
  ['Teclado mecánico', 89.5, 12, true, ['cat-1']],
  ['Ratón ergonómico', 34.99, 0, true, ['cat-1']],
  ['Monitor 27 pulgadas', 249, 7, true, ['cat-1']],
  ['Cargador USB-C 65 W', 29.95, 80, true, ['cat-1']],
  ['Webcam Full HD', 49, 0, false, ['cat-1']],
  ['Altavoz Bluetooth', 39.9, 22, true, ['cat-1', 'cat-2']],
  ['Lámpara de escritorio LED', 24.5, 41, true, ['cat-2', 'cat-1']],
  ['Juego de sábanas algodón', 45, 15, true, ['cat-2']],
  ['Cafetera de cápsulas', 79.99, 9, true, ['cat-2']],
  ['Aspirador de mano', 64.9, 3, true, ['cat-2']],
  ['Set de cuchillos de cocina', 54, 18, false, ['cat-2']],
  ['Robot de cocina', 199, 5, true, ['cat-2']],
  ['Freidora de aire', 89, 0, true, ['cat-2']],
  ['Disco duro externo 2 TB', 84.9, 26, true, ['cat-1']],
  ['Memoria USB 128 GB', 14.99, 120, true, ['cat-1']],
  ['Tablet 10 pulgadas', 179, 11, true, ['cat-1']],
  ['Soporte para portátil', 27.5, 33, true, ['cat-1', 'cat-2']],
  ['Cortinas opacas', 39, 14, true, ['cat-2']],
  ['Alfombra de salón', 99, 6, false, ['cat-2']],
  ['Router Wi-Fi 6', 119, 19, true, ['cat-1']],
  ['Regleta con protección', 18.9, 52, true, ['cat-1', 'cat-2']],
  ['Hub USB-C 7 en 1', 44.9, 28, true, ['cat-1']],
  ['Smartwatch deportivo', 129, 8, true, ['cat-1'], OTRO_VENDEDOR],
  ['Colchón viscoelástico', 349, 4, true, ['cat-2'], OTRO_VENDEDOR],
  ['Purificador de aire', 159, 10, true, ['cat-2', 'cat-1'], OTRO_VENDEDOR],
].map(([nombre, precio, stock, disponible, categorias, vendedorId = VENDEDOR_CON_PRODUCTOS], indice) => ({
  id: `p-${indice + 1}`,
  nombre,
  precio,
  stock,
  disponible,
  categorias,
  vendedorId,
  descripcion: `Descripción de ${nombre}.`,
  imagen: '',
  // Algunos productos de ejemplo tienen descuentos activos para probar la edición.
  descuento: { activo: indice % 5 === 0, porcentaje: indice % 5 === 0 ? 10 : null },
  descuentoPremium: { activo: indice % 7 === 0, porcentaje: indice % 7 === 0 ? 15 : null },
}))

export const aRespuesta = ({ id, nombre, precio, stock, disponible, categorias }) => ({
  id,
  nombre,
  precio,
  stock,
  disponible,
  categorias: categorias.map((idCategoria) => CATEGORIAS.get(idCategoria)),
})

// Detalle completo del producto, con los datos que necesita el formulario de edición.
const aDetalle = ({ id, nombre, descripcion, precio, stock, disponible, categorias, imagen, descuento, descuentoPremium }) => ({
  id,
  nombre,
  descripcion,
  precio,
  stock,
  disponible,
  categoriaIds: categorias,
  imagen,
  descuento,
  descuentoPremium,
})

const esPorcentajeValido = (descuento) =>
  !descuento?.activo || (Number(descuento.porcentaje) > 0 && Number(descuento.porcentaje) <= 100)

// Reglas mínimas de la HU31 que el backend real también debe imponer.
function esProductoValido(producto) {
  return (
    typeof producto?.nombre === 'string' &&
    producto.nombre.trim() !== '' &&
    Array.isArray(producto.categoriaIds) &&
    producto.categoriaIds.length > 0 &&
    Number(producto.precio) > 0 &&
    Number.isInteger(producto.stock) &&
    producto.stock >= 0 &&
    esPorcentajeValido(producto.descuento) &&
    esPorcentajeValido(producto.descuentoPremium)
  )
}

// Devuelve la respuesta de error si quien pregunta no es vendedor, o null si puede continuar.
function rechazarSiNoEsVendedor() {
  const usuario = getUsuarioEnSesion()
  if (!usuario) return new HttpResponse(null, { status: 401 })
  if (usuario.rol !== 'VENDEDOR') return new HttpResponse(null, { status: 403 })
  return null
}

// Solo el dueño del producto puede verlo o tocarlo; para el resto no existe.
function conProductoPropio(params, respuestaPara) {
  const rechazo = rechazarSiNoEsVendedor()
  if (rechazo) return rechazo

  const producto = PRODUCTOS.find(
    (candidato) => candidato.id === params.id && candidato.vendedorId === getUsuarioEnSesion().id,
  )
  return producto ? respuestaPara(producto) : new HttpResponse(null, { status: 404 })
}

const errorDeValidacion = () => HttpResponse.json({ code: 'VALIDATION_ERROR' }, { status: 400 })

// Las escrituras (POST, PUT y DELETE) validan y responden como el backend real, pero NO modifican
// los datos de ejemplo: a efectos prácticos, el frontend no añade ni elimina productos.
export const productsHandlers = [
  http.get('/api/seller/products/:id', ({ params }) =>
    conProductoPropio(params, (producto) => HttpResponse.json(aDetalle(producto))),
  ),

  http.post('/api/seller/products', async ({ request }) => {
    const rechazo = rechazarSiNoEsVendedor()
    if (rechazo) return rechazo

    const producto = await request.json()
    return esProductoValido(producto)
      ? HttpResponse.json({ ...producto, id: 'p-nuevo' }, { status: 201 })
      : errorDeValidacion()
  }),

  http.put('/api/seller/products/:id', ({ params, request }) =>
    conProductoPropio(params, async (existente) => {
      const producto = await request.json()
      return esProductoValido(producto) ? HttpResponse.json({ ...producto, id: existente.id }) : errorDeValidacion()
    }),
  ),

  http.delete('/api/seller/products/:id', ({ params }) =>
    conProductoPropio(params, () => new HttpResponse(null, { status: 204 })),
  ),

  http.get('/api/seller/products', ({ request }) => {
    const rechazo = rechazarSiNoEsVendedor()
    if (rechazo) return rechazo

    const usuario = getUsuarioEnSesion()
    const params = new URL(request.url).searchParams
    const busqueda = normalizar(params.get('search') ?? '')
    const categoria = params.get('category')
    const coincidentes = PRODUCTOS.filter(
      (producto) =>
        producto.vendedorId === usuario.id &&
        normalizar(producto.nombre).includes(busqueda) &&
        (!categoria || producto.categorias.includes(categoria)),
    )
    return HttpResponse.json(paginar(coincidentes, leerPagina(params), aRespuesta))
  }),
]
