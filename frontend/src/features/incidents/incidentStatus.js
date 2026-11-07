// Estados de una incidencia (HU44), con los mismos valores que el backend (EstadoIncidencia). Nace PENDIENTE y pasa a
// ABIERTA cuando un administrador la abre (HU50); no hay estado «cerrada»: una incidencia cerrada se elimina.
const ETIQUETAS = {
  PENDIENTE: 'Pendiente',
  ABIERTA: 'Abierta',
}

// Si el backend añade un estado que el frontend aún no conoce, se muestra su valor tal cual
export const etiquetaDeEstado = (estado) => ETIQUETAS[estado] ?? estado

// Color del badge de cada estado
export const colorDeEstado = (estado) => (estado === 'PENDIENTE' ? 'warning' : 'brand')
