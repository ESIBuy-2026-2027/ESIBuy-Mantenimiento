package es.uclm.esibuy.backend.estadisticas.domain.model;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Rankings globales de ventas y valoraciones para el panel del administrador. */
public record EstadisticasAdministrador(List<ProductoMasVendido> productosMasVendidos,
        List<ValoracionProducto> productosMejorValorados, List<VendedorMasValorado> vendedoresMejorValorados) {

    private static final int MAXIMO_RESULTADOS = 10;

    public static EstadisticasAdministrador calcular(List<VentaRegistrada> ventas, List<ProductoValorado> productos) {
        List<ProductoMasVendido> productosMasVendidos = productosMasVendidos(ventas);
        List<ProductoValorado> valorados = productos.stream()
                .filter(producto -> producto.numeroValoraciones() > 0 && producto.valoracionMedia() != null)
                .toList();
        List<ValoracionProducto> productosMejorValorados = productosMejorValorados(valorados);
        List<VendedorMasValorado> vendedoresMejorValorados = vendedoresMejorValorados(valorados);

        return new EstadisticasAdministrador(productosMasVendidos, productosMejorValorados,
                vendedoresMejorValorados);
    }

    private static List<ProductoMasVendido> productosMasVendidos(List<VentaRegistrada> ventas) {
        Map<String, ProductoMasVendido> porProducto = new LinkedHashMap<>();
        for (VentaRegistrada venta : ventas) {
            porProducto.merge(venta.productoId(),
                    new ProductoMasVendido(venta.productoId(), venta.nombreProducto(), venta.unidades(),
                            venta.importe()),
                    (acumulado, nueva) -> new ProductoMasVendido(acumulado.productoId(), acumulado.nombre(),
                            acumulado.unidades() + nueva.unidades(), acumulado.ingresos().add(nueva.ingresos())));
        }
        return porProducto.values().stream()
                .sorted(Comparator.comparingInt(ProductoMasVendido::unidades).reversed()
                        .thenComparing(Comparator.comparing(ProductoMasVendido::ingresos).reversed())
                        .thenComparing(ProductoMasVendido::nombre, String.CASE_INSENSITIVE_ORDER))
                .limit(MAXIMO_RESULTADOS)
                .toList();
    }

    private static List<ValoracionProducto> productosMejorValorados(List<ProductoValorado> productos) {
        return productos.stream()
                .map(producto -> new ValoracionProducto(producto.productoId(), producto.nombre(),
                        producto.valoracionMedia(), producto.numeroValoraciones()))
                .sorted(Comparator.comparingDouble(ValoracionProducto::media).reversed()
                        .thenComparing(Comparator.comparingInt(ValoracionProducto::numero).reversed())
                        .thenComparing(ValoracionProducto::nombre, String.CASE_INSENSITIVE_ORDER))
                .limit(MAXIMO_RESULTADOS)
                .toList();
    }

    private static List<VendedorMasValorado> vendedoresMejorValorados(List<ProductoValorado> productos) {
        Map<String, ValoracionesVendedor> porVendedor = new LinkedHashMap<>();
        for (ProductoValorado producto : productos) {
            porVendedor.merge(producto.vendedorId(),
                    new ValoracionesVendedor(producto.valoracionMedia() * producto.numeroValoraciones(),
                            producto.numeroValoraciones()),
                    (acumulado, nuevas) -> new ValoracionesVendedor(
                            acumulado.sumaPonderada() + nuevas.sumaPonderada(),
                            acumulado.numeroValoraciones() + nuevas.numeroValoraciones()));
        }
        return porVendedor.entrySet().stream()
                .map(entrada -> new VendedorMasValorado(entrada.getKey(),
                        entrada.getValue().sumaPonderada() / entrada.getValue().numeroValoraciones(),
                        entrada.getValue().numeroValoraciones()))
                .sorted(Comparator.comparingDouble(VendedorMasValorado::media).reversed()
                        .thenComparing(Comparator.comparingInt(VendedorMasValorado::numeroValoraciones).reversed())
                        .thenComparing(VendedorMasValorado::vendedorId))
                .limit(MAXIMO_RESULTADOS)
                .toList();
    }

    private record ValoracionesVendedor(double sumaPonderada, int numeroValoraciones) {
    }
}
