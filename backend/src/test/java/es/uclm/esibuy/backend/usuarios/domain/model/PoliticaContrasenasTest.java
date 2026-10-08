package es.uclm.esibuy.backend.usuarios.domain.model;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import es.uclm.esibuy.backend.usuarios.domain.exception.ContrasenaInvalidaException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PoliticaContrasenasTest {

    private static final String NOMBRE = "Ana";
    private static final String APELLIDOS = "García López";
    private static final Email EMAIL = new Email("agarcia@esibuy.es");

    @Test
    @DisplayName("HU1-CP05: acepta una contraseña segura")
    void aceptaContrasenaSegura() {
        assertDoesNotThrow(() -> validar("Tr3boles-Azules!"));
    }

    @Test
    @DisplayName("HU1-CP05: rechaza una contraseña nula")
    void rechazaContrasenaNula() {
        assertThrows(ContrasenaInvalidaException.class, () -> validar(null));
    }

    @Test
    @DisplayName("HU1-CP05: rechaza una contraseña de 7 caracteres")
    void rechazaContrasenaDeSieteCaracteres() {
        assertThrows(ContrasenaInvalidaException.class, () -> validar("Xk9#pQ2"));
    }

    @Test
    @DisplayName("HU1-CP05: acepta una contraseña de exactamente 8 caracteres")
    void aceptaContrasenaDeOchoCaracteres() {
        assertDoesNotThrow(() -> validar("Xk9#pQ2m"));
    }

    @Test
    @DisplayName("HU1-CP09: rechaza una contraseña de la lista de contraseñas comunes")
    void rechazaContrasenaDeListaNegra() {
        assertThrows(ContrasenaInvalidaException.class, () -> validar("12345678"));
        assertThrows(ContrasenaInvalidaException.class, () -> validar("password"));
    }

    @Test
    @DisplayName("HU1-CP09: rechaza una contraseña común aunque cambie mayúsculas y minúsculas")
    void rechazaContrasenaDeListaNegraConMayusculas() {
        assertThrows(ContrasenaInvalidaException.class, () -> validar("PASSWORD"));
    }

    @Test
    @DisplayName("HU1-CP10: rechaza una contraseña que contiene el nombre")
    void rechazaContrasenaQueContieneElNombre() {
        assertThrows(ContrasenaInvalidaException.class, () -> validar("clave-ANA-2026"));
    }

    @Test
    @DisplayName("HU1-CP10: rechaza una contraseña que contiene los apellidos")
    void rechazaContrasenaQueContieneLosApellidos() {
        assertThrows(ContrasenaInvalidaException.class, () -> validar("García López26"));
    }

    @Test
    @DisplayName("HU1-CP10: rechaza una contraseña que contiene la parte local del email")
    void rechazaContrasenaQueContieneLaParteLocalDelEmail() {
        assertThrows(ContrasenaInvalidaException.class, () -> validar("agarcia-2026"));
    }

    private static void validar(String contrasena) {
        PoliticaContrasenas.validar(contrasena, NOMBRE, APELLIDOS, EMAIL);
    }
}
