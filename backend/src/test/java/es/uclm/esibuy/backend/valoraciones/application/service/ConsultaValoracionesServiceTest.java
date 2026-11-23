package es.uclm.esibuy.backend.valoraciones.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.uclm.esibuy.backend.valoraciones.application.port.in.EstadoValoracion;
import es.uclm.esibuy.backend.valoraciones.application.port.out.ClienteEnSesionPort;
import es.uclm.esibuy.backend.valoraciones.application.port.out.ComprasPort;
import es.uclm.esibuy.backend.valoraciones.application.port.out.ValoracionRepositoryPort;
import es.uclm.esibuy.backend.valoraciones.domain.model.Valoracion;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ConsultaValoracionesServiceTest {

    private static final String EMAIL = "cliente@esibuy.test";
    private final ValoracionesGuardadas valoraciones = new ValoracionesGuardadas();
    private final ComprasFijas compras = new ComprasFijas();
    private final ConsultaValoracionesService servicio = new ConsultaValoracionesService(valoraciones,
            new ClienteFijo(), compras);

    @Test
    @DisplayName("CLI-03: consultar un producto devuelve todas sus valoraciones y solo las suyas")
    void devuelveLasValoracionesDelProducto() {
        Valoracion primera = new Valoracion("v-1", "p-1", "c-1", "Ana Pérez", 4, "Buena calidad", null);
        Valoracion segunda = new Valoracion("v-2", "p-1", "c-2", "Luis García", 2, "", null);
        valoraciones.guardadas.addAll(List.of(primera, segunda,
                new Valoracion("v-3", "p-2", "c-1", 5, "Perfecto")));

        assertEquals(List.of(primera, segunda), servicio.consultarPorProducto("p-1"));
    }

    @Test
    @DisplayName("CLI-03: un producto sin valoraciones devuelve una lista vacía")
    void unProductoSinValoracionesDevuelveUnaListaVacia() {
        assertTrue(servicio.consultarPorProducto("p-1").isEmpty());
    }

    @Test
    @DisplayName("CLI-04: distingue comprador que aún no ha valorado")
    void compradorSinValoracion() {
        compras.compradas.add("p-1");

        EstadoValoracion estado = servicio.consultarEstado(EMAIL, "p-1");

        assertTrue(estado.haComprado());
        assertFalse(estado.haValorado());
        assertNull(estado.miValoracionId());
    }

    @Test
    @DisplayName("CLI-04: devuelve el identificador de la valoración propia")
    void compradorQueYaHaValorado() {
        compras.compradas.add("p-1");
        valoraciones.guardadas.add(new Valoracion("v-1", "p-1", "c-1", 5, "Muy bien"));

        EstadoValoracion estado = servicio.consultarEstado(EMAIL, "p-1");

        assertTrue(estado.haComprado());
        assertTrue(estado.haValorado());
        assertEquals("v-1", estado.miValoracionId());
    }

    @Test
    @DisplayName("CLI-04: distingue cliente que no ha comprado ni valorado")
    void clienteSinCompraNiValoracion() {
        EstadoValoracion estado = servicio.consultarEstado(EMAIL, "p-1");

        assertFalse(estado.haComprado());
        assertFalse(estado.haValorado());
        assertNull(estado.miValoracionId());
    }

    private static final class ClienteFijo implements ClienteEnSesionPort {
        @Override
        public String idPorCorreo(String email) {
            return EMAIL.equals(email) ? "c-1" : "c-desconocido";
        }

        @Override
        public String nombreCompletoPorCorreo(String email) {
            return "Ana Pérez";
        }
    }

    private static final class ComprasFijas implements ComprasPort {
        private final Set<String> compradas = new HashSet<>();

        @Override
        public boolean haComprado(String emailCliente, String productoId) {
            return EMAIL.equals(emailCliente) && compradas.contains(productoId);
        }
    }

    private static final class ValoracionesGuardadas implements ValoracionRepositoryPort {
        private final List<Valoracion> guardadas = new ArrayList<>();

        @Override
        public Valoracion guardar(Valoracion valoracion) {
            throw new UnsupportedOperationException("Consultar valoraciones no guarda");
        }

        @Override
        public boolean existePorClienteYProducto(String clienteId, String productoId) {
            return guardadas.stream().anyMatch(v -> v.clienteId().equals(clienteId)
                    && v.productoId().equals(productoId));
        }

        @Override
        public List<Valoracion> buscarPorProducto(String productoId) {
            return guardadas.stream().filter(v -> v.productoId().equals(productoId)).toList();
        }

        @Override
        public void eliminarPorProducto(String productoId) {
            throw new UnsupportedOperationException("Consultar valoraciones no elimina");
        }
    }
}
