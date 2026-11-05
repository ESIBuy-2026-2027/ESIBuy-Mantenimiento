import { request } from './client'

export function getAdminCategories() {
  return request('/api/admin/categorias')
}

// Borrado físico de una categoría (HU42). Responde 204 sin cuerpo; 409 si tiene productos o es la principal de algún
// vendedor, y 404 si ya no existe.
export function deleteAdminCategory(id) {
  return request(`/api/admin/categorias/${encodeURIComponent(id)}`, { method: 'DELETE' })
}

// Alta de una categoría (HU40) con nombre, descripción y estado (ACTIVA o INACTIVA). Responde 201 con la fila nueva
// del listado; 409 CATEGORY_NAME_ALREADY_EXISTS si el nombre ya existe.
export function createAdminCategory({ nombre, descripcion, estado }) {
  return request('/api/admin/categorias', { method: 'POST', body: { nombre, descripcion, estado } })
}

// Edición del nombre y la descripción de una categoría (HU41); el estado no se edita. Responde con la fila
// actualizada; 409 CATEGORY_NAME_ALREADY_EXISTS si el nombre es el de otra y 404 si ya no existe.
export function updateAdminCategory(id, { nombre, descripcion }) {
  return request(`/api/admin/categorias/${encodeURIComponent(id)}`, { method: 'PUT', body: { nombre, descripcion } })
}
