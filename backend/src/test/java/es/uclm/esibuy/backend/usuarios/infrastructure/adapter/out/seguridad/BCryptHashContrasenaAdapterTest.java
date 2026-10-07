package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.seguridad;

import es.uclm.esibuy.backend.usuarios.application.port.out.HashContrasenaPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Test del adaptador real (sin Spring y sin mocks): se crea con new y usa BCrypt de verdad.
// Se prueba cómo usamos BCrypt (que delegamos en él y con qué configuración), no si BCrypt funciona.
class BCryptHashContrasenaAdapterTest {

    private final HashContrasenaPort adapter = new BCryptHashContrasenaAdapter(); // Tipado como el puerto: obliga a implementarlo y se prueba como lo usa el servicio

    @Test
    @DisplayName("HU1-CP02: el hash no es la contraseña en claro")
    void hashNoEsLaContrasenaEnClaro() {
        String hash = adapter.hashear("clave1234");

        assertNotEquals("clave1234", hash);
    }

    @Test
    @DisplayName("HU1-CP02: el hash es verificable con BCrypt")
    void hashEsVerificableConBCrypt() {
        String hash = adapter.hashear("clave1234");

        // matches recalcula el hash con la sal guardada dentro del propio hash y lo compara.
        // Solo da true si el adaptador ha usado BCrypt de verdad (es lo que hará el login).
        assertTrue(new BCryptPasswordEncoder().matches("clave1234", hash));
    }

    @Test
    @DisplayName("HU1-CP02: el hash usa el coste 10 acordado")
    void hashUsaElCosteAcordado() {
        // Coste de BCrypt = 10: mínimo recomendado por OWASP y valor por defecto de Spring
        String hash = adapter.hashear("clave1234");

        // Formato BCrypt: $<versión>$<coste>$<sal y hash>. El prefijo "$2a$10$" fija el coste acordado.
        assertTrue(hash.startsWith("$2a$10$"));
    }
}