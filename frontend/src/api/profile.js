import { request } from './client'

// Perfil del usuario en sesión (HU4): solo para clientes y vendedores. El backend lo deduce de la sesión, así que
// no se envía ningún identificador. Cada rol trae solo sus campos: fechaNacimiento y tipoCliente el cliente;
// nombreComercial y categoriaPrincipalId el vendedor. Los campos vacíos no llegan.
export function getProfile() {
  return request('/api/perfil')
}

// Edición del perfil propio (HU45): el PUT sustituye todos los datos modificables del rol del usuario en sesión
// (nombre, apellidos, avatar, teléfono y, según el rol, fechaNacimiento o nombreComercial y categoriaPrincipalId). El
// email, el DNI, el tipo de cliente y el rol no se editan. Responde el perfil actualizado, con la forma del GET. Los
// fallos de los datos son 400 (VALIDATION_ERROR, INVALID_PHONE, UNDERAGE) y 409 (COMMERCIAL_NAME_ALREADY_REGISTERED),
// con su code.
export function updateProfile(datos) {
  return request('/api/perfil', { method: 'PUT', body: datos })
}

// Eliminación de la cuenta propia del vendedor (HU47), confirmada con la contraseña actual. El backend borra la cuenta
// y cierra la sesión (204). Una contraseña incorrecta responde 400 con code WRONG_PASSWORD, no 401: la sesión sigue
// abierta.
export function deleteAccount(password) {
  return request('/api/perfil', { method: 'DELETE', body: { password } })
}
