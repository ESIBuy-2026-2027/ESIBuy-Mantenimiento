import { request } from './client'

export function listUsers({ search, page, size }) {
  const params = new URLSearchParams({ page, size })
  const busqueda = search?.trim()
  if (busqueda) {
    params.set('busqueda', busqueda)
  }
  return request(`/api/admin/usuarios?${params}`)
}
