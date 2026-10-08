package es.uclm.esibuy.backend.usuarios.application.service;

import es.uclm.esibuy.backend.usuarios.application.port.in.RegistrarUsuarioCommand;
import es.uclm.esibuy.backend.usuarios.application.port.in.RegistroUsuarioUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.out.HashContrasenaPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.UsuarioRepositoryPort;
import es.uclm.esibuy.backend.usuarios.domain.exception.ContrasenaInvalidaException;
import es.uclm.esibuy.backend.usuarios.domain.exception.CorreoYaRegistradoException;
import es.uclm.esibuy.backend.usuarios.domain.model.Rol;
import es.uclm.esibuy.backend.usuarios.domain.model.Usuario;
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
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
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

    @BeforeEach
    void crearServicio() {
        registroUsuario = new RegistroUsuarioService(usuarioRepository, hashContrasena);
    }

    @Test
    @DisplayName("HU1-CP01: registra al cliente con rol CLIENTE")
    void registraClienteConRolCliente() {
        when(hashContrasena.hashear("clave1234")).thenReturn("hash-simulado");
        RegistrarUsuarioCommand comando = new RegistrarUsuarioCommand("juan@uclm.es", "clave1234");

        registroUsuario.registrar(comando);

        ArgumentCaptor<Usuario> usuarioGuardado = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).guardar(usuarioGuardado.capture());
        assertEquals(Rol.CLIENTE, usuarioGuardado.getValue().getRol());
    }

    @Test
    @DisplayName("HU1-CP02: guarda la contraseña hasheada, nunca en claro")
    void guardaContrasenaHasheada() {
        when(hashContrasena.hashear("clave1234")).thenReturn("hash-simulado");
        RegistrarUsuarioCommand comando = new RegistrarUsuarioCommand("juan@uclm.es", "clave1234");

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
        when(hashContrasena.hashear("12345678")).thenReturn("hash-simulado");
        RegistrarUsuarioCommand comando = new RegistrarUsuarioCommand("juan@uclm.es", "12345678");

        registroUsuario.registrar(comando);

        verify(usuarioRepository).guardar(any(Usuario.class));
    }

    @Test
    @DisplayName("HU1-CP05: rechaza una contraseña de menos de 8 caracteres")
    void rechazaContrasenaDeMenosDe8Caracteres() {
        // Frontera: 7 caracteres → excepción
        RegistrarUsuarioCommand comando = new RegistrarUsuarioCommand("juan@uclm.es", "1234567");

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
        RegistrarUsuarioCommand comando = new RegistrarUsuarioCommand("juan@uclm.es", "clave1234");

        assertThrows(CorreoYaRegistradoException.class, () -> registroUsuario.registrar(comando));

        verify(usuarioRepository, never()).guardar(any(Usuario.class));
    }

    @Test
    @DisplayName("HU1-CP05: rechaza una contraseña nula y no guarda nada")
    void rechazaContrasenaNula() {
        RegistrarUsuarioCommand comando = new RegistrarUsuarioCommand("juan@uclm.es", null);

        assertThrows(ContrasenaInvalidaException.class, () -> registroUsuario.registrar(comando));

        verify(usuarioRepository, never()).guardar(any(Usuario.class));
    }

    @Test
    @DisplayName("HU1-CP01: registra un cliente con todos sus datos")
    void registraClienteConTodosSusDatos() {
        // TODO: comando completo (nombre, apellidos, fecha de nacimiento, dni, email, teléfono, password x2, tipo, imagen, acepta privacidad).
        // TODO: el usuario guardado tiene esos datos personales y de cliente, rol CLIENTE y tipo ESTANDAR.
        fail("Funcionalidad no implementada");
    }

    @Test
    @DisplayName("HU1-CP14: rechaza el registro si las contraseñas no coinciden")
    void rechazaContrasenasQueNoCoinciden() {
        // TODO: password y repeticionPassword distintas -> ContrasenaInvalidaException y no se guarda nada.
        fail("Funcionalidad no implementada");
    }

    @Test
    @DisplayName("HU1-CP06: rechaza el registro si no se acepta la política de privacidad")
    void rechazaSiNoAceptaLaPrivacidad() {
        // TODO: aceptaPrivacidad = false -> UsuarioInvalidoException y no se guarda nada.
        fail("Funcionalidad no implementada");
    }

    @Test
    @DisplayName("HU1-CP06: guarda el consentimiento con la fecha y la versión")
    void guardaElConsentimientoConLaFechaYLaVersion() {
        // TODO: el usuario guardado tiene ConsentimientoPrivacidad con el instante del Clock fijo y la versión "1.0".
        fail("Funcionalidad no implementada");
    }

    @Test
    @DisplayName("HU1-CP13: rechaza a un cliente menor de edad")
    void rechazaClienteMenorDeEdad() {
        // TODO: fecha de nacimiento de hace 17 años según el Clock fijo -> UsuarioInvalidoException y no se guarda nada.
        // TODO: (pasa sin cambios en producción tras la fase 1: comprueba que el servicio pasa al dominio la fecha del Clock).
        fail("Funcionalidad no implementada");
    }

    @Test
    @DisplayName("HU1-CP09: rechaza una contraseña común")
    void rechazaContrasenaComun() {
        // TODO: password "password1" (lista negra) -> ContrasenaInvalidaException y no se guarda nada. AB#226
        fail("Funcionalidad no implementada");
    }

    @Test
    @DisplayName("HU1-CP10: rechaza una contraseña que contiene el nombre")
    void rechazaContrasenaConElNombre() {
        // TODO: password que contiene el nombre del comando -> ContrasenaInvalidaException y no se guarda nada. AB#226
        fail("Funcionalidad no implementada");
    }
}
