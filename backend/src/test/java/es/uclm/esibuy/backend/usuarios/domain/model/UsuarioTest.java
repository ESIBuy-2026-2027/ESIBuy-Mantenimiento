package es.uclm.esibuy.backend.usuarios.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.time.Instant;
import java.time.LocalDate;

import es.uclm.esibuy.backend.usuarios.domain.exception.UsuarioInvalidoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UsuarioTest {

    private static final Email EMAIL = new Email("ana@esibuy.es");
    private static final String HASH = "hash-seguro";
    private static final LocalDate HOY = LocalDate.of(2026, 10, 10);
    private static final DatosPersonales DATOS_PERSONALES = new DatosPersonales("Ana", "García López", null);
    private static final DatosCliente DATOS_CLIENTE = new DatosCliente(
            TipoCliente.PREMIUM, new Dni("12345678Z"), LocalDate.of(1996, 5, 20), new Telefono("612345678"));
    private static final ConsentimientoPrivacidad CONSENTIMIENTO = new ConsentimientoPrivacidad(
            Instant.parse("2026-10-10T10:00:00Z"), "1.0");

    @Test
    @DisplayName("HU1-CP11: crea un usuario con datos correctos, pendiente de verificar")
    void creaUsuarioConDatosCorrectos() {
        Usuario usuario = new Usuario("u-1", DATOS_PERSONALES, EMAIL, HASH, Rol.CLIENTE, DATOS_CLIENTE, CONSENTIMIENTO);

        assertEquals("u-1", usuario.getId());
        assertEquals(DATOS_PERSONALES, usuario.getDatosPersonales());
        assertEquals(EMAIL, usuario.getEmail());
        assertEquals(HASH, usuario.getPasswordHash());
        assertEquals(Rol.CLIENTE, usuario.getRol());
        assertEquals(DATOS_CLIENTE, usuario.getDatosCliente());
        assertEquals(CONSENTIMIENTO, usuario.getConsentimiento());
        assertEquals("PENDIENTE", usuario.getEstado());
    }

    @Test
    @DisplayName("HU1-CP11: permite un usuario sin id antes de persistirlo")
    void permiteUsuarioSinIdAntesDePersistir() {
        Usuario usuario = new Usuario(null, DATOS_PERSONALES, EMAIL, HASH, Rol.CLIENTE, DATOS_CLIENTE, CONSENTIMIENTO);

        assertNull(usuario.getId());
    }

    @Test
    @DisplayName("HU1-CP11: rechaza un usuario sin datos personales")
    void rechazaUsuarioSinDatosPersonales() {
        assertThrows(UsuarioInvalidoException.class,
                () -> new Usuario("u-1", null, EMAIL, HASH, Rol.CLIENTE, DATOS_CLIENTE, CONSENTIMIENTO));
    }

    @Test
    @DisplayName("HU1-CP11: rechaza un usuario sin email")
    void rechazaEmailNulo() {
        assertThrows(UsuarioInvalidoException.class,
                () -> new Usuario("u-1", DATOS_PERSONALES, null, HASH, Rol.CLIENTE, DATOS_CLIENTE, CONSENTIMIENTO));
    }

    @Test
    @DisplayName("HU1-CP11: rechaza un usuario sin hash de contraseña")
    void rechazaContrasenaHashEnBlanco() {
        assertThrows(UsuarioInvalidoException.class,
                () -> new Usuario("u-1", DATOS_PERSONALES, EMAIL, null, Rol.CLIENTE, DATOS_CLIENTE, CONSENTIMIENTO));
        assertThrows(UsuarioInvalidoException.class,
                () -> new Usuario("u-1", DATOS_PERSONALES, EMAIL, "   ", Rol.CLIENTE, DATOS_CLIENTE, CONSENTIMIENTO));
    }

    @Test
    @DisplayName("HU1-CP11: rechaza un usuario sin rol")
    void rechazaRolNulo() {
        assertThrows(UsuarioInvalidoException.class,
                () -> new Usuario("u-1", DATOS_PERSONALES, EMAIL, HASH, null, null, CONSENTIMIENTO));
    }

    @Test
    @DisplayName("HU1-CP11: rechaza un cliente sin datos de cliente")
    void rechazaClienteSinDatosDeCliente() {
        assertThrows(UsuarioInvalidoException.class,
                () -> new Usuario("u-1", DATOS_PERSONALES, EMAIL, HASH, Rol.CLIENTE, null, CONSENTIMIENTO));
    }

    @Test
    @DisplayName("HU1-CP11: rechaza datos de cliente en un rol que no es cliente")
    void rechazaDatosDeClienteSiNoEsCliente() {
        assertThrows(UsuarioInvalidoException.class,
                () -> new Usuario("u-1", DATOS_PERSONALES, EMAIL, HASH, Rol.VENDEDOR, DATOS_CLIENTE, CONSENTIMIENTO));
        assertThrows(UsuarioInvalidoException.class,
                () -> new Usuario("u-1", DATOS_PERSONALES, EMAIL, HASH, Rol.ADMINISTRADOR, DATOS_CLIENTE, CONSENTIMIENTO));
    }

    @Test
    @DisplayName("HU1-CP11: un vendedor o un administrador no necesitan datos de cliente")
    void permiteVendedorYAdministradorSinDatosDeCliente() {
        Usuario vendedor = new Usuario("u-2", DATOS_PERSONALES, EMAIL, HASH, Rol.VENDEDOR, null, null);

        assertNull(vendedor.getDatosCliente());
        assertNull(vendedor.getConsentimiento());
    }

    @Test
    @DisplayName("HU1-CP11: un usuario nuevo no tiene fecha de alta hasta que se persiste")
    void unUsuarioNuevoNoTieneFechaDeAlta() {
        assertNull(cliente().getFechaAlta());
    }

    @Test
    @DisplayName("HU1-CP11: restaura la fecha de alta leída de la base de datos")
    void restauraLaFechaDeAlta() {
        Usuario usuario = cliente();
        Instant alta = Instant.parse("2026-10-01T10:00:00Z");

        usuario.restaurarFechaAlta(alta);

        assertEquals(alta, usuario.getFechaAlta());
    }

    @Test
    @DisplayName("HU1-CP16: una cuenta nueva nace en estado PENDIENTE y no cuenta como activa")
    void unUsuarioNuevoNaceEnEstadoPendiente() {
        Usuario usuario = cliente();

        assertEquals("PENDIENTE", usuario.getEstado());
        assertFalse(usuario.estaActivo());
    }

    @Test
    @DisplayName("HU1-CP16: una cuenta restaurada como PENDIENTE no cuenta como activa")
    void restaurarEstadoPendienteNoCuentaComoActivo() {
        Usuario usuario = cliente();

        usuario.restaurarEstado("PENDIENTE");

        assertEquals("PENDIENTE", usuario.getEstado());
        assertFalse(usuario.estaActivo());
    }

    @Test
    @DisplayName("HU1-CP16: una cuenta restaurada como ACTIVO cuenta como activa")
    void restaurarEstadoActivoCuentaComoActivo() {
        Usuario usuario = cliente();

        usuario.restaurarEstado("ACTIVO");

        assertEquals("ACTIVO", usuario.getEstado());
        assertTrue(usuario.estaActivo());
    }

    @Test
    @DisplayName("HU24-CP01: activar pasa una cuenta pendiente a activa")
    void activarPasaUnaCuentaPendienteAActiva() {
        Usuario usuario = cliente();   // nace PENDIENTE

        usuario.activar();

        assertEquals("ACTIVO", usuario.getEstado());
        assertTrue(usuario.estaActivo());
    }

    @Test
    @DisplayName("HU1-CP11: desactiva un usuario")
    void desactivaUsuario() {
        Usuario usuario = cliente();

        usuario.desactivar();

        assertFalse(usuario.estaActivo());
    }

    @Test
    @DisplayName("HU1-CP11: activa un usuario desactivado")
    void activaUsuarioDesactivado() {
        Usuario usuario = cliente();
        usuario.desactivar();

        usuario.activar();

        assertTrue(usuario.estaActivo());
    }

    @Test
    @DisplayName("HU1-CP16: un cliente creado con nuevoCliente nace en estado PENDIENTE")
    void unClienteNuevoNaceEnEstadoPendiente() {
        Usuario usuario = Usuario.nuevoCliente(DATOS_PERSONALES, EMAIL, HASH, DATOS_CLIENTE, CONSENTIMIENTO, HOY);

        assertEquals("PENDIENTE", usuario.getEstado());
        assertFalse(usuario.estaActivo());
    }

    @Test
    @DisplayName("HU1-CP13: registra a un cliente mayor de edad")
    void nuevoClienteMayorDeEdadSeRegistra() {
        // TODO: Usuario.nuevoCliente(...) con fecha de nacimiento de hace 30 años y hoy fijo devuelve un cliente
        // con rol CLIENTE, sin id y con sus DatosCliente.
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

    private static Usuario cliente() {
        return new Usuario("u-1", DATOS_PERSONALES, EMAIL, HASH, Rol.CLIENTE, DATOS_CLIENTE, CONSENTIMIENTO);
    }
}
