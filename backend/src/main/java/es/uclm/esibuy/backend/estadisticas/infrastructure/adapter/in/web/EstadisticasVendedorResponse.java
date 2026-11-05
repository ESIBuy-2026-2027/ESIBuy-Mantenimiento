package es.uclm.esibuy.backend.estadisticas.infrastructure.adapter.in.web;

import java.math.BigDecimal;
import java.util.List;

import es.uclm.esibuy.backend.estadisticas.domain.model.EstadisticasVendedor;

/** Respuesta de GET /api/seller/statistics (HU17): las estadísticas agrupadas en ventas, valoraciones y stock. */
public record EstadisticasVendedorResponse(Ventas ventas, Valoraciones valoraciones, Stock stock) {

    public record Ventas(int pedidos, int unidadesVendidas, BigDecimal ingresos, List<MasVendido> masVendidos) {
    }

    public record MasVendido(String productoId, String nombre, int unidades, BigDecimal ingresos) {
    }

    // media es null si el vendedor aún no tiene valoraciones
    public record Valoraciones(Double media, int total, List<ValoracionDeProducto> porProducto) {
    }

    public record ValoracionDeProducto(String productoId, String nombre, double media, int numero) {
    }

    public record Stock(int productos, int sinStock, int stockBajo, int noDisponibles) {
    }

    public static EstadisticasVendedorResponse desde(EstadisticasVendedor estadisticas) {
        throw new UnsupportedOperationException("Funcionalidad no implementada");
    }
}
