import { request } from './client'

// Pedidos del cliente (HU10; backend en PR #166). Crea el pedido de UN proveedor con los productos de ese proveedor
// que hay en el carrito del cliente en sesión, y los quita del carrito. Cada pedido solo puede tener un proveedor, se
// envía a una de las direcciones del cliente (HU5), que el servidor copia en `direccionEnvio`, y se paga con uno de
// sus métodos de pago (HU6). Responde 201 con el pedido en el mismo formato que el detalle (ver getOrder). 400 con
// `code: VALIDATION_ERROR` si la dirección o el método de pago no son del cliente, 409 con `code: CART_EMPTY` si no
// hay productos de ese proveedor en el carrito y 409 con `code: INSUFFICIENT_STOCK` si falta stock. El servidor vuelve
// a comprobar precios y existencias al crearlo.
export function createOrder(vendorId, direccionId, paymentMethodId) {
  return request('/api/customer/orders', {
    method: 'POST',
    body: { vendedorId: vendorId, direccionId, paymentMethodId },
  })
}

// Historial y detalle de los pedidos del cliente en sesión (HU11; backend en T377, PR #186). El historial llega del
// más reciente al más antiguo, cada pedido con el mismo formato que el detalle:
//   { id, numeroPedido, vendedor: { id, nombreComercial }, total, estado, paymentMethodId, direccionEnvio,
//     lineas: [{ productoId, nombre, cantidad, precioUnitario, subtotal }], createdAt }
// El detalle responde 400 con `code: ORDER_NOT_FOUND` si el pedido no existe o es de otro cliente.
export function getOrders() {
  return request('/api/customer/orders')
}

export function getOrder(orderId) {
  return request(`/api/customer/orders/${encodeURIComponent(orderId)}`)
}
