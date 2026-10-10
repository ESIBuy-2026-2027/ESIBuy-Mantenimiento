package es.uclm.esibuy.backend.usuarios.application.port.in;

import es.uclm.esibuy.backend.usuarios.domain.model.TipoCliente;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

class RegistrarUsuarioCommandTest {

    @Test
    @DisplayName("HU1-CP02: el toString del comando no muestra la contraseña ni su repetición")
    void toStringNoMuestraLaContrasena() {
        RegistrarUsuarioCommand comando = new RegistrarUsuarioCommand("Juan", "Pérez García",
                LocalDate.of(1996, 5, 20), "12345678Z", "juan@uclm.es", null, "clave1234", "clave1234",
                TipoCliente.ESTANDAR, null, true);

        // El toString se invoca solo al hacer log del comando o concatenarlo: ninguna de las dos contraseñas debe aparecer.
        assertFalse(comando.toString().contains("clave1234"));
    }
}
