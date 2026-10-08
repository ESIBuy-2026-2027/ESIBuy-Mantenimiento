import { request } from './client'
import { buildQuery } from './query'

export function getCategories() {
  return request('/api/categorias')
}

export function getSellerProducts({ search = '', category = '', page = 1, size = 10 } = {}) {
  return request(`/api/seller/products?${buildQuery({ page, size, search, category })}`)
}
