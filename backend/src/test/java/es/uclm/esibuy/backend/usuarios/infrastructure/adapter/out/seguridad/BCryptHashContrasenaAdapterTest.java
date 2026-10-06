package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.seguridad;

import es.uclm.esibuy.backend.usuarios.application.port.out.HashContrasenaPort;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

// Test del adaptador real (sin Spring y sin mocks): se crea con new y usa BCrypt de verdad.
// Se prueba cómo usamos BCrypt (que delegamos en él y con qué configuración), no si BCrypt funciona.
class BCryptHashContrasenaAdapterTest {

    private final HashContrasenaPort adapter = new BCryptHashContrasenaAdapter(); // Tipado como el puerto: obliga a implementarlo y se prueba como lo usa el servicio

    @Test
    void hashNoEsLaContrasenaEnClaro() {
        // HU1-CP02
        String hash = adapter.hashear("clave1234");

        assertNotEquals("clave1234", hash);
    }

    @Test
    void hashEsVerificableConBCrypt() {
        // HU1-CP02
        String hash = adapter.hashear("clave1234");

        // matches recalcula el hash con la sal guardada dentro del propio hash y lo compara.
        // Solo da true si el adaptador ha usado BCrypt de verdad (es lo que hará el login).
        assertTrue(new BCryptPasswordEncoder().matches("clave1234", hash));
    }

    @Test
    void hashUsaElCosteAcordado() {
        // HU1-CP02 (coste de BCrypt = 10: mínimo recomendado por OWASP y valor por defecto de Spring)
        // TODO: hashear "clave1234" con el adaptador.
        // TODO: comprobar que el hash empieza por "$2a$10$" (algoritmo 2a, coste 10).
        fail("Funcionalidad no implementada");
    }
}