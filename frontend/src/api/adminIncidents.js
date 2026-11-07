import { request } from './client'

// Supervisión de incidencias del administrador (HU49): las de todos los autores. Contrato en
// docs/contratos/hu49-supervision-incidencias.md.

// Una página de las incidencias, de las más antiguas a las más recientes: `page` empieza en 1 y `size` va de 1 a 50.
// Responde { items, page, size, totalItems, totalPages }; cada incidencia lleva `autor` ({ email, rol }), que es null
// si la cuenta del autor se ha eliminado.
export function getAdminIncidents({ page, size }) {
  return request(`/api/admin/incidencias?page=${page}&size=${size}`)
}

// Abre una incidencia pendiente con la respuesta del administrador (HU50) y responde 200 con ella. Contrato en
// docs/contratos/hu50-apertura-cierre-incidencias.md. 400 VALIDATION_ERROR si la respuesta no es válida, 404
// INCIDENT_NOT_FOUND si ya no existe y 409 INCIDENT_ALREADY_OPEN si ya está abierta.
export function openIncident(id, respuesta) {
  return request(`/api/admin/incidencias/${encodeURIComponent(id)}/abrir`, { method: 'PATCH', body: { respuesta } })
}

// Cierra una incidencia, pendiente o abierta (HU50): se elimina y responde 204 sin cuerpo (null). 404
// INCIDENT_NOT_FOUND si ya no existe.
export function closeIncident(id) {
  return request(`/api/admin/incidencias/${encodeURIComponent(id)}`, { method: 'DELETE' })
}
