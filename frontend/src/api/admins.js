import { request } from './client'

export function createAdmin(datos) {
  return request('/api/admin/admins', { method: 'POST', body: datos })
}
