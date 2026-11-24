import { request } from './client'

const esObjeto = (valor) => valor !== null && typeof valor === 'object' && !Array.isArray(valor)
const esNumero = Number.isFinite

function esProductoEnAlertaStock(producto) {
  return esObjeto(producto)
    && typeof producto.productoId === 'string'
    && typeof producto.nombre === 'string'
    && Number.isInteger(producto.stock)
}

function respuestaDeEstadisticasValida(data) {
  const ventas = data?.ventas
  const valoraciones = data?.valoraciones
  const stock = data?.stock

  return esObjeto(ventas)
    && Number.isInteger(ventas.pedidos)
    && Number.isInteger(ventas.unidadesVendidas)
    && esNumero(ventas.ingresos)
    && Array.isArray(ventas.masVendidos)
    && esObjeto(valoraciones)
    && (valoraciones.media === null || esNumero(valoraciones.media))
    && Number.isInteger(valoraciones.total)
    && Array.isArray(valoraciones.porProducto)
    && esObjeto(stock)
    && Number.isInteger(stock.productos)
    && Number.isInteger(stock.sinStock)
    && Number.isInteger(stock.stockBajo)
    && Number.isInteger(stock.noDisponibles)
    && Array.isArray(stock.productosConStockBajo)
    && stock.productosConStockBajo.every(esProductoEnAlertaStock)
    && Array.isArray(stock.productosSinStock)
    && stock.productosSinStock.every(esProductoEnAlertaStock)
}

// Estadísticas del vendedor en sesión (HU17), desde siempre y sin contar los pedidos cancelados. El backend lo
// deduce de la sesión, así que no se envía ningún identificador. Llegan agrupadas en `ventas` (con los 5 más
// vendidos), `valoraciones` (media ponderada, `null` si no hay ninguna, y solo los productos valorados, del mejor al
// peor) y `stock` (stock bajo es de 1 a 5 unidades, con el detalle de los productos con stock bajo y sin stock).
export function getSellerStatistics() {
  return request('/api/seller/statistics').then((data) => {
    if (!respuestaDeEstadisticasValida(data)) {
      throw new TypeError('El endpoint de estadísticas devolvió una respuesta con un formato inválido.')
    }
    return data
  })
}
