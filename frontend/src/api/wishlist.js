import { request } from './client'

// Lista de deseos del cliente en sesión (HU14), en el backend bajo /api/customer/wishlist (módulo deseos).
// Responde con la lista completa: { items: [{ productoId, addedAt }] }.

// Añade un producto a la lista. Si ya estaba, no es un error y la lista no cambia. 404 con
// `code: PRODUCT_NOT_FOUND` si el producto no existe o no está disponible (uno agotado sí se puede añadir).
export function addWishlistItem(productId) {
  return request('/api/customer/wishlist/items', { method: 'POST', body: { productoId: productId } })
}
