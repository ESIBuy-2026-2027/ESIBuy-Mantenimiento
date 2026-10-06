package es.uclm.esibuy.backend.usuarios.application.service;

import es.uclm.esibuy.backend.usuarios.application.port.in.RegistrarUsuarioCommand;
import es.uclm.esibuy.backend.usuarios.application.port.out.HashContrasenaPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.UsuarioRepositoryPort;
import es.uclm.esibuy.backend.usuarios.domain.model.Rol;
import es.uclm.esibuy.backend.usuarios.domain.model.Usuario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Cómo encaja el test en la arquitectura hexagonal:
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

    @InjectMocks
    private RegistroUsuarioService registroUsuarioService;

    @Test
    void registraClienteConRolCliente() {
        // HU1-CP01
        // PENDIENTE DE REVISAR CON EL EQUIPO: ¿también estado PENDIENTE?
        RegistrarUsuarioCommand comando = new RegistrarUsuarioCommand("juan@uclm.es", "clave1234");

        registroUsuarioService.registrar(comando);

        ArgumentCaptor<Usuario> usuarioGuardado = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).guardar(usuarioGuardado.capture());
        assertEquals(Rol.CLIENTE, usuarioGuardado.getValue().getRol());
    }

    @Test
    void guardaContrasenaHasheada() {
        // HU1-CP02
        when(hashContrasena.hashear("clave1234")).thenReturn("hash-simulado");
        RegistrarUsuarioCommand comando = new RegistrarUsuarioCommand("juan@uclm.es", "clave1234");

        registroUsuarioService.registrar(comando);

        ArgumentCaptor<Usuario> usuarioGuardado = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).guardar(usuarioGuardado.capture());
        assertEquals("hash-simulado", usuarioGuardado.getValue().getContrasenaHash());
        assertNotEquals("clave1234", usuarioGuardado.getValue().getContrasenaHash());
    }

    @Test
    void permiteContrasenaDeExactamente8Caracteres() {
        // HU1-CP05 (frontera: 8 caracteres → permitido)
        RegistrarUsuarioCommand comando = new RegistrarUsuarioCommand("juan@uclm.es", "12345678");

        registroUsuarioService.registrar(comando);

        verify(usuarioRepository).guardar(any(Usuario.class));
    }

    @Test
    void rechazaContrasenaDeMenosDe8Caracteres() {
        // HU1-CP05 (frontera: 7 caracteres → excepción)
        // TODO: intentar registrar con contraseña de 7 caracteres.
        // TODO: comprobar que lanza una excepción de negocio y no guarda nada.
        fail("Funcionalidad no implementada");
    }

    @Test
    void rechazaEmailYaRegistrado() {
        // HU1-CP03
        // TODO: simular que el email ya existe en el repositorio.
        // TODO: comprobar que lanza una excepción de negocio y no guarda nada.
        fail("Funcionalidad no implementada");
    }
}