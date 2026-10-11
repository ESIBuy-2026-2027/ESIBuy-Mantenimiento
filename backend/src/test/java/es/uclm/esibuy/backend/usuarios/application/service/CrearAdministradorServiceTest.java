package es.uclm.esibuy.backend.usuarios.application.service;

import es.uclm.esibuy.backend.usuarios.application.port.in.CrearAdministradorCommand;
import es.uclm.esibuy.backend.usuarios.application.port.in.CrearAdministradorUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.out.HashContrasenaPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.UsuarioRepositoryPort;
import es.uclm.esibuy.backend.usuarios.domain.exception.ContrasenaInvalidaException;
import es.uclm.esibuy.backend.usuarios.domain.exception.CorreoYaRegistradoException;
import es.uclm.esibuy.backend.usuarios.domain.model.DatosPersonales;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Cómo encaja el test en la arquitectura hexagonal (HU21):
// - CrearAdministradorUseCase (application.port.in): puerto de entrada.
// - CrearAdministradorCommand (application.port.in): datos de entrada del caso de uso.
// - CrearAdministradorService (application.service): implementa el caso de uso (bajo prueba).
@ExtendWith(MockitoExtension.class)
class CrearAdministradorServiceTest {

    @Mock
    private UsuarioRepositoryPort usuarioRepository;

    @Mock
    private HashContrasenaPort hashContrasena;

    private CrearAdministradorUseCase crearAdministrador; // Tipado como el puerto de entrada

    @BeforeEach
    void crearServicio() {
        crearAdministrador = new CrearAdministradorService(usuarioRepository, hashContrasena);
    }

    // Comando de un administrador válido para reutilizar en los tests.
    private static CrearAdministradorCommand comando(String email, String password) {
        return new CrearAdministradorCommand("Nora", "Díaz Ruiz", email, password);
    }

    @Test
    @DisplayName("HU21-CP01: crea el administrador en estado ACTIVO y con la contraseña cifrada")
    void creaAdministradorActivoYConContrasenaCifrada() {
        when(hashContrasena.hashear("Adm1n.S3gur0_2026!")).thenReturn("hash-simulado");
        CrearAdministradorCommand comando = comando("nora@esibuy.test", "Adm1n.S3gur0_2026!");

        crearAdministrador.crear(comando);

        ArgumentCaptor<Usuario> usuarioGuardado = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).guardar(usuarioGuardado.capture());
        
        Usuario usuario = usuarioGuardado.getValue();
        assertEquals(new DatosPersonales("Nora", "Díaz Ruiz", null), usuario.getDatosPersonales());
        assertEquals("nora@esibuy.test", usuario.getEmail().valor());
        assertEquals("hash-simulado", usuario.getPasswordHash());
        assertEquals(Rol.ADMINISTRADOR, usuario.getRol());
        assertEquals("ACTIVO", usuario.getEstado()); // La cuenta nace activa (AC2)
        assertNull(usuario.getDatosCliente()); // Un admin no tiene datos de cliente
    }

    @Test
    @DisplayName("HU21-CP02: rechaza la creación si el correo ya está registrado y no guarda nada")
    void rechazaEmailYaRegistrado() {
        when(usuarioRepository.existePorCorreo("nora@esibuy.test")).thenReturn(true);
        CrearAdministradorCommand comando = comando("nora@esibuy.test", "Adm1n.S3gur0_2026!");

        assertThrows(CorreoYaRegistradoException.class, () -> crearAdministrador.crear(comando));

        verify(usuarioRepository, never()).guardar(any(Usuario.class));
    }

    @Test
    @DisplayName("HU21-CP03: delega a la política de contraseñas y rechaza una muy corta")
    void rechazaContrasenaMuyCorta() {
        // lenient: si el servicio rechaza la contraseña (validación de dominio), no llegará a hashear
        lenient().when(hashContrasena.hashear(any())).thenReturn("hash-simulado");
        CrearAdministradorCommand comando = comando("nora@esibuy.test", "1234");

        assertThrows(ContrasenaInvalidaException.class, () -> crearAdministrador.crear(comando));

        verify(usuarioRepository, never()).guardar(any(Usuario.class));
    }

    @Test
    @DisplayName("HU21-CP04: rechaza una contraseña que contiene el nombre del administrador")
    void rechazaContrasenaConElNombre() {
        // La política de contraseñas recibe los DatosPersonales y debe saltar
        lenient().when(hashContrasena.hashear(any())).thenReturn("hash-simulado");
        CrearAdministradorCommand comando = comando("nora@esibuy.test", "clave-Nora-2026");

        assertThrows(ContrasenaInvalidaException.class, () -> crearAdministrador.crear(comando));

        verify(usuarioRepository, never()).guardar(any(Usuario.class));
    }
}