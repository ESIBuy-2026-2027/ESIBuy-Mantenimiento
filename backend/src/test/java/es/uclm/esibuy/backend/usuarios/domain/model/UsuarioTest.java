package es.uclm.esibuy.backend.usuarios.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.uclm.esibuy.backend.usuarios.domain.exception.UsuarioInvalidoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UsuarioTest {

    private static final Email EMAIL = new Email("ana@esibuy.es");
    private static final String HASH = "hash-seguro";

    @Test
    @DisplayName("HU1-CP11: crea un usuario con datos correctos y sin bloquear")
    void creaUsuarioConDatosCorrectos() {
        Email email = new Email("ana@esibuy.es");

        Usuario usuario = new Usuario("u-1", "Ana", "García López", email,
                "hash-seguro", Rol.CLIENTE, TipoCliente.PREMIUM);

        assertEquals("u-1", usuario.getId());
        assertEquals("Ana", usuario.getNombre());
        assertEquals("García López", usuario.getApellidos());
        assertEquals(email, usuario.getEmail());
        assertEquals("hash-seguro", usuario.getPasswordHash());
        assertEquals(Rol.CLIENTE, usuario.getRol());
        assertEquals(TipoCliente.PREMIUM, usuario.getTipoCliente());
        assertFalse(usuario.estaDesactivado());
    }

    @Test
    @DisplayName("HU1-CP11: permite un usuario sin id antes de persistirlo")
    void permiteUsuarioSinIdAntesDePersistir() {
        Usuario usuario = new Usuario(null, "Ana", "García López", EMAIL,
                HASH, Rol.CLIENTE, TipoCliente.ESTANDAR);

        assertNull(usuario.getId());
    }

    @Test
    @DisplayName("HU1-CP11: rechaza un nombre en blanco")
    void rechazaNombreEnBlanco() {
        assertThrows(UsuarioInvalidoException.class,
                () -> cliente("   ", "García López"));
    }

    @Test
    @DisplayName("HU1-CP11: rechaza unos apellidos en blanco")
    void rechazaApellidosEnBlanco() {
        assertThrows(UsuarioInvalidoException.class,
                () -> cliente("Ana", "   "));
    }

    @Test
    @DisplayName("HU1-CP11: permite un usuario sin nombre ni apellidos (datos de perfil opcionales)")
    void permiteUsuarioSinNombreNiApellidos() {
        Usuario usuario = cliente(null, null);

        assertNull(usuario.getNombre());
        assertNull(usuario.getApellidos());
    }

    @Test
    void creaClienteNuevoEstandarSinNombreNiId() {
        Usuario usuario = Usuario.nuevoCliente(EMAIL, HASH);

        assertNull(usuario.getId());
        assertNull(usuario.getNombre());
        assertNull(usuario.getApellidos());
        assertEquals(EMAIL, usuario.getEmail());
        assertEquals(HASH, usuario.getPasswordHash());
        assertEquals(Rol.CLIENTE, usuario.getRol());
        assertEquals(TipoCliente.ESTANDAR, usuario.getTipoCliente());
        assertFalse(usuario.estaDesactivado());
    }

    @Test
    @DisplayName("HU1-CP11: rechaza un usuario sin email")
    void rechazaEmailNulo() {
        assertThrows(UsuarioInvalidoException.class, () -> new Usuario("u-1", "Ana", "García López",
                null, HASH, Rol.CLIENTE, TipoCliente.ESTANDAR));
    }

    @Test
    @DisplayName("HU1-CP11: rechaza un usuario sin hash de contraseña")
    void rechazaContrasenaHashEnBlanco() {
        assertThrows(UsuarioInvalidoException.class, () -> new Usuario("u-1", "Ana", "García López",
                EMAIL, null, Rol.CLIENTE, TipoCliente.ESTANDAR));
        assertThrows(UsuarioInvalidoException.class, () -> new Usuario("u-1", "Ana", "García López",
                EMAIL, "   ", Rol.CLIENTE, TipoCliente.ESTANDAR));
    }

    @Test
    @DisplayName("HU1-CP11: rechaza un usuario sin rol")
    void rechazaRolNulo() {
        assertThrows(UsuarioInvalidoException.class, () -> new Usuario("u-1", "Ana", "García López",
                EMAIL, HASH, null, null));
    }

    @Test
    @DisplayName("HU1-CP11: rechaza un cliente sin tipo de cliente")
    void rechazaClienteSinTipoDeCliente() {
        assertThrows(UsuarioInvalidoException.class, () -> new Usuario("u-1", "Ana", "García López",
                EMAIL, HASH, Rol.CLIENTE, null));
    }

    @Test
    @DisplayName("HU1-CP11: rechaza un tipo de cliente en un rol que no es cliente")
    void rechazaTipoDeClienteSiNoEsCliente() {
        assertThrows(UsuarioInvalidoException.class, () -> new Usuario("u-1", "Ana", "García López",
                EMAIL, HASH, Rol.VENDEDOR, TipoCliente.PREMIUM));
        assertThrows(UsuarioInvalidoException.class, () -> new Usuario("u-1", "Ana", "García López",
                EMAIL, HASH, Rol.ADMINISTRADOR, TipoCliente.ESTANDAR));
    }

    @Test
    @DisplayName("HU1-CP11: desactiva un usuario")
    void desactivaUsuario() {
        Usuario usuario = cliente("Ana", "García López");

        usuario.desactivar();

        assertTrue(usuario.estaDesactivado());
    }

    @Test
    @DisplayName("HU1-CP11: activa un usuario desactivado")
    void activaUsuarioDesactivado() {
        Usuario usuario = cliente("Ana", "García López");
        usuario.desactivar();

        usuario.activar();

        assertFalse(usuario.estaDesactivado());
    }

    private static Usuario cliente(String nombre, String apellidos) {
        return new Usuario("u-1", nombre, apellidos, EMAIL, HASH, Rol.CLIENTE, TipoCliente.ESTANDAR);
    }
}
