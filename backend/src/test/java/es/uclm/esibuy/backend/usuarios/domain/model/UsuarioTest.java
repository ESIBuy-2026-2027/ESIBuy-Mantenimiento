package es.uclm.esibuy.backend.usuarios.domain.model;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;

import es.uclm.esibuy.backend.usuarios.domain.exception.MotivoRechazo;
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
    private static final DatosVendedor DATOS_VENDEDOR = new DatosVendedor(
            "Tienda Ana", "cat-1", new Dni("87654321X"), null);

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
    @DisplayName("HU1-CP11: bloquea un usuario")
    void bloqueaUsuario() {
        Usuario usuario = cliente();

        usuario.bloquear();

        assertFalse(usuario.estaActivo());
        assertEquals("BLOQUEADO", usuario.getEstado());
    }

    @Test
    @DisplayName("HU1-CP11: activa un usuario bloqueado")
    void activaUsuarioBloqueado() {
        Usuario usuario = cliente();
        usuario.bloquear();

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

    @Test
    @DisplayName("HU22-CP03: modificar los datos de un vendedor cambia sus datos personales y de vendedor")
    void modificarDatosCambiaLosDatosDeUnVendedor() {
        Usuario vendedor = Usuario.nuevoVendedor(DATOS_PERSONALES, EMAIL, HASH, DATOS_VENDEDOR, CONSENTIMIENTO);
        DatosPersonales nuevosDatosPersonales = new DatosPersonales("Vera", "Vendedora Ruiz", "avatar2");
        DatosVendedor nuevosDatosVendedor = new DatosVendedor(
                "TecnoVera", "cat-2", new Dni("10004119A"), new Telefono("620023757"));

        vendedor.modificarDatos(nuevosDatosPersonales, nuevosDatosVendedor, HOY);

        assertEquals(nuevosDatosPersonales, vendedor.getDatosPersonales());
        assertEquals(nuevosDatosVendedor, vendedor.getDatosVendedor());
        assertEquals(Rol.VENDEDOR, vendedor.getRol());
    }

    // Sin esta regla, editar un vendedor de la T241 sin sus datos de vendedor los borraba de MongoDB.
    @Test
    @DisplayName("HU22-CP05: al modificar los datos no se quitan los datos de vendedor a un vendedor")
    void modificarDatosNoQuitaLosDatosDeVendedorAUnVendedor() {
        Usuario vendedor = Usuario.nuevoVendedor(DATOS_PERSONALES, EMAIL, HASH, DATOS_VENDEDOR, CONSENTIMIENTO);
        DatosPersonales nuevosDatosPersonales = new DatosPersonales("Vera", "Vendedora Ruiz", null);

        assertThrows(UsuarioInvalidoException.class,
                () -> vendedor.modificarDatos(nuevosDatosPersonales, null, HOY));
        assertEquals(DATOS_PERSONALES, vendedor.getDatosPersonales());
        assertEquals(DATOS_VENDEDOR, vendedor.getDatosVendedor());
    }

    private static DatosCliente datosClienteNacidoEl(LocalDate fechaNacimiento) {
        return new DatosCliente(TipoCliente.ESTANDAR, new Dni("12345678Z"), fechaNacimiento, null);
    }

    private static Usuario cliente() {
        return new Usuario("u-1", DATOS_PERSONALES, EMAIL, HASH, Rol.CLIENTE, DATOS_CLIENTE, CONSENTIMIENTO);
    }

    // Registro del vendedor (T241): perfil sellado y factoría nuevoVendedor.

    @Test
    @DisplayName("HU1-CP19: nuevoVendedor nace en estado PENDIENTE con sus datos comerciales")
    void nuevoVendedorNaceEnEstadoPendienteConSusDatosComerciales() {
        Usuario vendedor = Usuario.nuevoVendedor(DATOS_PERSONALES, EMAIL, HASH, DATOS_VENDEDOR, CONSENTIMIENTO);

        assertNull(vendedor.getId());
        assertEquals(Rol.VENDEDOR, vendedor.getRol());
        assertEquals("PENDIENTE", vendedor.getEstado());
        assertEquals(DATOS_VENDEDOR, vendedor.getDatosVendedor());
        assertNull(vendedor.getDatosCliente());
    }

    @Test
    @DisplayName("HU1-CP21: rechaza datos de vendedor si el rol no es VENDEDOR")
    void rechazaDatosDeVendedorSiNoEsVendedor() {
        assertThrows(UsuarioInvalidoException.class,
                () -> new Usuario("u-1", DATOS_PERSONALES, EMAIL, HASH, Rol.CLIENTE, DATOS_VENDEDOR, CONSENTIMIENTO));
        assertThrows(UsuarioInvalidoException.class,
                () -> new Usuario("u-1", DATOS_PERSONALES, EMAIL, HASH, Rol.ADMINISTRADOR, DATOS_VENDEDOR, null));
    }

    @Test
    @DisplayName("HU1-CP21: nuevoVendedor exige los datos de vendedor")
    void nuevoVendedorExigeDatosDeVendedor() {
        assertThrows(UsuarioInvalidoException.class,
                () -> Usuario.nuevoVendedor(DATOS_PERSONALES, EMAIL, HASH, null, CONSENTIMIENTO));
    }

    @Test
    @DisplayName("HU21-CP05: el factory method crea un administrador activo y sin datos de cliente")
    void creaAdministradorNuevoActivoYSinDatosCliente() {
        DatosPersonales datosPersonales = new DatosPersonales("Nora", "Díaz", null);
        Email email = new Email("nora@esibuy.test");
        String hash = "hash-seguro";

        Usuario admin = Usuario.nuevoAdministrador(datosPersonales, email, hash);

        // Validamos el estado interno que fuerza el dominio
        assertEquals(Rol.ADMINISTRADOR, admin.getRol());
        assertEquals("ACTIVO", admin.getEstado());
        assertNull(admin.getId(), "Un usuario recién creado no debe tener ID hasta que se guarda");
        assertNull(admin.getDatosCliente(), "Los administradores no tienen datos de cliente");
        assertNull(admin.getConsentimiento(), "Los administradores no registran consentimiento de privacidad en su creación");
        assertEquals(datosPersonales, admin.getDatosPersonales());
        assertEquals(email, admin.getEmail());
    }

    @Test
    @DisplayName("HU1-CP22: el rechazo por minoría de edad indica su motivo")
    void menorDeEdadIndicaSuMotivo() {
        DatosCliente nacidoHace17Anos = datosClienteNacidoEl(HOY.minusYears(17));

        UsuarioInvalidoException excepcion = assertThrows(UsuarioInvalidoException.class,
                () -> Usuario.nuevoCliente(DATOS_PERSONALES, EMAIL, HASH, nacidoHace17Anos, CONSENTIMIENTO, HOY));

        assertEquals(MotivoRechazo.MENOR_DE_EDAD, excepcion.getMotivo());
    }

    // Fallos de la demo del Sprint 1: el contador de intentos fallidos no se reiniciaba nunca.

    @Test
    @DisplayName("HU2-CP26: tras caducar el bloqueo, el contador de intentos empieza de cero")
    void trasCaducarElBloqueoElContadorEmpiezaDeCero() {
        Usuario usuario = cliente();
        Instant ahora = Instant.parse("2026-10-12T10:00:00Z");
        for (int i = 0; i < 5; i++) {
            usuario.registrarIntentoFallido(ahora);
        }
        assertTrue(usuario.tieneBloqueoPorIntentos(ahora));

        // 16 minutos después el bloqueo de 15 ha caducado: un fallo más es el primero de una serie nueva
        Instant masTarde = ahora.plus(Duration.ofMinutes(16));
        usuario.registrarIntentoFallido(masTarde);

        assertEquals(1, usuario.getIntentosFallidos());
        assertFalse(usuario.tieneBloqueoPorIntentos(masTarde));
    }

    @Test
    @DisplayName("HU2-CP26: reiniciar los intentos quita el contador y el bloqueo")
    void reiniciarIntentosQuitaElContadorYElBloqueo() {
        Usuario usuario = cliente();
        Instant ahora = Instant.parse("2026-10-12T10:00:00Z");
        for (int i = 0; i < 5; i++) {
            usuario.registrarIntentoFallido(ahora);
        }

        usuario.reiniciarIntentos();

        assertEquals(0, usuario.getIntentosFallidos());
        assertFalse(usuario.tieneBloqueoPorIntentos(ahora));
    }

    // Recuperación de contraseña (HU3): el servicio guarda el hash de la contraseña nueva en el usuario.
    @Test
    @DisplayName("HU3-CP01: cambiar la contraseña sustituye el hash por el de la nueva")
    void cambiarContrasenaSustituyeElHash() {
        Usuario usuario = cliente();

        usuario.cambiarContrasena("hash-nuevo");

        assertEquals("hash-nuevo", usuario.getPasswordHash());
    }

    @Test
    @DisplayName("HU3-CP01: no se puede cambiar la contraseña por una vacía y se conserva la anterior")
    void cambiarContrasenaRechazaUnHashVacio() {
        Usuario usuario = cliente();

        assertThrows(UsuarioInvalidoException.class, () -> usuario.cambiarContrasena(null));
        assertThrows(UsuarioInvalidoException.class, () -> usuario.cambiarContrasena("  "));
        assertEquals(HASH, usuario.getPasswordHash());
    }

    @Test
    @DisplayName("HU3-CP01: cambiar la contraseña quita el bloqueo por intentos fallidos de login")
    void cambiarContrasenaQuitaElBloqueoPorIntentos() {
        Usuario usuario = cliente();
        Instant ahora = Instant.parse("2026-10-12T10:00:00Z");
        for (int i = 0; i < 5; i++) {
            usuario.registrarIntentoFallido(ahora);
        }

        usuario.cambiarContrasena("hash-nuevo");

        assertEquals(0, usuario.getIntentosFallidos());
        assertFalse(usuario.tieneBloqueoPorIntentos(ahora));
    }
}
