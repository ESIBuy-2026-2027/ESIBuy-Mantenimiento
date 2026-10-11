package es.uclm.esibuy.backend.usuarios.domain.model;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import es.uclm.esibuy.backend.usuarios.domain.exception.ContrasenaInvalidaException;
import es.uclm.esibuy.backend.usuarios.domain.exception.MotivoRechazo;
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
        // Contraseñas comunes que cumplen la composición (T239): solo la lista negra puede rechazarlas
        assertThrows(ContrasenaInvalidaException.class, () -> validar("Password1!"));
        assertThrows(ContrasenaInvalidaException.class, () -> validar("P@ssw0rd"));
    }

    @Test
    @DisplayName("HU1-CP09: rechaza una contraseña común aunque cambie mayúsculas y minúsculas")
    void rechazaContrasenaDeListaNegraConMayusculas() {
        assertThrows(ContrasenaInvalidaException.class, () -> validar("pASSWORD1!"));
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
        assertThrows(ContrasenaInvalidaException.class, () -> validar("tr3boles-azules!"));
    }

    @Test
    @DisplayName("HU1-CP17: rechaza una contraseña sin minúscula")
    void rechazaContrasenaSinMinuscula() {
        assertThrows(ContrasenaInvalidaException.class, () -> validar("TR3BOLES-AZULES!"));
    }

    @Test
    @DisplayName("HU1-CP17: rechaza una contraseña sin número")
    void rechazaContrasenaSinNumero() {
        assertThrows(ContrasenaInvalidaException.class, () -> validar("Treboles-Azules!"));
    }

    @Test
    @DisplayName("HU1-CP17: rechaza una contraseña sin símbolo")
    void rechazaContrasenaSinSimbolo() {
        assertThrows(ContrasenaInvalidaException.class, () -> validar("Tr3bolesAzules"));
    }

    @Test
    @DisplayName("HU1-CP17: acepta letras con tilde y la ñ como mayúsculas y minúsculas")
    void aceptaLetrasConTildeYEnie() {
        // La única mayúscula es la Ñ y las minúsculas llevan tilde: las clases Unicode las reconocen
        assertDoesNotThrow(() -> validar("Ñandú-pájaro9"));
    }

    @Test
    @DisplayName("HU1-CP17: el espacio cuenta como símbolo, igual que en el frontend")
    void aceptaElEspacioComoSimbolo() {
        // El único carácter que no es letra ni número es el espacio
        assertDoesNotThrow(() -> validar("Treboles Azules9"));
    }

    private static void validar(String contrasena) {
        PoliticaContrasenas.validar(contrasena, DATOS_PERSONALES, EMAIL);
    }

    // Fallos de la demo del Sprint 1: el rechazo indica su motivo para que la API pueda devolver un code.

    @Test
    @DisplayName("HU1-CP22: el rechazo por datos personales indica su motivo")
    void rechazoConDatosPersonalesIndicaSuMotivo() {
        ContrasenaInvalidaException excepcion = assertThrows(ContrasenaInvalidaException.class,
                () -> validar("clave-ANA-2026"));

        assertEquals(MotivoRechazo.CONTRASENA_CON_DATOS_PERSONALES, excepcion.getMotivo());
    }
}
