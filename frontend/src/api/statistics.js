import { request } from './client'

// Estadísticas del vendedor en sesión (HU17), desde siempre y sin contar los pedidos cancelados. El backend lo
// deduce de la sesión, así que no se envía ningún identificador. Llegan agrupadas en `ventas` (con los 5 más
// vendidos), `valoraciones` (media ponderada, `null` si no hay ninguna, y solo los productos valorados, del mejor al
// peor) y `stock` (stock bajo es de 1 a 5 unidades).
export function getSellerStatistics() {
  return request('/api/seller/statistics')
}
