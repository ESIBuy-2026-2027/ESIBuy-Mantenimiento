package es.uclm.esibuy.backend.preferencias.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// Preferencias del cliente (HU46, T287): las categorías que le interesan, que son sus favoritas (AB#528).
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: casos HU46-CP01 y CP02, HU528-CP01 y CP02, propuestos.
class PreferenciasTest {

    private static final String CLIENTE = "cliente-1";

    @Test
    @DisplayName("HU46-CP01: un cliente que aún no ha elegido preferencias no tiene ninguna categoría")
    void unClienteSinPreferenciasNoTieneCategorias() {
        Preferencias preferencias = Preferencias.ninguna(CLIENTE);

        assertNull(preferencias.getId());
        assertEquals(CLIENTE, preferencias.getClienteId());
        assertEquals(List.of(), preferencias.getCategorias());
    }

    @Test
    @DisplayName("HU46-CP02: las categorías se leen en su orden y sin repetir ninguna")
    void lasCategoriasNoSeRepiten() {
        Preferencias preferencias = new Preferencias("pr-1", CLIENTE, List.of("cat-2", "cat-1", "cat-2"));

        assertEquals(List.of("cat-2", "cat-1"), preferencias.getCategorias());
    }

    @Test
    @DisplayName("HU528-CP01: marcar una categoría la añade al final y conserva las que ya eran favoritas")
    void marcarAnadeLaCategoriaAlFinal() {
        Preferencias preferencias = new Preferencias("pr-1", CLIENTE, List.of("cat-2"));

        assertTrue(preferencias.marcar("cat-1"));

        assertEquals(List.of("cat-2", "cat-1"), preferencias.getCategorias());
    }

    @Test
    @DisplayName("HU528-CP01: marcar una categoría que ya es favorita no la repite ni cambia nada")
    void marcarUnaFavoritaNoLaRepite() {
        Preferencias preferencias = new Preferencias("pr-1", CLIENTE, List.of("cat-2", "cat-1"));

        assertFalse(preferencias.marcar("cat-2"));

        assertEquals(List.of("cat-2", "cat-1"), preferencias.getCategorias());
    }

    @Test
    @DisplayName("HU528-CP02: desmarcar una favorita quita solo esa categoría")
    void desmarcarQuitaSoloEsaCategoria() {
        Preferencias preferencias = new Preferencias("pr-1", CLIENTE, List.of("cat-2", "cat-1", "cat-3"));

        assertTrue(preferencias.desmarcar("cat-1"));

        assertEquals(List.of("cat-2", "cat-3"), preferencias.getCategorias());
    }

    @Test
    @DisplayName("HU528-CP02: desmarcar una categoría que no es favorita no cambia nada")
    void desmarcarUnaQueNoEsFavoritaNoCambiaNada() {
        Preferencias preferencias = new Preferencias("pr-1", CLIENTE, List.of("cat-2"));

        assertFalse(preferencias.desmarcar("cat-1"));

        assertEquals(List.of("cat-2"), preferencias.getCategorias());
    }
}
