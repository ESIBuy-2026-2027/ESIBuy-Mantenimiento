const CATEGORY_STATUS = new Map([
  ['ACTIVA', { label: 'Activa', color: 'success' }],
  ['INACTIVA', { label: 'Inactiva', color: 'subtle' }],
])

const UNKNOWN_STATUS = { label: 'Desconocido', color: 'subtle' }

// Etiquetas de los estados para el desplegable del filtro.
export const CATEGORY_STATUS_LABELS = Object.fromEntries(
  [...CATEGORY_STATUS].map(([value, { label }]) => [value, label]),
)

export const getCategoryStatus = (status) => CATEGORY_STATUS.get(status) ?? UNKNOWN_STATUS
