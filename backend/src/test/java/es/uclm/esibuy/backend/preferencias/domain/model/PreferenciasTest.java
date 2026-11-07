package es.uclm.esibuy.backend.preferencias.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// Preferencias del cliente (HU46, T287): las categorías que le interesan.
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: casos HU46-CP01 a CP03, propuestos.
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
    @DisplayName("HU46-CP02: elegir categorías las guarda en el orden elegido y sin repetir ninguna")
    void elegirCategoriasLasGuardaSinRepetidas() {
        Preferencias preferencias = Preferencias.ninguna(CLIENTE);

        preferencias.elegirCategorias(List.of("cat-2", "cat-1", "cat-2"));

        assertEquals(List.of("cat-2", "cat-1"), preferencias.getCategorias());
    }

    @Test
    @DisplayName("HU46-CP03: elegir una lista vacía quita todas las categorías")
    void elegirUnaListaVaciaQuitaLasCategorias() {
        Preferencias preferencias = new Preferencias("pr-1", CLIENTE, List.of("cat-1", "cat-2"));

        preferencias.elegirCategorias(List.of());

        assertEquals("pr-1", preferencias.getId());
        assertEquals(List.of(), preferencias.getCategorias());
    }
}
