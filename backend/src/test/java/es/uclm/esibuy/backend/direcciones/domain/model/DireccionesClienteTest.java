package es.uclm.esibuy.backend.direcciones.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.uclm.esibuy.backend.direcciones.domain.exception.DireccionNoEncontradaException;
import es.uclm.esibuy.backend.direcciones.domain.exception.LimiteDireccionesException;
import es.uclm.esibuy.backend.direcciones.domain.exception.NuevaPrincipalObligatoriaException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DireccionesClienteTest {

    private static final String CLIENTE = "cliente-1";

    private static DatosDireccion datos(String calle) {
        return new DatosDireccion("Juan Pérez", calle, null, new CodigoPostal("13071"), "Ciudad Real",
                "Ciudad Real", new TelefonoContacto("612345678"));
    }

    // Dirección aún no guardada, como la que llega al dar de alta una
    private static DireccionEnvio nuevaDireccion() {
        return DireccionEnvio.nueva(CLIENTE, datos("Calle Mayor 1"));
    }

    // Dirección ya guardada, como las que se cargan del repositorio
    private static DireccionEnvio direccionGuardada(String id) {
        return new DireccionEnvio(id, CLIENTE, datos("Calle Mayor 1"), false);
    }

    private static DireccionEnvio direccionGuardadaPrincipal(String id) {
        return new DireccionEnvio(id, CLIENTE, datos("Calle Mayor 1"), true);
    }

    @Test
    @DisplayName("HU5-CP04: permite añadir direcciones hasta el límite de cinco")
    void anadeDireccionesHastaElLimite() {
        DireccionesCliente direcciones = new DireccionesCliente(new ArrayList<>());

        for (int i = 0; i < 5; i++) {
            direcciones.anadir(nuevaDireccion());
        }

        assertEquals(5, direcciones.getDirecciones().size());
    }

    @Test
    @DisplayName("HU5-CP04: rechaza añadir una sexta dirección")
    void rechazaSextaDireccion() {
        DireccionesCliente direcciones = new DireccionesCliente(new ArrayList<>());
        for (int i = 0; i < 5; i++) {
            direcciones.anadir(nuevaDireccion());
        }

        // Frontera: 5 caben, la sexta no; y no se queda añadida
        assertThrows(LimiteDireccionesException.class, () -> direcciones.anadir(nuevaDireccion()));
        assertEquals(5, direcciones.getDirecciones().size());
    }

    @Test
    @DisplayName("HU5-CP05: la primera dirección añadida queda como principal")
    void primeraDireccionQuedaPrincipal() {
        DireccionesCliente direcciones = new DireccionesCliente(new ArrayList<>());
        DireccionEnvio primera = nuevaDireccion();

        direcciones.anadir(primera);

        assertTrue(primera.isPrincipal());
    }

    @Test
    @DisplayName("HU5-CP05: una dirección añadida después no cambia la principal")
    void direccionPosteriorNoCambiaLaPrincipal() {
        DireccionesCliente direcciones = new DireccionesCliente(new ArrayList<>());
        DireccionEnvio primera = nuevaDireccion();
        DireccionEnvio segunda = nuevaDireccion();
        direcciones.anadir(primera);

        direcciones.anadir(segunda);

        assertTrue(primera.isPrincipal());
        assertFalse(segunda.isPrincipal());
    }

    @Test
    @DisplayName("HU5-CP05: marcar otra dirección deja solo una como principal")
    void marcarOtraDejaSoloUnaPrincipal() {
        DireccionEnvio actual = direccionGuardadaPrincipal("d1");
        DireccionEnvio otra = direccionGuardada("d2");
        DireccionesCliente direcciones = new DireccionesCliente(List.of(actual, otra));

        direcciones.marcarPrincipal("d2");

        assertTrue(otra.isPrincipal());
        assertFalse(actual.isPrincipal());
    }

    @Test
    @DisplayName("HU5-CP05: rechaza marcar una dirección que no es del cliente")
    void rechazaMarcarDireccionInexistente() {
        DireccionEnvio actual = direccionGuardadaPrincipal("d1");
        DireccionesCliente direcciones = new DireccionesCliente(List.of(actual));

        assertThrows(DireccionNoEncontradaException.class, () -> direcciones.marcarPrincipal("de-otro-cliente"));
        // Un id erróneo no deja al cliente sin principal
        assertTrue(actual.isPrincipal());
    }

    @Test
    @DisplayName("HU5-CP06: eliminar una dirección que no es la principal conserva la principal")
    void eliminaDireccionNoPrincipal() {
        DireccionEnvio principal = direccionGuardadaPrincipal("d1");
        DireccionesCliente direcciones = new DireccionesCliente(List.of(principal, direccionGuardada("d2")));

        // Sin elegir sustituta: la principal no cambia
        direcciones.eliminar("d2", null);

        assertEquals(List.of(principal), direcciones.getDirecciones());
        assertTrue(principal.isPrincipal());
    }

    @Test
    @DisplayName("HU5-CP06: eliminar la principal exige indicar cuál pasa a serlo")
    void eliminarPrincipalExigeElegirOtra() {
        DireccionesCliente direcciones = new DireccionesCliente(
                List.of(direccionGuardadaPrincipal("d1"), direccionGuardada("d2")));

        assertThrows(NuevaPrincipalObligatoriaException.class, () -> direcciones.eliminar("d1", null));
        // No se elimina nada si falta la elección
        assertEquals(2, direcciones.getDirecciones().size());
    }

    @Test
    @DisplayName("HU5-CP06: eliminar la principal y elegir otra la deja como principal")
    void eliminarPrincipalYElegirOtra() {
        DireccionEnvio sustituta = direccionGuardada("d3");
        DireccionesCliente direcciones = new DireccionesCliente(
                List.of(direccionGuardadaPrincipal("d1"), direccionGuardada("d2"), sustituta));

        direcciones.eliminar("d1", "d3");

        assertEquals(2, direcciones.getDirecciones().size());
        assertTrue(sustituta.isPrincipal());
    }

    @Test
    @DisplayName("HU5-CP06: rechaza elegir como sustituta la misma dirección que se elimina")
    void rechazaElegirComoSustitutaLaDireccionEliminada() {
        DireccionEnvio principal = direccionGuardadaPrincipal("d1");
        DireccionesCliente direcciones = new DireccionesCliente(List.of(principal, direccionGuardada("d2")));

        // Elegirse a sí misma equivale a no elegir: el cliente se quedaría sin principal teniendo otras
        assertThrows(NuevaPrincipalObligatoriaException.class, () -> direcciones.eliminar("d1", "d1"));
        assertEquals(2, direcciones.getDirecciones().size());
        assertTrue(principal.isPrincipal());
    }

    @Test
    @DisplayName("HU5-CP06: rechaza elegir como sustituta una dirección que no es del cliente")
    void rechazaSustitutaDeOtroCliente() {
        DireccionEnvio principal = direccionGuardadaPrincipal("d1");
        DireccionesCliente direcciones = new DireccionesCliente(List.of(principal, direccionGuardada("d2")));

        assertThrows(DireccionNoEncontradaException.class, () -> direcciones.eliminar("d1", "de-otro-cliente"));
        assertEquals(2, direcciones.getDirecciones().size());
        assertTrue(principal.isPrincipal());
    }

    @Test
    @DisplayName("HU5-CP06: eliminar la única dirección deja la lista vacía sin exigir elección")
    void eliminarLaUnicaDireccionDejaListaVacia() {
        DireccionesCliente direcciones = new DireccionesCliente(List.of(direccionGuardadaPrincipal("d1")));

        // Es la principal, pero no queda ninguna otra que elegir
        direcciones.eliminar("d1", null);

        assertTrue(direcciones.getDirecciones().isEmpty());
    }

    @Test
    @DisplayName("HU5-CP06: rechaza eliminar una dirección que no es del cliente")
    void rechazaEliminarDireccionInexistente() {
        DireccionesCliente direcciones = new DireccionesCliente(List.of(direccionGuardadaPrincipal("d1")));

        assertThrows(DireccionNoEncontradaException.class, () -> direcciones.eliminar("de-otro-cliente", null));
        assertEquals(1, direcciones.getDirecciones().size());
    }

    @Test
    @DisplayName("HU5-CP07: editar una dirección del cliente cambia sus datos y la devuelve")
    void editarDireccionDelCliente() {
        DireccionEnvio direccion = direccionGuardadaPrincipal("d1");
        DireccionesCliente direcciones = new DireccionesCliente(List.of(direccion, direccionGuardada("d2")));

        DireccionEnvio editada = direcciones.editar("d1", datos("Calle Toledo 5"));

        assertEquals(direccion, editada);
        assertEquals(datos("Calle Toledo 5"), direccion.getDatos());
        // Editar no cambia cuál es la principal
        assertTrue(direccion.isPrincipal());
    }

    @Test
    @DisplayName("HU5-CP07: rechaza editar una dirección que no es del cliente")
    void rechazaEditarDireccionInexistente() {
        DireccionesCliente direcciones = new DireccionesCliente(List.of(direccionGuardadaPrincipal("d1")));
        DatosDireccion nuevos = datos("Calle Toledo 5");

        assertThrows(DireccionNoEncontradaException.class, () -> direcciones.editar("de-otro-cliente", nuevos));
    }
}
