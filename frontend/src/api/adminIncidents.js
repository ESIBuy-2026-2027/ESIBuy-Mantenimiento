import { request } from './client'

// Supervisión de incidencias del administrador (HU49): las de todos los autores. Contrato en
// docs/contratos/hu49-supervision-incidencias.md.

// Una página de las incidencias, de las más antiguas a las más recientes: `page` empieza en 1 y `size` va de 1 a 50.
// Responde { items, page, size, totalItems, totalPages }; cada incidencia lleva `autor` ({ email, rol }), que es null
// si la cuenta del autor se ha eliminado.
export function getAdminIncidents({ page, size }) {
  return request(`/api/admin/incidencias?page=${page}&size=${size}`)
}
