import { request } from './client'

// searchBy, sort y order son opcionales: el listado general (HU19) no los envía y busca también por email.
export function listUsers({ search, role, status, page, size, searchBy, sort, order }) {
  const params = new URLSearchParams({ page, size })
  const filtros = { search: search?.trim(), role, status, searchBy, sort, order }

  for (const [nombre, valor] of Object.entries(filtros)) {
    if (valor) {
      params.set(nombre, valor)
    }
  }

  return request(`/api/admin/usuarios?${params}`)
}

// Ficha completa de un usuario, sin la contraseña (HU20).
export function getUser(userId) {
  return request(`/api/admin/usuarios/${encodeURIComponent(userId)}`)
}

// Modifica los datos de cualquier usuario (cliente, administrador o vendedor) y su rol (HU22). Nunca se envía la
// contraseña, ni el email de clientes y administradores.
export function updateUser(userId, datos) {
  return request(`/api/admin/usuarios/${encodeURIComponent(userId)}`, { method: 'PUT', body: datos })
}

// Activa (HU24) o desactiva (HU23) una cuenta: estado es 'ACTIVO' o 'BLOQUEADO'.
export function updateUserStatus(userId, estado) {
  return request(`/api/admin/usuarios/${encodeURIComponent(userId)}/status`, {
    method: 'PATCH',
    body: { estado },
  })
}

// Elimina un administrador (HU37; la usa también la tabla desde la HU19). Contrato del backend
// (UsuarioController.eliminar): 204 sin cuerpo; 404 si la cuenta no existe o no es de un administrador (hoy solo se
// eliminan administradores); 409 con CANNOT_DELETE_SELF si es la propia cuenta y LAST_ACTIVE_ADMIN si es el último
// administrador activo; 401 sin sesión y 403 ACCESS_DENIED con otro rol. El borrado es físico y la sesión de la
// cuenta eliminada deja de valer en su siguiente petición.
export function deleteUser(userId) {
  return request(`/api/admin/usuarios/${encodeURIComponent(userId)}`, { method: 'DELETE' })
}
