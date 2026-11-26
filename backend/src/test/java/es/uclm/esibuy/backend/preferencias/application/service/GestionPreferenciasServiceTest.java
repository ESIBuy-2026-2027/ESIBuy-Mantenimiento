package es.uclm.esibuy.backend.preferencias.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import es.uclm.esibuy.backend.preferencias.application.port.out.PreferenciasRepositoryPort;
import es.uclm.esibuy.backend.preferencias.domain.exception.CategoriaNoDisponibleException;
import es.uclm.esibuy.backend.preferencias.domain.model.Preferencias;

// Consultar las preferencias del cliente (HU46, T287), y marcar y desmarcar sus categorías favoritas (AB#528).
// Sin Spring: los puertos de salida se sustituyen por dobles de prueba en memoria.
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: casos HU46-CP04, CP05 y HU528-CP03 a CP07, propuestos.
class GestionPreferenciasServiceTest {

    private static final String EMAIL = "cliente@esibuy.test";
    private static final String CLIENTE = "c-1";

    private final RepositorioEnMemoria repositorio = new RepositorioEnMemoria();
    // El cliente en sesión es CLIENTE. Activas: cat-1, cat-2 y cat-4; cat-3 está inactiva y cualquier otra no existe
    private final GestionPreferenciasService servicio = new GestionPreferenciasService(
            repositorio, email -> CLIENTE, () -> Set.of("cat-1", "cat-2", "cat-4"));

    @Test
    @DisplayName("HU46-CP04: un cliente sin preferencias guardadas no tiene ninguna categoría, y consultar no guarda nada")
    void consultarSinPreferenciasDevuelveUnaListaVacia() {
        assertEquals(List.of(), servicio.consultar(EMAIL));
        assertTrue(repositorio.guardadas.isEmpty());
    }

    @Test
    @DisplayName("HU46-CP05: consultar devuelve, en su orden, las categorías elegidas que siguen activas")
    void consultarDevuelveSoloLasCategoriasActivas() {
        repositorio.guardar(new Preferencias(null, CLIENTE, List.of("cat-2", "cat-3", "cat-1")));

        assertEquals(List.of("cat-2", "cat-1"), servicio.consultar(EMAIL));
    }

    @Test
    @DisplayName("HU528-CP03: marcar la primera categoría crea las favoritas del cliente con ella")
    void marcarPorPrimeraVezCreaLasFavoritas() {
        assertEquals(List.of("cat-4"), servicio.marcar(EMAIL, "cat-4"));

        assertEquals(1, repositorio.guardadas.size());
        assertEquals(CLIENTE, repositorio.guardadas.get(0).getClienteId());
        assertEquals(List.of("cat-4"), repositorio.guardadas.get(0).getCategorias());
    }

    @Test
    @DisplayName("HU528-CP03: marcar otra categoría la añade a las favoritas del cliente y no toca las de otro")
    void marcarOtraLaAnadeSoloAlCliente() {
        repositorio.guardar(new Preferencias(null, CLIENTE, List.of("cat-2")));
        repositorio.guardar(new Preferencias(null, "c-2", List.of("cat-4")));

        assertEquals(List.of("cat-2", "cat-1"), servicio.marcar(EMAIL, "cat-1"));

        assertEquals(List.of("cat-2", "cat-1"), servicio.consultar(EMAIL));
        assertEquals(List.of("cat-4"), repositorio.buscarPorCliente("c-2").orElseThrow().getCategorias());
    }

    @Test
    @DisplayName("HU528-CP04: marcar una categoría que ya es favorita no es un error y no guarda nada")
    void marcarUnaFavoritaNoGuardaNada() {
        repositorio.guardar(new Preferencias(null, CLIENTE, List.of("cat-2", "cat-1")));
        repositorio.vecesGuardado = 0;

        assertEquals(List.of("cat-2", "cat-1"), servicio.marcar(EMAIL, "cat-2"));
        assertEquals(0, repositorio.vecesGuardado);
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {"cat-3", "cat-9"})
    @DisplayName("HU528-CP05: marcar una categoría inactiva o que no existe falla sin guardar nada")
    void marcarUnaCategoriaNoDisponibleFalla(String categoria) {
        assertThrows(CategoriaNoDisponibleException.class, () -> servicio.marcar(EMAIL, categoria));
        assertTrue(repositorio.guardadas.isEmpty());
    }

    @Test
    @DisplayName("HU528-CP06: desmarcar una favorita la quita y devuelve las que quedan")
    void desmarcarQuitaLaFavorita() {
        repositorio.guardar(new Preferencias(null, CLIENTE, List.of("cat-2", "cat-1")));

        assertEquals(List.of("cat-1"), servicio.desmarcar(EMAIL, "cat-2"));
        assertEquals(List.of("cat-1"), servicio.consultar(EMAIL));
    }

    @Test
    @DisplayName("HU528-CP06: desmarcar una categoría que no es favorita no es un error y no guarda nada")
    void desmarcarUnaQueNoEsFavoritaNoGuardaNada() {
        repositorio.guardar(new Preferencias(null, CLIENTE, List.of("cat-2")));
        repositorio.vecesGuardado = 0;

        assertEquals(List.of("cat-2"), servicio.desmarcar(EMAIL, "cat-1"));
        assertEquals(0, repositorio.vecesGuardado);
    }

    @Test
    @DisplayName("HU528-CP06: desmarcar sin tener favoritas no es un error y no crea ninguna")
    void desmarcarSinFavoritasNoCreaNada() {
        assertEquals(List.of(), servicio.desmarcar(EMAIL, "cat-1"));
        assertTrue(repositorio.guardadas.isEmpty());
    }

    @Test
    @DisplayName("HU528-CP07: marcar y desmarcar conservan las favoritas inactivas, que no se muestran")
    void marcarYDesmarcarConservanLasInactivas() {
        repositorio.guardar(new Preferencias(null, CLIENTE, List.of("cat-3", "cat-2")));

        assertEquals(List.of("cat-2", "cat-1"), servicio.marcar(EMAIL, "cat-1"));
        assertEquals(List.of("cat-1"), servicio.desmarcar(EMAIL, "cat-2"));

        assertEquals(List.of("cat-3", "cat-1"), repositorio.guardadas.get(0).getCategorias());
    }

    @Test
    @DisplayName("HU528-CP07: una favorita que ya no está activa también se puede desmarcar")
    void desmarcarUnaInactivaLaQuita() {
        repositorio.guardar(new Preferencias(null, CLIENTE, List.of("cat-3", "cat-2")));

        assertEquals(List.of("cat-2"), servicio.desmarcar(EMAIL, "cat-3"));
        assertEquals(List.of("cat-2"), repositorio.guardadas.get(0).getCategorias());
    }

    @Test
    @DisplayName("HU46-CP23: eliminar las preferencias del cliente las borra y no toca las de otro cliente")
    void eliminarBorraLasPreferenciasDelCliente() {
        repositorio.guardar(new Preferencias(null, CLIENTE, List.of("cat-1", "cat-2")));
        repositorio.guardar(new Preferencias(null, "c-2", List.of("cat-4")));

        servicio.eliminar(EMAIL);

        assertTrue(repositorio.buscarPorCliente(CLIENTE).isEmpty());
        assertTrue(repositorio.buscarPorCliente("c-2").isPresent());
    }

    @Test
    @DisplayName("HU46-CP23: eliminar las preferencias de un cliente que no tiene no es un error")
    void eliminarSinPreferenciasNoEsUnError() {
        servicio.eliminar(EMAIL);

        assertTrue(repositorio.buscarPorCliente(CLIENTE).isEmpty());
    }

    // Guarda las preferencias en una lista: unas por cliente, que se sustituyen al volver a guardar.
    private static final class RepositorioEnMemoria implements PreferenciasRepositoryPort {

        private final List<Preferencias> guardadas = new ArrayList<>();
        private int vecesGuardado;

        @Override
        public Optional<Preferencias> buscarPorCliente(String clienteId) {
            return guardadas.stream().filter(p -> p.getClienteId().equals(clienteId)).findFirst();
        }

        @Override
        public Preferencias guardar(Preferencias preferencias) {
            vecesGuardado++;
            guardadas.removeIf(p -> p.getClienteId().equals(preferencias.getClienteId()));
            Preferencias conId = new Preferencias(
                    preferencias.getId() == null ? "pr-" + (guardadas.size() + 1) : preferencias.getId(),
                    preferencias.getClienteId(), preferencias.getCategorias());
            guardadas.add(conId);
            return conId;
        }

        @Override
        public void eliminarPorCliente(String clienteId) {
            guardadas.removeIf(p -> p.getClienteId().equals(clienteId));
        }
    }
}
