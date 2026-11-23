package es.uclm.esibuy.backend.usuarios.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import es.uclm.esibuy.backend.usuarios.domain.exception.MotivoRechazo;
import es.uclm.esibuy.backend.usuarios.domain.exception.UsuarioInvalidoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DniTest {

    @Test
    @DisplayName("Acepta un DNI con formato oficial")
    void creaDniConFormatoOficial() {
        Dni dni = new Dni("12345678Z");
        assertEquals("12345678Z", dni.valor());
    }

    @Test
    @DisplayName("Acepta un DNI con un formato alternativo")
    void creaDniConFormatoAlternativo() {
        Dni dni = new Dni("AB-1234-X");
        assertEquals("AB-1234-X", dni.valor());
    }

    @Test
    @DisplayName("Rechaza un DNI vacío")
    void rechazaDniVacio() {
        UsuarioInvalidoException excepcion = assertThrows(
                UsuarioInvalidoException.class, () -> new Dni(" "));
        assertEquals(MotivoRechazo.DNI_INVALIDO, excepcion.getMotivo());
    }

    @Test
    @DisplayName("Rechaza un DNI nulo")
    void rechazaDniNulo() {
        assertThrows(UsuarioInvalidoException.class, () -> new Dni(null));
    }
}
