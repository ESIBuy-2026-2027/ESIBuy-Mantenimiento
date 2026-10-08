package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.persistence;

import es.uclm.esibuy.backend.usuarios.domain.model.Email;
import es.uclm.esibuy.backend.usuarios.domain.model.Rol;
import es.uclm.esibuy.backend.usuarios.domain.model.TipoCliente;
import es.uclm.esibuy.backend.usuarios.domain.model.Usuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Test unitario del mapper entre Usuario y el documento de MongoDB, en los dos sentidos
// (AB#216, AB#224 y AB#225). PENDIENTE DE REVISAR CON EL EQUIPO: IDs de caso propuestos.
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

    @Test
    @DisplayName("HU1-CP01: el usuario conserva nombre, apellidos y tipo de cliente")
    void conservaNombreApellidosYTipoDeCliente() {
        Usuario premium = new Usuario("u-1", "Ana", "García López", new Email("ana@uclm.es"),
                "hash-simulado", Rol.CLIENTE, TipoCliente.PREMIUM);
        UsuarioDocumentMapper mapper = new UsuarioDocumentMapper();

        Usuario leido = mapper.aDominio(mapper.aDocumento(premium));

        assertEquals("Ana", leido.getNombre());
        assertEquals("García López", leido.getApellidos());
        assertEquals(TipoCliente.PREMIUM, leido.getTipoCliente());
    }

    @Test
    @DisplayName("HU2-CP01: un documento antiguo sin los campos nuevos toma los valores por defecto")
    void unDocumentoAntiguoSinCamposNuevosTomaLosValoresPorDefecto() {
        // Documento guardado antes de la T225: solo email, hash y rol.
        UsuarioDocument antiguo = new UsuarioDocument("juan@uclm.es", "hash-simulado", "CLIENTE");

        Usuario leido = new UsuarioDocumentMapper().aDominio(antiguo);

        assertTrue(leido.estaActivo());
        assertEquals(0, leido.getIntentosFallidos());
        assertFalse(leido.tieneBloqueoPorIntentos(Instant.parse("2026-10-08T10:00:00Z")));
        assertEquals(TipoCliente.ESTANDAR, leido.getTipoCliente());
    }
}
