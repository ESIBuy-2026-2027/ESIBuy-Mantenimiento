package es.uclm.esibuy.backend.usuarios.application.port.in;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

class VerificarCodigoEmailCommandTest {

    @Test
    @DisplayName("HU2-CP12: el toString del comando no muestra el código")
    void toStringNoMuestraElCodigo() {
        VerificarCodigoEmailCommand comando = new VerificarCodigoEmailCommand("juan@uclm.es", "654321");

        // El toString se invoca solo al hacer log del comando o concatenarlo: el código no debe aparecer.
        assertFalse(comando.toString().contains("654321"));
    }
}
