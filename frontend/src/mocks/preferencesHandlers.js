import { http, HttpResponse } from 'msw'
import { getUsuarioEnSesion } from './authHandlers'

// Preferencias de cada cliente de ejemplo (HU46): identificador del cliente → identificadores de las categorías que
// le interesan. Como el backend real, viven en el servidor: se pierden al reiniciar los mocks.
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

export const preferencesHandlers = [
  http.get('/api/customer/preferences', () => {
    const rechazo = rechazarSiNoEsCliente()
    if (rechazo) return rechazo
    return HttpResponse.json({ categorias: preferencias.get(getUsuarioEnSesion().id) ?? [] })
  }),

  // Sustituye las categorías elegidas: sin lista o con alguna que no está activa, 400 y no se guarda nada.
  http.put('/api/customer/preferences', async ({ request }) => {
    const rechazo = rechazarSiNoEsCliente()
    if (rechazo) return rechazo

    const { categorias } = await request.json()
    if (!Array.isArray(categorias) || !categorias.every((id) => CATEGORIAS_ACTIVAS.has(id))) {
      return HttpResponse.json({ code: 'VALIDATION_ERROR' }, { status: 400 })
    }
    const sinRepetidas = [...new Set(categorias)]
    preferencias.set(getUsuarioEnSesion().id, sinRepetidas)
    return HttpResponse.json({ categorias: sinRepetidas })
  }),
]
