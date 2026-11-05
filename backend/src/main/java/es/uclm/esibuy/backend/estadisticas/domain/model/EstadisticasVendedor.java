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
        int pedidos = (int) ventas.stream().map(VentaRegistrada::pedidoId).distinct().count();
        int unidades = ventas.stream().mapToInt(VentaRegistrada::unidades).sum();
        BigDecimal ingresos = ventas.stream().map(VentaRegistrada::importe).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new EstadisticasVendedor(pedidos, unidades, ingresos, List.of(), null, 0, List.of(), 0, 0, 0, 0);
    }
}
