import { request } from './client'

// searchBy, sort y order son opcionales: el listado general (HU19) no los envía y busca también por email.
export function listUsers({ search, role, status, page, size, searchBy, sort, order }) {
  const params = new URLSearchParams({ page, size })
  const filtros = { search: search?.trim(), role, status, searchBy, sort, order }

  for (const [nombre, valor] of Object.entries(filtros)) {
    if (valor) {
      params.set(nombre, valor)
    }
  }

  return request(`/api/admin/usuarios?${params}`)
}

// Activa (HU24) o desactiva (HU23) una cuenta: estado es 'ACTIVO' o 'DESACTIVADO'.
export function updateUserStatus(userId, estado) {
  return request(`/api/admin/usuarios/${encodeURIComponent(userId)}/status`, {
    method: 'PATCH',
    body: { estado },
  })
}
