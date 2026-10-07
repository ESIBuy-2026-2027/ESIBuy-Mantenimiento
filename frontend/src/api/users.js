import { request } from './client'

export function listUsers({ search, role, page, size }) {
  const params = new URLSearchParams({ page, size })
  const busqueda = search?.trim()
  if (busqueda) {
    params.set('busqueda', busqueda)
  }
  if (role) {
    params.set('rol', role)
  }
  return request(`/api/admin/usuarios?${params}`)
}
