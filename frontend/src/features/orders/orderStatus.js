// Estados de un pedido (HU11), con los mismos valores que el backend (EstadoPedido).
// Los pasos del envío, en el orden en que avanza un pedido; CANCELADO no es un paso (lo usará la HU12).
export const PASOS_DEL_ENVIO = ['PAGADO', 'PREPARANDO', 'ENVIADO', 'ENTREGADO']

export const CANCELADO = 'CANCELADO'

const ETIQUETAS = {
  PAGADO: 'Pagado',
  PREPARANDO: 'Preparando',
  ENVIADO: 'Enviado',
  ENTREGADO: 'Entregado',
  [CANCELADO]: 'Cancelado',
}

// Si el backend añade un estado que el frontend aún no conoce, se muestra su valor tal cual
export const etiquetaDeEstado = (estado) => ETIQUETAS[estado] ?? estado

// Color del badge de cada estado
export const colorDeEstado = (estado) => {
  if (estado === 'ENTREGADO') return 'success'
  if (estado === CANCELADO) return 'danger'
  return 'brand'
}
