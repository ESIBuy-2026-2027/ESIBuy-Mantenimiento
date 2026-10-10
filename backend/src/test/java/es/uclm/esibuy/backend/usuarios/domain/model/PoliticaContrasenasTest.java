package es.uclm.esibuy.backend.usuarios.domain.model;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import es.uclm.esibuy.backend.usuarios.domain.exception.ContrasenaInvalidaException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PoliticaContrasenasTest {

    private static final DatosPersonales DATOS_PERSONALES = new DatosPersonales("Ana", "García López", null);
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
        assertThrows(ContrasenaInvalidaException.class, () -> validar("Agarcia-2026"));
    }

    @Test
    @DisplayName("HU1-CP05: rechaza una contraseña de 65 caracteres")
    void rechazaContrasenaDe65Caracteres() {
        // Frontera superior: BCrypt solo usa los primeros 72 bytes; el máximo de 64 lo evita
        assertThrows(ContrasenaInvalidaException.class, () -> validar("Tr3boles-Azules!".repeat(4) + "x"));
    }

    @Test
    @DisplayName("HU1-CP05: acepta una contraseña de exactamente 64 caracteres")
    void aceptaContrasenaDe64Caracteres() {
        assertDoesNotThrow(() -> validar("Tr3boles-Azules!".repeat(4)));
    }

    // Composición de la contraseña (T239): las mismas reglas que el frontend (REQUISITOS_PASSWORD).

    @Test
    @DisplayName("HU1-CP17: rechaza una contraseña sin mayúscula")
    void rechazaContrasenaSinMayuscula() {
        // TODO ciclo 2
        fail("Funcionalidad no implementada");
    }

    @Test
    @DisplayName("HU1-CP17: rechaza una contraseña sin minúscula")
    void rechazaContrasenaSinMinuscula() {
        // TODO ciclo 3
        fail("Funcionalidad no implementada");
    }

    @Test
    @DisplayName("HU1-CP17: rechaza una contraseña sin número")
    void rechazaContrasenaSinNumero() {
        // TODO ciclo 4
        fail("Funcionalidad no implementada");
    }

    @Test
    @DisplayName("HU1-CP17: rechaza una contraseña sin símbolo")
    void rechazaContrasenaSinSimbolo() {
        // TODO ciclo 5
        fail("Funcionalidad no implementada");
    }

    private static void validar(String contrasena) {
        PoliticaContrasenas.validar(contrasena, DATOS_PERSONALES, EMAIL);
    }
}
