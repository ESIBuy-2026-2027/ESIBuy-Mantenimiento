package es.uclm.esibuy.backend.estadisticas.infrastructure.adapter.in.web;

import java.math.BigDecimal;
import java.util.List;

import es.uclm.esibuy.backend.estadisticas.domain.model.EstadisticasVendedor;

/** Respuesta de GET /api/seller/statistics (HU17): las estadísticas agrupadas en ventas, valoraciones y stock. */
public record EstadisticasVendedorResponse(Ventas ventas, Valoraciones valoraciones, Stock stock) {

    public record Ventas(int pedidos, int unidadesVendidas, BigDecimal ingresos, List<MasVendido> masVendidos,
            List<MenosVendido> menosVendidos) {
    }

    public record MasVendido(String productoId, String nombre, int unidades, BigDecimal ingresos) {
    }

    public record MenosVendido(String productoId, String nombre, int unidades, BigDecimal ingresos) {
    }

    // media es null si el vendedor aún no tiene valoraciones
    public record Valoraciones(Double media, int total, List<ValoracionDeProducto> porProducto) {
    }

    public record ValoracionDeProducto(String productoId, String nombre, double media, int numero) {
    }

    public record Stock(int productos, int sinStock, int stockBajo, int noDisponibles,
            List<ProductoEnAlertaStock> productosConStockBajo,
            List<ProductoEnAlertaStock> productosSinStock) {
    }

    public record ProductoEnAlertaStock(String productoId, String nombre, int stock) {
    }

    public static EstadisticasVendedorResponse desde(EstadisticasVendedor estadisticas) {
        List<MasVendido> masVendidos = estadisticas.masVendidos().stream()
                .map(producto -> new MasVendido(producto.productoId(), producto.nombre(), producto.unidades(),
                        producto.ingresos()))
                .toList();
        List<MenosVendido> menosVendidos = estadisticas.menosVendidos().stream()
                .map(producto -> new MenosVendido(producto.productoId(), producto.nombre(), producto.unidades(),
                        producto.ingresos()))
                .toList();
        List<ValoracionDeProducto> porProducto = estadisticas.valoracionesPorProducto().stream()
                .map(valoracion -> new ValoracionDeProducto(valoracion.productoId(), valoracion.nombre(),
                        valoracion.media(), valoracion.numero()))
                .toList();
        List<ProductoEnAlertaStock> productosConStockBajo = estadisticas.productosStockBajo().stream()
                .map(producto -> new ProductoEnAlertaStock(producto.productoId(), producto.nombre(), producto.stock()))
                .toList();
        List<ProductoEnAlertaStock> productosSinStock = estadisticas.productosSinStock().stream()
                .map(producto -> new ProductoEnAlertaStock(producto.productoId(), producto.nombre(), producto.stock()))
                .toList();
        return new EstadisticasVendedorResponse(
                new Ventas(estadisticas.pedidos(), estadisticas.unidadesVendidas(), estadisticas.ingresos(),
                        masVendidos, menosVendidos),
                new Valoraciones(estadisticas.valoracionMedia(), estadisticas.numeroValoraciones(), porProducto),
                new Stock(estadisticas.productos(), estadisticas.sinStock(), estadisticas.stockBajo(),
                        estadisticas.noDisponibles(), productosConStockBajo, productosSinStock));
    }
}
