package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.in.web;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

class LoginRequestTest {

    @Test
    @DisplayName("HU1-CP02: el toString de la petición de login no muestra la contraseña")
    void toStringNoMuestraLaContrasena() {
        LoginRequest peticion = new LoginRequest("juan@uclm.es", "clave1234");

        // El toString se invoca solo al hacer log de la petición o concatenarla: la contraseña no debe aparecer.
        assertFalse(peticion.toString().contains("clave1234"));
    }
}
