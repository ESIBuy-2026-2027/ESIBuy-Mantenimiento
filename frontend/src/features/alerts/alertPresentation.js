// Presentación común de las alertas, para «Mis alertas» (AB#529) y la notificación al iniciar sesión (AB#531).

// Colores de cada tipo de alerta, como los carteles de una tienda: rojo para las rebajas, verde para el stock y morado
// para las novedades (AB#530)
export const ROJO = '#D92D20'
export const VERDE = '#079455'
export const MORADO = '#7A5AF8'
// Color de la marca (el de la cabecera)
export const NARANJA = '#B54708'

// Marcador para un producto sin imagen
export const SIN_IMAGEN =
  "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24'%3E%3Cpath d='M3 3l18 18M10 12l-4 6h12l-3-4-2 2M6 6h11a2 2 0 0 1 2 2v8' fill='none' stroke='%23999999' stroke-width='1.5' stroke-linecap='round' stroke-linejoin='round'/%3E%3C/svg%3E"

// Porcentaje de rebaja, redondeado: de 59,90 € a 53,91 € es un 10 %
export const porcentajeRebaja = (anterior, nuevo) => Math.round((1 - nuevo / anterior) * 100)

// Una bajada de precio que trae los dos precios (sin ellos no se puede enseñar la rebaja)
export const esBajadaConPrecios = (alerta) =>
  alerta.tipo === 'BAJADA_PRECIO' && alerta.producto.precioAnterior != null && alerta.producto.precioNuevo != null
