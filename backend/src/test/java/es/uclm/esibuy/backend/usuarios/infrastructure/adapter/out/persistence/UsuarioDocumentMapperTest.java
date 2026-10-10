package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.persistence;

import es.uclm.esibuy.backend.usuarios.domain.exception.UsuarioInvalidoException;
import es.uclm.esibuy.backend.usuarios.domain.model.ConsentimientoPrivacidad;
import es.uclm.esibuy.backend.usuarios.domain.model.DatosCliente;
import es.uclm.esibuy.backend.usuarios.domain.model.DatosPersonales;
import es.uclm.esibuy.backend.usuarios.domain.model.Dni;
import es.uclm.esibuy.backend.usuarios.domain.model.Email;
import es.uclm.esibuy.backend.usuarios.domain.model.Rol;
import es.uclm.esibuy.backend.usuarios.domain.model.Telefono;
import es.uclm.esibuy.backend.usuarios.domain.model.TipoCliente;
import es.uclm.esibuy.backend.usuarios.domain.model.Usuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Test unitario del mapper entre Usuario y el documento de MongoDB, en los dos sentidos
// (AB#216, AB#224, AB#225 y AB#229). PENDIENTE DE REVISAR CON EL EQUIPO: IDs de caso propuestos.
class UsuarioDocumentMapperTest {

    private static final LocalDate HOY = LocalDate.of(2026, 10, 10);
    private static final LocalDate NACIMIENTO = LocalDate.of(1996, 5, 20);
    private static final Instant ACEPTADA_EN = Instant.parse("2026-10-10T10:00:00Z");
    private static final DatosPersonales DATOS_PERSONALES = new DatosPersonales("Juan", "Pérez García", "avatar2");
    private static final DatosCliente DATOS_CLIENTE = new DatosCliente(
            TipoCliente.PREMIUM, new Dni("12345678Z"), NACIMIENTO, new Telefono("612345678"));
    private static final ConsentimientoPrivacidad CONSENTIMIENTO = new ConsentimientoPrivacidad(ACEPTADA_EN, "1.0");

    private final UsuarioDocumentMapper mapper = new UsuarioDocumentMapper();

    @Test
    @DisplayName("HU1-CP02: el documento conserva el email, el hash de la contraseña y el rol")
    void convierteUsuarioEnDocumento() {
        UsuarioDocument documento = mapper.aDocumento(clienteNuevo());

        assertEquals("juan@uclm.es", documento.getEmail());
        assertEquals("hash-simulado", documento.getPasswordHash());
        assertEquals("CLIENTE", documento.getRol());
    }

    @Test
    @DisplayName("HU1-CP01: el documento guarda los datos del cliente y el consentimiento con los nombres del modelo de BBDD")
    void convierteLosDatosDelClienteEnDocumento() {
        UsuarioDocument documento = mapper.aDocumento(clienteNuevo());

        assertEquals("Juan", documento.getNombre());
        assertEquals("Pérez García", documento.getApellidos());
        assertEquals("avatar2", documento.getImagenPerfil());
        assertEquals("PREMIUM", documento.getTipoCliente());
        assertEquals("12345678Z", documento.getDni());
        assertEquals(NACIMIENTO, documento.getFechaNacimiento());
        assertEquals("612345678", documento.getTelefono());
        assertTrue(documento.isPrivacyPolicyAccepted());
        assertEquals(ACEPTADA_EN, documento.getPrivacyPolicyAcceptedAt());
        assertEquals("1.0", documento.getPrivacyPolicyVersion());
    }

    @Test
    @DisplayName("HU2-CP01: el usuario conserva el email, el hash de la contraseña y el rol del documento")
    void convierteDocumentoEnUsuario() {
        Usuario usuario = mapper.aDominio(documentoDeCliente("juan@uclm.es"));

        assertEquals("juan@uclm.es", usuario.getEmail().valor());
        assertEquals("hash-simulado", usuario.getPasswordHash());
        assertEquals(Rol.CLIENTE, usuario.getRol());
    }

    @Test
    @DisplayName("HU1-CP01: el usuario conserva los datos personales, los de cliente y el consentimiento tras ir y volver")
    void conservaLosDatosDelClienteEnElMapeo() {
        Usuario leido = mapper.aDominio(mapper.aDocumento(clienteNuevo()));

        assertEquals(DATOS_PERSONALES, leido.getDatosPersonales());
        assertEquals(DATOS_CLIENTE, leido.getDatosCliente());
        assertEquals(CONSENTIMIENTO, leido.getConsentimiento());
    }

    @Test
    @DisplayName("HU1-CP11: un vendedor se lee sin datos de cliente")
    void unVendedorSeLeeSinDatosDeCliente() {
        UsuarioDocument vendedor = new UsuarioDocument("tienda@uclm.es", "hash-simulado", "VENDEDOR");
        vendedor.setNombre("Luis");
        vendedor.setApellidos("Martín");

        Usuario leido = mapper.aDominio(vendedor);

        assertEquals(Rol.VENDEDOR, leido.getRol());
        assertNull(leido.getDatosCliente());
    }

    @Test
    @DisplayName("HU19-CP01: el usuario recupera la fecha de alta del documento")
    void recuperaLaFechaDeAlta() {
        UsuarioDocument documento = documentoDeCliente("juan@uclm.es");
        documento.setCreatedAt(Instant.parse("2026-10-01T10:00:00Z"));

        Usuario leido = mapper.aDominio(documento);

        assertEquals(Instant.parse("2026-10-01T10:00:00Z"), leido.getFechaAlta());
    }

    @Test
    @DisplayName("HU2-CP01: un documento sin los campos de la T225 toma los valores por defecto")
    void unDocumentoAntiguoSinCamposNuevosTomaLosValoresPorDefecto() {
        // Documento guardado antes de la T225: sin estado, intentos, bloqueo ni tipo de cliente.
        UsuarioDocument antiguo = documentoDeCliente("juan@uclm.es");
        antiguo.setEstado(null);
        antiguo.setTipoCliente(null);

        Usuario leido = mapper.aDominio(antiguo);

        assertTrue(leido.estaActivo());
        assertEquals(0, leido.getIntentosFallidos());
        assertFalse(leido.tieneBloqueoPorIntentos(Instant.parse("2026-10-08T10:00:00Z")));
        assertEquals(TipoCliente.ESTANDAR, leido.getDatosCliente().tipoCliente());
    }

    @Test
    @DisplayName("HU19-CP09: conserva PENDIENTE al mapear y volver a guardar")
    void conservaEstadoPendienteEnElMapeo() {
        UsuarioDocument pendiente = documentoDeCliente("pendiente@esibuy.test");
        pendiente.setEstado("PENDIENTE");

        UsuarioDocument guardado = mapper.aDocumento(mapper.aDominio(pendiente));

        assertEquals("PENDIENTE", guardado.getEstado());
    }

    @Test
    @DisplayName("HU1-CP11: un documento que no cumple las reglas del dominio no se confunde con un error del cliente")
    void documentoInvalidoNoSeConfundeConUnErrorDelCliente() {
        // Un documento corrupto (p. ej. un DNI mal formado guardado a mano) es un fallo del servidor, no del
        // cliente: el mapper lo envuelve en una excepción de infraestructura para que acabe en un 500 y no en
        // el 400 del registro. La causa original se conserva para el log.
        UsuarioDocument corrupto = documentoDeCliente("juan@uclm.es");
        corrupto.setDni("DNI-INVALIDO");

        UsuarioPersistidoInvalidoException excepcion = assertThrows(UsuarioPersistidoInvalidoException.class,
                () -> mapper.aDominio(corrupto));

        assertInstanceOf(UsuarioInvalidoException.class, excepcion.getCause());
    }

    private static Usuario clienteNuevo() {
        return Usuario.nuevoCliente(DATOS_PERSONALES, new Email("juan@uclm.es"), "hash-simulado",
                DATOS_CLIENTE, CONSENTIMIENTO, HOY);
    }

    // Documento de un cliente con los campos obligatorios del dominio (T229): sin ellos no se puede reconstruir.
    static UsuarioDocument documentoDeCliente(String email) {
        UsuarioDocument documento = new UsuarioDocument(email, "hash-simulado", "CLIENTE");
        documento.setNombre("Juan");
        documento.setApellidos("Pérez García");
        documento.setTipoCliente("ESTANDAR");
        documento.setDni("12345678Z");
        documento.setFechaNacimiento(NACIMIENTO);
        documento.setEstado("ACTIVO");
        return documento;
    }
}
