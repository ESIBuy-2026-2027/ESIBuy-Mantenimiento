import { request } from './client'

// Perfil del usuario en sesión (HU4): solo para clientes y vendedores. El backend lo deduce de la sesión, así que
// no se envía ningún identificador. Cada rol trae solo sus campos: fechaNacimiento y tipoCliente el cliente;
// nombreComercial y categoriaPrincipalId el vendedor. Los campos vacíos no llegan.
export function getProfile() {
  return request('/api/perfil')
}

// Eliminación de la cuenta propia del vendedor (HU47), confirmada con la contraseña actual. El backend borra la cuenta
// y cierra la sesión (204). Una contraseña incorrecta responde 400 con code WRONG_PASSWORD, no 401: la sesión sigue
// abierta.
export function deleteAccount(password) {
  return request('/api/perfil', { method: 'DELETE', body: { password } })
}
