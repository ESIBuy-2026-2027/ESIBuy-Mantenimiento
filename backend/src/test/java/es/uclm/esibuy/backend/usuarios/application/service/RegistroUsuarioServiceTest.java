package es.uclm.esibuy.backend.usuarios.application.service;

import es.uclm.esibuy.backend.usuarios.application.port.in.RegistrarUsuarioCommand;
import es.uclm.esibuy.backend.usuarios.application.port.in.RegistroUsuarioUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.out.HashContrasenaPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.UsuarioRepositoryPort;
import es.uclm.esibuy.backend.usuarios.domain.exception.ContrasenaInvalidaException;
import es.uclm.esibuy.backend.usuarios.domain.exception.CorreoYaRegistradoException;
import es.uclm.esibuy.backend.usuarios.domain.exception.DniYaRegistradoException;
import es.uclm.esibuy.backend.usuarios.domain.exception.UsuarioInvalidoException;
import es.uclm.esibuy.backend.usuarios.domain.model.ConsentimientoPrivacidad;
import es.uclm.esibuy.backend.usuarios.domain.model.DatosCliente;
import es.uclm.esibuy.backend.usuarios.domain.model.DatosPersonales;
import es.uclm.esibuy.backend.usuarios.domain.model.Dni;
import es.uclm.esibuy.backend.usuarios.domain.model.Telefono;
import es.uclm.esibuy.backend.usuarios.domain.model.Rol;
import es.uclm.esibuy.backend.usuarios.domain.model.Usuario;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import es.uclm.esibuy.backend.usuarios.domain.model.TipoCliente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Cómo encaja el test en la arquitectura hexagonal:
// - RegistroUsuarioUseCase (application.port.in): puerto de entrada, lo que la aplicación ofrece.
//   El test usa el servicio a través de él, igual que lo hará el controlador REST.
// - RegistrarUsuarioCommand (application.port.in): datos de entrada del caso de uso.
//   El controlador REST traducirá su DTO a este comando; el núcleo no conoce los DTO.
// - RegistroUsuarioService (application.service): implementa el caso de uso. Es la clase bajo prueba.
// - UsuarioRepositoryPort (application.port.out): lo que el servicio necesita del exterior.
//   En producción lo implementará el adaptador de MongoDB; aquí lo sustituye un mock de Mockito.
// - HashContrasenaPort (application.port.out): calcula el hash de la contraseña.
//   En producción lo implementará un adaptador con el algoritmo de hash elegido; aquí, otro mock.
// - Usuario y Rol (domain.model): Java puro, sin Spring ni MongoDB.

@ExtendWith(MockitoExtension.class)
class RegistroUsuarioServiceTest {

    @Mock
    private UsuarioRepositoryPort usuarioRepository;

    @Mock
    private HashContrasenaPort hashContrasena;

    private RegistroUsuarioUseCase registroUsuario; // Tipado como el puerto de entrada: obliga a implementarlo y se prueba como lo usará el controlador

    // Reloj fijo: la fecha de hoy (mayoría de edad) y el instante del consentimiento no dependen de cuándo se ejecuta el test.
    private static final Clock RELOJ = Clock.fixed(Instant.parse("2026-10-10T10:00:00Z"), ZoneOffset.UTC);

    @BeforeEach
    void crearServicio() {
        registroUsuario = new RegistroUsuarioService(usuarioRepository, hashContrasena, RELOJ);
    }

    // Comando de un cliente válido (T229); solo cambian el email y la contraseña, que es lo que prueba cada test.
    private static RegistrarUsuarioCommand comando(String email, String password) {
        return new RegistrarUsuarioCommand("Juan", "Pérez García", LocalDate.of(1996, 5, 20), "12345678Z",
                email, null, password, password, TipoCliente.ESTANDAR, null, true);
    }

    @Test
    @DisplayName("HU1-CP01: registra al cliente con rol CLIENTE")
    void registraClienteConRolCliente() {
        when(hashContrasena.hashear("clave1234")).thenReturn("hash-simulado");
        RegistrarUsuarioCommand comando = comando("juan@uclm.es", "clave1234");

        registroUsuario.registrar(comando);

        ArgumentCaptor<Usuario> usuarioGuardado = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).guardar(usuarioGuardado.capture());
        assertEquals(Rol.CLIENTE, usuarioGuardado.getValue().getRol());
        assertEquals("PENDIENTE", usuarioGuardado.getValue().getEstado()); // La cuenta se crea en estado PENDIENTE de activación por un administrador.
    }

    @Test
    @DisplayName("HU1-CP02: guarda la contraseña hasheada, nunca en claro")
    void guardaContrasenaHasheada() {
        when(hashContrasena.hashear("clave1234")).thenReturn("hash-simulado");
        RegistrarUsuarioCommand comando = comando("juan@uclm.es", "clave1234");

        registroUsuario.registrar(comando);

        ArgumentCaptor<Usuario> usuarioGuardado = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).guardar(usuarioGuardado.capture());
        assertEquals("hash-simulado", usuarioGuardado.getValue().getPasswordHash());
        assertNotEquals("clave1234", usuarioGuardado.getValue().getPasswordHash());
    }

    @Test
    @DisplayName("HU1-CP05: acepta una contraseña de exactamente 8 caracteres")
    void permiteContrasenaDeExactamente8Caracteres() {
        // Frontera: 8 caracteres → permitido
        when(hashContrasena.hashear("Xk9#pQ2m")).thenReturn("hash-simulado");
        RegistrarUsuarioCommand comando = comando("juan@uclm.es", "Xk9#pQ2m");

        registroUsuario.registrar(comando);

        verify(usuarioRepository).guardar(any(Usuario.class));
    }

    @Test
    @DisplayName("HU1-CP05: rechaza una contraseña de menos de 8 caracteres")
    void rechazaContrasenaDeMenosDe8Caracteres() {
        // Frontera: 7 caracteres → excepción
        RegistrarUsuarioCommand comando = comando("juan@uclm.es", "1234567");

        // assertThrows(TipoDeExcepcion.class, () -> codigo) ejecuta el código de la lambda
        // y comprueba que lanza exactamente esa excepción (o una subclase suya):
        // - si la lanza, el test sigue;
        // - si no lanza nada, o lanza otra distinta, el test falla.
        // El código va dentro de una lambda (() -> ...) y no se llama directamente porque,
        // si se llamara aquí, la excepción saldría antes de que assertThrows pudiera capturarla.
        assertThrows(ContrasenaInvalidaException.class, () -> registroUsuario.registrar(comando));

        verify(usuarioRepository, never()).guardar(any(Usuario.class));
    }

    @Test
    @DisplayName("HU1-CP03: rechaza un correo ya registrado y no guarda nada")
    void rechazaEmailYaRegistrado() {
        when(usuarioRepository.existePorCorreo("juan@uclm.es")).thenReturn(true);
        RegistrarUsuarioCommand comando = comando("juan@uclm.es", "clave1234");

        assertThrows(CorreoYaRegistradoException.class, () -> registroUsuario.registrar(comando));

        verify(usuarioRepository, never()).guardar(any(Usuario.class));
    }

    @Test
    @DisplayName("HU1-CP16: rechaza un DNI ya registrado y no guarda nada")
    void rechazaDniYaRegistrado() {
        when(usuarioRepository.existePorDni("12345678Z")).thenReturn(true);
        lenient().when(hashContrasena.hashear("clave1234")).thenReturn("hash-simulado");

        assertThrows(DniYaRegistradoException.class, () -> registroUsuario.registrar(comando("juan@uclm.es", "clave1234")));

        verify(usuarioRepository, never()).guardar(any(Usuario.class));
    }

    @Test
    @DisplayName("HU1-CP05: rechaza una contraseña nula y no guarda nada")
    void rechazaContrasenaNula() {
        RegistrarUsuarioCommand comando = comando("juan@uclm.es", null);

        assertThrows(ContrasenaInvalidaException.class, () -> registroUsuario.registrar(comando));

        verify(usuarioRepository, never()).guardar(any(Usuario.class));
    }

    @Test
    @DisplayName("HU1-CP01: registra un cliente con todos sus datos")
    void registraClienteConTodosSusDatos() {
        when(hashContrasena.hashear("clave1234")).thenReturn("hash-simulado");
        RegistrarUsuarioCommand comando = new RegistrarUsuarioCommand("Juan", "Pérez García",
                LocalDate.of(1996, 5, 20), "12345678Z", "juan@uclm.es", "612345678", "clave1234", "clave1234",
                TipoCliente.PREMIUM, "avatar2", true);

        registroUsuario.registrar(comando);

        ArgumentCaptor<Usuario> usuarioGuardado = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).guardar(usuarioGuardado.capture());
        Usuario usuario = usuarioGuardado.getValue();
        assertEquals(new DatosPersonales("Juan", "Pérez García", "avatar2"), usuario.getDatosPersonales());
        assertEquals(new DatosCliente(TipoCliente.PREMIUM, new Dni("12345678Z"), LocalDate.of(1996, 5, 20),
                new Telefono("612345678")), usuario.getDatosCliente());
        assertEquals("juan@uclm.es", usuario.getEmail().valor());
        assertEquals("hash-simulado", usuario.getPasswordHash());
        assertEquals(Rol.CLIENTE, usuario.getRol());
    }

    @Test
    @DisplayName("HU1-CP14: rechaza el registro si las contraseñas no coinciden")
    void rechazaContrasenasQueNoCoinciden() {
        RegistrarUsuarioCommand comando = new RegistrarUsuarioCommand("Juan", "Pérez García",
                LocalDate.of(1996, 5, 20), "12345678Z", "juan@uclm.es", null, "clave1234", "clave1235",
                TipoCliente.ESTANDAR, null, true);

        assertThrows(ContrasenaInvalidaException.class, () -> registroUsuario.registrar(comando));

        verify(usuarioRepository, never()).guardar(any(Usuario.class));
    }

    @Test
    @DisplayName("HU1-CP06: rechaza el registro si no se acepta la política de privacidad")
    void rechazaSiNoAceptaLaPrivacidad() {
        // lenient: si el servicio rechaza antes de hashear (lo correcto), este stub no se usa y Mockito no debe quejarse.
        // Sin el stub, el hash nulo haría fallar al dominio por otro motivo y el test pasaría sin comprobar la privacidad.
        lenient().when(hashContrasena.hashear("clave1234")).thenReturn("hash-simulado");
        RegistrarUsuarioCommand comando = new RegistrarUsuarioCommand("Juan", "Pérez García",
                LocalDate.of(1996, 5, 20), "12345678Z", "juan@uclm.es", null, "clave1234", "clave1234",
                TipoCliente.ESTANDAR, null, false);

        assertThrows(UsuarioInvalidoException.class, () -> registroUsuario.registrar(comando));

        verify(usuarioRepository, never()).guardar(any(Usuario.class));
    }

    @Test
    @DisplayName("HU1-CP06: guarda el consentimiento con la fecha y la versión")
    void guardaElConsentimientoConLaFechaYLaVersion() {
        when(hashContrasena.hashear("clave1234")).thenReturn("hash-simulado");

        registroUsuario.registrar(comando("juan@uclm.es", "clave1234"));

        ArgumentCaptor<Usuario> usuarioGuardado = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).guardar(usuarioGuardado.capture());
        assertEquals(new ConsentimientoPrivacidad(RELOJ.instant(), "1.0"), usuarioGuardado.getValue().getConsentimiento());
    }

    @Test
    @DisplayName("HU1-CP13: rechaza a un cliente menor de edad")
    void rechazaClienteMenorDeEdad() {
        // Comprueba que el servicio pasa al dominio la fecha del reloj: la regla vive en Usuario.nuevoCliente.
        when(hashContrasena.hashear("clave1234")).thenReturn("hash-simulado");
        LocalDate hace17Anos = LocalDate.now(RELOJ).minusYears(17);
        RegistrarUsuarioCommand comando = new RegistrarUsuarioCommand("Juan", "Pérez García", hace17Anos,
                "12345678Z", "juan@uclm.es", null, "clave1234", "clave1234", TipoCliente.ESTANDAR, null, true);

        assertThrows(UsuarioInvalidoException.class, () -> registroUsuario.registrar(comando));

        verify(usuarioRepository, never()).guardar(any(Usuario.class));
    }

    @Test
    @DisplayName("HU1-CP09: rechaza una contraseña común")
    void rechazaContrasenaComun() {
        lenient().when(hashContrasena.hashear("password1")).thenReturn("hash-simulado");

        assertThrows(ContrasenaInvalidaException.class, () -> registroUsuario.registrar(comando("juan@uclm.es", "password1")));

        verify(usuarioRepository, never()).guardar(any(Usuario.class));
    }

    @Test
    @DisplayName("HU1-CP10: rechaza una contraseña que contiene el nombre")
    void rechazaContrasenaConElNombre() {
        // El comando lleva nombre "Juan": la política lo recibe a través de DatosPersonales
        lenient().when(hashContrasena.hashear("clave-Juan-2026")).thenReturn("hash-simulado");

        assertThrows(ContrasenaInvalidaException.class, () -> registroUsuario.registrar(comando("juan@uclm.es", "clave-Juan-2026")));

        verify(usuarioRepository, never()).guardar(any(Usuario.class));
    }
}
