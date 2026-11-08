import { request } from './client'

// Lista de deseos del cliente en sesión (HU14), en el backend bajo /api/customer/wishlist (módulo deseos).

// La lista con los datos actuales de cada producto, en el orden en que se añadieron:
// { items: [{ productoId, nombre, imagen, precioFinal, stock, disponible, addedAt }] }. El precio es el que le toca
// al cliente (estándar o premium). Un producto desactivado sigue en la lista con `disponible: false`.
export function getWishlist() {
  return request('/api/customer/wishlist')
}

// Añade un producto a la lista y responde con ella, solo con los identificadores: { items: [{ productoId, addedAt }] }.
// Si ya estaba, no es un error y la lista no cambia. 404 con `code: PRODUCT_NOT_FOUND` si el producto no existe o no
// está disponible (uno agotado sí se puede añadir).
export function addWishlistItem(productId) {
  return request('/api/customer/wishlist/items', { method: 'POST', body: { productoId: productId } })
}

// Quita un producto de la lista, esté o no disponible, y responde con la lista como `getWishlist`. Quitar uno que no
// está no es un error.
export function removeWishlistItem(productId) {
  return request(`/api/customer/wishlist/items/${encodeURIComponent(productId)}`, { method: 'DELETE' })
}
