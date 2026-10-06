package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.seguridad;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;

// Test del adaptador real (sin Spring y sin mocks): se crea con new y usa BCrypt de verdad.
// Se prueba cómo usamos BCrypt (que delegamos en él y con qué configuración), no si BCrypt funciona.
class BCryptHashContrasenaAdapterTest {

    @Test
    void hashNoEsLaContrasenaEnClaro() {
        // HU1-CP02
        // TODO: hashear "clave1234" con el adaptador.
        // TODO: comprobar que el resultado no es "clave1234".
        fail("Funcionalidad no implementada");
    }

    @Test
    void hashEsVerificableConBCrypt() {
        // HU1-CP02
        // TODO: hashear "clave1234" con el adaptador.
        // TODO: comprobar con BCryptPasswordEncoder.matches que el hash corresponde a "clave1234".
        fail("Funcionalidad no implementada");
    }

    @Test
    void hashUsaElCosteAcordado() {
        // HU1-CP02 (coste de BCrypt = 10: mínimo recomendado por OWASP y valor por defecto de Spring)
        // TODO: hashear "clave1234" con el adaptador.
        // TODO: comprobar que el hash empieza por "$2a$10$" (algoritmo 2a, coste 10).
        fail("Funcionalidad no implementada");
    }
}
