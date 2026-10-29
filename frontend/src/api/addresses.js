import { request } from './client'
import { buildQuery } from './query'

// Direcciones de envío del cliente en sesión (HU5): el backend lo deduce de la sesión, así que no se envía ningún
// identificador de cliente. Cada dirección llega como
// `{ id, destinatario, calle, detalles, codigoPostal, ciudad, provincia, telefono, principal }`.
const URL_DIRECCIONES = '/api/customer/addresses'

export function getAddresses() {
  return request(URL_DIRECCIONES)
}

// Alta y edición envían los mismos campos: `{ destinatario, calle, detalles, codigoPostal, ciudad, provincia,
// telefono }`. La principal no se envía: se marca con setPrincipalAddress.
export function addAddress(direccion) {
  return request(URL_DIRECCIONES, { method: 'POST', body: direccion })
}

export function updateAddress(id, direccion) {
  return request(`${URL_DIRECCIONES}/${encodeURIComponent(id)}`, { method: 'PUT', body: direccion })
}

// Solo puede haber una principal: el backend desmarca la anterior. Responde 204 sin cuerpo.
export function setPrincipalAddress(id) {
  return request(`${URL_DIRECCIONES}/${encodeURIComponent(id)}/principal`, { method: 'PUT' })
}

// Si se elimina la principal y quedan otras, `nuevaPrincipal` es el id de la que la sustituye. Si no hace falta
// (no es la principal o es la única), no se envía. Responde 204 sin cuerpo.
export function deleteAddress(id, nuevaPrincipal) {
  const query = buildQuery({ nuevaPrincipal })
  return request(`${URL_DIRECCIONES}/${encodeURIComponent(id)}${query ? `?${query}` : ''}`, { method: 'DELETE' })
}
