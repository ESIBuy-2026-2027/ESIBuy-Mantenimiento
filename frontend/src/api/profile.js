import { request } from './client'

// Perfil del usuario en sesión (HU4): solo para clientes y vendedores. El backend lo deduce de la sesión, así que
// no se envía ningún identificador. Cada rol trae solo sus campos: fechaNacimiento y tipoCliente el cliente;
// nombreComercial y categoriaPrincipalId el vendedor. Los campos vacíos no llegan.
export function getProfile() {
  return request('/api/perfil')
}
