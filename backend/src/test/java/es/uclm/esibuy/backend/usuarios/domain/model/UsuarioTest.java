package es.uclm.esibuy.backend.usuarios.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.uclm.esibuy.backend.usuarios.domain.exception.UsuarioInvalidoException;
import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UsuarioTest {

    private static final Email EMAIL = new Email("ana@esibuy.es");
    private static final String HASH = "hash-seguro";
    private static final DatosPersonales ANA = new DatosPersonales("Ana", "García López", null, null);
    private static final DatosCliente ESTANDAR = new DatosCliente(TipoCliente.ESTANDAR, null);

    @Test
    @DisplayName("HU1-CP11: crea un usuario con datos correctos y sin bloquear")
    void creaUsuarioConDatosCorrectos() {
        Email email = new Email("ana@esibuy.es");
        DatosPersonales datosPersonales = new DatosPersonales("Ana", "García López", "12345678Z", "612345678");
        DatosCliente datosCliente = new DatosCliente(TipoCliente.PREMIUM, LocalDate.of(2001, 4, 12));

        Usuario usuario = new Usuario("u-1", datosPersonales, email, "hash-seguro", datosCliente);

        assertEquals("u-1", usuario.getId());
        assertEquals(datosPersonales, usuario.getDatosPersonales());
        assertEquals("Ana", usuario.getNombre());
        assertEquals("García López", usuario.getApellidos());
        assertEquals(email, usuario.getEmail());
        assertEquals("hash-seguro", usuario.getPasswordHash());
        assertEquals(datosCliente, usuario.getDatosRol());
        assertEquals(Rol.CLIENTE, usuario.getRol());
        assertEquals(TipoCliente.PREMIUM, usuario.getTipoCliente());
        assertFalse(usuario.estaDesactivado());
    }

    @Test
    @DisplayName("HU1-CP11: permite un usuario sin id antes de persistirlo")
    void permiteUsuarioSinIdAntesDePersistir() {
        Usuario usuario = new Usuario(null, ANA, EMAIL, HASH, ESTANDAR);

        assertNull(usuario.getId());
    }

    @Test
    @DisplayName("HU1-CP11: permite un usuario sin nombre ni apellidos (datos de perfil opcionales)")
    void permiteUsuarioSinNombreNiApellidos() {
        Usuario usuario = new Usuario("u-1", DatosPersonales.sinDatos(), EMAIL, HASH, ESTANDAR);

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
    @DisplayName("HU1-CP11: rechaza un usuario sin datos personales")
    void rechazaUsuarioSinDatosPersonales() {
        assertThrows(UsuarioInvalidoException.class, () -> new Usuario("u-1", null, EMAIL, HASH, ESTANDAR));
    }

    @Test
    @DisplayName("HU1-CP11: rechaza un usuario sin email")
    void rechazaEmailNulo() {
        assertThrows(UsuarioInvalidoException.class, () -> new Usuario("u-1", ANA, null, HASH, ESTANDAR));
    }

    @Test
    @DisplayName("HU1-CP11: rechaza un usuario sin hash de contraseña")
    void rechazaContrasenaHashEnBlanco() {
        assertThrows(UsuarioInvalidoException.class, () -> new Usuario("u-1", ANA, EMAIL, null, ESTANDAR));
        assertThrows(UsuarioInvalidoException.class, () -> new Usuario("u-1", ANA, EMAIL, "   ", ESTANDAR));
    }

    @Test
    @DisplayName("HU1-CP11: rechaza un usuario sin datos de rol (sin rol)")
    void rechazaUsuarioSinDatosDeRol() {
        assertThrows(UsuarioInvalidoException.class, () -> new Usuario("u-1", ANA, EMAIL, HASH, null));
    }

    @Test
    @DisplayName("HU1-CP11: el rol sale de los datos de rol y solo los clientes tienen tipo de cliente")
    void elRolSaleDeLosDatosDeRol() {
        Usuario vendedor = new Usuario("u-1", ANA, EMAIL, HASH, new DatosVendedor("Electrónica La Mancha", "cat-1"));
        Usuario administrador = new Usuario("u-2", ANA, EMAIL, HASH,
                new DatosAdministrador("Ciudad Real", LocalDate.of(2026, 9, 1)));

        assertEquals(Rol.VENDEDOR, vendedor.getRol());
        assertNull(vendedor.getTipoCliente());
        assertEquals(Rol.ADMINISTRADOR, administrador.getRol());
        assertNull(administrador.getTipoCliente());
    }

    @Test
    @DisplayName("HU1-CP11: desactiva un usuario")
    void desactivaUsuario() {
        Usuario usuario = cliente();

        usuario.desactivar();

        assertTrue(usuario.estaDesactivado());
    }

    @Test
    @DisplayName("HU1-CP11: activa un usuario desactivado")
    void activaUsuarioDesactivado() {
        Usuario usuario = cliente();
        usuario.desactivar();

        usuario.activar();

        assertFalse(usuario.estaDesactivado());
    }

    @Test
    @DisplayName("HU1-CP11: reconstruye un usuario guardado conservando su estado de acceso")
    void reconstruyeUsuarioConSuEstadoDeAcceso() {
        // AB#210: lo usa la persistencia al leer un usuario de MongoDB.
        Instant bloqueadoHasta = Instant.parse("2026-10-08T12:15:00Z");

        Usuario usuario = Usuario.reconstruir(cliente(), true, 3, bloqueadoHasta);

        assertEquals("u-1", usuario.getId());
        assertEquals(ANA, usuario.getDatosPersonales());
        assertEquals(ESTANDAR, usuario.getDatosRol());
        assertTrue(usuario.estaDesactivado());
        assertEquals(3, usuario.getIntentosFallidos());
        assertEquals(bloqueadoHasta, usuario.getBloqueadoHasta());
    }

    @Test
    @DisplayName("HU1-CP11: rechaza reconstruir un usuario con intentos fallidos negativos")
    void rechazaReconstruirConIntentosNegativos() {
        Usuario datos = cliente();

        assertThrows(UsuarioInvalidoException.class, () -> Usuario.reconstruir(datos, false, -1, null));
    }

    @Test
    @DisplayName("HU2-CP03: un usuario reconstruido con bloqueo vigente sigue bloqueado")
    void usuarioReconstruidoConBloqueoSigueBloqueado() {
        Instant ahora = Instant.parse("2026-10-08T12:00:00Z");

        Usuario usuario = Usuario.reconstruir(cliente(), false, 5, ahora.plusSeconds(600));

        assertTrue(usuario.tieneBloqueoPorIntentos(ahora));
    }

    private static Usuario cliente() {
        return new Usuario("u-1", ANA, EMAIL, HASH, ESTANDAR);
    }
}
