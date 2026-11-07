package es.uclm.esibuy.backend.direcciones.application.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verifyNoInteractions;

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

import es.uclm.esibuy.backend.direcciones.application.port.in.DireccionCommand;
import es.uclm.esibuy.backend.direcciones.application.port.in.GestionDireccionesUseCase;
import es.uclm.esibuy.backend.direcciones.application.port.out.ClienteEnSesionPort;
import es.uclm.esibuy.backend.direcciones.application.port.out.DireccionRepositoryPort;

// Seguridad en la capa de negocio (R8): la gestión de direcciones exige el rol CLIENTE y que el correo recibido
// sea el de la sesión. Las anotaciones solo se evalúan con Spring, así que se usa un contexto mínimo (sin
// MongoDB) con el servicio real y dobles de prueba de los puertos de salida.
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: caso HU5-CP29, propuesto.
@SpringJUnitConfig(SeguridadDireccionesServiceTest.Configuracion.class)
class SeguridadDireccionesServiceTest {

    private static final String CLIENTE = "cliente@esibuy.test";
    private static final String OTRO_CLIENTE = "otro@esibuy.test";
    private static final DireccionCommand DIRECCION =
            new DireccionCommand("Ana Pérez", "Calle Mayor 1", null, "13001", "Ciudad Real", "Ciudad Real",
                    "612345678");

    @Configuration
    @EnableMethodSecurity
    static class Configuracion {
        @Bean
        DireccionRepositoryPort direccionRepository() {
            return mock(DireccionRepositoryPort.class);
        }

        @Bean
        ClienteEnSesionPort clienteEnSesion() {
            return mock(ClienteEnSesionPort.class);
        }

        @Bean
        GestionDireccionesUseCase gestionDirecciones(DireccionRepositoryPort repositorio,
                ClienteEnSesionPort sesion) {
            return new GestionDireccionesService(repositorio, sesion);
        }
    }

    @Autowired
    private DireccionRepositoryPort direccionRepository;
    @Autowired
    private ClienteEnSesionPort clienteEnSesion;
    @Autowired
    private GestionDireccionesUseCase gestionDirecciones;

    @BeforeEach
    void limpiarDobles() {
        reset(direccionRepository, clienteEnSesion);
    }

    private void comprobarQueRechaza(String email) {
        assertAll(
                () -> assertThrows(AccessDeniedException.class, () -> gestionDirecciones.listar(email)),
                () -> assertThrows(AccessDeniedException.class, () -> gestionDirecciones.anadir(email, DIRECCION)),
                () -> assertThrows(AccessDeniedException.class,
                        () -> gestionDirecciones.editar(email, "d-1", DIRECCION)),
                () -> assertThrows(AccessDeniedException.class, () -> gestionDirecciones.marcarPrincipal(email, "d-1")),
                () -> assertThrows(AccessDeniedException.class, () -> gestionDirecciones.eliminar(email, "d-1", null)),
                () -> assertThrows(AccessDeniedException.class,
                        () -> gestionDirecciones.consultarParaPedido(email, "d-1")));
        verifyNoInteractions(direccionRepository, clienteEnSesion);
    }

    @Test
    @WithMockUser(username = "vendedor@esibuy.test", roles = "VENDEDOR")
    @DisplayName("HU5-CP29: un vendedor no puede usar las direcciones de un cliente")
    void unVendedorNoUsaLasDirecciones() {
        comprobarQueRechaza("vendedor@esibuy.test");
    }

    @Test
    @WithMockUser(username = "admin@esibuy.test", roles = "ADMINISTRADOR")
    @DisplayName("HU5-CP29: un administrador no puede usar las direcciones de un cliente")
    void unAdministradorNoUsaLasDirecciones() {
        comprobarQueRechaza("admin@esibuy.test");
    }

    @Test
    @WithMockUser(username = CLIENTE, roles = "CLIENTE")
    @DisplayName("HU5-CP29: un cliente no opera con el correo de otro cliente y no se toca nada")
    void unClienteNoOperaConElCorreoDeOtro() {
        comprobarQueRechaza(OTRO_CLIENTE);
    }

    @Test
    @WithMockUser(username = CLIENTE, roles = "CLIENTE")
    @DisplayName("HU5-CP29: un cliente sí opera con su propio correo")
    void unClienteOperaConSuPropioCorreo() {
        assertAll(
                () -> assertDoesNotThrow(() -> gestionDirecciones.listar(CLIENTE)),
                () -> assertDoesNotThrow(() -> gestionDirecciones.consultarParaPedido(CLIENTE, "d-1")));
    }

    @Test
    @DisplayName("HU5-CP29: sin sesión se rechaza cualquier operación de direcciones")
    void sinSesionSeRechazaTodo() {
        assertAll(
                () -> assertThrows(AuthenticationCredentialsNotFoundException.class,
                        () -> gestionDirecciones.listar(CLIENTE)),
                () -> assertThrows(AuthenticationCredentialsNotFoundException.class,
                        () -> gestionDirecciones.anadir(CLIENTE, DIRECCION)));
        verifyNoInteractions(direccionRepository, clienteEnSesion);
    }

    @Test
    @WithMockUser(username = CLIENTE, roles = "CLIENTE")
    @DisplayName("HU5-CP32: el cliente elimina todas sus direcciones con su propio correo")
    void elClienteEliminaTodasConSuPropioCorreo() {
        assertDoesNotThrow(() -> gestionDirecciones.eliminarTodas(CLIENTE));
    }

    @Test
    @WithMockUser(username = CLIENTE, roles = "CLIENTE")
    @DisplayName("HU5-CP32: un cliente no elimina las direcciones de otro cliente")
    void unClienteNoEliminaTodasConElCorreoDeOtro() {
        assertThrows(AccessDeniedException.class, () -> gestionDirecciones.eliminarTodas(OTRO_CLIENTE));
        verifyNoInteractions(direccionRepository, clienteEnSesion);
    }

    @Test
    @WithMockUser(username = "vendedor@esibuy.test", roles = "VENDEDOR")
    @DisplayName("HU5-CP32: un vendedor no elimina direcciones de clientes")
    void unVendedorNoEliminaTodas() {
        assertThrows(AccessDeniedException.class, () -> gestionDirecciones.eliminarTodas("vendedor@esibuy.test"));
    }

    @Test
    @WithMockUser(username = "admin@esibuy.test", roles = "ADMINISTRADOR")
    @DisplayName("HU5-CP32: un administrador no elimina direcciones de clientes")
    void unAdministradorNoEliminaTodas() {
        assertThrows(AccessDeniedException.class, () -> gestionDirecciones.eliminarTodas("admin@esibuy.test"));
    }

    @Test
    @DisplayName("HU5-CP32: sin sesión no se eliminan direcciones")
    void sinSesionNoSeEliminanTodas() {
        assertThrows(AuthenticationCredentialsNotFoundException.class,
                () -> gestionDirecciones.eliminarTodas(CLIENTE));
        verifyNoInteractions(direccionRepository, clienteEnSesion);
    }
}