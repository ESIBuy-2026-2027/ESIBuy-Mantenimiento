import { request } from './client'

const esObjeto = (valor) => valor !== null && typeof valor === 'object' && !Array.isArray(valor)
const esNumero = Number.isFinite
const esCadena = (valor) => typeof valor === 'string'

function esProductoMasVendido(producto) {
  return esObjeto(producto)
    && esCadena(producto.productoId)
    && esCadena(producto.nombre)
    && Number.isInteger(producto.unidades)
    && esNumero(producto.ingresos)
}

function esProductoMejorValorado(producto) {
  return esObjeto(producto)
    && esCadena(producto.productoId)
    && esCadena(producto.nombre)
    && esNumero(producto.media)
    && Number.isInteger(producto.numeroValoraciones)
}

function esValoracionPorProducto(producto) {
  return esObjeto(producto)
    && esCadena(producto.productoId)
    && esCadena(producto.nombre)
    && esNumero(producto.media)
    && Number.isInteger(producto.numero)
}

function esProductoValoradoVendedor(producto) {
  return esObjeto(producto)
    && esCadena(producto.productoId)
    && esCadena(producto.nombre)
    && esCadena(producto.imagen)
    && esNumero(producto.media)
    && Number.isInteger(producto.numero)
}

function esVendedorMejorValorado(vendedor) {
  return esObjeto(vendedor)
    && esCadena(vendedor.vendedorId)
    && esCadena(vendedor.nombreComercial)
    && esNumero(vendedor.media)
    && Number.isInteger(vendedor.numeroValoraciones)
}

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
    && ventas.masVendidos.every(esProductoMasVendido)
    && Array.isArray(ventas.menosVendidos)
    && ventas.menosVendidos.every(esProductoMasVendido)
    && esObjeto(valoraciones)
    && (valoraciones.media === null || esNumero(valoraciones.media))
    && Number.isInteger(valoraciones.total)
    && Array.isArray(valoraciones.porProducto)
    && valoraciones.porProducto.every(esValoracionPorProducto)
    && Array.isArray(valoraciones.mejorValorados)
    && valoraciones.mejorValorados.every(esProductoValoradoVendedor)
    && Array.isArray(valoraciones.peorValorados)
    && valoraciones.peorValorados.every(esProductoValoradoVendedor)
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
// deduce de la sesión, así que no se envía ningún identificador. Cada ranking contiene hasta 10 productos;
// `menosVendidos` también incluye los productos sin ventas y los rankings de valoración solo los valorados.
// `stock` incluye el detalle de los productos con stock bajo y sin stock.
export function getSellerStatistics() {
  return request('/api/seller/statistics').then((data) => {
    if (!respuestaDeEstadisticasValida(data)) {
      throw new TypeError('El endpoint de estadísticas devolvió una respuesta con un formato inválido.')
    }
    return data
  })
}

function respuestaDeEstadisticasAdminValida(data) {
  return esObjeto(data)
    && Array.isArray(data.productosMasVendidos)
    && data.productosMasVendidos.every(esProductoMasVendido)
    && Array.isArray(data.productosMejorValorados)
    && data.productosMejorValorados.every(esProductoMejorValorado)
    && Array.isArray(data.vendedoresMejorValorados)
    && data.vendedoresMejorValorados.every(esVendedorMejorValorado)
}

export function getAdminStatistics() {
  return request('/api/admin/statistics').then((data) => {
    if (!respuestaDeEstadisticasAdminValida(data)) {
      throw new TypeError(
        'El endpoint de estadísticas del administrador devolvió una respuesta con un formato inválido.',
      )
    }
    return data
  })
}
