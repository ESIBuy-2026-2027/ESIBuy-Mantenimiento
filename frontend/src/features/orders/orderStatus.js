// Estados de un pedido (HU11), con los mismos valores que el backend (EstadoPedido).
// Los pasos del seguimiento del envío, en el orden en que avanza un pedido. «En reparto» no es un estado del pedido,
// sino el tramo en que está un envío ENVIADO hasta que el cliente confirma la recepción (regla de negocio): mientras
// tanto el estado sigue siendo ENVIADO. Un pedido PAGADO ya está en preparación, así que comparte el primer paso.
// CANCELADO no es un paso del envío.
export const PASOS_DEL_ENVIO = ['PREPARANDO', 'ENVIADO', 'EN_REPARTO', 'ENTREGADO']

const ETIQUETAS_PASOS = {
  PREPARANDO: 'Preparación',
  ENVIADO: 'Enviado',
  EN_REPARTO: 'En reparto',
  ENTREGADO: 'Entregado',
}

export const etiquetaDelPaso = (paso) => ETIQUETAS_PASOS[paso] ?? paso

// Paso en el que está cada estado del pedido; un estado que no es un paso del envío no está aquí
const PASO_DEL_ESTADO = {
  PAGADO: 'PREPARANDO',
  PREPARANDO: 'PREPARANDO',
  ENVIADO: 'EN_REPARTO',
  ENTREGADO: 'ENTREGADO',
}

// Posición del estado en los pasos del seguimiento, o -1 si no es un paso (CANCELADO o un estado que el frontend aún no conoce)
export const indiceDelPaso = (estado) => PASOS_DEL_ENVIO.indexOf(PASO_DEL_ESTADO[estado])

// Fecha en la que el envío llegó a ese paso, si el histórico la tiene. La preparación empieza cuando se paga el pedido;
// «En reparto» no tiene fecha propia porque no es un estado.
export const fechaDelPaso = (paso, historial) => {
  if (paso === 'EN_REPARTO') return null
  const estados = paso === 'PREPARANDO' ? ['PREPARANDO', 'PAGADO'] : [paso]
  return historial.find((cambio) => estados.includes(cambio.estado))?.fecha ?? null
}

// Solo se confirma la recepción de un envío que ya ha salido; el backend lo exige igualmente (Pedido.confirmarEntrega)
export const puedeConfirmarEntrega = (estado) => estado === 'ENVIADO'

export const CANCELADO = 'CANCELADO'

// HU12: solo se cancela un pedido que aún no se ha enviado, la misma regla que el backend (Pedido.cancelar)
export const esCancelable = (estado) => estado === 'PAGADO' || estado === 'PREPARANDO'

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
