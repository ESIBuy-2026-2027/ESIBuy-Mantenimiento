import { request } from './client'
import { buildQuery } from './query'

export function getAdminSellers({
  search = '',
  category = '',
  status = '',
  sort = 'status',
  order = 'asc',
  page = 1,
  size = 10,
} = {}) {
  return request(`/api/admin/sellers?${buildQuery({ page, size, search, category, status, sort, order })}`)
}

export function getAdminSeller(sellerId) {
  return request(`/api/admin/sellers/${encodeURIComponent(sellerId)}`)
}

export function getAdminSellerProducts(sellerId, { page = 1, size = 5 } = {}) {
  return request(`/api/admin/sellers/${encodeURIComponent(sellerId)}/products?${buildQuery({ page, size })}`)
}

export function getAdminSellerOrders(sellerId, { page = 1, size = 5 } = {}) {
  return request(`/api/admin/sellers/${encodeURIComponent(sellerId)}/orders?${buildQuery({ page, size })}`)
}
