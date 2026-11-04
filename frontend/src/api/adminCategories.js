import { request } from './client'

export function getAdminCategories() {
  return request('/api/admin/categorias')
}

// Borrado físico de una categoría (HU42). Responde 204 sin cuerpo; 409 si tiene productos o es la principal de algún
// vendedor, y 404 si ya no existe.
export function deleteAdminCategory(id) {
  return request(`/api/admin/categorias/${encodeURIComponent(id)}`, { method: 'DELETE' })
}
