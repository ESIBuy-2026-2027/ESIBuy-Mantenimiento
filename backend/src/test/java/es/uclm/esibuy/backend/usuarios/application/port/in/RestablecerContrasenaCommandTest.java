package es.uclm.esibuy.backend.usuarios.application.port.in;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RestablecerContrasenaCommandTest {

    @Test
    @DisplayName("HU3-CP08: el toString del comando no muestra la contraseña nueva ni el código")
    void toStringNoMuestraLaContrasenaNiElCodigo() {
        RestablecerContrasenaCommand comando = new RestablecerContrasenaCommand("juan@uclm.es", "654321",
                "Nueva-Clave-9!");

        // El toString se invoca al hacer log del comando: la contraseña y el código (que permite cambiarla) no deben
        // aparecer. El email sí, para saber de qué cuenta se trata.
        assertFalse(comando.toString().contains("Nueva-Clave-9!"));
        assertFalse(comando.toString().contains("654321"));
        assertTrue(comando.toString().contains("juan@uclm.es"));
    }
}
