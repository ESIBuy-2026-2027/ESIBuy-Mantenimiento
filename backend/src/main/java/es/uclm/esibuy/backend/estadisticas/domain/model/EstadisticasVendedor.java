package es.uclm.esibuy.backend.estadisticas.domain.model;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Estadísticas básicas de ventas, valoraciones y stock de un vendedor (HU17). Se calculan siempre a partir de sus
 * ventas y de sus productos, nunca se guardan.
 */
public record EstadisticasVendedor(int pedidos, int unidadesVendidas, BigDecimal ingresos,
        List<ProductoMasVendido> masVendidos, List<ProductoMenosVendido> menosVendidos,
        Double valoracionMedia, int numeroValoraciones,
        List<ValoracionProducto> valoracionesPorProducto, int productos, int sinStock, int stockBajo,
        int noDisponibles, List<ProductoInventariado> productosStockBajo,
        List<ProductoInventariado> productosSinStock) {

    private static final int MAXIMO_RANKING = 10;
    // Decisión del equipo: un producto con entre 1 y 5 unidades tiene stock bajo; con 0, no tiene stock
    private static final int UMBRAL_STOCK_BAJO = 5;

    public static EstadisticasVendedor calcular(List<VentaRegistrada> ventas, List<ProductoInventariado> productos) {
        int pedidos = (int) ventas.stream().map(VentaRegistrada::pedidoId).distinct().count();
        int unidades = ventas.stream().mapToInt(VentaRegistrada::unidades).sum();
        BigDecimal ingresos = ventas.stream().map(VentaRegistrada::importe).reduce(BigDecimal.ZERO, BigDecimal::add);
        Map<String, ProductoMasVendido> ventasPorProducto = ventasPorProducto(ventas);
        List<ProductoInventariado> valorados = productos.stream().filter(p -> p.numeroValoraciones() > 0).toList();
        int numeroValoraciones = valorados.stream().mapToInt(ProductoInventariado::numeroValoraciones).sum();
        List<ProductoInventariado> productosStockBajo = productos.stream()
                .filter(p -> p.stock() > 0 && p.stock() <= UMBRAL_STOCK_BAJO)
                .sorted(Comparator.comparingInt(ProductoInventariado::stock).thenComparing(ProductoInventariado::nombre))
                .toList();
        List<ProductoInventariado> productosSinStock = productos.stream()
                .filter(p -> p.stock() == 0)
                .sorted(Comparator.comparing(ProductoInventariado::nombre))
                .toList();
        return new EstadisticasVendedor(pedidos, unidades, ingresos, masVendidos(ventasPorProducto, productos),
                menosVendidos(ventasPorProducto, productos),
                mediaPonderada(valorados, numeroValoraciones), numeroValoraciones, valoracionesPorProducto(valorados),
                productos.size(), contar(productos, p -> p.stock() == 0),
                contar(productos, p -> p.stock() > 0 && p.stock() <= UMBRAL_STOCK_BAJO),
                contar(productos, p -> !p.disponible()), productosStockBajo, productosSinStock);
    }

    private static int contar(List<ProductoInventariado> productos, Predicate<ProductoInventariado> condicion) {
        return (int) productos.stream().filter(condicion).count();
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

    // Los rankings parten del inventario del vendedor: las ventas de otros productos nunca se exponen.
    private static Map<String, ProductoMasVendido> ventasPorProducto(List<VentaRegistrada> ventas) {
        Map<String, ProductoMasVendido> porProducto = new LinkedHashMap<>();
        for (VentaRegistrada venta : ventas) {
            porProducto.merge(venta.productoId(),
                    new ProductoMasVendido(venta.productoId(), venta.nombreProducto(), venta.unidades(),
                            venta.importe()),
                    (acumulado, nueva) -> new ProductoMasVendido(acumulado.productoId(), acumulado.nombre(),
                            acumulado.unidades() + nueva.unidades(), acumulado.ingresos().add(nueva.ingresos())));
        }
        return porProducto;
    }

    // A igualdad de unidades, se conserva primero el producto que más ha ingresado.
    private static List<ProductoMasVendido> masVendidos(Map<String, ProductoMasVendido> ventasPorProducto,
            List<ProductoInventariado> productos) {
        return productos.stream()
                .filter(producto -> ventasPorProducto.containsKey(producto.productoId()))
                .map(producto -> {
                    ProductoMasVendido venta = ventasPorProducto.get(producto.productoId());
                    return new ProductoMasVendido(producto.productoId(), producto.nombre(), venta.unidades(),
                            venta.ingresos());
                })
                .sorted(Comparator.comparingInt(ProductoMasVendido::unidades)
                        .thenComparing(ProductoMasVendido::ingresos)
                        .reversed()
                        .thenComparing(ProductoMasVendido::nombre, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(ProductoMasVendido::productoId))
                .limit(MAXIMO_RANKING)
                .toList();
    }

    private static List<ProductoMenosVendido> menosVendidos(Map<String, ProductoMasVendido> ventasPorProducto,
            List<ProductoInventariado> productos) {
        return productos.stream()
                .map(producto -> {
                    ProductoMasVendido venta = ventasPorProducto.get(producto.productoId());
                    return new ProductoMenosVendido(producto.productoId(), producto.nombre(),
                            venta == null ? 0 : venta.unidades(),
                            venta == null ? BigDecimal.ZERO : venta.ingresos());
                })
                .sorted(Comparator.comparingInt(ProductoMenosVendido::unidades)
                        .thenComparing(ProductoMenosVendido::nombre, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(ProductoMenosVendido::productoId))
                .limit(MAXIMO_RANKING)
                .toList();
    }
}
