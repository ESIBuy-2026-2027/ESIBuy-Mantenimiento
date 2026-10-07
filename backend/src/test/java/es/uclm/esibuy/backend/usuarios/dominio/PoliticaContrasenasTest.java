package es.uclm.esibuy.backend.usuarios.dominio;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class PoliticaContrasenasTest {

    private static final String NOMBRE = "Ana";
    private static final String APELLIDOS = "García López";
    private static final Email EMAIL = new Email("agarcia@esibuy.es");

    @Test
    void aceptaContrasenaSegura() {
        assertDoesNotThrow(() -> validar("Tr3boles-Azules!"));
    }

    @Test
    void rechazaContrasenaNula() {
        assertThrows(ContrasenaInvalidaException.class, () -> validar(null));
    }

    @Test
    void rechazaContrasenaDeSieteCaracteres() {
        assertThrows(ContrasenaInvalidaException.class, () -> validar("Xk9#pQ2"));
    }

    @Test
    void aceptaContrasenaDeOchoCaracteres() {
        assertDoesNotThrow(() -> validar("Xk9#pQ2m"));
    }

    @Test
    void rechazaContrasenaDeListaNegra() {
        assertThrows(ContrasenaInvalidaException.class, () -> validar("12345678"));
        assertThrows(ContrasenaInvalidaException.class, () -> validar("password"));
    }

    @Test
    void rechazaContrasenaDeListaNegraConMayusculas() {
        assertThrows(ContrasenaInvalidaException.class, () -> validar("PASSWORD"));
    }

    @Test
    void rechazaContrasenaQueContieneElNombre() {
        assertThrows(ContrasenaInvalidaException.class, () -> validar("clave-ANA-2026"));
    }

    @Test
    void rechazaContrasenaQueContieneLosApellidos() {
        assertThrows(ContrasenaInvalidaException.class, () -> validar("García López26"));
    }

    @Test
    void rechazaContrasenaQueContieneLaParteLocalDelEmail() {
        assertThrows(ContrasenaInvalidaException.class, () -> validar("agarcia-2026"));
    }

    private static void validar(String contrasena) {
        PoliticaContrasenas.validar(contrasena, NOMBRE, APELLIDOS, EMAIL);
    }
}
