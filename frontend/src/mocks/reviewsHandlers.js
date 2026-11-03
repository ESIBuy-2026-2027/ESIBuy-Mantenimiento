import { http, HttpResponse } from 'msw'
import { getUsuarioEnSesion } from './authHandlers'

// Valoraciones de los productos (HU13), con el contrato del backend (T371, PR #179). Son la fuente de la media y el
// número de valoraciones del catálogo (productsHandlers), como hará el backend: así el catálogo, el detalle y la lista
// siempre dicen lo mismo.

const VALORACIONES_POR_PRODUCTO = 10
// Comentarios de ejemplo según la puntuación; los vacíos son valoraciones sin comentario
const COMENTARIOS = {
  5: ['Muy contento con la compra, cumple lo que promete.', '', 'Llegó bien embalado y rápido.'],
  4: ['Buena calidad por el precio.', ''],
  3: ['Esperaba algo más, pero está bien.', ''],
  2: ['No me ha convencido.', ''],
}

// Para tener variedad sin escribirla a mano: cada séptimo producto no tiene valoraciones; el resto, 10 con una media
// que sale de su número (de 2,5 a 5,0), así que son siempre las mismas. Con 10 puntuaciones enteras de 1 a 5 la media
// es exacta con un decimal: 4,7 son siete 5 y tres 4.
function valoracionesDeEjemplo(productoId) {
  const numero = Number(productoId.slice(2))
  if (!Number.isInteger(numero) || numero % 7 === 0) return []

  const suma = 25 + ((numero * 37) % 26)
  const base = Math.floor(suma / VALORACIONES_POR_PRODUCTO)
  const conUnPuntoMas = suma % VALORACIONES_POR_PRODUCTO
  return Array.from({ length: VALORACIONES_POR_PRODUCTO }, (_, indice) => {
    const puntuacion = indice < conUnPuntoMas ? base + 1 : base
    const comentarios = COMENTARIOS[puntuacion]
    return {
      id: `v-${numero}-${indice + 1}`,
      productoId,
      puntuacion,
      comentario: comentarios[(numero + indice) % comentarios.length],
    }
  })
}

// Valoraciones enviadas durante la sesión (POST), por producto, y quién ha valorado qué ("u-3:p-2")
let valoracionesNuevas = new Map()
let valoradoPor = new Set()

export function resetReviewsMock() {
  valoracionesNuevas = new Map()
  valoradoPor = new Set()
}

export const valoracionesDe = (productoId) => [
  ...valoracionesDeEjemplo(productoId),
  ...(valoracionesNuevas.get(productoId) ?? []),
]

// Productos que cada cliente ha comprado: los de sus pedidos de ejemplo que no están cancelados (ordersHandlers).
// Carlos (cliente@esibuy.test) puede valorar los auriculares (p-1), el teclado (p-2) y el smartwatch (p-24).
const COMPRAS_DE_EJEMPLO = new Map([['u-3', new Set(['p-1', 'p-2', 'p-24'])]])

const MAXIMO_COMENTARIO = 500
const esValida = ({ puntuacion, comentario = '' }) =>
  Number.isInteger(puntuacion) &&
  puntuacion >= 1 &&
  puntuacion <= 5 &&
  typeof comentario === 'string' &&
  comentario.trim().length <= MAXIMO_COMENTARIO

// Media y número de valoraciones de un producto, como los calculará el backend para el catálogo
export function resumenDeValoraciones(productoId) {
  const valoraciones = valoracionesDe(productoId)
  if (valoraciones.length === 0) return { valoracion: null, numValoraciones: 0 }
  const suma = valoraciones.reduce((total, valoracion) => total + valoracion.puntuacion, 0)
  return { valoracion: Math.round((suma / valoraciones.length) * 10) / 10, numValoraciones: valoraciones.length }
}

export const reviewsHandlers = [
  http.get('/api/products/:id/valoraciones', ({ params }) => {
    const usuario = getUsuarioEnSesion()
    if (!usuario) return new HttpResponse(null, { status: 401 })
    if (usuario.rol !== 'CLIENTE') return new HttpResponse(null, { status: 403 })

    return HttpResponse.json(valoracionesDe(params.id))
  }),

  // Valorar un producto (HU13): como el backend, solo si el cliente lo ha comprado y una vez por producto
  http.post('/api/products/:id/valoraciones', async ({ params, request }) => {
    const usuario = getUsuarioEnSesion()
    if (!usuario) return new HttpResponse(null, { status: 401 })
    if (usuario.rol !== 'CLIENTE') return new HttpResponse(null, { status: 403 })

    const datos = await request.json()
    if (!esValida(datos)) return HttpResponse.json({ code: 'VALIDATION_ERROR' }, { status: 400 })
    if (!COMPRAS_DE_EJEMPLO.get(usuario.id)?.has(params.id)) {
      return HttpResponse.json({ code: 'PRODUCT_NOT_PURCHASED' }, { status: 403 })
    }
    const clave = `${usuario.id}:${params.id}`
    if (valoradoPor.has(clave)) return HttpResponse.json({ code: 'REVIEW_ALREADY_EXISTS' }, { status: 409 })

    const valoracion = {
      id: `v-nueva-${valoradoPor.size + 1}`,
      productoId: params.id,
      puntuacion: datos.puntuacion,
      comentario: (datos.comentario ?? '').trim(),
    }
    valoradoPor.add(clave)
    valoracionesNuevas.set(params.id, [...(valoracionesNuevas.get(params.id) ?? []), valoracion])
    return HttpResponse.json(valoracion, { status: 201 })
  }),
]
