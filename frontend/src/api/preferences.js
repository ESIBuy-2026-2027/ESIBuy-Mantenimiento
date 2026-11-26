import { request } from './client'

// Preferencias del cliente en sesión (HU46), en el backend bajo /api/customer/preferences (módulo preferencias): sus
// categorías favoritas (AB#528), como { categorias: [id] }. El backend saca el cliente de la sesión.

// Solo devuelve las favoritas que siguen activas; sin ninguna, una lista vacía.
export function getPreferences() {
  return request('/api/customer/preferences')
}

// Marca una categoría como favorita y responde con las favoritas, como `getPreferences`. Si ya lo era, no es un error.
// 404 con `code: CATEGORY_NOT_FOUND` si la categoría no existe o no está activa.
export function addFavoriteCategory(categoryId) {
  return request('/api/customer/preferences/categories', { method: 'POST', body: { categoriaId: categoryId } })
}

// Quita una categoría de las favoritas y responde con las que quedan. Quitar una que no lo es no es un error.
export function removeFavoriteCategory(categoryId) {
  return request(`/api/customer/preferences/categories/${encodeURIComponent(categoryId)}`, { method: 'DELETE' })
}
