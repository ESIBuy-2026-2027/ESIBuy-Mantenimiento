import { request } from './client'

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
