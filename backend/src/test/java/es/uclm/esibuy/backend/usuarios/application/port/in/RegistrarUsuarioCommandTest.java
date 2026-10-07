package es.uclm.esibuy.backend.usuarios.application.port.in;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

class RegistrarUsuarioCommandTest {

    @Test
    @DisplayName("HU1-CP02: el toString del comando no muestra la contraseña")
    void toStringNoMuestraLaContrasena() {
        RegistrarUsuarioCommand comando = new RegistrarUsuarioCommand("juan@uclm.es", "clave1234");

        // El toString se invoca solo al hacer log del comando o concatenarlo: la contraseña no debe aparecer.
        assertFalse(comando.toString().contains("clave1234"));
    }
}
