package es.uclm.esibuy.backend.usuarios.infrastructure.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;

// Test técnico: Spring monta la cadena del registro al arrancar (HU1, AB#216).
class UsuariosConfigTest {

    @Test
    @DisplayName("Configuración: el contexto conecta servicio, hash y adaptador de MongoDB")
    void elContextoConectaLasPiezasDelRegistro() {
        // TODO RegistroUsuarioUseCase es RegistroUsuarioService, HashContrasenaPort es
        //      BCryptHashContrasenaAdapter y UsuarioRepositoryPort es UsuarioMongoAdapter
        fail("Funcionalidad no implementada");
    }
}
