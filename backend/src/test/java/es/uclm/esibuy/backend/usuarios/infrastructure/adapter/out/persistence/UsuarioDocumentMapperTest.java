package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.persistence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;

// Test unitario del mapper dominio -> documento de MongoDB (HU1, AB#216).
// PENDIENTE DE REVISAR CON EL EQUIPO: IDs de caso propuestos.
class UsuarioDocumentMapperTest {

    @Test
    @DisplayName("HU1-CP02: el documento conserva el email, el hash de la contraseña y el rol")
    void convierteUsuarioEnDocumento() {
        // TODO Usuario("juan@uclm.es", "hash-simulado", CLIENTE) -> documento con
        //      email "juan@uclm.es", passwordHash "hash-simulado" y rol "CLIENTE"
        fail("Funcionalidad no implementada");
    }
}
