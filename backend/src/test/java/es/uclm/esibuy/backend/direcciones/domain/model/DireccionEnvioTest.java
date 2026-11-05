package es.uclm.esibuy.backend.direcciones.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.uclm.esibuy.backend.direcciones.domain.exception.DireccionInvalidaException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DireccionEnvioTest {

    private static DatosDireccion datos(String calle) {
        return new DatosDireccion("Juan Pérez", calle, null, new CodigoPostal("13071"), "Ciudad Real",
                "Ciudad Real", new TelefonoContacto("612345678"));
    }

    @Test
    @DisplayName("HU5-CP03: una dirección nueva se crea asociada a un cliente por identificador y no es principal")
    void creaDireccionAsociadaAUnCliente() {
        DireccionEnvio direccion = DireccionEnvio.nueva("cliente-1", datos("Calle Mayor 1"));

        // Sin id hasta que se guarda; la principal la decide DireccionesCliente
        assertNull(direccion.getId());
        assertEquals("cliente-1", direccion.getClienteId());
        assertEquals(datos("Calle Mayor 1"), direccion.getDatos());
        assertFalse(direccion.isPrincipal());
    }

    @Test
    @DisplayName("HU5-CP03: una dirección guardada se reconstruye conservando si es la principal")
    void reconstruyeDireccionGuardadaConservandoLaPrincipal() {
        // Lo usa el mapper al leer de MongoDB
        DireccionEnvio direccion = new DireccionEnvio("d1", "cliente-1", datos("Calle Mayor 1"), true);

        assertEquals("d1", direccion.getId());
        assertTrue(direccion.isPrincipal());
    }

    @Test
    @DisplayName("HU5-CP03: rechaza una dirección sin cliente o sin datos")
    void rechazaDireccionSinClienteOSinDatos() {
        DatosDireccion datos = datos("Calle Mayor 1");

        assertThrows(DireccionInvalidaException.class, () -> DireccionEnvio.nueva(null, datos));
        assertThrows(DireccionInvalidaException.class, () -> DireccionEnvio.nueva(" ", datos));
        assertThrows(DireccionInvalidaException.class, () -> DireccionEnvio.nueva("cliente-1", null));
    }

    @Test
    @DisplayName("HU5-CP05: marcar y desmarcar cambian si la dirección es la principal")
    void marcaYDesmarcaLaPrincipal() {
        DireccionEnvio direccion = DireccionEnvio.nueva("cliente-1", datos("Calle Mayor 1"));

        direccion.marcarPrincipal();
        assertTrue(direccion.isPrincipal());

        direccion.desmarcarPrincipal();
        assertFalse(direccion.isPrincipal());
    }

    @Test
    @DisplayName("HU5-CP07: editar cambia los datos y conserva el id, el cliente y la principal")
    void editarCambiaLosDatosYConservaLoDemas() {
        DireccionEnvio direccion = new DireccionEnvio("d1", "cliente-1", datos("Calle Mayor 1"), true);

        direccion.editar(datos("Calle Toledo 5"));

        assertEquals(datos("Calle Toledo 5"), direccion.getDatos());
        assertEquals("d1", direccion.getId());
        assertEquals("cliente-1", direccion.getClienteId());
        assertTrue(direccion.isPrincipal());
    }

    @Test
    @DisplayName("HU5-CP07: rechaza editar sin datos y conserva los anteriores")
    void rechazaEditarSinDatos() {
        DireccionEnvio direccion = new DireccionEnvio("d1", "cliente-1", datos("Calle Mayor 1"), false);

        assertThrows(DireccionInvalidaException.class, () -> direccion.editar(null));
        assertEquals(datos("Calle Mayor 1"), direccion.getDatos());
    }
}
