import { request } from './client'

// Carrito del cliente (HU9), en el backend bajo /api/customer/cart (módulo carrito, AB#385 a AB#390).
// El carrito vive en el backend, es del cliente en sesión y todas las operaciones responden con el carrito completo y
// ya agrupado por proveedor, con el total de cada grupo calculado por el servidor:
//   { grupos: [{ vendedor: { id, nombreComercial }, total, lineas: [{ productoId, nombre, imagen, cantidad,
//     precioUnitario, subtotal, stock }] }], totalProductos, total }
export function getCart() {
  return request('/api/customer/cart')
}

// Añade `cantidad` unidades a las que ya hubiera. 404 si el producto no existe o no está disponible y 409 con
// `code: INSUFFICIENT_STOCK` si no hay existencias suficientes.
export function addCartItem(productId, quantity) {
  return request('/api/customer/cart/items', { method: 'POST', body: { productoId: productId, cantidad: quantity } })
}

// Fija la cantidad de un producto del carrito (no la suma). Mismos errores que al añadir.
export function setCartItemQuantity(productId, quantity) {
  return request(`/api/customer/cart/items/${encodeURIComponent(productId)}`, { method: 'PUT', body: { cantidad: quantity } })
}

export function removeCartItem(productId) {
  return request(`/api/customer/cart/items/${encodeURIComponent(productId)}`, { method: 'DELETE' })
}

export function clearCart() {
  return request('/api/customer/cart', { method: 'DELETE' })
}
