import { request } from './client'

export function getCategorias() {
  return request('/api/categorias')
}
