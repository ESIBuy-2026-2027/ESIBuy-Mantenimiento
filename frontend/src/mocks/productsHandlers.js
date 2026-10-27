import { http, HttpResponse } from 'msw'
import { getUsuarioEnSesion } from './authHandlers'
import { leerPagina, normalizar, paginar } from './paginar'

export const VENDEDOR_CON_PRODUCTOS = 'u-2'
export const OTRO_VENDEDOR = 'u-99'

export const CATEGORIAS = new Map([
  ['cat-1', 'Tecnología'],
  ['cat-2', 'Hogar'],
])

// [nombre, precio, stock, disponible, categoría]. El vendedor u-2 tiene 23 (3 páginas de 10);
// u-5 (vendedor-vacio@esibuy.test) no tiene ninguno; u-99 tiene los últimos 3, que u-2 nunca debe ver.
export const PRODUCTOS = [
  ['Auriculares inalámbricos', 59.9, 34, true, 'cat-1'],
  ['Teclado mecánico', 89.5, 12, true, 'cat-1'],
  ['Ratón ergonómico', 34.99, 0, true, 'cat-1'],
  ['Monitor 27 pulgadas', 249, 7, true, 'cat-1'],
  ['Cargador USB-C 65 W', 29.95, 80, true, 'cat-1'],
  ['Webcam Full HD', 49, 0, false, 'cat-1'],
  ['Altavoz Bluetooth', 39.9, 22, true, 'cat-1'],
  ['Lámpara de escritorio LED', 24.5, 41, true, 'cat-2'],
  ['Juego de sábanas algodón', 45, 15, true, 'cat-2'],
  ['Cafetera de cápsulas', 79.99, 9, true, 'cat-2'],
  ['Aspirador de mano', 64.9, 3, true, 'cat-2'],
  ['Set de cuchillos de cocina', 54, 18, false, 'cat-2'],
  ['Robot de cocina', 199, 5, true, 'cat-2'],
  ['Freidora de aire', 89, 0, true, 'cat-2'],
  ['Disco duro externo 2 TB', 84.9, 26, true, 'cat-1'],
  ['Memoria USB 128 GB', 14.99, 120, true, 'cat-1'],
  ['Tablet 10 pulgadas', 179, 11, true, 'cat-1'],
  ['Soporte para portátil', 27.5, 33, true, 'cat-1'],
  ['Cortinas opacas', 39, 14, true, 'cat-2'],
  ['Alfombra de salón', 99, 6, false, 'cat-2'],
  ['Router Wi-Fi 6', 119, 19, true, 'cat-1'],
  ['Regleta con protección', 18.9, 52, true, 'cat-1'],
  ['Hub USB-C 7 en 1', 44.9, 28, true, 'cat-1'],
  ['Smartwatch deportivo', 129, 8, true, 'cat-1', OTRO_VENDEDOR],
  ['Colchón viscoelástico', 349, 4, true, 'cat-2', OTRO_VENDEDOR],
  ['Purificador de aire', 159, 10, true, 'cat-2', OTRO_VENDEDOR],
].map(([nombre, precio, stock, disponible, categoria, vendedorId = VENDEDOR_CON_PRODUCTOS], indice) => ({
  id: `p-${indice + 1}`,
  nombre,
  precio,
  stock,
  disponible,
  categoria,
  vendedorId,
  descripcion: `Descripción de ${nombre}.`,
  imagen: '',
  // Algunos productos de ejemplo tienen descuentos activos para probar la edición.
  descuento: { activo: indice % 5 === 0, porcentaje: indice % 5 === 0 ? 10 : null },
  descuentoPremium: { activo: indice % 7 === 0, porcentaje: indice % 7 === 0 ? 15 : null },
}))

export const aRespuesta = ({ id, nombre, precio, stock, disponible, categoria }) => ({
  id,
  nombre,
  precio,
  stock,
  disponible,
  // Como el backend real: el identificador de la categoría, no su nombre.
  categoriaId: categoria,
})

const TAMANO_CATALOGO = 10

// Imagen de ejemplo: un dibujo con la inicial del producto, para que el catálogo se vea sin depender de internet.
const COLORES_IMAGEN = ['#0f6cbd', '#0e700e', '#c239b3', '#da3b01', '#005b70', '#8764b8']
const imagenDeEjemplo = (nombre, numero) => {
  const color = COLORES_IMAGEN[numero % COLORES_IMAGEN.length]
  const svg =
    `<svg xmlns="http://www.w3.org/2000/svg" width="640" height="400" viewBox="0 0 640 400">` +
    `<rect width="640" height="400" fill="${color}"/>` +
    `<text x="320" y="240" font-family="Segoe UI, Arial" font-size="160" font-weight="600" fill="#ffffff" ` +
    `text-anchor="middle">${nombre.charAt(0)}</text></svg>`
  return `data:image/svg+xml,${encodeURIComponent(svg)}`
}

// Para tener variedad sin escribirla a mano: cada séptimo producto no tiene valoraciones y cada noveno, imagen.
// Las demás valoraciones salen de su número, así que son siempre las mismas.
const aCatalogo = ({ id, nombre, categoria }) => {
  const numero = Number(id.slice(2))
  const sinValorar = numero % 7 === 0
  return {
    id,
    nombre,
    imagen: numero % 9 === 0 ? '' : imagenDeEjemplo(nombre, numero),
    categoriaId: categoria,
    valoracion: sinValorar ? null : Math.round(25 + ((numero * 37) % 26)) / 10,
    numValoraciones: sinValorar ? 0 : 3 + ((numero * 13) % 90),
  }
}

// Nombre comercial de cada vendedor de los productos de ejemplo (el mismo que en el listado de vendedores del
// administrador; no se importa de allí porque ese archivo ya importa este).
const NOMBRES_COMERCIALES = {
  [VENDEDOR_CON_PRODUCTOS]: 'TecnoVera',
  [OTRO_VENDEDOR]: 'Otero Hogar y Tecnología',
}

// El descuento que le corresponde al cliente: el premium si es Premium y lo tiene; si no, el normal. El servidor es
// quien lo calcula, el frontend solo lo pinta. Los clientes de ejemplo son Estándar.
function descuentoDelCliente(producto, tipoCliente = 'ESTANDAR') {
  if (tipoCliente === 'PREMIUM' && producto.descuentoPremium.activo) return producto.descuentoPremium.porcentaje
  return producto.descuento.activo ? producto.descuento.porcentaje : null
}

// Detalle de un producto del catálogo para el cliente (HU8).
function aDetalleDeCatalogo(producto) {
  const { imagen, categoriaId, valoracion, numValoraciones } = aCatalogo(producto)
  const porcentaje = descuentoDelCliente(producto)
  return {
    id: producto.id,
    nombre: producto.nombre,
    descripcion: producto.descripcion,
    imagen,
    categoriaId,
    vendedor: { id: producto.vendedorId, nombreComercial: NOMBRES_COMERCIALES[producto.vendedorId] ?? 'Vendedor' },
    precio: producto.precio,
    descuento: porcentaje === null ? null : { porcentaje },
    precioFinal: porcentaje === null ? producto.precio : Math.round(producto.precio * (100 - porcentaje)) / 100,
    stock: producto.stock,
    disponible: producto.disponible,
    valoracion,
    numValoraciones,
  }
}

const comparar = (a, b) => a.localeCompare(b, 'es')

// Como el backend: por nombre (por defecto) o por valoración (sin valoraciones cuentan como 0). Los empates se
// desempatan por nombre, siempre de la A a la Z.
function ordenDelCatalogo(criterio, sentido) {
  const signo = sentido === 'desc' ? -1 : 1
  const porNombre = (a, b) => comparar(a.nombre, b.nombre)
  const porCriterio = {
    valoracion: (a, b) => (a.valoracion ?? 0) - (b.valoracion ?? 0),
  }[criterio]

  if (!porCriterio) return (a, b) => signo * porNombre(a, b)
  return (a, b) => signo * porCriterio(a, b) || porNombre(a, b)
}

// Detalle completo del producto, con los datos que necesita el formulario de edición.
const aDetalle = ({ id, nombre, descripcion, precio, stock, disponible, categoria, imagen, descuento, descuentoPremium }) => ({
  id,
  nombre,
  descripcion,
  precio,
  stock,
  disponible,
  categoriaId: categoria,
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
    CATEGORIAS.has(producto.categoriaId) &&
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

// Como el backend (HU31): un vendedor no puede tener dos productos con el mismo nombre, sin distinguir mayúsculas
// ni contar los espacios de los extremos. Al editar, el propio producto no cuenta.
const nombreEnUso = (nombre, idEditado = null) =>
  PRODUCTOS.some(
    (producto) =>
      producto.vendedorId === getUsuarioEnSesion().id &&
      producto.id !== idEditado &&
      producto.nombre.toLowerCase() === nombre.trim().toLowerCase(),
  )

const errorDeNombreEnUso = () =>
  HttpResponse.json(
    { message: 'El producto ya existe con el mismo nombre', code: 'PRODUCT_NAME_ALREADY_EXISTS' },
    { status: 409 },
  )

// Valida el producto y, si es correcto, comprueba que su nombre no esté ya en uso antes de responder.
function responderAlGuardar(producto, respuestaCorrecta, idEditado) {
  if (!esProductoValido(producto)) return errorDeValidacion()
  if (nombreEnUso(producto.nombre, idEditado)) return errorDeNombreEnUso()
  return respuestaCorrecta()
}

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
    return responderAlGuardar(producto, () => HttpResponse.json({ ...producto, id: 'p-nuevo' }, { status: 201 }))
  }),

  http.put('/api/seller/products/:id', ({ params, request }) =>
    conProductoPropio(params, async (existente) => {
      const producto = await request.json()
      return responderAlGuardar(producto, () => HttpResponse.json({ ...producto, id: existente.id }), existente.id)
    }),
  ),

  http.delete('/api/seller/products/:id', ({ params }) =>
    conProductoPropio(params, () => new HttpResponse(null, { status: 204 })),
  ),

  // Catálogo del cliente (HU7): los productos disponibles de todos los vendedores.
  http.get('/api/products', ({ request }) => {
    const usuario = getUsuarioEnSesion()
    if (!usuario) return new HttpResponse(null, { status: 401 })
    if (usuario.rol !== 'CLIENTE') return new HttpResponse(null, { status: 403 })

    const params = new URL(request.url).searchParams
    const busqueda = normalizar(params.get('search') ?? '')
    const categoria = params.get('category')
    const minima = Number(params.get('minRating')) || 0
    const coincidentes = PRODUCTOS.filter((producto) => producto.disponible)
      .map((producto) => aCatalogo(producto))
      .filter(
        (producto) =>
          normalizar(producto.nombre).includes(busqueda) &&
          (!categoria || producto.categoriaId === categoria) &&
          (producto.valoracion ?? 0) >= minima,
      )
      .sort(ordenDelCatalogo(params.get('sort'), params.get('order')))
    return HttpResponse.json(paginar(coincidentes, leerPagina(params, TAMANO_CATALOGO)))
  }),

  // Detalle de un producto del catálogo (HU8): solo los disponibles; el resto, como si no existieran.
  http.get('/api/products/:id', ({ params }) => {
    const usuario = getUsuarioEnSesion()
    if (!usuario) return new HttpResponse(null, { status: 401 })
    if (usuario.rol !== 'CLIENTE') return new HttpResponse(null, { status: 403 })

    const producto = PRODUCTOS.find((candidato) => candidato.id === params.id && candidato.disponible)
    return producto ? HttpResponse.json(aDetalleDeCatalogo(producto)) : new HttpResponse(null, { status: 404 })
  }),

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
        (!categoria || producto.categoria === categoria),
    )
    return HttpResponse.json(paginar(coincidentes, leerPagina(params), aRespuesta))
  }),
]
