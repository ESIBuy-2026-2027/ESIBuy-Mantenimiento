package es.uclm.esibuy.backend.estadisticas.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
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
        assertEquals(List.of(), estadisticas.menosVendidos());
    }

    @Test
    @DisplayName("HU17-CP02: los más vendidos suman las unidades de cada producto y van de más a menos unidades")
    void losMasVendidosAgrupanPorProductoYOrdenanPorUnidades() {
        List<VentaRegistrada> ventas = List.of(
                venta("o-1", "p-1", 2, "40.00"),
                venta("o-1", "p-2", 5, "25.00"),
                venta("o-2", "p-1", 4, "80.00"));

        EstadisticasVendedor estadisticas = EstadisticasVendedor.calcular(ventas, List.of(
                producto("p-1", 10, true, null, 0), producto("p-2", 10, true, null, 0)));

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

        EstadisticasVendedor estadisticas = EstadisticasVendedor.calcular(ventas, List.of(
                producto("p-1", 10, true, null, 0), producto("p-2", 10, true, null, 0)));

        assertEquals(List.of("p-2", "p-1"),
                estadisticas.masVendidos().stream().map(ProductoMasVendido::productoId).toList());
    }

    @Test
    @DisplayName("HU17-CP02: solo se muestran los 10 productos más vendidos y en orden descendente")
    void soloSeMuestranLosDiezMasVendidos() {
        List<VentaRegistrada> ventas = new ArrayList<>();
        List<ProductoInventariado> productos = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            String id = "p-%02d".formatted(i);
            ventas.add(venta("o-1", id, i, i + ".00"));
            productos.add(producto(id, 10, true, null, 0));
        }

        EstadisticasVendedor estadisticas = EstadisticasVendedor.calcular(ventas, productos);

        assertEquals(List.of("p-12", "p-11", "p-10", "p-09", "p-08", "p-07", "p-06", "p-05", "p-04", "p-03"),
                estadisticas.masVendidos().stream().map(ProductoMasVendido::productoId).toList());
    }

    @Test
    @DisplayName("HU17-CP02: los 10 menos vendidos van en orden ascendente e incluyen los productos sin ventas")
    void losDiezMenosVendidosIncluyenProductosSinVentas() {
        List<ProductoInventariado> productos = new ArrayList<>();
        List<VentaRegistrada> ventas = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            String id = "p-%02d".formatted(i);
            productos.add(producto(id, 10, true, null, 0));
            if (i > 2) {
                ventas.add(venta("o-1", id, i - 2, (i - 2) + ".00"));
            }
        }
        ventas.add(venta("o-1", "p-ajeno", 100, "100.00"));

        EstadisticasVendedor estadisticas = EstadisticasVendedor.calcular(ventas, productos);

        assertEquals(List.of("p-01", "p-02", "p-03", "p-04", "p-05", "p-06", "p-07", "p-08", "p-09", "p-10"),
                estadisticas.menosVendidos().stream().map(ProductoMenosVendido::productoId).toList());
        assertEquals(List.of(0, 0, 1, 2, 3, 4, 5, 6, 7, 8),
                estadisticas.menosVendidos().stream().map(ProductoMenosVendido::unidades).toList());
        assertEquals(10, estadisticas.menosVendidos().size());
        assertFalse(estadisticas.menosVendidos().stream().anyMatch(p -> p.productoId().equals("p-ajeno")));
        assertFalse(estadisticas.masVendidos().stream().anyMatch(p -> p.productoId().equals("p-ajeno")));
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
    @DisplayName("HU17-CP03: el período diario calcula la distribución de valoraciones y rellena días sin ventas")
    void calculaDistribucionYSerieDiariaParaTodoElPeriodo() {
        List<VentaRegistrada> ventas = List.of(
                new VentaRegistrada("o-1", "p-1", "Producto p-1", 2, new BigDecimal("20.00"),
                        Instant.parse("2026-03-15T10:00:00Z")),
                new VentaRegistrada("o-1", "p-2", "Producto p-2", 1, new BigDecimal("10.00"),
                        Instant.parse("2026-03-15T10:00:00Z")));
        ResumenValoracionesPeriodo resumen = new ResumenValoracionesPeriodo(4.5, 2,
                List.of(new ValoracionProductoPeriodo("p-1", 4.5, 2)),
                List.of(new PuntuacionValoracion(4, 1, 0), new PuntuacionValoracion(5, 1, 0)));

        EstadisticasVendedor estadisticas = EstadisticasVendedor.calcular(ventas, List.of(
                        producto("p-1", 10, true, null, 0), producto("p-2", 10, true, null, 0)),
                resumen, PeriodoEstadisticasVendedor.TODO_EL_PERIODO, LocalDate.parse("2026-03-16"));

        assertEquals(List.of(LocalDate.parse("2026-03-15")),
                estadisticas.evolucionVentas().stream().map(EvolucionVentas::fecha).toList());
        assertEquals(1, estadisticas.evolucionVentas().get(0).pedidos());
        assertEquals(3, estadisticas.evolucionVentas().get(0).unidades());
        assertEquals(new BigDecimal("30.00"), estadisticas.evolucionVentas().get(0).ingresos());
        assertEquals(List.of(0, 0, 0, 1, 1),
                estadisticas.distribucionValoraciones().stream().map(PuntuacionValoracion::numero).toList());
        assertEquals(List.of(0.0, 0.0, 0.0, 50.0, 50.0),
                estadisticas.distribucionValoraciones().stream().map(PuntuacionValoracion::porcentaje).toList());
    }

    @Test
    @DisplayName("HU17-CP01: todo el período agrupa la evolución por meses e incluye meses sin ventas")
    void todoElPeriodoAgrupaLaEvolucionPorDias() {
        List<VentaRegistrada> ventas = List.of(
                new VentaRegistrada("o-1", "p-1", "Producto p-1", 1, new BigDecimal("10.00"),
                        Instant.parse("2026-01-15T10:00:00Z")),
                new VentaRegistrada("o-2", "p-1", "Producto p-1", 2, new BigDecimal("20.00"),
                        Instant.parse("2026-03-15T10:00:00Z")));

        EstadisticasVendedor estadisticas = EstadisticasVendedor.calcular(ventas, List.of(),
                new ResumenValoracionesPeriodo(null, 0, List.of(), List.of()),
                PeriodoEstadisticasVendedor.TODO_EL_PERIODO, LocalDate.parse("2026-03-16"));

        assertEquals(60, estadisticas.evolucionVentas().size());
        assertEquals(LocalDate.parse("2026-01-15"), estadisticas.evolucionVentas().get(0).fecha());
        assertEquals(LocalDate.parse("2026-03-15"), estadisticas.evolucionVentas().get(59).fecha());
        assertEquals(2, estadisticas.evolucionVentas().get(59).unidades());
        assertEquals(GranularidadVentas.DIA, estadisticas.granularidadVentas());
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
    @DisplayName("HU17-CP04: los rankings de valoración muestran los 10 mejores y peores con imagen y solo valorados")
    void losRankingsDeValoracionMuestranLosDiezMejoresYPeoresValorados() {
        List<ProductoInventariado> productos = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            String id = "p-%02d".formatted(i);
            productos.add(new ProductoInventariado(id, "Producto " + id, "/api/product-images/" + id, 10, true,
                    i / 4.0, i));
        }
        productos.add(producto("p-13", 10, true, null, 0));

        EstadisticasVendedor estadisticas = EstadisticasVendedor.calcular(List.of(), productos);

        assertEquals(List.of("p-12", "p-11", "p-10", "p-09", "p-08", "p-07", "p-06", "p-05", "p-04", "p-03"),
                estadisticas.mejorValorados().stream().map(ValoracionProductoVendedor::productoId).toList());
        assertEquals(List.of("p-01", "p-02", "p-03", "p-04", "p-05", "p-06", "p-07", "p-08", "p-09", "p-10"),
                estadisticas.peorValorados().stream().map(ValoracionProductoVendedor::productoId).toList());
        assertEquals("/api/product-images/p-12", estadisticas.mejorValorados().get(0).imagen());
        assertEquals(3.0, estadisticas.mejorValorados().get(0).media());
        assertEquals(12, estadisticas.mejorValorados().get(0).numero());
        assertFalse(estadisticas.mejorValorados().stream().anyMatch(p -> p.productoId().equals("p-13")));
        assertFalse(estadisticas.peorValorados().stream().anyMatch(p -> p.productoId().equals("p-13")));
    }

    @Test
    @DisplayName("HU17-CP05: el stock cuenta los productos, los que no tienen stock y los no disponibles")
    void elStockCuentaProductosSinStockYNoDisponibles() {
        List<ProductoInventariado> productos = List.of(
                producto("p-1", 0, true, null, 0),
                producto("p-2", 20, false, null, 0),
                producto("p-3", 0, false, null, 0),
                producto("p-4", 20, true, null, 0));

        EstadisticasVendedor estadisticas = EstadisticasVendedor.calcular(List.of(), productos);

        assertEquals(4, estadisticas.productos());
        assertEquals(2, estadisticas.sinStock());
        assertEquals(2, estadisticas.noDisponibles());
        assertEquals(List.of(
                producto("p-1", 0, true, null, 0),
                producto("p-3", 0, false, null, 0)),
                estadisticas.productosSinStock());
    }

    @Test
    @DisplayName("HU17-CP05: tiene stock bajo el producto con entre 1 y 5 unidades")
    void stockBajoEsEntreUnaYCincoUnidades() {
        List<ProductoInventariado> productos = List.of(
                producto("p-0", 0, true, null, 0),
                producto("p-1", 1, true, null, 0),
                producto("p-5", 5, true, null, 0),
                producto("p-6", 6, true, null, 0));

        EstadisticasVendedor estadisticas = EstadisticasVendedor.calcular(List.of(), productos);

        // Frontera: 0 es sin stock, no stock bajo; 6 ya no es stock bajo
        assertEquals(2, estadisticas.stockBajo());
        assertEquals(List.of(
                producto("p-1", 1, true, null, 0),
                producto("p-5", 5, true, null, 0)),
                estadisticas.productosStockBajo());
    }

    private static VentaRegistrada venta(String pedidoId, String productoId, int unidades, String importe) {
        return new VentaRegistrada(pedidoId, productoId, "Producto " + productoId, unidades, new BigDecimal(importe));
    }

    private static ProductoInventariado producto(String productoId, int stock, boolean disponible, Double media,
            int numeroValoraciones) {
        return new ProductoInventariado(productoId, "Producto " + productoId, "", stock, disponible, media,
                numeroValoraciones);
    }
}
