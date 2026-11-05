package es.uclm.esibuy.backend.estadisticas.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// Cálculo de las estadísticas del vendedor (HU17, T280): ventas, valoraciones y stock a partir de sus ventas y de
// sus productos. Java puro, sin Spring.
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: casos HU17-CP01 a CP06, propuestos.
class EstadisticasVendedorTest {

    @Test
    @DisplayName("HU17-CP01: las ventas cuentan los pedidos distintos, las unidades y los ingresos")
    void lasVentasCuentanPedidosUnidadesEIngresos() {
        List<VentaRegistrada> ventas = List.of(
                venta("o-1", "p-1", 2, "40.00"),
                venta("o-1", "p-2", 1, "15.50"),
                venta("o-2", "p-1", 3, "60.00"));

        EstadisticasVendedor estadisticas = EstadisticasVendedor.calcular(ventas, List.of());

        assertEquals(2, estadisticas.pedidos());
        assertEquals(6, estadisticas.unidadesVendidas());
        assertEquals(new BigDecimal("115.50"), estadisticas.ingresos());
    }

    @Test
    @DisplayName("HU17-CP01: un vendedor sin ventas tiene las ventas a cero")
    void sinVentasLasVentasEstanACero() {
        EstadisticasVendedor estadisticas = EstadisticasVendedor.calcular(List.of(), List.of());

        assertEquals(0, estadisticas.pedidos());
        assertEquals(0, estadisticas.unidadesVendidas());
        assertEquals(0, BigDecimal.ZERO.compareTo(estadisticas.ingresos()));
        assertEquals(List.of(), estadisticas.masVendidos());
    }

    @Test
    @DisplayName("HU17-CP02: los más vendidos suman las unidades de cada producto y van de más a menos unidades")
    void losMasVendidosAgrupanPorProductoYOrdenanPorUnidades() {
        List<VentaRegistrada> ventas = List.of(
                venta("o-1", "p-1", 2, "40.00"),
                venta("o-1", "p-2", 5, "25.00"),
                venta("o-2", "p-1", 4, "80.00"));

        EstadisticasVendedor estadisticas = EstadisticasVendedor.calcular(ventas, List.of());

        assertEquals(List.of(
                new ProductoMasVendido("p-1", "Producto p-1", 6, new BigDecimal("120.00")),
                new ProductoMasVendido("p-2", "Producto p-2", 5, new BigDecimal("25.00"))),
                estadisticas.masVendidos());
    }

    @Test
    @DisplayName("HU17-CP02: a igualdad de unidades, primero el producto que más ha ingresado")
    void losMasVendidosDesempatanPorIngresos() {
        List<VentaRegistrada> ventas = List.of(
                venta("o-1", "p-1", 3, "30.00"),
                venta("o-2", "p-2", 3, "90.00"));

        EstadisticasVendedor estadisticas = EstadisticasVendedor.calcular(ventas, List.of());

        assertEquals(List.of("p-2", "p-1"),
                estadisticas.masVendidos().stream().map(ProductoMasVendido::productoId).toList());
    }

    @Test
    @DisplayName("HU17-CP02: solo se muestran los 5 productos más vendidos")
    void soloSeMuestranLosCincoMasVendidos() {
        List<VentaRegistrada> ventas = List.of(
                venta("o-1", "p-1", 6, "6.00"),
                venta("o-1", "p-2", 5, "5.00"),
                venta("o-1", "p-3", 4, "4.00"),
                venta("o-1", "p-4", 3, "3.00"),
                venta("o-1", "p-5", 2, "2.00"),
                venta("o-1", "p-6", 1, "1.00"));

        EstadisticasVendedor estadisticas = EstadisticasVendedor.calcular(ventas, List.of());

        assertEquals(List.of("p-1", "p-2", "p-3", "p-4", "p-5"),
                estadisticas.masVendidos().stream().map(ProductoMasVendido::productoId).toList());
    }

    @Test
    @DisplayName("HU17-CP03: la media global pondera la media de cada producto por su número de valoraciones")
    void laMediaGlobalEstaPonderadaPorElNumeroDeValoraciones() {
        List<ProductoInventariado> productos = List.of(
                producto("p-1", 10, true, 5.0, 3),
                producto("p-2", 10, true, 1.0, 1),
                producto("p-3", 10, true, null, 0));

        EstadisticasVendedor estadisticas = EstadisticasVendedor.calcular(List.of(), productos);

        // (5 × 3 + 1 × 1) / 4 = 4: sin ponderar sería 3
        assertEquals(4.0, estadisticas.valoracionMedia(), 0.0001);
        assertEquals(4, estadisticas.numeroValoraciones());
    }

    @Test
    @DisplayName("HU17-CP03: sin valoraciones la media global no existe y el total es cero")
    void sinValoracionesNoHayMediaGlobal() {
        List<ProductoInventariado> productos = List.of(producto("p-1", 10, true, null, 0));

        EstadisticasVendedor estadisticas = EstadisticasVendedor.calcular(List.of(), productos);

        assertNull(estadisticas.valoracionMedia());
        assertEquals(0, estadisticas.numeroValoraciones());
    }

    @Test
    @DisplayName("HU17-CP04: la valoración por producto solo incluye los valorados, del mejor al peor")
    void laValoracionPorProductoSoloIncluyeLosValoradosDelMejorAlPeor() {
        List<ProductoInventariado> productos = List.of(
                producto("p-1", 10, true, 3.5, 2),
                producto("p-2", 10, true, null, 0),
                producto("p-3", 10, true, 4.5, 6));

        EstadisticasVendedor estadisticas = EstadisticasVendedor.calcular(List.of(), productos);

        assertEquals(List.of(
                new ValoracionProducto("p-3", "Producto p-3", 4.5, 6),
                new ValoracionProducto("p-1", "Producto p-1", 3.5, 2)),
                estadisticas.valoracionesPorProducto());
    }

    @Test
    @DisplayName("HU17-CP05: el stock cuenta los productos, los que no tienen stock y los no disponibles")
    void elStockCuentaProductosSinStockYNoDisponibles() {
        fail("Funcionalidad no implementada");
    }

    @Test
    @DisplayName("HU17-CP05: tiene stock bajo el producto con entre 1 y 5 unidades")
    void stockBajoEsEntreUnaYCincoUnidades() {
        fail("Funcionalidad no implementada");
    }

    private static VentaRegistrada venta(String pedidoId, String productoId, int unidades, String importe) {
        return new VentaRegistrada(pedidoId, productoId, "Producto " + productoId, unidades, new BigDecimal(importe));
    }

    private static ProductoInventariado producto(String productoId, int stock, boolean disponible, Double media,
            int numeroValoraciones) {
        return new ProductoInventariado(productoId, "Producto " + productoId, stock, disponible, media,
                numeroValoraciones);
    }
}
