package es.uclm.esibuy.backend.deseos.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ListaDeseosTest {

    private static final String CLIENTE = "cliente-1";
    private static final Instant AYER = Instant.parse("2026-11-03T10:00:00Z");
    private static final Instant HOY = Instant.parse("2026-11-04T10:00:00Z");

    @Test
    @DisplayName("HU14-CP01: añadir el primer producto lo guarda en la lista con su fecha")
    void anadirElPrimerProductoLoGuardaConSuFecha() {
        ListaDeseos lista = ListaDeseos.vacia(CLIENTE);

        lista.anadir("p-1", HOY);

        assertEquals(CLIENTE, lista.getClienteId());
        assertEquals(List.of(new ItemDeseo("p-1", HOY)), lista.getItems());
    }

    @Test
    @DisplayName("HU14-CP02: añadir otro producto conserva los anteriores")
    void anadirOtroProductoConservaLosAnteriores() {
        ListaDeseos lista = new ListaDeseos("l-1", CLIENTE, List.of(new ItemDeseo("p-1", AYER)));

        lista.anadir("p-2", HOY);

        assertEquals(List.of(new ItemDeseo("p-1", AYER), new ItemDeseo("p-2", HOY)), lista.getItems());
    }

    @Test
    @DisplayName("HU14-CP03: añadir un producto que ya está no lo duplica ni cambia su fecha")
    void anadirUnProductoQueYaEstaNoLoDuplica() {
        ListaDeseos lista = new ListaDeseos("l-1", CLIENTE, List.of(new ItemDeseo("p-1", AYER)));

        lista.anadir("p-1", HOY);

        assertEquals(List.of(new ItemDeseo("p-1", AYER)), lista.getItems());
    }

    @Test
    @DisplayName("HU14-CP13: un producto eliminado desaparece de la lista, que conserva los demás y dice que ha cambiado")
    void unProductoEliminadoDesapareceDeLaLista() {
        ListaDeseos lista = new ListaDeseos("l-1", CLIENTE,
                List.of(new ItemDeseo("p-1", AYER), new ItemDeseo("p-2", HOY)));

        boolean cambiada = lista.conservarSolo(Set.of("p-2"));

        assertTrue(cambiada);
        assertEquals(List.of(new ItemDeseo("p-2", HOY)), lista.getItems());
    }

    @Test
    @DisplayName("HU14-CP13: si no hay productos eliminados la lista no cambia y lo dice")
    void sinProductosEliminadosLaListaNoCambia() {
        ListaDeseos lista = new ListaDeseos("l-1", CLIENTE, List.of(new ItemDeseo("p-1", AYER)));

        boolean cambiada = lista.conservarSolo(Set.of("p-1"));

        assertFalse(cambiada);
        assertEquals(List.of(new ItemDeseo("p-1", AYER)), lista.getItems());
    }
}
