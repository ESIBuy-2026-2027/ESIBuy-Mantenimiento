import { ApiError } from '../../api/client'

// Mensajes de la pasarela (HU10) según el `code` con el que responde el backend al crear un pedido.
const MENSAJES = {
  PAYMENT_REJECTED: 'El pago ha sido rechazado. Elige otro método de pago o inténtalo de nuevo.',
  TOTAL_MISMATCH:
    'El importe de tu compra ha cambiado (un precio, un descuento o una cantidad). Revísala antes de pagar.',
  CART_EMPTY: 'Ya no quedan productos de este vendedor en tu carrito.',
  VALIDATION_ERROR: 'La dirección de envío o el método de pago ya no son válidos. Elígelos de nuevo.',
}

const MENSAJE_SIN_STOCK = 'Ya no queda stock suficiente de algún producto. Ajusta las cantidades y vuelve a intentarlo.'

export const MENSAJE_GENERICO = 'No se ha podido completar el pedido. Inténtalo de nuevo más tarde.'

// Lo que ha cambiado en el carrito: el cliente tiene que volver a revisar su compra antes de pagar.
const CODIGOS_QUE_PIDEN_REVISAR = new Set(['TOTAL_MISMATCH', 'INSUFFICIENT_STOCK', 'CART_EMPTY'])

// El mensaje que ve el cliente. Si el servidor dice qué producto se ha quedado sin stock, se le dice.
export function mensajeDeError(error) {
  if (!(error instanceof ApiError)) return MENSAJE_GENERICO
  if (error.code === 'INSUFFICIENT_STOCK') return error.detail ? `${error.detail}. Ajusta la cantidad.` : MENSAJE_SIN_STOCK
  return MENSAJES[error.code] ?? MENSAJE_GENERICO
}

export function pideRevisarLaCompra(error) {
  return error instanceof ApiError && CODIGOS_QUE_PIDEN_REVISAR.has(error.code)
}
