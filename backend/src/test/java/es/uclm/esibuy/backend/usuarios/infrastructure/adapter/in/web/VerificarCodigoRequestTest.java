package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.in.web;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

class VerificarCodigoRequestTest {

    @Test
    @DisplayName("HU2-CP12: el toString de la petición del código no muestra el código")
    void toStringNoMuestraElCodigo() {
        VerificarCodigoRequest peticion = new VerificarCodigoRequest("654321");

        // El código es un factor de autenticación: no debe aparecer si la petición acaba en un log.
        assertFalse(peticion.toString().contains("654321"));
    }
}
