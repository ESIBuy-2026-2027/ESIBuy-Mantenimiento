// Media y número de valoraciones del detalle (HU13, decisión del equipo): salen de la lista de valoraciones; si no hay
// ninguna (o aún no ha llegado), se usan las del catálogo. El backend todavía no rellena valoracion ni numValoraciones.
export function reviewSummary(product, reviews) {
  if (reviews?.length > 0) {
    const total = reviews.reduce((suma, review) => suma + review.puntuacion, 0)
    return { valoracion: total / reviews.length, numValoraciones: reviews.length }
  }
  return { valoracion: product.valoracion, numValoraciones: product.numValoraciones }
}
