package es.uclm.esibuy.backend.estadisticas.domain.model;

import java.math.BigDecimal;
import java.util.List;

/**
 * Estadísticas básicas de ventas, valoraciones y stock de un vendedor (HU17). Se calculan siempre a partir de sus
 * ventas y de sus productos, nunca se guardan.
 */
public record EstadisticasVendedor(int pedidos, int unidadesVendidas, BigDecimal ingresos,
        List<ProductoMasVendido> masVendidos, Double valoracionMedia, int numeroValoraciones,
        List<ValoracionProducto> valoracionesPorProducto, int productos, int sinStock, int stockBajo,
        int noDisponibles) {

    public static EstadisticasVendedor calcular(List<VentaRegistrada> ventas, List<ProductoInventariado> productos) {
        throw new UnsupportedOperationException("Funcionalidad no implementada");
    }
}
