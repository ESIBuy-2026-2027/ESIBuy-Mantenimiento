package es.uclm.esibuy.backend.valoraciones.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.uclm.esibuy.backend.valoraciones.application.port.in.ValorarProductoCommand;
import es.uclm.esibuy.backend.valoraciones.application.port.out.ClienteEnSesionPort;
import es.uclm.esibuy.backend.valoraciones.application.port.out.ComprasPort;
import es.uclm.esibuy.backend.valoraciones.application.port.out.ValoracionRepositoryPort;
import es.uclm.esibuy.backend.valoraciones.domain.exception.ProductoNoCompradoException;
import es.uclm.esibuy.backend.valoraciones.domain.model.Valoracion;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// Valoración de un producto por el cliente (HU13, T370). Sin Spring: los puertos de salida se sustituyen por
// dobles de prueba (clases internas de este test).
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: casos HU13-CP09 y CP10, propuestos.
class ValoracionProductosServiceTest {

    private static final String EMAIL = "cliente@esibuy.test";

    private final ValoracionesEnMemoria valoraciones = new ValoracionesEnMemoria();
    private final ComprasFijas compras = new ComprasFijas();
    private final ValoracionProductosService servicio = new ValoracionProductosService(valoraciones,
            new ClienteFijo(), compras);

    ValoracionProductosServiceTest() {
        // El cliente ha comprado p-1
        compras.compradas.add("p-1");
    }

    private Valoracion valorar(String productoId, int puntuacion, String comentario) {
        return servicio.valorar(new ValorarProductoCommand(EMAIL, productoId, puntuacion, comentario));
    }

    @Test
    @DisplayName("HU13-CP09: el cliente que ha comprado un producto lo valora y la valoración se guarda con su id")
    void elClienteQueHaCompradoValoraElProducto() {
        valorar("p-1", 4, "Buena calidad");

        assertEquals(List.of(new Valoracion("v-1", "p-1", "c-1", 4, "Buena calidad")), valoraciones.guardadas);
    }

    @Test
    @DisplayName("HU13-CP09: valorar devuelve la valoración guardada, ya con su id")
    void valorarDevuelveLaValoracionGuardada() {
        Valoracion valoracion = valorar("p-1", 5, null);

        assertEquals(new Valoracion("v-1", "p-1", "c-1", 5, ""), valoracion);
    }

    @Test
    @DisplayName("HU13-CP10: el cliente no puede valorar un producto que no ha comprado y no se guarda nada")
    void noPuedeValorarUnProductoQueNoHaComprado() {
        assertThrows(ProductoNoCompradoException.class, () -> valorar("p-2", 4, "Me lo prestaron"));
        assertTrue(valoraciones.guardadas.isEmpty());
    }

    // El correo de la sesión corresponde al cliente c-1
    private static final class ClienteFijo implements ClienteEnSesionPort {
        @Override
        public String idPorCorreo(String email) {
            return EMAIL.equals(email) ? "c-1" : "c-desconocido";
        }
    }

    // Productos que el cliente de la sesión ha comprado (pedidos pagados)
    private static final class ComprasFijas implements ComprasPort {
        private final Set<String> compradas = new HashSet<>();

        @Override
        public boolean haComprado(String emailCliente, String productoId) {
            return EMAIL.equals(emailCliente) && compradas.contains(productoId);
        }
    }

    // Guarda en memoria y asigna ids v-1, v-2... como haría la base de datos
    private static final class ValoracionesEnMemoria implements ValoracionRepositoryPort {
        private final List<Valoracion> guardadas = new ArrayList<>();

        @Override
        public Valoracion guardar(Valoracion valoracion) {
            Valoracion guardada = new Valoracion("v-" + (guardadas.size() + 1), valoracion.productoId(),
                    valoracion.clienteId(), valoracion.puntuacion(), valoracion.comentario());
            guardadas.add(guardada);
            return guardada;
        }

        @Override
        public boolean existePorClienteYProducto(String clienteId, String productoId) {
            return guardadas.stream()
                    .anyMatch(v -> v.clienteId().equals(clienteId) && v.productoId().equals(productoId));
        }

        @Override
        public List<Valoracion> buscarPorProducto(String productoId) {
            return guardadas.stream().filter(v -> v.productoId().equals(productoId)).toList();
        }
    }
}
