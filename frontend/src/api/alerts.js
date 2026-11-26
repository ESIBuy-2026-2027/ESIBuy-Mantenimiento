import { request } from './client'

// Alertas del cliente en sesión (AB#529), en el backend bajo /api/customer/alerts (módulo alertas). No se envían por
// correo: se guardan y el cliente las ve en su cuenta. Responde
// { mensaje, alertas: [{ id, tipo, fecha, leida, producto: { id, nombre, precioAnterior, precioNuevo } }] },
// de la más reciente a la más antigua. `tipo` es BAJADA_PRECIO, DISPONIBILIDAD o PRODUCTO_NUEVO (AB#530); los precios
// solo los lleva una bajada de precio. 401 sin sesión y 403 (`code: ACCESS_DENIED`) con otro rol.
export function getAlerts() {
  return request('/api/customer/alerts')
}
