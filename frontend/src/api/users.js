import { request } from './client'

export function listUsers({ search, role, status, page, size }) {
  const params = new URLSearchParams({ page, size })
  const filtros = { search: search?.trim(), role, status }

  for (const [nombre, valor] of Object.entries(filtros)) {
    if (valor) {
      params.set(nombre, valor)
    }
  }

  return request(`/api/admin/usuarios?${params}`)
}
