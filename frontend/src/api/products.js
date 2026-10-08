import { request } from './client'
import { buildQuery } from './query'

export function getCategories() {
  return request('/api/categorias')
}

export function getSellerProduct(productId) {
  return request(`/api/seller/products/${encodeURIComponent(productId)}`)
}

export function createSellerProduct(product) {
  return request('/api/seller/products', { method: 'POST', body: product })
}

export function updateSellerProduct(productId, product) {
  return request(`/api/seller/products/${encodeURIComponent(productId)}`, { method: 'PUT', body: product })
}

export function deleteSellerProduct(productId) {
  return request(`/api/seller/products/${encodeURIComponent(productId)}`, { method: 'DELETE' })
}

export function getSellerProducts({ search = '', category = '', page = 1, size = 10 } = {}) {
  return request(`/api/seller/products?${buildQuery({ page, size, search, category })}`)
}
