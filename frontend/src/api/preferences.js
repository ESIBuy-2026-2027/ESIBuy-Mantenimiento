import { request } from './client'

// Preferencias del cliente en sesión (HU46), en el backend bajo /api/customer/preferences (módulo preferencias): las
// categorías que le interesan, como { categorias: [id] }. El backend saca el cliente de la sesión.

// Solo devuelve las categorías elegidas que siguen activas; sin preferencias guardadas, una lista vacía.
export function getPreferences() {
  return request('/api/customer/preferences')
}

// Sustituye las categorías elegidas (una lista vacía quita todas) y responde con las guardadas. 400 con
// `code: VALIDATION_ERROR` si alguna no existe o no está activa.
export function savePreferences(categorias) {
  return request('/api/customer/preferences', { method: 'PUT', body: { categorias } })
}
