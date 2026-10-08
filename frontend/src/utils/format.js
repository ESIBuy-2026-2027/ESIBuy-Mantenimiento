const priceFormatter = new Intl.NumberFormat('es-ES', { style: 'currency', currency: 'EUR' })
const dateFormatter = new Intl.DateTimeFormat('es-ES', {
  day: '2-digit',
  month: '2-digit',
  year: 'numeric',
  timeZone: 'UTC',
})

export const formatPrice = (amount) => priceFormatter.format(amount)

// Recibe una fecha ISO sin hora (AAAA-MM-DD) y la muestra como DD/MM/AAAA.
export const formatDate = (isoDate) => dateFormatter.format(new Date(isoDate))
