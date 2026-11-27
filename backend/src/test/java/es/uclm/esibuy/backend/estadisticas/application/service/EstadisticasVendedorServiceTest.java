package es.uclm.esibuy.backend.estadisticas.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import es.uclm.esibuy.backend.estadisticas.application.port.out.InventarioPort;
import es.uclm.esibuy.backend.estadisticas.application.port.out.VendedorEnSesionPort;
import es.uclm.esibuy.backend.estadisticas.application.port.out.VentasPort;
import es.uclm.esibuy.backend.estadisticas.application.port.out.ValoracionesPort;
import es.uclm.esibuy.backend.estadisticas.domain.model.EstadisticasVendedor;
import es.uclm.esibuy.backend.estadisticas.domain.model.PeriodoEstadisticasVendedor;
import es.uclm.esibuy.backend.estadisticas.domain.model.ProductoInventariado;
import es.uclm.esibuy.backend.estadisticas.domain.model.PuntuacionValoracion;
import es.uclm.esibuy.backend.estadisticas.domain.model.ResumenValoracionesPeriodo;
import es.uclm.esibuy.backend.estadisticas.domain.model.VentaRegistrada;
import es.uclm.esibuy.backend.estadisticas.domain.model.ValoracionProductoPeriodo;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
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
    private static final Instant AHORA = Instant.parse("2026-03-16T12:00:00Z");

    private final VentasFijas ventas = new VentasFijas();
    private final InventarioFijo inventario = new InventarioFijo();
    private final ValoracionesFijas valoraciones = new ValoracionesFijas();
    private final EstadisticasVendedorService servicio =
            new EstadisticasVendedorService(new VendedorFijo(), ventas, inventario, valoraciones,
                    Clock.fixed(AHORA, ZoneOffset.UTC));

    @Test
    @DisplayName("HU17-CP10: las estadísticas se calculan con las ventas y los productos del vendedor de la sesión")
    void calculaConLasVentasYLosProductosDelVendedorDeLaSesion() {
        ventas.delVendedor.put("v-1", List.of(
                new VentaRegistrada("o-1", "p-1", "Teclado", 2, new BigDecimal("40.00"),
                        Instant.parse("2026-03-15T10:00:00Z")),
                new VentaRegistrada("o-2", "p-historico", "Antiguo", 5, new BigDecimal("500.00"),
                        Instant.parse("2026-01-15T10:00:00Z"))));
        ventas.delVendedor.put("v-2", List.of(
                new VentaRegistrada("o-9", "p-9", "Ajeno", 7, new BigDecimal("700.00"),
                        Instant.parse("2026-03-15T10:00:00Z"))));
        inventario.delVendedor.put("v-1", List.of(
                new ProductoInventariado("p-1", "Teclado", "/api/product-images/p-1", 3, true, 4.0, 1)));
        inventario.delVendedor.put("v-2", List.of(
                new ProductoInventariado("p-9", "Producto ajeno", "/api/product-images/p-9", 0, false, 1.0, 12)));
        valoraciones.resumen = new ResumenValoracionesPeriodo(4.0, 1,
                List.of(new ValoracionProductoPeriodo("p-1", 4.0, 1)),
                List.of(new PuntuacionValoracion(5, 1, 0)));

        EstadisticasVendedor estadisticas = servicio.consultar(EMAIL,
                PeriodoEstadisticasVendedor.TODO_EL_PERIODO);

        assertEquals(2, estadisticas.pedidos());
        assertEquals(new BigDecimal("540.00"), estadisticas.ingresos());
        assertEquals(7, estadisticas.unidadesVendidas());
        assertEquals(1, estadisticas.productos());
        assertEquals(1, estadisticas.stockBajo());
        assertEquals(0, estadisticas.sinStock());
        assertEquals(0, estadisticas.noDisponibles());
        assertEquals(1, estadisticas.numeroValoraciones());
        assertEquals(List.of("p-1"),
                estadisticas.mejorValorados().stream().map(valoracion -> valoracion.productoId()).toList());
        assertEquals(List.of("p-1"),
                estadisticas.peorValorados().stream().map(valoracion -> valoracion.productoId()).toList());
        assertEquals("/api/product-images/p-1", estadisticas.mejorValorados().get(0).imagen());
        assertEquals(List.of(), estadisticas.productosSinStock());
        assertEquals(List.of("v-1"), ventas.consultas);
        assertEquals(List.of("v-1"), inventario.consultas);
        assertEquals(7, estadisticas.evolucionVentas().size());
        assertEquals(0, estadisticas.evolucionVentas().get(0).unidades());
        assertEquals(2, estadisticas.evolucionVentas().get(5).unidades());
        assertEquals("vendedor@esibuy.test", valoraciones.emailConsultado);
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

    private static final class ValoracionesFijas implements ValoracionesPort {
        private ResumenValoracionesPeriodo resumen = new ResumenValoracionesPeriodo(null, 0, List.of(), List.of());
        private String emailConsultado;

        @Override
        public ResumenValoracionesPeriodo resumenDe(String emailVendedor, java.util.Optional<Instant> desde,
                Instant hasta) {
            emailConsultado = emailVendedor;
            return resumen;
        }
    }
}
