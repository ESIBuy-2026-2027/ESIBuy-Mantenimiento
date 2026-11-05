package es.uclm.esibuy.backend.estadisticas.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import es.uclm.esibuy.backend.estadisticas.application.port.out.InventarioPort;
import es.uclm.esibuy.backend.estadisticas.application.port.out.VendedorEnSesionPort;
import es.uclm.esibuy.backend.estadisticas.application.port.out.VentasPort;
import es.uclm.esibuy.backend.estadisticas.domain.model.EstadisticasVendedor;
import es.uclm.esibuy.backend.estadisticas.domain.model.ProductoInventariado;
import es.uclm.esibuy.backend.estadisticas.domain.model.VentaRegistrada;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// Consulta de las estadísticas del vendedor en sesión (HU17, T280). Sin Spring: los puertos de salida se sustituyen
// por dobles de prueba (clases internas de este test). El cálculo se prueba en EstadisticasVendedorTest.
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: caso HU17-CP10, propuesto.
class EstadisticasVendedorServiceTest {

    private static final String EMAIL = "vendedor@esibuy.test";

    private final VentasFijas ventas = new VentasFijas();
    private final InventarioFijo inventario = new InventarioFijo();
    private final EstadisticasVendedorService servicio =
            new EstadisticasVendedorService(new VendedorFijo(), ventas, inventario);

    @Test
    @DisplayName("HU17-CP10: las estadísticas se calculan con las ventas y los productos del vendedor de la sesión")
    void calculaConLasVentasYLosProductosDelVendedorDeLaSesion() {
        ventas.delVendedor.put("v-1", List.of(
                new VentaRegistrada("o-1", "p-1", "Teclado", 2, new BigDecimal("40.00"))));
        ventas.delVendedor.put("v-2", List.of(
                new VentaRegistrada("o-9", "p-9", "Ajeno", 7, new BigDecimal("700.00"))));
        inventario.delVendedor.put("v-1", List.of(
                new ProductoInventariado("p-1", "Teclado", 3, true, 4.0, 1)));

        EstadisticasVendedor estadisticas = servicio.consultar(EMAIL);

        assertEquals(1, estadisticas.pedidos());
        assertEquals(new BigDecimal("40.00"), estadisticas.ingresos());
        assertEquals(1, estadisticas.productos());
        assertEquals(1, estadisticas.stockBajo());
        assertEquals(List.of("v-1"), ventas.consultas);
        assertEquals(List.of("v-1"), inventario.consultas);
    }

    // El correo de la sesión corresponde al vendedor v-1
    private static final class VendedorFijo implements VendedorEnSesionPort {
        @Override
        public String idPorCorreo(String email) {
            return EMAIL.equals(email) ? "v-1" : "v-desconocido";
        }
    }

    // Las ventas de cada vendedor; apunta cada consulta que recibe
    private static final class VentasFijas implements VentasPort {
        private final Map<String, List<VentaRegistrada>> delVendedor = new HashMap<>();
        private final List<String> consultas = new ArrayList<>();

        @Override
        public List<VentaRegistrada> ventasDe(String vendedorId) {
            consultas.add(vendedorId);
            return delVendedor.getOrDefault(vendedorId, List.of());
        }
    }

    // Los productos de cada vendedor; apunta cada consulta que recibe
    private static final class InventarioFijo implements InventarioPort {
        private final Map<String, List<ProductoInventariado>> delVendedor = new HashMap<>();
        private final List<String> consultas = new ArrayList<>();

        @Override
        public List<ProductoInventariado> productosDe(String vendedorId) {
            consultas.add(vendedorId);
            return delVendedor.getOrDefault(vendedorId, List.of());
        }
    }
}
