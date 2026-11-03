import { request } from './client'

// Pedidos del cliente (HU10). Contrato propuesto por el frontend: el backend aún no lo tiene (AB#380 a AB#382).
// Crea el pedido de UN proveedor con los productos de ese proveedor que hay en el carrito del cliente en sesión, y los
// quita del carrito. Cada pedido solo puede tener un proveedor y se paga con uno de los métodos de pago del cliente
// (HU6), que se guarda en `paymentMethodId`. Responde 201 con
//   { id, vendedor: { id, nombreComercial }, total, estado, paymentMethodId, lineas: [{ productoId, nombre, cantidad,
//     precioUnitario, subtotal }] }
// 400 con `code: VALIDATION_ERROR` si el método de pago no es del cliente y 409 con `code: CART_EMPTY` si no hay
// productos de ese proveedor en el carrito. El servidor vuelve a comprobar precios y existencias al crearlo.
export function createOrder(vendorId, paymentMethodId) {
  return request('/api/orders', { method: 'POST', body: { vendedorId: vendorId, paymentMethodId } })
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
