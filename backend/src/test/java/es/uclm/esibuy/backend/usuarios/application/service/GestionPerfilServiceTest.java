package es.uclm.esibuy.backend.usuarios.application.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneOffset;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.uclm.esibuy.backend.usuarios.application.port.in.EditarPerfilCommand;
import es.uclm.esibuy.backend.usuarios.application.port.out.HashContrasenaPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.UsuarioRepositoryPort;
import es.uclm.esibuy.backend.usuarios.domain.exception.ContrasenaActualIncorrectaException;
import es.uclm.esibuy.backend.usuarios.domain.exception.CredencialesInvalidasException;
import es.uclm.esibuy.backend.usuarios.domain.exception.NombreComercialYaRegistradoException;
import es.uclm.esibuy.backend.usuarios.domain.exception.UsuarioInvalidoException;
import es.uclm.esibuy.backend.usuarios.domain.model.DatosCliente;
import es.uclm.esibuy.backend.usuarios.domain.model.DatosPersonales;
import es.uclm.esibuy.backend.usuarios.domain.model.DatosVendedor;
import es.uclm.esibuy.backend.usuarios.domain.model.Dni;
import es.uclm.esibuy.backend.usuarios.domain.model.Email;
import es.uclm.esibuy.backend.usuarios.domain.model.Rol;
import es.uclm.esibuy.backend.usuarios.domain.model.TipoCliente;
import es.uclm.esibuy.backend.usuarios.domain.model.Usuario;

// Consulta (HU4, T297) y edición (HU45, T298) del perfil propio, y eliminación de la cuenta propia del cliente
// (HU25, T275). El repositorio y el hash son dobles de prueba y el reloj está fijo: se prueba el caso de uso sin
// MongoDB ni Spring, y la mayoría de edad no depende del día en que se ejecute.
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: casos HU4-CP01 y CP04, HU25-CP01 y CP02, y HU45-CP01 a CP04, propuestos.
@ExtendWith(MockitoExtension.class)
class GestionPerfilServiceTest {

    private static final String EMAIL = "lucia@esibuy.test";
    private static final String EMAIL_VENDEDOR = "tienda@esibuy.test";
    private static final String PASSWORD = "Clave-Segura-123";
    private static final String DNI = "12345678Z";

    @Mock
    private UsuarioRepositoryPort usuarioRepository;

    @Mock
    private HashContrasenaPort hashContrasena;

    private GestionPerfilService servicio;

    // Se crea a mano porque el reloj no es un mock y @InjectMocks no lo inyectaría
    @BeforeEach
    void crearServicio() {
        Clock reloj = Clock.fixed(Instant.parse("2026-11-05T10:00:00Z"), ZoneOffset.UTC);
        servicio = new GestionPerfilService(usuarioRepository, hashContrasena, reloj);
    }

    @Test
    @DisplayName("HU4-CP01: consultar el perfil devuelve el usuario del correo de la sesión")
    void consultarPerfilDevuelveElUsuarioDelCorreo() {
        Usuario lucia = lucia();
        when(usuarioRepository.buscarPorCorreo(EMAIL)).thenReturn(Optional.of(lucia));

        assertSame(lucia, servicio.consultarPerfil(EMAIL));
    }

    @Test
    @DisplayName("HU4-CP04: si el usuario de la sesión ya no existe, la sesión no es válida")
    void consultarPerfilDeUnCorreoQueNoExisteLanzaExcepcion() {
        when(usuarioRepository.buscarPorCorreo(EMAIL)).thenReturn(Optional.empty());

        assertThrows(CredencialesInvalidasException.class, () -> servicio.consultarPerfil(EMAIL));
    }

    @Test
    @DisplayName("HU25-CP01: con la contraseña actual correcta se elimina la cuenta del cliente de la sesión")
    void eliminarCuentaConLaContrasenaCorrectaBorraAlUsuario() {
        when(usuarioRepository.buscarPorCorreo(EMAIL)).thenReturn(Optional.of(lucia()));
        when(hashContrasena.coincide(PASSWORD, "hash-simulado")).thenReturn(true);

        servicio.eliminarCuenta(EMAIL, PASSWORD);

        verify(usuarioRepository).eliminarPorId("u-1");
    }

    @Test
    @DisplayName("HU25-CP02: con una contraseña incorrecta no se elimina la cuenta")
    void eliminarCuentaConLaContrasenaIncorrectaNoBorraNada() {
        when(usuarioRepository.buscarPorCorreo(EMAIL)).thenReturn(Optional.of(lucia()));
        when(hashContrasena.coincide("Otra-Clave-1", "hash-simulado")).thenReturn(false);

        assertThrows(ContrasenaActualIncorrectaException.class, () -> servicio.eliminarCuenta(EMAIL, "Otra-Clave-1"));
        verify(usuarioRepository, never()).eliminarPorId(anyString());
    }

    @Test
    @DisplayName("HU45-CP01: un cliente edita sus datos y se guardan; su email, DNI y tipo de cliente no cambian")
    void unClienteEditaSusDatosYSeGuardan() {
        when(usuarioRepository.buscarPorCorreo(EMAIL)).thenReturn(Optional.of(lucia()));
        EditarPerfilCommand comando = new EditarPerfilCommand("Lucía María", "Torres Gil", "avatar-2.png",
                "612345678", LocalDate.of(1995, Month.MARCH, 3), "se ignora", "se ignora");

        Usuario editado = servicio.editarPerfil(EMAIL, comando);

        verify(usuarioRepository).guardar(editado);
        DatosCliente datos = editado.getDatosCliente();
        assertAll(
                () -> assertEquals(new DatosPersonales("Lucía María", "Torres Gil", "avatar-2.png"),
                        editado.getDatosPersonales()),
                () -> assertEquals("612345678", datos.telefono().valor()),
                () -> assertEquals(LocalDate.of(1995, Month.MARCH, 3), datos.fechaNacimiento()),
                () -> assertEquals(EMAIL, editado.getEmail().valor()),
                () -> assertEquals(DNI, datos.dni().valor()),
                () -> assertEquals(TipoCliente.PREMIUM, datos.tipoCliente()));
    }

    @Test
    @DisplayName("HU45-CP02: un vendedor edita sus datos y se guardan; su DNI no cambia")
    void unVendedorEditaSusDatosYSeGuardan() {
        when(usuarioRepository.buscarPorCorreo(EMAIL_VENDEDOR)).thenReturn(Optional.of(vendedor()));
        when(usuarioRepository.existePorNombreComercial("Tienda Nueva")).thenReturn(false);
        EditarPerfilCommand comando = new EditarPerfilCommand("Mario", "Ruiz Sanz", null, "",
                null, "Tienda Nueva", "cat-2");

        Usuario editado = servicio.editarPerfil(EMAIL_VENDEDOR, comando);

        verify(usuarioRepository).guardar(editado);
        DatosVendedor datos = editado.getDatosVendedor();
        assertAll(
                () -> assertEquals("Mario", editado.getDatosPersonales().nombre()),
                () -> assertEquals("Tienda Nueva", datos.nombreComercial()),
                () -> assertEquals("cat-2", datos.categoriaPrincipalId()),
                () -> assertNull(datos.telefono()),
                () -> assertEquals(DNI, datos.dni().valor()));
    }

    @Test
    @DisplayName("HU45-CP03: un vendedor no puede usar el nombre comercial de otro vendedor")
    void unVendedorNoUsaElNombreComercialDeOtro() {
        when(usuarioRepository.buscarPorCorreo(EMAIL_VENDEDOR)).thenReturn(Optional.of(vendedor()));
        when(usuarioRepository.existePorNombreComercial("Tienda Ajena")).thenReturn(true);
        EditarPerfilCommand comando = new EditarPerfilCommand("Mario", "Ruiz Sanz", null, null,
                null, "Tienda Ajena", "cat-1");

        assertThrows(NombreComercialYaRegistradoException.class, () -> servicio.editarPerfil(EMAIL_VENDEDOR, comando));
        verify(usuarioRepository, never()).guardar(any());
    }

    @Test
    @DisplayName("HU45-CP04: con un nombre en blanco no se guarda nada")
    void conDatosNoValidosNoSeGuardaNada() {
        when(usuarioRepository.buscarPorCorreo(EMAIL)).thenReturn(Optional.of(lucia()));
        EditarPerfilCommand comando = new EditarPerfilCommand(" ", "Torres Vega", null, null,
                LocalDate.of(1996, Month.MAY, 20), null, null);

        assertThrows(UsuarioInvalidoException.class, () -> servicio.editarPerfil(EMAIL, comando));
        verify(usuarioRepository, never()).guardar(any());
    }

    @Test
    @DisplayName("HU45-CP04: un cliente no puede cambiar su fecha de nacimiento a una de menor de edad")
    void unClienteNoPuedeQuedarMenorDeEdad() {
        when(usuarioRepository.buscarPorCorreo(EMAIL)).thenReturn(Optional.of(lucia()));
        EditarPerfilCommand comando = new EditarPerfilCommand("Lucía", "Torres Vega", null, null,
                LocalDate.of(2010, Month.JANUARY, 1), null, null);

        assertThrows(UsuarioInvalidoException.class, () -> servicio.editarPerfil(EMAIL, comando));
        verify(usuarioRepository, never()).guardar(any());
    }

    @Test
    @DisplayName("HU45-CP04: un vendedor sin datos de vendedor no edita su perfil, porque su DNI no se pone aquí")
    void unVendedorSinDatosDeVendedorNoEditaSuPerfil() {
        Usuario sinDatos = new Usuario("v-2", new DatosPersonales("Ana", "López Mora", null),
                new Email(EMAIL_VENDEDOR), "hash-simulado", Rol.VENDEDOR, null, null);
        when(usuarioRepository.buscarPorCorreo(EMAIL_VENDEDOR)).thenReturn(Optional.of(sinDatos));
        EditarPerfilCommand comando = new EditarPerfilCommand("Ana", "López Mora", null, null,
                null, "Tienda Ana", "cat-1");

        assertThrows(UsuarioInvalidoException.class, () -> servicio.editarPerfil(EMAIL_VENDEDOR, comando));
        verify(usuarioRepository, never()).guardar(any());
    }

    private static Usuario lucia() {
        return new Usuario("u-1", new DatosPersonales("Lucía", "Torres Vega", null), new Email(EMAIL),
                "hash-simulado", Rol.CLIENTE,
                new DatosCliente(TipoCliente.PREMIUM, new Dni(DNI), LocalDate.of(1996, Month.MAY, 20), null),
                null);
    }

    private static Usuario vendedor() {
        return new Usuario("v-1", new DatosPersonales("Mario", "Ruiz Sanz", null), new Email(EMAIL_VENDEDOR),
                "hash-simulado", Rol.VENDEDOR, new DatosVendedor("Tienda Mario", "cat-1", new Dni(DNI), null), null);
    }
}
