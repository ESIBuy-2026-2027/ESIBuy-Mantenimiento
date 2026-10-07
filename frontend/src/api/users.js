import { request } from './client'

export function listUsers({ search, role, status, page, size }) {
  const params = new URLSearchParams({ page, size })
  const busqueda = search?.trim()
  if (busqueda) {
    params.set('busqueda', busqueda)
  }
  if (role) {
    params.set('rol', role)
  }
  if (status) {
    params.set('estado', status)
  }
  return request(`/api/admin/usuarios?${params}`)
}
