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
// sort: nombre o valoracion. order: asc o desc. minRating: valoración media mínima (de 1 a 5).
export function getCatalogProducts({
  search = '',
  category = '',
  minRating = '',
  sort = 'nombre',
  order = 'asc',
  page = 1,
  size = 10,
} = {}) {
  return request(`/api/products?${buildQuery({ page, size, search, category, minRating, sort, order })}`)
}

export function getSellerProducts({ search = '', category = '', page = 1, size = 10 } = {}) {
  return request(`/api/seller/products?${buildQuery({ page, size, search, category })}`)
}
