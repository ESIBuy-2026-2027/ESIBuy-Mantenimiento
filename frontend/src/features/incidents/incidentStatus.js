// Estados de una incidencia (HU44), con los mismos valores que el backend (EstadoIncidencia). Nace PENDIENTE, pasa a
// ABIERTA cuando un administrador la abre (HU50) y a RESUELTA cuando la da por solucionada (HU192); no hay estado
// «cerrada»: una incidencia cerrada se elimina.
export const ESTADO_INCIDENCIA = {
  PENDIENTE: 'PENDIENTE',
  ABIERTA: 'ABIERTA',
  RESUELTA: 'RESUELTA',
}

export const ETIQUETAS_DE_ESTADO = {
  [ESTADO_INCIDENCIA.PENDIENTE]: 'Pendiente',
  [ESTADO_INCIDENCIA.ABIERTA]: 'Abierta',
  [ESTADO_INCIDENCIA.RESUELTA]: 'Resuelta',
}

const COLORES = {
  [ESTADO_INCIDENCIA.PENDIENTE]: 'warning',
  [ESTADO_INCIDENCIA.ABIERTA]: 'brand',
  [ESTADO_INCIDENCIA.RESUELTA]: 'success',
}

// Si el backend añade un estado que el frontend aún no conoce, se muestra su valor tal cual
export const etiquetaDeEstado = (estado) => ETIQUETAS_DE_ESTADO[estado] ?? estado

// Color del badge de cada estado
export const colorDeEstado = (estado) => COLORES[estado] ?? 'brand'
