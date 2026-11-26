import { request } from './client'

// Alertas del cliente en sesión (AB#529), en el backend bajo /api/customer/alerts (módulo alertas). No se envían por
// correo: se guardan y el cliente las ve en su cuenta. Responde
// { mensaje, alertas: [{ id, tipo, fecha, leida, producto: { id, nombre, imagen, precioAnterior, precioNuevo } }] },
// de la más reciente a la más antigua. `tipo` es BAJADA_PRECIO, DISPONIBILIDAD o PRODUCTO_NUEVO (AB#530); el producto
// es como era en ese momento (la imagen, vacía si no tenía) y los precios solo los lleva una bajada de precio. 401 sin sesión y 403 (`code: ACCESS_DENIED`) con otro rol.
export function getAlerts() {
  return request('/api/customer/alerts')
}

// Marca como leída una alerta del cliente en sesión (AB#531): sigue en su lista, pero deja de ser nueva. Responde
// { mensaje, alerta } con la alerta ya leída (si ya lo estaba, igual). 404 (`code: ALERT_NOT_FOUND`) si no existe o es
// de otro cliente, 401 sin sesión y 403 (`code: ACCESS_DENIED`) con otro rol.
export function markAlertAsRead(id) {
  return request(`/api/customer/alerts/${encodeURIComponent(id)}/read`, { method: 'PATCH' })
}
