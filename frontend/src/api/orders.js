import { request } from './client'

// Pedidos del cliente (HU10; backend en PR #166). Crea el pedido de UN proveedor con los productos de ese proveedor
// que hay en el carrito del cliente en sesión, y los quita del carrito. Cada pedido solo puede tener un proveedor, se
// envía a una de las direcciones del cliente (HU5), que el servidor copia en `direccionEnvio`, y se paga con uno de
// sus métodos de pago (HU6). `totalEsperado` es el total que el cliente ha visto al confirmar. Responde 201 con el
// pedido en el mismo formato que el detalle (ver getOrder). Errores: 400 con `code: VALIDATION_ERROR` si la dirección
// o el método de pago no son del cliente; 402 con `code: PAYMENT_REJECTED` si el pago (simulado) se rechaza; 409 con
// `code: CART_EMPTY` si no hay productos de ese vendedor en el carrito, `code: INSUFFICIENT_STOCK` si falta stock
// (con el producto en `mensaje`) y `code: TOTAL_MISMATCH` si el total ya no es el que vio el cliente. El servidor vuelve
// a comprobar precios, descuentos y existencias al crearlo.
export function createOrder(vendorId, direccionId, paymentMethodId, totalEsperado) {
  return request('/api/customer/orders', {
    method: 'POST',
    body: { vendedorId: vendorId, direccionId, paymentMethodId, totalEsperado },
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

// Seguimiento del envío de un pedido del cliente (PBI 540). `getOrderTracking` responde
//   { pedidoId, estado, fechaEstimadaEntrega, fechaEntrega, historial: [{ estado, fecha }] }
// con las fechas a null si no están disponibles y el histórico del primer estado al último; 400 con
// `code: ORDER_NOT_FOUND` si el pedido no existe o es de otro cliente. `confirmDelivery` es la confirmación de recepción
// por el cliente: responde con el mismo formato, ya `ENTREGADO`; 409 con `code: ORDER_NOT_DELIVERABLE` si el pedido no
// está `ENVIADO` (aún no sale, ya se entregó o se canceló) y 400 `ORDER_NOT_FOUND` como la consulta.
export function getOrderTracking(orderId) {
  return request(`/api/customer/orders/${encodeURIComponent(orderId)}/tracking`)
}

export function confirmDelivery(orderId) {
  return request(`/api/customer/orders/${encodeURIComponent(orderId)}/confirm-delivery`, { method: 'POST' })
}

// Cancela un pedido del cliente que aún no se ha enviado (HU12; backend en T374, PR #207). Responde con el pedido ya
// cancelado; 409 con `code: ORDER_NOT_CANCELLABLE` si ya no se puede cancelar, y 400 con `code: ORDER_NOT_FOUND` si no
// existe o es de otro cliente.
export function cancelOrder(orderId) {
  return request(`/api/customer/orders/${encodeURIComponent(orderId)}/cancel`, { method: 'POST' })
}

// Confirma que el cliente ha recibido un pedido enviado y lo marca como entregado.
export function confirmOrderDelivery(orderId) {
  return request(`/api/customer/orders/${encodeURIComponent(orderId)}/deliver`, { method: 'POST' })
}

// Pedidos reales del vendedor en sesión (listado y detalle). El backend solo devuelve pedidos cuyo vendedorId
// coincide con la sesión y nunca permite acceder al pedido de otro vendedor.
export function getSellerOrders() {
  return request('/api/seller/orders')
}

export function getSellerOrder(orderId) {
  return request(`/api/seller/orders/${encodeURIComponent(orderId)}`)
}

export function advanceSellerOrder(orderId) {
  return request(`/api/seller/orders/${encodeURIComponent(orderId)}/advance`, { method: 'POST' })
}

export function revertSellerOrder(orderId) {
  return request(`/api/seller/orders/${encodeURIComponent(orderId)}/revert`, { method: 'POST' })
}

export function cancelSellerOrder(orderId) {
  return request(`/api/seller/orders/${encodeURIComponent(orderId)}/cancel`, { method: 'POST' })
}