import { request } from './client'

export function listUsers({ page, size }) {
  const params = new URLSearchParams({ page, size })
  return request(`/api/admin/usuarios?${params}`)
}
