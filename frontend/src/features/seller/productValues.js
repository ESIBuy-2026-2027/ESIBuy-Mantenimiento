const NAME_MAX_LENGTH = 100
const DESCRIPTION_MAX_LENGTH = 1000
const MIN_DISCOUNT = 1
const MAX_DISCOUNT = 99

export const emptyProductValues = () => ({
  nombre: '',
  descripcion: '',
  precio: '',
  stock: '',
  categoriaId: '',
  imagen: '',
  disponible: true,
  descuentoPorcentaje: '',
  descuentoPremiumPorcentaje: '',
})

const percentToText = (percent) => (percent === null || percent === undefined ? '' : String(percent))

export const valuesFromProduct = (product) => ({
  nombre: product.nombre,
  descripcion: product.descripcion,
  precio: String(product.precio),
  stock: String(product.stock),
  categoriaId: product.categoriaId,
  imagen: product.imagen,
  disponible: product.disponible,
  descuentoPorcentaje: percentToText(product.descuento.porcentaje),
  descuentoPremiumPorcentaje: percentToText(product.descuentoPremium.porcentaje),
})

function isHttpUrl(text) {
  try {
    return ['http:', 'https:'].includes(new URL(text).protocol)
  } catch {
    return false
  }
}

function discountError(percent, label) {
  if (percent.trim() === '') return undefined
  const value = Number(percent)
  return !/^\d+$/.test(percent) || value < MIN_DISCOUNT || value > MAX_DISCOUNT
    ? `${label} debe ser un número entero entre ${MIN_DISCOUNT} y ${MAX_DISCOUNT}.`
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

  if (!values.categoriaId) errors.categoriaId = 'Elige una categoría.'

  if (!price) errors.precio = 'Introduce el precio.'
  else if (!/^\d+(?:[.,]\d{1,2})?$/.test(price) || Number(price.replace(',', '.')) <= 0) {
    errors.precio = 'El precio debe ser un número mayor que 0 y puede tener hasta dos decimales.'
  }

  if (stock && !/^\d+$/.test(stock)) errors.stock = 'El stock debe ser un número entero mayor o igual que 0.'

  if (values.descripcion.length > DESCRIPTION_MAX_LENGTH) {
    errors.descripcion = `La descripción no puede superar los ${DESCRIPTION_MAX_LENGTH} caracteres.`
  }

  if (image && !isHttpUrl(image) && !/^\/api\/product-images\/[a-fA-F0-9]{24}$/.test(image)) {
    errors.imagen = 'Introduce una dirección web válida (http o https).'
  }

  const discount = discountError(values.descuentoPorcentaje, 'El descuento general')
  if (discount) errors.descuentoPorcentaje = discount

  const premiumDiscount = discountError(values.descuentoPremiumPorcentaje, 'El descuento adicional Premium')
  if (premiumDiscount) errors.descuentoPremiumPorcentaje = premiumDiscount

  return errors
}

const toDiscount = (percent) => {
  const active = percent.trim() !== ''
  return { activo: active, porcentaje: active ? Number(percent) : null }
}

// Cuerpo que se envía al backend. No incluye al vendedor: lo deduce el servidor de la sesión.
export const toProductPayload = (values) => ({
  nombre: values.nombre.trim(),
  descripcion: values.descripcion.trim(),
  precio: Number(values.precio.replace(',', '.')),
  stock: values.stock.trim() === '' ? 0 : Number(values.stock),
  categoriaId: values.categoriaId,
  imagen: values.imagen.trim(),
  disponible: values.disponible,
  descuento: toDiscount(values.descuentoPorcentaje),
  descuentoPremium: toDiscount(values.descuentoPremiumPorcentaje),
})
