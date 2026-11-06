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
import es.uclm.esibuy.backend.preferencias.domain.exception.PreferenciasInvalidasException;
import es.uclm.esibuy.backend.preferencias.domain.model.Preferencias;

// Consultar y guardar las preferencias del cliente (HU46, T287). Sin Spring: los puertos de salida se sustituyen por
// dobles de prueba en memoria.
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: casos HU46-CP04 a CP09, propuestos.
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
    @DisplayName("HU46-CP06: guardar por primera vez crea las preferencias del cliente con las categorías elegidas, sin repetidas")
    void guardarPorPrimeraVezCreaLasPreferencias() {
        List<String> guardadas = servicio.guardar(EMAIL, List.of("cat-4", "cat-1", "cat-4"));

        assertEquals(List.of("cat-4", "cat-1"), guardadas);
        assertEquals(1, repositorio.guardadas.size());
        assertEquals(CLIENTE, repositorio.guardadas.get(0).getClienteId());
        assertEquals(List.of("cat-4", "cat-1"), repositorio.guardadas.get(0).getCategorias());
    }

    @Test
    @DisplayName("HU46-CP07: guardar otra vez sustituye las categorías anteriores en las mismas preferencias")
    void guardarOtraVezSustituyeLasCategorias() {
        repositorio.guardar(new Preferencias(null, CLIENTE, List.of("cat-1")));

        assertEquals(List.of("cat-2"), servicio.guardar(EMAIL, List.of("cat-2")));
        assertEquals(1, repositorio.guardadas.size());
        assertEquals(List.of("cat-2"), repositorio.guardadas.get(0).getCategorias());
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {"cat-3", "cat-9"})
    @DisplayName("HU46-CP08: guardar una categoría inactiva o que no existe falla sin guardar nada")
    void guardarUnaCategoriaNoValidaFalla(String categoria) {
        List<String> categorias = List.of("cat-1", categoria);

        assertThrows(PreferenciasInvalidasException.class, () -> servicio.guardar(EMAIL, categorias));
        assertTrue(repositorio.guardadas.isEmpty());
    }

    @Test
    @DisplayName("HU46-CP09: guardar sin indicar las categorías falla sin guardar nada")
    void guardarSinCategoriasFalla() {
        assertThrows(PreferenciasInvalidasException.class, () -> servicio.guardar(EMAIL, null));
        assertTrue(repositorio.guardadas.isEmpty());
    }

    // Guarda las preferencias en una lista: unas por cliente, que se sustituyen al volver a guardar.
    private static final class RepositorioEnMemoria implements PreferenciasRepositoryPort {

        private final List<Preferencias> guardadas = new ArrayList<>();

        @Override
        public Optional<Preferencias> buscarPorCliente(String clienteId) {
            return guardadas.stream().filter(p -> p.getClienteId().equals(clienteId)).findFirst();
        }

        @Override
        public Preferencias guardar(Preferencias preferencias) {
            guardadas.removeIf(p -> p.getClienteId().equals(preferencias.getClienteId()));
            Preferencias conId = new Preferencias(
                    preferencias.getId() == null ? "pr-" + (guardadas.size() + 1) : preferencias.getId(),
                    preferencias.getClienteId(), preferencias.getCategorias());
            guardadas.add(conId);
            return conId;
        }
    }
}
