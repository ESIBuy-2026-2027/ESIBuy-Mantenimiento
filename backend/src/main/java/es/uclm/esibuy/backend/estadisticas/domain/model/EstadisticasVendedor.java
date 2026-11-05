package es.uclm.esibuy.backend.estadisticas.domain.model;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Estadísticas básicas de ventas, valoraciones y stock de un vendedor (HU17). Se calculan siempre a partir de sus
 * ventas y de sus productos, nunca se guardan.
 */
public record EstadisticasVendedor(int pedidos, int unidadesVendidas, BigDecimal ingresos,
        List<ProductoMasVendido> masVendidos, Double valoracionMedia, int numeroValoraciones,
        List<ValoracionProducto> valoracionesPorProducto, int productos, int sinStock, int stockBajo,
        int noDisponibles) {

    private static final int MAXIMO_MAS_VENDIDOS = 5;

    public static EstadisticasVendedor calcular(List<VentaRegistrada> ventas, List<ProductoInventariado> productos) {
        int pedidos = (int) ventas.stream().map(VentaRegistrada::pedidoId).distinct().count();
        int unidades = ventas.stream().mapToInt(VentaRegistrada::unidades).sum();
        BigDecimal ingresos = ventas.stream().map(VentaRegistrada::importe).reduce(BigDecimal.ZERO, BigDecimal::add);
        List<ProductoInventariado> valorados = productos.stream().filter(p -> p.numeroValoraciones() > 0).toList();
        int numeroValoraciones = valorados.stream().mapToInt(ProductoInventariado::numeroValoraciones).sum();
        return new EstadisticasVendedor(pedidos, unidades, ingresos, masVendidos(ventas),
                mediaPonderada(valorados, numeroValoraciones), numeroValoraciones, valoracionesPorProducto(valorados),
                0, 0, 0, 0);
    }

    // Cada producto pesa tanto como valoraciones tiene: es la media de todas las valoraciones del vendedor. Sin
    // valoraciones no hay media.
    private static Double mediaPonderada(List<ProductoInventariado> valorados, int numeroValoraciones) {
        if (numeroValoraciones == 0) {
            return null;
        }
        double suma = valorados.stream().mapToDouble(p -> p.valoracionMedia() * p.numeroValoraciones()).sum();
        return suma / numeroValoraciones;
    }

    // Del mejor valorado al peor
    private static List<ValoracionProducto> valoracionesPorProducto(List<ProductoInventariado> valorados) {
        return valorados.stream()
                .map(p -> new ValoracionProducto(p.productoId(), p.nombre(), p.valoracionMedia(),
                        p.numeroValoraciones()))
                .sorted(Comparator.comparingDouble(ValoracionProducto::media).reversed())
                .toList();
    }

    // Suma las ventas de cada producto y se queda con los que más unidades han vendido; a igualdad de unidades, el
    // que más ha ingresado. El nombre es el de su primera venta.
    private static List<ProductoMasVendido> masVendidos(List<VentaRegistrada> ventas) {
        Map<String, ProductoMasVendido> porProducto = new LinkedHashMap<>();
        for (VentaRegistrada venta : ventas) {
            porProducto.merge(venta.productoId(),
                    new ProductoMasVendido(venta.productoId(), venta.nombreProducto(), venta.unidades(),
                            venta.importe()),
                    (acumulado, nueva) -> new ProductoMasVendido(acumulado.productoId(), acumulado.nombre(),
                            acumulado.unidades() + nueva.unidades(), acumulado.ingresos().add(nueva.ingresos())));
        }
        return porProducto.values().stream()
                .sorted(Comparator.comparingInt(ProductoMasVendido::unidades)
                        .thenComparing(ProductoMasVendido::ingresos)
                        .reversed())
                .limit(MAXIMO_MAS_VENDIDOS)
                .toList();
    }
}
