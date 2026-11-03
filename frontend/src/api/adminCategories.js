import { request } from './client'

export function getAdminCategories() {
  return request('/api/admin/categorias')
}
