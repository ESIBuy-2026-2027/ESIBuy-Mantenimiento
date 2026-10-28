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
