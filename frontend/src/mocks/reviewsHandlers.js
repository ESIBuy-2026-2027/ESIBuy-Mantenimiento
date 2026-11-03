import { http, HttpResponse } from 'msw'
import { getUsuarioEnSesion } from './authHandlers'

// Valoraciones de los productos (HU13), con el contrato del backend (T371, PR #179). Ningún producto tiene
// valoraciones al empezar: así el detalle enseña la media de ejemplo del catálogo.
const valoracionesPorProducto = new Map()

export const reviewsHandlers = [
  http.get('/api/products/:id/valoraciones', ({ params }) => {
    const usuario = getUsuarioEnSesion()
    if (!usuario) return new HttpResponse(null, { status: 401 })
    if (usuario.rol !== 'CLIENTE') return new HttpResponse(null, { status: 403 })

    return HttpResponse.json(valoracionesPorProducto.get(params.id) ?? [])
  }),
]
