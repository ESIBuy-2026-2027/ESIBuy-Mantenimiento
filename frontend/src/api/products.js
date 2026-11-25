import { request } from './client'
import { buildQuery } from './query'

export function getCategories() {
  return request('/api/categorias')
}

export function getSellerProduct(productId) {
  return request(`/api/seller/products/${encodeURIComponent(productId)}`)
}

export function createSellerProduct(product) {
  return request('/api/seller/products', { method: 'POST', body: product })
}

export function updateSellerProduct(productId, product) {
  return request(`/api/seller/products/${encodeURIComponent(productId)}`, { method: 'PUT', body: product })
}

// Sube una imagen binaria del producto propio. El backend valida formato, tamaño y análisis antivirus.
export function uploadSellerProductImage(productId, file) {
  const formData = new FormData()
  formData.append('file', file)
  return request(`/api/seller/products/${encodeURIComponent(productId)}/image`, {
    method: 'POST',
    body: formData,
  })
}

export function deleteSellerProduct(productId) {
  return request(`/api/seller/products/${encodeURIComponent(productId)}`, { method: 'DELETE' })
}

// Detalle de un producto del catálogo para el cliente (HU8). Contrato propuesto por el frontend: el backend aún no lo
// tiene. Responde 404 si el producto no existe o no está disponible. El precio con el descuento que le corresponde a
// quien consulta (cliente Estándar o Premium) lo calcula el servidor: `precioFinal` y `descuento` ({ porcentaje } o null).
export function getCatalogProduct(productId) {
  return request(`/api/products/${encodeURIComponent(productId)}`)
}

// Valoraciones de un producto (CLI-03). Incluye autor y fecha de creación; no expone el identificador del cliente.
export function getProductReviews(productId) {
  return request(`/api/products/${encodeURIComponent(productId)}/valoraciones`)
}

// Estado de compra y valoración del cliente autenticado (CLI-04). La identidad se resuelve en el backend mediante la sesión.
export function getProductReviewStatus(productId) {
  return request(`/api/products/${encodeURIComponent(productId)}/valoraciones/estado`)
}

// Valora un producto comprado (HU13; backend en T370, PR #179) con { puntuacion (1 a 5), comentario (hasta 500) }.
// Responde 201 con la valoración; 400 VALIDATION_ERROR si los datos no son válidos, 403 PRODUCT_NOT_PURCHASED si el
// cliente no lo ha comprado y 409 REVIEW_ALREADY_EXISTS si ya lo valoró.
export function createProductReview(productId, review) {
  return request(`/api/products/${encodeURIComponent(productId)}/valoraciones`, { method: 'POST', body: review })
}

// Activa o desactiva un producto propio (HU16; backend en T282 y T283). Responde con el producto actualizado
// y 404 si el producto no existe o es de otro vendedor.
export function updateSellerProductAvailability(productId, disponible) {
  return request(`/api/seller/products/${encodeURIComponent(productId)}/availability`, {
    method: 'PATCH',
    body: { disponible },
  })
}

// Catálogo del cliente (HU7). Contrato propuesto por el frontend: el backend (T352 y T353) aún no lo tiene.
// sort: nombre, valoracion o precio. order: asc o desc. minRating: valoración media mínima (de 1 a 5).
// Selección pública de la portada: no requiere sesión ni expone datos comerciales.
export function getFeaturedProducts(limit = 10) {
  return request(`/api/products/featured?${buildQuery({ limit })}`)
}

export function getCatalogProducts({
  search = '',
  category = '',
  minRating = '',
  inStock = false,
  offersOnly = false,
  minPrice = 0,
  maxPrice = null,
  sort = 'nombre',
  order = 'asc',
  page = 1,
  size = 10,
} = {}) {
  return request(`/api/products?${buildQuery({ page, size, search, category, minRating, inStock, offersOnly, minPrice: minPrice > 0 ? minPrice : '', maxPrice: maxPrice ?? '', sort, order })}`)
}

export function getSellerProducts({
  search = '', category = '', status = '', sort = '', minPrice = '', maxPrice = '', page = 1, size = 10,
} = {}) {
  return request(`/api/seller/products?${buildQuery({
    page, size, search, category, status, sort, minPrice, maxPrice,
  })}`)
}
