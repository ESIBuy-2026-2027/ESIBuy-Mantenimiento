import { request } from './client'

export function getCategories() {
  return request('/api/categorias')
}

export function getSellerProducts({ search = '', category = '', page = 1, size = 10 } = {}) {
  const params = new URLSearchParams({ page: String(page), size: String(size) })
  if (search) params.set('search', search)
  if (category) params.set('category', category)
  return request(`/api/seller/products?${params}`)
}
