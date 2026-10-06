package es.uclm.esibuy.backend.usuarios.application.service;

import es.uclm.esibuy.backend.usuarios.application.port.in.RegistrarUsuarioCommand;
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
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.verify;

// Cómo encaja el test en la arquitectura hexagonal:
// - RegistrarUsuarioCommand (application.port.in): datos de entrada del caso de uso.
//   El controlador REST traducirá su DTO a este comando; el núcleo no conoce los DTO.
// - RegistroUsuarioService (application.service): implementa el caso de uso. Es la clase bajo prueba.
// - UsuarioRepositoryPort (application.port.out): lo que el servicio necesita del exterior.
//   En producción lo implementará el adaptador de MongoDB; aquí lo sustituye un mock de Mockito.
// - Usuario y Rol (domain.model): Java puro, sin Spring ni MongoDB.

@ExtendWith(MockitoExtension.class)
class RegistroUsuarioServiceTest {

    @Mock
    private UsuarioRepositoryPort usuarioRepository;

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
        // TODO: registrar un usuario con contraseña "clave1234".
        // TODO: comprobar que se guarda el hash devuelto por el puerto de hash
        //       y nunca la contraseña en claro.
        fail("Funcionalidad no implementada");
    }

    @Test
    void permiteContrasenaDeExactamente8Caracteres() {
        // HU1-CP05 (frontera: 8 caracteres → permitido)
        // TODO: registrar un usuario con contraseña de 8 caracteres.
        // TODO: comprobar que el usuario se guarda.
        fail("Funcionalidad no implementada");
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