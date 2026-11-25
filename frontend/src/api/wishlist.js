import { request } from './client'

// Listas de deseos del cliente en sesión (HU14, PBI 534).

export function getWishlist() {
  return request('/api/customer/wishlist')
}

export function addWishlistItem(productId) {
  return request('/api/customer/wishlist/items', { method: 'POST', body: { productoId: productId } })
}

export function removeWishlistItem(productId) {
  return request(`/api/customer/wishlist/items/${encodeURIComponent(productId)}`, { method: 'DELETE' })
}

export function getWishlists() {
  return request('/api/customer/wishlists')
}

export function createWishlist(nombre) {
  return request('/api/customer/wishlists', { method: 'POST', body: { nombre } })
}

export function renameWishlist(listaId, nombre) {
  return request(`/api/customer/wishlists/${encodeURIComponent(listaId)}`, { method: 'PATCH', body: { nombre } })
}

export function deleteWishlist(listaId) {
  return request(`/api/customer/wishlists/${encodeURIComponent(listaId)}`, { method: 'DELETE' })
}

export function addItemToWishlist(listaId, productoId) {
  return request(`/api/customer/wishlists/${encodeURIComponent(listaId)}/items`, {
    method: 'POST',
    body: { productoId },
  })
}

export function removeItemFromWishlist(listaId, productoId) {
  return request(`/api/customer/wishlists/${encodeURIComponent(listaId)}/items/${encodeURIComponent(productoId)}`, {
    method: 'DELETE',
  })
}

export function updateWishlistItemQuantity(listaId, productoId, cantidad) {
  return request(`/api/customer/wishlists/${encodeURIComponent(listaId)}/items/${encodeURIComponent(productoId)}`, {
    method: 'PATCH',
    body: { cantidad },
  })
}
