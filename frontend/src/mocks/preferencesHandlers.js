import { http, HttpResponse } from 'msw'
import { getUsuarioEnSesion } from './authHandlers'

// Preferencias de cada cliente de ejemplo (HU46): identificador del cliente → identificadores de sus categorías
// favoritas (AB#528). Como el backend real, viven en el servidor: se pierden al reiniciar los mocks.
const preferencias = new Map()

export function resetPreferencesMock() {
  preferencias.clear()
}

// Categorías activas del mock (las de GET /api/categorias de handlers.js)
const CATEGORIAS_ACTIVAS = new Set(['cat-1', 'cat-2'])

const rechazarSiNoEsCliente = () => {
  const usuario = getUsuarioEnSesion()
  if (!usuario) return new HttpResponse(null, { status: 401 })
  if (usuario.rol !== 'CLIENTE') return new HttpResponse(null, { status: 403 })
  return null
}

// Aplica un cambio a las favoritas del cliente en sesión (sin repetidas) y las devuelve
const cambiarFavoritas = (cambiar) => {
  const { id } = getUsuarioEnSesion()
  const favoritas = new Set(preferencias.get(id))
  cambiar(favoritas)
  preferencias.set(id, [...favoritas])
  return [...favoritas]
}

export const preferencesHandlers = [
  http.get('/api/customer/preferences', () => {
    const rechazo = rechazarSiNoEsCliente()
    if (rechazo) return rechazo
    return HttpResponse.json({ categorias: preferencias.get(getUsuarioEnSesion().id) ?? [] })
  }),

  // Marca una categoría como favorita. Una repetida no es un error y no cambia nada; una que no existe o no está activa
  // responde 404.
  http.post('/api/customer/preferences/categories', async ({ request }) => {
    const rechazo = rechazarSiNoEsCliente()
    if (rechazo) return rechazo

    const { categoriaId } = await request.json()
    if (typeof categoriaId !== 'string' || !categoriaId.trim()) {
      return HttpResponse.json({ code: 'VALIDATION_ERROR' }, { status: 400 })
    }
    if (!CATEGORIAS_ACTIVAS.has(categoriaId)) {
      return HttpResponse.json({ code: 'CATEGORY_NOT_FOUND' }, { status: 404 })
    }
    return HttpResponse.json({ categorias: cambiarFavoritas((favoritas) => favoritas.add(categoriaId)) })
  }),

  // Quita una categoría de las favoritas. Quitar una que no lo es no es un error.
  http.delete('/api/customer/preferences/categories/:categoriaId', ({ params }) => {
    const rechazo = rechazarSiNoEsCliente()
    if (rechazo) return rechazo
    return HttpResponse.json({ categorias: cambiarFavoritas((favoritas) => favoritas.delete(params.categoriaId)) })
  }),
]
