import { request } from './client'
import { buildQuery } from './query'

// Métodos de pago del cliente en sesión (HU6): el backend lo deduce de la sesión, así que no se envía ningún
// identificador de cliente. Cada método llega solo con su máscara (`{ id, tipo, mascara, predeterminado }`), nunca
// con los datos de pago.
const URL_METODOS = '/api/customer/payment-methods'

export function getPaymentMethods() {
  return request(URL_METODOS)
}

// Alta de un método: `{ tipo, ... }` con solo los campos de su tipo (tarjeta: numeroTarjeta, titular, caducidad y
// cvv; PayPal: urlPayPal; Bizum: telefonoBizum). El CVV solo lo valida el backend: no se guarda en ningún sitio.
export function addPaymentMethod(metodo) {
  return request(URL_METODOS, { method: 'POST', body: metodo })
}

// Solo puede haber uno predeterminado: el backend desmarca el anterior. Responde 204 sin cuerpo.
export function setDefaultPaymentMethod(id) {
  return request(`${URL_METODOS}/${encodeURIComponent(id)}/default`, { method: 'PUT' })
}

// Si se elimina el predeterminado y quedan otros, `nuevoPredeterminado` es el id del que lo sustituye. Si no hace
// falta (no es el predeterminado o es el único), no se envía. Responde 204 sin cuerpo.
export function deletePaymentMethod(id, nuevoPredeterminado) {
  const query = buildQuery({ nuevoPredeterminado })
  return request(`${URL_METODOS}/${encodeURIComponent(id)}${query ? `?${query}` : ''}`, { method: 'DELETE' })
}
