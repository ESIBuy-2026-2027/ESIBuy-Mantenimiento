package es.uclm.esibuy.backend.estadisticas.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        fail("Funcionalidad no implementada");
    }

    @Test
    @DisplayName("HU17-CP02: a igualdad de unidades, primero el producto que más ha ingresado")
    void losMasVendidosDesempatanPorIngresos() {
        fail("Funcionalidad no implementada");
    }

    @Test
    @DisplayName("HU17-CP02: solo se muestran los 5 productos más vendidos")
    void soloSeMuestranLosCincoMasVendidos() {
        fail("Funcionalidad no implementada");
    }

    @Test
    @DisplayName("HU17-CP03: la media global pondera la media de cada producto por su número de valoraciones")
    void laMediaGlobalEstaPonderadaPorElNumeroDeValoraciones() {
        fail("Funcionalidad no implementada");
    }

    @Test
    @DisplayName("HU17-CP03: sin valoraciones la media global no existe y el total es cero")
    void sinValoracionesNoHayMediaGlobal() {
        fail("Funcionalidad no implementada");
    }

    @Test
    @DisplayName("HU17-CP04: la valoración por producto solo incluye los valorados, del mejor al peor")
    void laValoracionPorProductoSoloIncluyeLosValoradosDelMejorAlPeor() {
        fail("Funcionalidad no implementada");
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
}
