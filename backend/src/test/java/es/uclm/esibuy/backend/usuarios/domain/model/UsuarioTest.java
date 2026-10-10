package es.uclm.esibuy.backend.usuarios.domain.model;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    @DisplayName("HU1-CP12: una cuenta nueva nace en estado PENDIENTE y no cuenta como activa")
    void unUsuarioNuevoNaceEnEstadoPendiente() {
        Usuario usuario = cliente();

        assertEquals("PENDIENTE", usuario.getEstado());
        assertFalse(usuario.estaActivo());
    }

    @Test
    @DisplayName("HU1-CP12: una cuenta restaurada como PENDIENTE no cuenta como activa")
    void restaurarEstadoPendienteNoCuentaComoActivo() {
        Usuario usuario = cliente();

        usuario.restaurarEstado("PENDIENTE");

        assertEquals("PENDIENTE", usuario.getEstado());
        assertFalse(usuario.estaActivo());
    }

    @Test
    @DisplayName("HU1-CP12: una cuenta restaurada como ACTIVO cuenta como activa")
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
    @DisplayName("HU1-CP12: un cliente creado con nuevoCliente nace en estado PENDIENTE")
    void unClienteNuevoNaceEnEstadoPendiente() {
        Usuario usuario = Usuario.nuevoCliente(DATOS_PERSONALES, EMAIL, HASH, DATOS_CLIENTE, CONSENTIMIENTO, HOY);

        assertEquals("PENDIENTE", usuario.getEstado());
        assertFalse(usuario.estaActivo());
    }

    @Test
    @DisplayName("HU1-CP13: registra a un cliente mayor de edad")
    void nuevoClienteMayorDeEdadSeRegistra() {
        DatosCliente nacidoHace30Anos = datosClienteNacidoEl(HOY.minusYears(30));

        Usuario usuario = Usuario.nuevoCliente(DATOS_PERSONALES, EMAIL, HASH, nacidoHace30Anos, CONSENTIMIENTO, HOY);

        assertNull(usuario.getId());
        assertEquals(Rol.CLIENTE, usuario.getRol());
        assertEquals(nacidoHace30Anos, usuario.getDatosCliente());
    }

    @Test
    @DisplayName("HU1-CP13: rechaza a un cliente menor de edad")
    void rechazaClienteMenorDeEdad() {
        DatosCliente nacidoHace17Anos = datosClienteNacidoEl(HOY.minusYears(17));

        assertThrows(UsuarioInvalidoException.class,
                () -> Usuario.nuevoCliente(DATOS_PERSONALES, EMAIL, HASH, nacidoHace17Anos, CONSENTIMIENTO, HOY));
    }

    @Test
    @DisplayName("HU1-CP13: acepta a un cliente que cumple 18 años hoy")
    void aceptaClienteQueCumple18Hoy() {
        // Frontera: cumple 18 hoy -> mayor de edad; con un día más de nacimiento (mañana cumpliría) -> menor
        DatosCliente cumple18Hoy = datosClienteNacidoEl(HOY.minusYears(18));
        DatosCliente cumple18Manana = datosClienteNacidoEl(HOY.minusYears(18).plusDays(1));

        assertDoesNotThrow(() -> Usuario.nuevoCliente(DATOS_PERSONALES, EMAIL, HASH, cumple18Hoy, CONSENTIMIENTO, HOY));
        assertThrows(UsuarioInvalidoException.class,
                () -> Usuario.nuevoCliente(DATOS_PERSONALES, EMAIL, HASH, cumple18Manana, CONSENTIMIENTO, HOY));
    }

    @Test
    @DisplayName("HU22-CP01: modificar los datos de un cliente cambia sus datos personales y de cliente")
    void modificarDatosCambiaLosDatosDeUnCliente() {
        Usuario usuario = cliente();
        DatosPersonales nuevosDatosPersonales = new DatosPersonales("Carla", "Moreno Gil", "avatar1");
        DatosCliente nuevosDatosCliente = new DatosCliente(
                TipoCliente.ESTANDAR, new Dni("10004119A"), LocalDate.of(1978, 4, 13), new Telefono("620023757"));

        usuario.modificarDatos(nuevosDatosPersonales, nuevosDatosCliente, HOY);

        assertEquals(nuevosDatosPersonales, usuario.getDatosPersonales());
        assertEquals(nuevosDatosCliente, usuario.getDatosCliente());
        assertEquals(EMAIL, usuario.getEmail());
        assertEquals(Rol.CLIENTE, usuario.getRol());
    }

    @Test
    @DisplayName("HU22-CP02: modificar los datos de un administrador cambia sus datos personales")
    void modificarDatosCambiaLosDatosDeUnAdministrador() {
        Usuario administrador = new Usuario("u-3", DATOS_PERSONALES, EMAIL, HASH, Rol.ADMINISTRADOR, null, null);
        DatosPersonales nuevosDatosPersonales = new DatosPersonales("Diego", "Delgado Iglesias", "avatar3");

        administrador.modificarDatos(nuevosDatosPersonales, null, HOY);

        assertEquals(nuevosDatosPersonales, administrador.getDatosPersonales());
        assertNull(administrador.getDatosCliente());
    }

    @Test
    @DisplayName("HU22-CP05: rechaza modificar los datos de un cliente a los de un menor de edad y no cambia nada")
    void modificarDatosRechazaUnClienteMenorDeEdad() {
        Usuario usuario = cliente();
        DatosPersonales nuevosDatosPersonales = new DatosPersonales("Carla", "Moreno Gil", null);
        DatosCliente nacidoHace17Anos = datosClienteNacidoEl(HOY.minusYears(17));

        assertThrows(UsuarioInvalidoException.class,
                () -> usuario.modificarDatos(nuevosDatosPersonales, nacidoHace17Anos, HOY));
        assertEquals(DATOS_PERSONALES, usuario.getDatosPersonales());
        assertEquals(DATOS_CLIENTE, usuario.getDatosCliente());
    }

    @Test
    @DisplayName("HU22-CP05: al modificar los datos acepta a un cliente que cumple 18 años hoy y rechaza al que los cumple mañana")
    void modificarDatosAceptaUnClienteQueCumple18Hoy() {
        Usuario usuario = cliente();
        DatosCliente cumple18Hoy = datosClienteNacidoEl(HOY.minusYears(18));
        DatosCliente cumple18Manana = datosClienteNacidoEl(HOY.minusYears(18).plusDays(1));

        assertDoesNotThrow(() -> usuario.modificarDatos(DATOS_PERSONALES, cumple18Hoy, HOY));
        assertThrows(UsuarioInvalidoException.class,
                () -> usuario.modificarDatos(DATOS_PERSONALES, cumple18Manana, HOY));
    }

    @Test
    @DisplayName("HU22-CP05: al modificar los datos no se quitan los datos de cliente a un cliente ni se ponen a otro rol")
    void modificarDatosExigeDatosDeClienteCoherentesConElRol() {
        Usuario usuario = cliente();
        Usuario administrador = new Usuario("u-3", DATOS_PERSONALES, EMAIL, HASH, Rol.ADMINISTRADOR, null, null);

        assertThrows(UsuarioInvalidoException.class, () -> usuario.modificarDatos(DATOS_PERSONALES, null, HOY));
        assertThrows(UsuarioInvalidoException.class,
                () -> administrador.modificarDatos(DATOS_PERSONALES, DATOS_CLIENTE, HOY));
        assertEquals(DATOS_CLIENTE, usuario.getDatosCliente());
        assertNull(administrador.getDatosCliente());
    }

    private static DatosCliente datosClienteNacidoEl(LocalDate fechaNacimiento) {
        return new DatosCliente(TipoCliente.ESTANDAR, new Dni("12345678Z"), fechaNacimiento, null);
    }

    private static Usuario cliente() {
        return new Usuario("u-1", DATOS_PERSONALES, EMAIL, HASH, Rol.CLIENTE, DATOS_CLIENTE, CONSENTIMIENTO);
    }
}
