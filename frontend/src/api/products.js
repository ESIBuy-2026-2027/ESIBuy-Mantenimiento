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

// Catálogo del cliente (HU7). Contrato propuesto por el frontend: el backend (T352 y T353) aún no lo tiene.
export function getCatalogProducts({ page = 1, size = 12 } = {}) {
  return request(`/api/products?${buildQuery({ page, size })}`)
}

export function getSellerProducts({ search = '', category = '', page = 1, size = 10 } = {}) {
  return request(`/api/seller/products?${buildQuery({ page, size, search, category })}`)
}
