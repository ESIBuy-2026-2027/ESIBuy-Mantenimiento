package es.uclm.esibuy.backend.usuarios.application.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.Clock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import es.uclm.esibuy.backend.usuarios.application.port.in.AdministracionUsuariosUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.in.CrearAdministradorCommand;
import es.uclm.esibuy.backend.usuarios.application.port.in.CrearAdministradorUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.in.EditarUsuarioCommand;
import es.uclm.esibuy.backend.usuarios.application.port.in.ListadoUsuariosUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.in.ListarUsuariosQuery;
import es.uclm.esibuy.backend.usuarios.application.port.out.HashContrasenaPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.UsuarioRepositoryPort;
import es.uclm.esibuy.backend.usuarios.domain.exception.UsuarioNoEncontradoException;

// Seguridad en la capa de negocio (R8): la administración de usuarios exige el rol ADMINISTRADOR y, cuando recibe
// el correo del administrador, que sea el de la sesión. consultarNombreComercial es del cliente (lo usa el carrito).
// Las anotaciones solo se evalúan con Spring, así que se usa un contexto mínimo (sin MongoDB) con los servicios
// reales y dobles de prueba de los puertos de salida.
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: casos HU20-CP07 y HU42-CP08, propuestos.
@SpringJUnitConfig(SeguridadAdministracionUsuariosServiceTest.Configuracion.class)
class SeguridadAdministracionUsuariosServiceTest {

    private static final String ADMIN = "admin@esibuy.test";
    private static final String OTRO_ADMIN = "otro-admin@esibuy.test";
    private static final EditarUsuarioCommand EDICION =
            new EditarUsuarioCommand(null, null, null, null, null, null, null, null, null);
    private static final ListarUsuariosQuery CONSULTA = new ListarUsuariosQuery(null, null, null, 0, 10);
    private static final CrearAdministradorCommand ALTA =
            new CrearAdministradorCommand("Ana", "López", "ana@esibuy.test", null, "Clave-Segura-123");

    @Configuration
    @EnableMethodSecurity
    static class Configuracion {
        @Bean
        UsuarioRepositoryPort usuarioRepository() {
            return mock(UsuarioRepositoryPort.class);
        }

        @Bean
        HashContrasenaPort hashContrasena() {
            return mock(HashContrasenaPort.class);
        }

        @Bean
        AdministracionUsuariosUseCase administracionUsuarios(UsuarioRepositoryPort repositorio) {
            return new AdministracionUsuariosService(repositorio, Clock.systemUTC());
        }

        @Bean
        ListadoUsuariosUseCase listadoUsuarios(UsuarioRepositoryPort repositorio) {
            return new ListadoUsuariosService(repositorio);
        }

        @Bean
        CrearAdministradorUseCase crearAdministrador(UsuarioRepositoryPort repositorio, HashContrasenaPort hash) {
            return new CrearAdministradorService(repositorio, hash);
        }
    }

    @Autowired
    private UsuarioRepositoryPort usuarioRepository;
    @Autowired
    private HashContrasenaPort hashContrasena;
    @Autowired
    private AdministracionUsuariosUseCase administracionUsuarios;
    @Autowired
    private ListadoUsuariosUseCase listadoUsuarios;
    @Autowired
    private CrearAdministradorUseCase crearAdministrador;

    @BeforeEach
    void limpiarDobles() {
        reset(usuarioRepository, hashContrasena);
    }

    // Lo que hace el administrador sobre las cuentas; consultarNombreComercial es del cliente y va aparte
    private void comprobarQueRechazaLasOperacionesDelAdministrador(String email) {
        assertAll(
                () -> assertThrows(AccessDeniedException.class, () -> administracionUsuarios.activar("u-1")),
                () -> assertThrows(AccessDeniedException.class, () -> administracionUsuarios.consultar("u-1")),
                () -> assertThrows(AccessDeniedException.class, () -> administracionUsuarios.consultarVendedor("u-1")),
                () -> assertThrows(AccessDeniedException.class, () -> administracionUsuarios.editar("u-1", EDICION)),
                () -> assertThrows(AccessDeniedException.class, () -> administracionUsuarios.bloquear("u-1", email)),
                () -> assertThrows(AccessDeniedException.class, () -> administracionUsuarios.activarVendedor("u-1")),
                () -> assertThrows(AccessDeniedException.class, () -> administracionUsuarios.bloquearVendedor("u-1")),
                () -> assertThrows(AccessDeniedException.class,
                        () -> administracionUsuarios.eliminarAdministrador("u-1", email)),
                () -> assertThrows(AccessDeniedException.class, () -> listadoUsuarios.listar(CONSULTA)),
                () -> assertThrows(AccessDeniedException.class, () -> crearAdministrador.crear(ALTA)));
        verifyNoInteractions(usuarioRepository, hashContrasena);
    }
 
    private void comprobarQueRechazaLaPreguntaPorLosVendedores() {
        assertThrows(AccessDeniedException.class, () -> administracionUsuarios.existenVendedoresConCategoria("c-1"));
        verifyNoInteractions(usuarioRepository);
    }

    @Test
    @WithMockUser(username = "cliente@esibuy.test", roles = "CLIENTE")
    @DisplayName("HU20-CP07: un cliente no puede usar las operaciones de administración de usuarios")
    void unClienteNoAdministraUsuarios() {
        comprobarQueRechazaLasOperacionesDelAdministrador("cliente@esibuy.test");
    }

    @Test
    @WithMockUser(username = "vendedor@esibuy.test", roles = "VENDEDOR")
    @DisplayName("HU20-CP07: un vendedor no puede usar las operaciones de administración de usuarios")
    void unVendedorNoAdministraUsuarios() {
        comprobarQueRechazaLasOperacionesDelAdministrador("vendedor@esibuy.test");
    }

    @Test
    @WithMockUser(username = ADMIN, roles = "ADMINISTRADOR")
    @DisplayName("HU20-CP07: un administrador no bloquea ni elimina con el correo de otro administrador")
    void unAdministradorNoOperaConElCorreoDeOtro() {
        assertAll(
                () -> assertThrows(AccessDeniedException.class,
                        () -> administracionUsuarios.bloquear("u-1", OTRO_ADMIN)),
                () -> assertThrows(AccessDeniedException.class,
                        () -> administracionUsuarios.eliminarAdministrador("u-1", OTRO_ADMIN)));
        verifyNoInteractions(usuarioRepository);
    }

    @Test
    @WithMockUser(username = ADMIN, roles = "ADMINISTRADOR")
    @DisplayName("HU20-CP07: un administrador sí usa las operaciones (un usuario que no existe sigue siendo 404)")
    void unAdministradorUsaLasOperaciones() {
        assertAll(
                () -> assertThrows(UsuarioNoEncontradoException.class, () -> administracionUsuarios.consultar("u-1")),
                () -> assertThrows(UsuarioNoEncontradoException.class,
                        () -> administracionUsuarios.bloquear("u-1", ADMIN)),
                () -> assertThrows(UsuarioNoEncontradoException.class,
                        () -> administracionUsuarios.eliminarAdministrador("u-1", ADMIN)),
                () -> assertDoesNotThrow(() -> listadoUsuarios.listar(CONSULTA)));
    }

    @Test
    @WithMockUser(username = "cliente@esibuy.test", roles = "CLIENTE")
    @DisplayName("HU20-CP07: un cliente sí consulta el nombre comercial de un vendedor (lo usa el carrito)")
    void unClienteConsultaElNombreComercial() {
        assertThrows(UsuarioNoEncontradoException.class, () -> administracionUsuarios.consultarNombreComercial("v-1"));
    }

    @Test
    @WithMockUser(username = ADMIN, roles = "ADMINISTRADOR")
    @DisplayName("HU20-CP07: un administrador no consulta el nombre comercial, que es del flujo de compra del cliente")
    void unAdministradorNoConsultaElNombreComercial() {
        assertThrows(AccessDeniedException.class, () -> administracionUsuarios.consultarNombreComercial("v-1"));
    }

    @Test
    @DisplayName("HU20-CP07: sin sesión se rechaza cualquier operación de administración de usuarios")
    void sinSesionSeRechazaTodo() {
        assertAll(
                () -> assertThrows(AuthenticationCredentialsNotFoundException.class,
                        () -> administracionUsuarios.consultar("u-1")),
                () -> assertThrows(AuthenticationCredentialsNotFoundException.class,
                        () -> listadoUsuarios.listar(CONSULTA)),
                () -> assertThrows(AuthenticationCredentialsNotFoundException.class,
                        () -> crearAdministrador.crear(ALTA)));
        verifyNoInteractions(usuarioRepository, hashContrasena);
    }

    @Test
    @WithMockUser(username = "cliente@esibuy.test", roles = "CLIENTE")
    @DisplayName("HU42-CP08: un cliente no puede preguntar si hay vendedores con una categoría como principal")
    void unClienteNoPreguntaPorLosVendedoresDeUnaCategoria() {
        comprobarQueRechazaLaPreguntaPorLosVendedores();
    }

    @Test
    @WithMockUser(username = "vendedor@esibuy.test", roles = "VENDEDOR")
    @DisplayName("HU42-CP08: un vendedor no puede preguntar si hay vendedores con una categoría como principal")
    void unVendedorNoPreguntaPorLosVendedoresDeUnaCategoria() {
        comprobarQueRechazaLaPreguntaPorLosVendedores();
    }

    @Test
    @WithMockUser(username = ADMIN, roles = "ADMINISTRADOR")
    @DisplayName("HU42-CP08: un administrador sí pregunta si hay vendedores con una categoría como principal")
    void unAdministradorPreguntaPorLosVendedoresDeUnaCategoria() {
        assertDoesNotThrow(() -> administracionUsuarios.existenVendedoresConCategoria("c-1"));
    }

    @Test
    @DisplayName("HU42-CP08: sin sesión se rechaza la pregunta por los vendedores de una categoría")
    void sinSesionSeRechazaLaPreguntaPorLosVendedores() {
        assertThrows(AuthenticationCredentialsNotFoundException.class,
                () -> administracionUsuarios.existenVendedoresConCategoria("c-1"));
        verifyNoInteractions(usuarioRepository);
    }
}