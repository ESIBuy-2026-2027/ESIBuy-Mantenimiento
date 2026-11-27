package es.uclm.esibuy.backend.estadisticas.infrastructure.adapter.in.web;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import es.uclm.esibuy.backend.estadisticas.domain.model.EstadisticasVendedor;
import es.uclm.esibuy.backend.estadisticas.domain.model.GranularidadVentas;
import es.uclm.esibuy.backend.estadisticas.domain.model.PeriodoEstadisticasVendedor;
import es.uclm.esibuy.backend.estadisticas.domain.model.ValoracionProducto;
import es.uclm.esibuy.backend.estadisticas.domain.model.ValoracionProductoVendedor;

/** Respuesta de GET /api/seller/statistics (HU17): las estadísticas agrupadas en ventas, valoraciones y stock. */
public record EstadisticasVendedorResponse(PeriodoEstadisticasVendedor periodo, Ventas ventas,
        Valoraciones valoraciones, Stock stock) {

    public record Ventas(int pedidos, int unidadesVendidas, BigDecimal ingresos, List<MasVendido> masVendidos,
            List<MenosVendido> menosVendidos, GranularidadVentas granularidad,
            List<EvolucionVenta> evolucion) {
    }

    public record EvolucionVenta(LocalDate fecha, int pedidos, int unidades, BigDecimal ingresos) {
    }

    public record MasVendido(String productoId, String nombre, String imagen, int unidades, BigDecimal ingresos) {
    }

    public record MenosVendido(String productoId, String nombre, String imagen, int unidades, BigDecimal ingresos) {
    }

    // media es null si el vendedor aún no tiene valoraciones
    public record Valoraciones(Double media, int total, List<ValoracionDeProducto> porProducto,
            List<ProductoValoradoResponse> mejorValorados, List<ProductoValoradoResponse> peorValorados,
            List<PuntuacionResponse> distribucion) {
    }

    public record ValoracionDeProducto(String productoId, String nombre, double media, int numero) {
    }

    public record PuntuacionResponse(int puntuacion, int numero, double porcentaje) {
    }

    public record ProductoValoradoResponse(String productoId, String nombre, String imagen, double media, int numero) {
    }

    public record Stock(int productos, int sinStock, int stockBajo, int noDisponibles,
            List<ProductoEnAlertaStock> productosConStockBajo,
            List<ProductoEnAlertaStock> productosSinStock) {
    }

    public record ProductoEnAlertaStock(String productoId, String nombre, int stock) {
    }

    public static EstadisticasVendedorResponse desde(EstadisticasVendedor estadisticas,
            PeriodoEstadisticasVendedor periodo) {
        List<MasVendido> masVendidos = estadisticas.masVendidos().stream()
                .map(producto -> new MasVendido(producto.productoId(), producto.nombre(), producto.imagen(),
                        producto.unidades(),
                        producto.ingresos()))
                .toList();
        List<MenosVendido> menosVendidos = estadisticas.menosVendidos().stream()
                .map(producto -> new MenosVendido(producto.productoId(), producto.nombre(), producto.imagen(),
                        producto.unidades(),
                        producto.ingresos()))
                .toList();
        List<EvolucionVenta> evolucion = estadisticas.evolucionVentas().stream()
                .map(punto -> new EvolucionVenta(punto.fecha(), punto.pedidos(), punto.unidades(), punto.ingresos()))
                .toList();
        List<PuntuacionResponse> distribucion = estadisticas.distribucionValoraciones().stream()
                .map(puntuacion -> new PuntuacionResponse(puntuacion.puntuacion(), puntuacion.numero(),
                        puntuacion.porcentaje()))
                .toList();
        List<ValoracionDeProducto> porProducto = mapearValoraciones(estadisticas.valoracionesPorProducto());
        List<ProductoValoradoResponse> mejorValorados = mapearRankingValoraciones(estadisticas.mejorValorados());
        List<ProductoValoradoResponse> peorValorados = mapearRankingValoraciones(estadisticas.peorValorados());
        List<ProductoEnAlertaStock> productosConStockBajo = estadisticas.productosStockBajo().stream()
                .map(producto -> new ProductoEnAlertaStock(producto.productoId(), producto.nombre(), producto.stock()))
                .toList();
        List<ProductoEnAlertaStock> productosSinStock = estadisticas.productosSinStock().stream()
                .map(producto -> new ProductoEnAlertaStock(producto.productoId(), producto.nombre(), producto.stock()))
                .toList();
        return new EstadisticasVendedorResponse(
                periodo,
                new Ventas(estadisticas.pedidos(), estadisticas.unidadesVendidas(), estadisticas.ingresos(),
                        masVendidos, menosVendidos, estadisticas.granularidadVentas(), evolucion),
                new Valoraciones(estadisticas.valoracionMedia(), estadisticas.numeroValoraciones(), porProducto,
                        mejorValorados, peorValorados, distribucion),
                new Stock(estadisticas.productos(), estadisticas.sinStock(), estadisticas.stockBajo(),
                        estadisticas.noDisponibles(), productosConStockBajo, productosSinStock));
    }

    private static List<ValoracionDeProducto> mapearValoraciones(
            List<ValoracionProducto> valoraciones) {
        return valoraciones.stream()
                .map(valoracion -> new ValoracionDeProducto(valoracion.productoId(), valoracion.nombre(),
                        valoracion.media(), valoracion.numero()))
                .toList();
    }

    private static List<ProductoValoradoResponse> mapearRankingValoraciones(
            List<ValoracionProductoVendedor> valoraciones) {
        return valoraciones.stream()
                .map(valoracion -> new ProductoValoradoResponse(valoracion.productoId(), valoracion.nombre(),
                        valoracion.imagen(), valoracion.media(), valoracion.numero()))
                .toList();
    }
}
