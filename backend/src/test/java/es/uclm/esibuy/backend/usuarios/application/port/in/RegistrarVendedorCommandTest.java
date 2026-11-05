package es.uclm.esibuy.backend.usuarios.application.port.in;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

class RegistrarVendedorCommandTest {

    @Test
    @DisplayName("HU1-CP02: el toString del comando del vendedor no muestra la contraseña ni su repetición")
    void toStringNoMuestraLaContrasena() {
        RegistrarVendedorCommand comando = new RegistrarVendedorCommand("Luis", "Pérez García", "Tienda Luis",
                "cat-1", "12345678Z", "luis@uclm.es", null, "Clave-1234", "Clave-1234", null, true);

        // El toString se invoca solo al hacer log del comando o concatenarlo: ninguna de las dos contraseñas debe aparecer.
        assertFalse(comando.toString().contains("Clave-1234"));
    }
}
