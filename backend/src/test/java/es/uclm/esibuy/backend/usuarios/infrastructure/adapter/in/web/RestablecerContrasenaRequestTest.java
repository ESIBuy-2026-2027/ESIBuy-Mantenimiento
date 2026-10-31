package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.in.web;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RestablecerContrasenaRequestTest {

    @Test
    @DisplayName("HU3-CP17: el toString de la petición de restablecer no muestra la contraseña nueva ni el código")
    void toStringNoMuestraLaContrasenaNiElCodigo() {
        RestablecerContrasenaRequest peticion = new RestablecerContrasenaRequest("juan@uclm.es", "654321",
                "Nueva-Clave-9!");

        // Como RestablecerContrasenaCommand: si la petición acaba en un log, solo se ve de qué cuenta se trata.
        assertFalse(peticion.toString().contains("Nueva-Clave-9!"));
        assertFalse(peticion.toString().contains("654321"));
        assertTrue(peticion.toString().contains("juan@uclm.es"));
    }
}
