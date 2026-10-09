package es.uclm.esibuy.backend.usuarios.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import es.uclm.esibuy.backend.usuarios.domain.exception.UsuarioInvalidoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UsuarioTest {

    private static final Email EMAIL = new Email("ana@esibuy.es");
    private static final String HASH = "hash-seguro";

    @Test
    @DisplayName("HU1-CP11: crea un usuario con datos correctos, pendiente de verificar")
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
        assertEquals("PENDIENTE", usuario.getEstado());
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
        assertEquals("PENDIENTE", usuario.getEstado());
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

        assertFalse(usuario.estaActivo());
    }

    @Test
    @DisplayName("HU1-CP11: activa un usuario desactivado")
    void activaUsuarioDesactivado() {
        Usuario usuario = cliente("Ana", "García López");
        usuario.desactivar();

        usuario.activar();

        assertTrue(usuario.estaActivo());
    }

    @Test
    @DisplayName("HU1-CP11: rechaza un cliente sin datos de cliente")
    void rechazaClienteSinDatosDeCliente() {
        // TODO: construir un CLIENTE con datosCliente = null lanza UsuarioInvalidoException.
        fail("Funcionalidad no implementada");
    }

    @Test
    @DisplayName("HU1-CP11: rechaza datos de cliente en un rol que no es cliente")
    void rechazaDatosDeClienteSiNoEsCliente() {
        // TODO: construir un VENDEDOR o un ADMINISTRADOR con DatosCliente lanza UsuarioInvalidoException.
        fail("Funcionalidad no implementada");
    }

    @Test
    @DisplayName("HU1-CP13: registra a un cliente mayor de edad")
    void nuevoClienteMayorDeEdadSeRegistra() {
        // TODO: Usuario.nuevoCliente(...) con fecha de nacimiento de hace 30 años y hoy fijo devuelve un cliente
        // TODO: con rol CLIENTE, activo, sin id y con sus DatosCliente.
        fail("Funcionalidad no implementada");
    }

    @Test
    @DisplayName("HU1-CP13: rechaza a un cliente menor de edad")
    void rechazaClienteMenorDeEdad() {
        // TODO: nuevoCliente con fecha de nacimiento de hace 17 años lanza UsuarioInvalidoException.
        fail("Funcionalidad no implementada");
    }

    @Test
    @DisplayName("HU1-CP13: acepta a un cliente que cumple 18 años hoy")
    void aceptaClienteQueCumple18Hoy() {
        // TODO: frontera: fecha de nacimiento = hoy menos 18 años exactos -> se registra.
        fail("Funcionalidad no implementada");
    }

    @Test
    @DisplayName("HU1-CP16: una cuenta nueva nace en estado PENDIENTE y no cuenta como activa")
    void unUsuarioNuevoNaceEnEstadoPendiente() {
        Usuario usuario = cliente("Ana", "García López");

        assertEquals("PENDIENTE", usuario.getEstado());
        assertFalse(usuario.estaActivo());
    }

    @Test
    @DisplayName("HU1-CP16: un cliente creado con nuevoCliente nace en estado PENDIENTE")
    void unClienteNuevoNaceEnEstadoPendiente() {
        Usuario usuario = Usuario.nuevoCliente(EMAIL, HASH);

        assertEquals("PENDIENTE", usuario.getEstado());
        assertFalse(usuario.estaActivo());
    }

    @Test
    @DisplayName("HU1-CP16: una cuenta restaurada como PENDIENTE no cuenta como activa")
    void restaurarEstadoPendienteNoCuentaComoActivo() {
        Usuario usuario = cliente("Ana", "García López");

        usuario.restaurarEstado("PENDIENTE");

        assertEquals("PENDIENTE", usuario.getEstado());
        assertFalse(usuario.estaActivo());
    }

    @Test
    @DisplayName("HU1-CP16: una cuenta restaurada como ACTIVO cuenta como activa")
    void restaurarEstadoActivoCuentaComoActivo() {
        Usuario usuario = cliente("Ana", "García López");

        usuario.restaurarEstado("ACTIVO");

        assertEquals("ACTIVO", usuario.getEstado());
        assertTrue(usuario.estaActivo());
    }

    @Test
    @DisplayName("HU24-CP01: activar pasa una cuenta pendiente a activa")
    void activarPasaUnaCuentaPendienteAActiva() {
        Usuario usuario = cliente("Ana", "García López");   // nace PENDIENTE

        usuario.activar();

        assertEquals("ACTIVO", usuario.getEstado());
        assertTrue(usuario.estaActivo());
    }

    private static Usuario cliente(String nombre, String apellidos) {
        return new Usuario("u-1", nombre, apellidos, EMAIL, HASH, Rol.CLIENTE, TipoCliente.ESTANDAR);
    }
}
