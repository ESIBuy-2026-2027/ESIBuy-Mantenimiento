const NAME_MAX_LENGTH = 100
const DESCRIPTION_MAX_LENGTH = 1000
const MAX_DISCOUNT = 100

export const emptyProductValues = () => ({
  nombre: '',
  descripcion: '',
  precio: '',
  stock: '',
  categoriaIds: [],
  imagen: '',
  disponible: true,
  descuentoActivo: false,
  descuentoPorcentaje: '',
  descuentoPremiumActivo: false,
  descuentoPremiumPorcentaje: '',
})

const percentToText = (percent) => (percent === null || percent === undefined ? '' : String(percent))

export const valuesFromProduct = (product) => ({
  nombre: product.nombre,
  descripcion: product.descripcion,
  precio: String(product.precio),
  stock: String(product.stock),
  categoriaIds: product.categoriaIds,
  imagen: product.imagen,
  disponible: product.disponible,
  descuentoActivo: product.descuento.activo,
  descuentoPorcentaje: percentToText(product.descuento.porcentaje),
  descuentoPremiumActivo: product.descuentoPremium.activo,
  descuentoPremiumPorcentaje: percentToText(product.descuentoPremium.porcentaje),
})

function isHttpUrl(text) {
  try {
    return ['http:', 'https:'].includes(new URL(text).protocol)
  } catch {
    return false
  }
}

function discountError(active, percent, label) {
  if (!active) return undefined
  const value = Number(percent)
  return percent.trim() === '' || !(value > 0 && value <= MAX_DISCOUNT)
    ? `${label} debe ser mayor que 0 y no superar ${MAX_DISCOUNT}.`
    : undefined
}

// Devuelve un objeto con un mensaje por cada campo incorrecto (vacío si no hay ningún error),
// para poder mostrar todos los errores a la vez.
export function validateProduct(values) {
  const errors = {}
  const name = values.nombre.trim()
  const price = values.precio.trim()
  const stock = values.stock.trim()
  const image = values.imagen.trim()

  if (!name) errors.nombre = 'Introduce el nombre del producto.'
  else if (name.length > NAME_MAX_LENGTH) errors.nombre = `El nombre no puede superar los ${NAME_MAX_LENGTH} caracteres.`

  if (values.categoriaIds.length === 0) errors.categoriaIds = 'Elige al menos una categoría.'

  if (!price) errors.precio = 'Introduce el precio.'
  else if (Number.isNaN(Number(price)) || Number(price) <= 0) errors.precio = 'El precio debe ser mayor que 0.'

  if (stock && !/^\d+$/.test(stock)) errors.stock = 'El stock debe ser un número entero mayor o igual que 0.'

  if (values.descripcion.length > DESCRIPTION_MAX_LENGTH) {
    errors.descripcion = `La descripción no puede superar los ${DESCRIPTION_MAX_LENGTH} caracteres.`
  }

  if (image && !isHttpUrl(image)) errors.imagen = 'Introduce una dirección web válida (http o https).'

  const discount = discountError(values.descuentoActivo, values.descuentoPorcentaje, 'El descuento')
  if (discount) errors.descuentoPorcentaje = discount

  const premiumDiscount = discountError(
    values.descuentoPremiumActivo,
    values.descuentoPremiumPorcentaje,
    'El descuento premium',
  )
  if (premiumDiscount) errors.descuentoPremiumPorcentaje = premiumDiscount

  return errors
}

const toDiscount = (active, percent) => ({ activo: active, porcentaje: active ? Number(percent) : null })

// Cuerpo que se envía al backend. No incluye al vendedor: lo deduce el servidor de la sesión.
export const toProductPayload = (values) => ({
  nombre: values.nombre.trim(),
  descripcion: values.descripcion.trim(),
  precio: Number(values.precio),
  stock: values.stock.trim() === '' ? 0 : Number(values.stock),
  categoriaIds: values.categoriaIds,
  imagen: values.imagen.trim(),
  disponible: values.disponible,
  descuento: toDiscount(values.descuentoActivo, values.descuentoPorcentaje),
  descuentoPremium: toDiscount(values.descuentoPremiumActivo, values.descuentoPremiumPorcentaje),
})
