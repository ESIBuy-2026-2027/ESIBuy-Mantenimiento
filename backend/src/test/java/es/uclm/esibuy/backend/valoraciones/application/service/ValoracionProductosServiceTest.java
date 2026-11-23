package es.uclm.esibuy.backend.valoraciones.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.uclm.esibuy.backend.valoraciones.application.port.in.ValorarProductoCommand;
import es.uclm.esibuy.backend.valoraciones.application.port.out.ClienteEnSesionPort;
import es.uclm.esibuy.backend.valoraciones.application.port.out.ComprasPort;
import es.uclm.esibuy.backend.valoraciones.application.port.out.ProductosPort;
import es.uclm.esibuy.backend.valoraciones.application.port.out.ValoracionRepositoryPort;
import es.uclm.esibuy.backend.valoraciones.domain.exception.ProductoNoCompradoException;
import es.uclm.esibuy.backend.valoraciones.domain.exception.ValoracionDuplicadaException;
import es.uclm.esibuy.backend.valoraciones.domain.model.Valoracion;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ValoracionProductosServiceTest {

    private static final String EMAIL = "cliente@esibuy.test";
    private final ValoracionesEnMemoria valoraciones = new ValoracionesEnMemoria();
    private final ComprasFijas compras = new ComprasFijas();
    private final ProductosAnotados productos = new ProductosAnotados();
    private final ValoracionProductosService servicio = new ValoracionProductosService(valoraciones,
            new ClienteFijo(), compras, productos);

    ValoracionProductosServiceTest() {
        compras.compradas.add("p-1");
    }

    private Valoracion valorar(String productoId, int puntuacion, String comentario) {
        return servicio.valorar(new ValorarProductoCommand(EMAIL, productoId, puntuacion, comentario));
    }

    @Test
    @DisplayName("CLI-03: al crear una valoración guarda nombre del autor y fecha")
    void guardaAutorYFecha() {
        Valoracion guardada = valorar("p-1", 4, "Buena calidad");

        assertEquals("Ana Pérez", guardada.nombreCliente());
        assertNotNull(guardada.fechaCreacion());
        assertEquals("c-1", guardada.clienteId());
        assertEquals("Buena calidad", guardada.comentario());
    }

    @Test
    @DisplayName("HU13-CP10: el cliente no puede valorar un producto que no ha comprado")
    void noPuedeValorarUnProductoQueNoHaComprado() {
        assertThrows(ProductoNoCompradoException.class, () -> valorar("p-2", 4, "Me lo prestaron"));
        assertTrue(valoraciones.guardadas.isEmpty());
    }

    @Test
    @DisplayName("HU13-CP11: no se puede valorar dos veces el mismo producto")
    void noPuedeValorarDosVecesElMismoProducto() {
        valorar("p-1", 4, "Buena calidad");

        assertThrows(ValoracionDuplicadaException.class, () -> valorar("p-1", 1, "He cambiado de opinión"));
        assertEquals(1, valoraciones.guardadas.size());
    }

    @Test
    @DisplayName("HU13-CP28: al valorar, el producto recibe la media y el número de todas sus valoraciones")
    void alValorarElProductoRecibeLaMediaYElNumero() {
        valoraciones.guardadas.add(new Valoracion("v-a", "p-1", "c-2", 5, ""));
        valoraciones.guardadas.add(new Valoracion("v-b", "p-1", "c-3", 4, ""));
        valoraciones.guardadas.add(new Valoracion("v-c", "p-9", "c-2", 1, ""));

        valorar("p-1", 3, "Normal");

        assertEquals(List.of(new Actualizacion("p-1", 4.0, 3)), productos.actualizaciones);
    }

    private record Actualizacion(String productoId, double media, int numero) {
    }

    private static final class ProductosAnotados implements ProductosPort {
        private final List<Actualizacion> actualizaciones = new ArrayList<>();

        @Override
        public void actualizarValoracion(String productoId, double media, int numero) {
            actualizaciones.add(new Actualizacion(productoId, media, numero));
        }
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

    private static final class ValoracionesEnMemoria implements ValoracionRepositoryPort {
        private final List<Valoracion> guardadas = new ArrayList<>();

        @Override
        public Valoracion guardar(Valoracion valoracion) {
            Valoracion guardada = new Valoracion("v-" + (guardadas.size() + 1), valoracion.productoId(),
                    valoracion.clienteId(), valoracion.nombreCliente(), valoracion.puntuacion(),
                    valoracion.comentario(), valoracion.fechaCreacion());
            guardadas.add(guardada);
            return guardada;
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
            guardadas.removeIf(valoracion -> valoracion.productoId().equals(productoId));
        }
    }
}
