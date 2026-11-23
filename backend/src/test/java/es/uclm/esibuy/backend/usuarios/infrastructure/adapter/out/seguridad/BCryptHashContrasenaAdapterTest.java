
package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.seguridad;

import es.uclm.esibuy.backend.usuarios.application.port.out.HashContrasenaPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.security.SecureRandom;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BCryptHashContrasenaAdapterTest {

    private static final String PEPPER = generarPepper();

    private final HashContrasenaPort adapter = new BCryptHashContrasenaAdapter(PEPPER);

    private static String generarPepper() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }

    @Test
    @DisplayName("HU1-CP02: el hash no es la contraseña en claro")
    void hashNoEsLaContrasenaEnClaro() {
        String hash = adapter.hashear("clave1234");

        assertNotEquals("clave1234", hash);
        assertTrue(hash.startsWith("{bcrypt-pepper-v1}"));
    }

    @Test
    @DisplayName("HU1-CP02: BCrypt almacena el resultado protegido con Pepper")
    void hashUsaBCryptSobreLaContrasenaProtegida() {
        String hash = adapter.hashear("clave1234");

        String hashBCrypt = hash.substring("{bcrypt-pepper-v1}".length());

        assertTrue(hashBCrypt.startsWith("$2a$10$"));
        assertFalse(new BCryptPasswordEncoder().matches("clave1234", hashBCrypt));
    }

    @Test
    @DisplayName("HU2-CP02: coincide con la contraseña correcta y rechaza otra")
    void coincideConLaContrasenaCorrecta() {
        String hash = adapter.hashear("clave1234");

        assertTrue(adapter.coincide("clave1234", hash));
        assertFalse(adapter.coincide("otraClave", hash));
    }

    @Test
    @DisplayName("HU2-CP02: acepta hashes BCrypt antiguos sin Pepper")
    void aceptaHashesAntiguos() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);
        String hashAntiguo = encoder.encode("clave1234");

        assertTrue(adapter.coincide("clave1234", hashAntiguo));
        assertFalse(adapter.coincide("otraClave", hashAntiguo));
    }

    @Test
    @DisplayName("Rechaza un Pepper que no tenga 32 bytes")
    void rechazaPepperDeLongitudIncorrecta() {
        String pepperIncorrecto = Base64.getEncoder()
                .encodeToString(new byte[16]);

        assertThrows(
                IllegalArgumentException.class,
                () -> new BCryptHashContrasenaAdapter(pepperIncorrecto));
    }

    @Test
    @DisplayName("Rechaza un Pepper que no esté codificado en Base64")
    void rechazaPepperInvalido() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new BCryptHashContrasenaAdapter("pepper-invalido"));
    }
}
