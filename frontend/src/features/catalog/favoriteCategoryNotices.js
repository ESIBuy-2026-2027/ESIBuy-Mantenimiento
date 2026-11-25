// Avisos de las categorías favoritas (AB#528), los mismos en el detalle del producto y en Mis favoritos.
// Llevan el nombre de la categoría y nunca el texto del backend.
export const FAVORITAS_NO_CARGADAS = 'No se han podido cargar tus categorías favoritas. Inténtalo de nuevo.'

export function avisoDeExito(nombre, marcada) {
  const texto = marcada
    ? `${nombre} se ha añadido a tus categorías favoritas.`
    : `${nombre} se ha quitado de tus categorías favoritas.`
  return { intent: 'success', texto }
}

// Al marcar una que ya no existe o no está activa (404), reintentarlo no serviría
function textoDeFallo(nombre, marcada, error) {
  if (error?.code === 'CATEGORY_NOT_FOUND') return `${nombre} ya no está disponible.`
  return marcada
    ? `No se ha podido añadir ${nombre} a tus categorías favoritas. Inténtalo de nuevo.`
    : `No se ha podido quitar ${nombre} de tus categorías favoritas. Inténtalo de nuevo.`
}

export function avisoDeFallo(nombre, marcada, error) {
  return { intent: 'error', texto: textoDeFallo(nombre, marcada, error) }
}
