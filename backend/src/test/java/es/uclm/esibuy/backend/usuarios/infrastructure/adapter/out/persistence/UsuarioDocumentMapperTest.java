package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.persistence;

import es.uclm.esibuy.backend.usuarios.domain.model.Email;
import es.uclm.esibuy.backend.usuarios.domain.model.Rol;
import es.uclm.esibuy.backend.usuarios.domain.model.Usuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

// Test unitario del mapper dominio -> documento de MongoDB (HU1, AB#216).
// PENDIENTE DE REVISAR CON EL EQUIPO: IDs de caso propuestos.
// Con el Usuario de la T214 (Alberto), getEmail() ya devuelve Email. PENDIENTE: el mapper
// aún no persiste nombre, apellidos, tipoCliente ni desactivado.
class UsuarioDocumentMapperTest {

    @Test
    @DisplayName("HU1-CP02: el documento conserva el email, el hash de la contraseña y el rol")
    void convierteUsuarioEnDocumento() {
        Usuario usuario = Usuario.nuevoCliente(new Email("juan@uclm.es"), "hash-simulado");

        UsuarioDocument documento = new UsuarioDocumentMapper().aDocumento(usuario);

        assertEquals("juan@uclm.es", documento.getEmail());
        assertEquals("hash-simulado", documento.getPasswordHash());
        assertEquals("CLIENTE", documento.getRol());
    }

    @Test
    @DisplayName("HU2-CP01: el usuario conserva el email, el hash de la contraseña y el rol del documento")
    void convierteDocumentoEnUsuario() {
        UsuarioDocument documento = new UsuarioDocument("juan@uclm.es", "hash-simulado", "CLIENTE");

        Usuario usuario = new UsuarioDocumentMapper().aDominio(documento);

        assertEquals("juan@uclm.es", usuario.getEmail().valor());
        assertEquals("hash-simulado", usuario.getPasswordHash());
        assertEquals(Rol.CLIENTE, usuario.getRol());
    }
}
