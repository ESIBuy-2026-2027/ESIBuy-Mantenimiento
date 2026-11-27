package es.uclm.esibuy.backend.valoraciones.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import es.uclm.esibuy.backend.valoraciones.application.port.out.IdentidadVendedorPort;
import es.uclm.esibuy.backend.valoraciones.application.port.out.ProductosDelVendedorPort;
import es.uclm.esibuy.backend.valoraciones.application.port.out.ValoracionRepositoryPort;
import es.uclm.esibuy.backend.valoraciones.domain.model.PuntuacionAgregadaProducto;

class EstadisticasValoracionesVendedorServiceTest {

    private static final String EMAIL = "vendedor@esibuy.test";

    private final ValoracionesFijas valoraciones = new ValoracionesFijas();
    private final ProductosFijos productos = new ProductosFijos();
    private final EstadisticasValoracionesVendedorService servicio =
            new EstadisticasValoracionesVendedorService(valoraciones, email -> "v-1", productos);

    @Test
    @DisplayName("HU17-CP03: agrega la media y distribución únicamente de productos del vendedor en sesión")
    void resumeValoracionesSoloDeProductosPropios() {
        productos.ids = List.of("p-1", "p-2");
        valoraciones.agregados = List.of(
                new PuntuacionAgregadaProducto("p-1", 5, 2),
                new PuntuacionAgregadaProducto("p-2", 3, 1),
                new PuntuacionAgregadaProducto("p-ajeno", 1, 100));

        var resumen = servicio.consultar(EMAIL, Optional.empty(), Instant.parse("2026-03-17T00:00:00Z"));

        assertEquals(3, resumen.total());
        assertEquals(13.0 / 3, resumen.media(), 0.0001);
        assertEquals(List.of("p-1", "p-2"),
                resumen.productos().stream().map(producto -> producto.productoId()).sorted().toList());
        assertEquals(List.of(0, 0, 1, 0, 2),
                resumen.distribucion().stream().map(puntuacion -> puntuacion.numero()).toList());
        assertEquals(List.of("p-1", "p-2"), valoraciones.productosConsultados);
    }

    @Test
    @DisplayName("HU17-CP03: si el vendedor no tiene productos, no consulta valoraciones y no hay media")
    void sinProductosNoConsultaValoraciones() {
        var resumen = servicio.consultar(EMAIL, Optional.empty(), Instant.parse("2026-03-17T00:00:00Z"));

        assertNull(resumen.media());
        assertEquals(0, resumen.total());
        assertEquals(5, resumen.distribucion().size());
        assertEquals(0, valoraciones.consultas);
    }

    private static final class ProductosFijos implements ProductosDelVendedorPort {
        private List<String> ids = List.of();

        @Override
        public List<String> idsDe(String vendedorId) {
            assertEquals("v-1", vendedorId);
            return ids;
        }
    }

    private static final class ValoracionesFijas implements ValoracionRepositoryPort {
        private List<PuntuacionAgregadaProducto> agregados = List.of();
        private List<String> productosConsultados = List.of();
        private int consultas;

        @Override
        public List<PuntuacionAgregadaProducto> resumirPorProductos(List<String> productoIds, Instant desde,
                Instant hasta) {
            productosConsultados = List.copyOf(productoIds);
            consultas++;
            return agregados;
        }

        @Override
        public List<PuntuacionAgregadaProducto> resumirPorProductos(List<String> productoIds) {
            productosConsultados = List.copyOf(productoIds);
            consultas++;
            return agregados;
        }

        @Override
        public es.uclm.esibuy.backend.valoraciones.domain.model.Valoracion guardar(
                es.uclm.esibuy.backend.valoraciones.domain.model.Valoracion valoracion) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean existePorClienteYProducto(String clienteId, String productoId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<es.uclm.esibuy.backend.valoraciones.domain.model.Valoracion> buscarPorProducto(String productoId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void eliminarPorProducto(String productoId) {
            throw new UnsupportedOperationException();
        }
    }
}
