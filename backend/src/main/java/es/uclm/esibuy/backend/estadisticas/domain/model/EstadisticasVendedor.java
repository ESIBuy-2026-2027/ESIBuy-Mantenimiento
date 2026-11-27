package es.uclm.esibuy.backend.estadisticas.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Estadísticas básicas de ventas, valoraciones y stock de un vendedor (HU17). Se calculan siempre a partir de sus
 * ventas y de sus productos, nunca se guardan.
 */
public record EstadisticasVendedor(int pedidos, int unidadesVendidas, BigDecimal ingresos,
        List<ProductoMasVendido> masVendidos, List<ProductoMenosVendido> menosVendidos,
        Double valoracionMedia, int numeroValoraciones,
        List<ValoracionProducto> valoracionesPorProducto, List<ValoracionProductoVendedor> mejorValorados,
        List<ValoracionProductoVendedor> peorValorados, int productos, int sinStock, int stockBajo,
        int noDisponibles, List<ProductoInventariado> productosStockBajo,
        List<ProductoInventariado> productosSinStock, List<PuntuacionValoracion> distribucionValoraciones,
        List<EvolucionVentas> evolucionVentas, GranularidadVentas granularidadVentas) {

    private static final int MAXIMO_RANKING = 10;
    // Decisión del equipo: un producto con entre 1 y 5 unidades tiene stock bajo; con 0, no tiene stock
    private static final int UMBRAL_STOCK_BAJO = 5;

    public static EstadisticasVendedor calcular(List<VentaRegistrada> ventas, List<ProductoInventariado> productos) {
        List<ValoracionProductoPeriodo> valoraciones = productos.stream()
                .filter(p -> p.numeroValoraciones() > 0)
                .map(p -> new ValoracionProductoPeriodo(p.productoId(), p.valoracionMedia(), p.numeroValoraciones()))
                .toList();
        int total = valoraciones.stream().mapToInt(ValoracionProductoPeriodo::numero).sum();
        Double media = total == 0 ? null : valoraciones.stream()
                .mapToDouble(v -> v.media() * v.numero()).sum() / total;
        return calcular(ventas, productos,
                new ResumenValoracionesPeriodo(media, total, valoraciones, List.of()),
                PeriodoEstadisticasVendedor.TODO_EL_PERIODO, LocalDate.now(ZoneOffset.UTC), ZoneOffset.UTC);
    }

    public static EstadisticasVendedor calcular(List<VentaRegistrada> ventas, List<ProductoInventariado> productos,
            ResumenValoracionesPeriodo resumenValoraciones, PeriodoEstadisticasVendedor periodo, LocalDate hoy) {
        return calcular(ventas, productos, resumenValoraciones, periodo, hoy, ZoneOffset.UTC);
    }

    public static EstadisticasVendedor calcular(List<VentaRegistrada> ventas, List<ProductoInventariado> productos,
            ResumenValoracionesPeriodo resumenValoraciones, PeriodoEstadisticasVendedor periodo, LocalDate hoy,
            ZoneId zonaHoraria) {
        int pedidos = (int) ventas.stream().map(VentaRegistrada::pedidoId).distinct().count();
        int unidades = ventas.stream().mapToInt(VentaRegistrada::unidades).sum();
        BigDecimal ingresos = ventas.stream().map(VentaRegistrada::importe).reduce(BigDecimal.ZERO, BigDecimal::add);
        Map<String, ProductoMasVendido> ventasPorProducto = ventasPorProducto(ventas);
        Map<String, ProductoInventariado> productosPorId = new HashMap<>();
        productos.forEach(producto -> productosPorId.put(producto.productoId(), producto));
        List<ValoracionProductoPeriodo> valorados = resumenValoraciones.productos().stream()
                .filter(valoracion -> valoracion.numero() > 0 && productosPorId.containsKey(valoracion.productoId()))
                .toList();
        int numeroValoraciones = resumenValoraciones.total();
        List<ValoracionProducto> valoracionesPorProducto = valoracionesPorProducto(valorados, productosPorId);
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
                resumenValoraciones.media(), numeroValoraciones, valoracionesPorProducto,
                mejoresValoraciones(valorados, productosPorId), peoresValoraciones(valorados, productosPorId),
                productos.size(), contar(productos, p -> p.stock() == 0),
                contar(productos, p -> p.stock() > 0 && p.stock() <= UMBRAL_STOCK_BAJO),
                contar(productos, p -> !p.disponible()), productosStockBajo, productosSinStock,
                distribucionValoraciones(resumenValoraciones), evolucionVentas(ventas, hoy, zonaHoraria),
                periodo.granularidad());
    }

    private static int contar(List<ProductoInventariado> productos, Predicate<ProductoInventariado> condicion) {
        return (int) productos.stream().filter(condicion).count();
    }

    // Del mejor valorado al peor
    private static List<ValoracionProducto> valoracionesPorProducto(List<ValoracionProductoPeriodo> valorados,
            Map<String, ProductoInventariado> productosPorId) {
        return valorados.stream()
                .map(valoracion -> new ValoracionProducto(valoracion.productoId(),
                        productosPorId.get(valoracion.productoId()).nombre(), valoracion.media(), valoracion.numero()))
                .sorted(Comparator.comparingDouble(ValoracionProducto::media).reversed())
                .toList();
    }

    private static List<ValoracionProductoVendedor> mejoresValoraciones(List<ValoracionProductoPeriodo> valorados,
            Map<String, ProductoInventariado> productosPorId) {
        return valorados.stream()
                .map(valoracion -> crearValoracionProductoVendedor(valoracion, productosPorId))
                .sorted(Comparator.comparingDouble(ValoracionProductoVendedor::media).reversed()
                        .thenComparing(ValoracionProductoVendedor::nombre, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(ValoracionProductoVendedor::productoId))
                .limit(MAXIMO_RANKING)
                .toList();
    }

    private static List<ValoracionProductoVendedor> peoresValoraciones(List<ValoracionProductoPeriodo> valorados,
            Map<String, ProductoInventariado> productosPorId) {
        return valorados.stream()
                .map(valoracion -> crearValoracionProductoVendedor(valoracion, productosPorId))
                .sorted(Comparator.comparingDouble(ValoracionProductoVendedor::media)
                        .thenComparing(ValoracionProductoVendedor::nombre, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(ValoracionProductoVendedor::productoId))
                .limit(MAXIMO_RANKING)
                .toList();
    }

    private static ValoracionProductoVendedor crearValoracionProductoVendedor(ValoracionProductoPeriodo valoracion,
            Map<String, ProductoInventariado> productosPorId) {
        ProductoInventariado producto = productosPorId.get(valoracion.productoId());
        return new ValoracionProductoVendedor(producto.productoId(), producto.nombre(), producto.imagen(),
                valoracion.media(), valoracion.numero());
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
                            venta.ingresos(), producto.imagen());
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
                            venta == null ? BigDecimal.ZERO : venta.ingresos(), producto.imagen());
                })
                .sorted(Comparator.comparingInt(ProductoMenosVendido::unidades)
                        .thenComparing(ProductoMenosVendido::nombre, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(ProductoMenosVendido::productoId))
                .limit(MAXIMO_RANKING)
                .toList();
    }

    private static List<PuntuacionValoracion> distribucionValoraciones(ResumenValoracionesPeriodo resumen) {
        Map<Integer, Integer> cantidades = new HashMap<>();
        resumen.distribucion().forEach(puntuacion -> cantidades.put(puntuacion.puntuacion(), puntuacion.numero()));
        List<PuntuacionValoracion> distribucion = new ArrayList<>();
        for (int estrellas = 1; estrellas <= 5; estrellas++) {
            int numero = cantidades.getOrDefault(estrellas, 0);
            double porcentaje = resumen.total() == 0 ? 0.0 : (double) numero * 100 / resumen.total();
            distribucion.add(new PuntuacionValoracion(estrellas, numero, porcentaje));
        }
        return List.copyOf(distribucion);
    }

    private static List<EvolucionVentas> evolucionVentas(List<VentaRegistrada> ventas, LocalDate hoy,
            ZoneId zonaHoraria) {
        Map<LocalDate, AcumuladoDiario> acumulados = new HashMap<>();
        for (VentaRegistrada venta : ventas) {
            if (venta.fechaPedido() == null) {
                continue;
            }
            LocalDate fecha = venta.fechaPedido().atZone(zonaHoraria).toLocalDate();
            acumulados.computeIfAbsent(fecha, ignorado -> new AcumuladoDiario()).agregar(venta);
        }
        return evolucionDiaria(acumulados, hoy);
    }

    private static List<EvolucionVentas> evolucionDiaria(Map<LocalDate, AcumuladoDiario> acumulados, LocalDate hoy) {
        List<EvolucionVentas> evolucion = new ArrayList<>();
        for (int diasAtras = 6; diasAtras >= 0; diasAtras--) {
            LocalDate fecha = hoy.minusDays(diasAtras);
            evolucion.add(aEvolucion(fecha, acumulados.getOrDefault(fecha, new AcumuladoDiario())));
        }
        return List.copyOf(evolucion);
    }

    private static EvolucionVentas aEvolucion(LocalDate fecha, AcumuladoDiario acumulado) {
        return new EvolucionVentas(fecha, acumulado.pedidos.size(), acumulado.unidades, acumulado.ingresos);
    }

    private static final class AcumuladoDiario {
        private final Set<String> pedidos = new HashSet<>();
        private int unidades;
        private BigDecimal ingresos = BigDecimal.ZERO;

        private void agregar(VentaRegistrada venta) {
            pedidos.add(venta.pedidoId());
            unidades += venta.unidades();
            ingresos = ingresos.add(venta.importe());
        }
    }
}
