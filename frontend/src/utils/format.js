const priceFormatter = new Intl.NumberFormat('es-ES', { style: 'currency', currency: 'EUR' })
const dateFormatter = new Intl.DateTimeFormat('es-ES', {
  day: '2-digit',
  month: '2-digit',
  year: 'numeric',
  timeZone: 'UTC',
})

const ratingFormatter = new Intl.NumberFormat('es-ES', { minimumFractionDigits: 1, maximumFractionDigits: 1 })

export const formatPrice = (amount) => priceFormatter.format(amount)

// Valoración media con un decimal y coma: 4,5.
export const formatRating = (value) => ratingFormatter.format(value)

// Recibe una fecha ISO sin hora (AAAA-MM-DD) y la muestra como DD/MM/AAAA.
export const formatDate = (isoDate) => dateFormatter.format(new Date(isoDate))

const dateTimeFormatter = new Intl.DateTimeFormat('es-ES', {
  day: '2-digit',
  month: '2-digit',
  year: 'numeric',
  hour: '2-digit',
  minute: '2-digit',
  hour12: false,
})

// Recibe un instante ISO-8601 (como `2026-11-06T10:15:30Z`) y lo muestra en la hora local como DD/MM/AAAA, HH:MM.
export const formatDateTime = (isoDateTime) => dateTimeFormatter.format(new Date(isoDateTime))
