import { request } from './client'

// Incidencias del usuario en sesión (HU44): solo cliente y vendedor; el backend saca el autor de la sesión.

// Una página de las incidencias del usuario, de las más antiguas a las más recientes: `page` empieza en 1 y `size`
// va de 1 a 50. Responde { items, page, size, totalItems, totalPages }; sin incidencias, items vacío y totalPages 0.
export function getIncidents({ page, size }) {
  return request(`/api/perfil/incidencias?page=${page}&size=${size}`)
}

// Crea una incidencia pendiente y responde 201 con ella. 400 con `code: VALIDATION_ERROR` si el asunto (hasta 100
// caracteres) o la descripción (hasta 1000) no son válidos; el mensaje es genérico.
export function createIncident({ asunto, descripcion }) {
  return request('/api/perfil/incidencias', { method: 'POST', body: { asunto, descripcion } })
}
