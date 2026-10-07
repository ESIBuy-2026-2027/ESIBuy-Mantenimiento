package es.uclm.esibuy.backend.usuarios.dominio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class UsuarioTest {

    private static final Email EMAIL = new Email("ana@esibuy.es");
    private static final String HASH = "hash-seguro";

    @Test
    void creaUsuarioConDatosCorrectos() {
        Email email = new Email("ana@esibuy.es");

        Usuario usuario = new Usuario("u-1", "Ana", "García López", email,
                "hash-seguro", Rol.CLIENTE, TipoCliente.PREMIUM);

        assertEquals("u-1", usuario.getId());
        assertEquals("Ana", usuario.getNombre());
        assertEquals("García López", usuario.getApellidos());
        assertEquals(email, usuario.getEmail());
        assertEquals("hash-seguro", usuario.getContrasenaHash());
        assertEquals(Rol.CLIENTE, usuario.getRol());
        assertEquals(TipoCliente.PREMIUM, usuario.getTipoCliente());
        assertFalse(usuario.estaBloqueado());
    }

    @Test
    void permiteUsuarioSinIdAntesDePersistir() {
        Usuario usuario = new Usuario(null, "Ana", "García López", EMAIL,
                HASH, Rol.CLIENTE, TipoCliente.ESTANDAR);

        assertNull(usuario.getId());
    }

    @Test
    void rechazaNombreEnBlanco() {
        assertThrows(UsuarioInvalidoException.class,
                () -> cliente(null, "García López"));
        assertThrows(UsuarioInvalidoException.class,
                () -> cliente("   ", "García López"));
    }

    @Test
    void rechazaApellidosEnBlanco() {
        assertThrows(UsuarioInvalidoException.class,
                () -> cliente("Ana", null));
        assertThrows(UsuarioInvalidoException.class,
                () -> cliente("Ana", "   "));
    }

    @Test
    void rechazaEmailNulo() {
        assertThrows(UsuarioInvalidoException.class, () -> new Usuario("u-1", "Ana", "García López",
                null, HASH, Rol.CLIENTE, TipoCliente.ESTANDAR));
    }

    @Test
    void rechazaContrasenaHashEnBlanco() {
        assertThrows(UsuarioInvalidoException.class, () -> new Usuario("u-1", "Ana", "García López",
                EMAIL, null, Rol.CLIENTE, TipoCliente.ESTANDAR));
        assertThrows(UsuarioInvalidoException.class, () -> new Usuario("u-1", "Ana", "García López",
                EMAIL, "   ", Rol.CLIENTE, TipoCliente.ESTANDAR));
    }

    @Test
    void rechazaRolNulo() {
        assertThrows(UsuarioInvalidoException.class, () -> new Usuario("u-1", "Ana", "García López",
                EMAIL, HASH, null, null));
    }

    @Test
    void rechazaClienteSinTipoDeCliente() {
        assertThrows(UsuarioInvalidoException.class, () -> new Usuario("u-1", "Ana", "García López",
                EMAIL, HASH, Rol.CLIENTE, null));
    }

    @Test
    void rechazaTipoDeClienteSiNoEsCliente() {
        assertThrows(UsuarioInvalidoException.class, () -> new Usuario("u-1", "Ana", "García López",
                EMAIL, HASH, Rol.VENDEDOR, TipoCliente.PREMIUM));
        assertThrows(UsuarioInvalidoException.class, () -> new Usuario("u-1", "Ana", "García López",
                EMAIL, HASH, Rol.ADMINISTRADOR, TipoCliente.ESTANDAR));
    }

    @Test
    void bloqueaUsuario() {
        Usuario usuario = cliente("Ana", "García López");

        usuario.bloquear();

        assertTrue(usuario.estaBloqueado());
    }

    @Test
    void desbloqueaUsuarioBloqueado() {
        Usuario usuario = cliente("Ana", "García López");
        usuario.bloquear();

        usuario.desbloquear();

        assertFalse(usuario.estaBloqueado());
    }

    private static Usuario cliente(String nombre, String apellidos) {
        return new Usuario("u-1", nombre, apellidos, EMAIL, HASH, Rol.CLIENTE, TipoCliente.ESTANDAR);
    }
}
