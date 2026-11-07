package es.uclm.esibuy.backend.metodospago.application.service;

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

import es.uclm.esibuy.backend.metodospago.application.port.in.AnadirMetodoPagoCommand;
import es.uclm.esibuy.backend.metodospago.application.port.in.GestionMetodosPagoUseCase;
import es.uclm.esibuy.backend.metodospago.application.port.out.CifradoDatosPagoPort;
import es.uclm.esibuy.backend.metodospago.application.port.out.ClienteEnSesionPort;
import es.uclm.esibuy.backend.metodospago.application.port.out.MetodoPagoRepositoryPort;
import es.uclm.esibuy.backend.metodospago.domain.model.TipoMetodoPago;

// Seguridad en la capa de negocio (R8): la gestión de métodos de pago exige el rol CLIENTE y que el correo
// recibido sea el de la sesión. Las anotaciones solo se evalúan con Spring, así que se usa un contexto mínimo
// (sin MongoDB) con el servicio real y dobles de prueba de los puertos de salida.
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: caso HU6-CP37, propuesto.
@SpringJUnitConfig(SeguridadMetodosPagoServiceTest.Configuracion.class)
class SeguridadMetodosPagoServiceTest {

    private static final String CLIENTE = "cliente@esibuy.test";
    private static final String OTRO_CLIENTE = "otro@esibuy.test";

    @Configuration
    @EnableMethodSecurity
    static class Configuracion {
        @Bean
        MetodoPagoRepositoryPort metodoPagoRepository() {
            return mock(MetodoPagoRepositoryPort.class);
        }

        @Bean
        CifradoDatosPagoPort cifradoDatosPago() {
            return mock(CifradoDatosPagoPort.class);
        }

        @Bean
        ClienteEnSesionPort clienteEnSesion() {
            return mock(ClienteEnSesionPort.class);
        }

        @Bean
        GestionMetodosPagoUseCase gestionMetodosPago(MetodoPagoRepositoryPort repositorio,
                CifradoDatosPagoPort cifrado, ClienteEnSesionPort sesion) {
            return new GestionMetodosPagoService(repositorio, cifrado, sesion, Clock.systemUTC());
        }
    }

    @Autowired
    private MetodoPagoRepositoryPort metodoPagoRepository;
    @Autowired
    private CifradoDatosPagoPort cifradoDatosPago;
    @Autowired
    private ClienteEnSesionPort clienteEnSesion;
    @Autowired
    private GestionMetodosPagoUseCase gestionMetodosPago;

    @BeforeEach
    void limpiarDobles() {
        reset(metodoPagoRepository, cifradoDatosPago, clienteEnSesion);
    }

    private static AnadirMetodoPagoCommand altaDe(String email) {
        return new AnadirMetodoPagoCommand(email, TipoMetodoPago.BIZUM, null, null, null, null, "612345678");
    }

    private void comprobarQueRechaza(String email) {
        AnadirMetodoPagoCommand alta = altaDe(email);
        assertAll(
                () -> assertThrows(AccessDeniedException.class, () -> gestionMetodosPago.listar(email)),
                () -> assertThrows(AccessDeniedException.class, () -> gestionMetodosPago.anadir(alta)),
                () -> assertThrows(AccessDeniedException.class,
                        () -> gestionMetodosPago.marcarPredeterminado(email, "m-1")),
                () -> assertThrows(AccessDeniedException.class, () -> gestionMetodosPago.eliminar(email, "m-1", null)),
                () -> assertThrows(AccessDeniedException.class, () -> gestionMetodosPago.esDelCliente(email, "m-1")));
        verifyNoInteractions(metodoPagoRepository, cifradoDatosPago, clienteEnSesion);
    }

    @Test
    @WithMockUser(username = "vendedor@esibuy.test", roles = "VENDEDOR")
    @DisplayName("HU6-CP37: un vendedor no puede usar los métodos de pago de un cliente")
    void unVendedorNoUsaLosMetodosDePago() {
        comprobarQueRechaza("vendedor@esibuy.test");
    }

    @Test
    @WithMockUser(username = "admin@esibuy.test", roles = "ADMINISTRADOR")
    @DisplayName("HU6-CP37: un administrador no puede usar los métodos de pago de un cliente")
    void unAdministradorNoUsaLosMetodosDePago() {
        comprobarQueRechaza("admin@esibuy.test");
    }

    @Test
    @WithMockUser(username = CLIENTE, roles = "CLIENTE")
    @DisplayName("HU6-CP37: un cliente no opera con el correo de otro cliente y no se toca nada")
    void unClienteNoOperaConElCorreoDeOtro() {
        comprobarQueRechaza(OTRO_CLIENTE);
    }

    @Test
    @WithMockUser(username = CLIENTE, roles = "CLIENTE")
    @DisplayName("HU6-CP37: un cliente sí opera con su propio correo")
    void unClienteOperaConSuPropioCorreo() {
        assertAll(
                () -> assertDoesNotThrow(() -> gestionMetodosPago.listar(CLIENTE)),
                () -> assertDoesNotThrow(() -> gestionMetodosPago.esDelCliente(CLIENTE, "m-1")));
    }

    @Test
    @DisplayName("HU6-CP37: sin sesión se rechaza cualquier operación de métodos de pago")
    void sinSesionSeRechazaTodo() {
        AnadirMetodoPagoCommand alta = altaDe(CLIENTE);
        assertAll(
                () -> assertThrows(AuthenticationCredentialsNotFoundException.class,
                        () -> gestionMetodosPago.listar(CLIENTE)),
                () -> assertThrows(AuthenticationCredentialsNotFoundException.class,
                        () -> gestionMetodosPago.anadir(alta)));
        verifyNoInteractions(metodoPagoRepository, cifradoDatosPago, clienteEnSesion);
    }

    @Test
    @WithMockUser(username = CLIENTE, roles = "CLIENTE")
    @DisplayName("HU6-CP40: el cliente elimina todos sus métodos de pago con su propio correo")
    void elClienteEliminaTodosConSuPropioCorreo() {
        assertDoesNotThrow(() -> gestionMetodosPago.eliminarTodos(CLIENTE));
    }

    @Test
    @WithMockUser(username = CLIENTE, roles = "CLIENTE")
    @DisplayName("HU6-CP40: un cliente no elimina los métodos de pago de otro cliente")
    void unClienteNoEliminaTodosConElCorreoDeOtro() {
        assertThrows(AccessDeniedException.class, () -> gestionMetodosPago.eliminarTodos(OTRO_CLIENTE));
        verifyNoInteractions(metodoPagoRepository, cifradoDatosPago, clienteEnSesion);
    }

    @Test
    @WithMockUser(username = "vendedor@esibuy.test", roles = "VENDEDOR")
    @DisplayName("HU6-CP40: un vendedor no elimina métodos de pago de clientes")
    void unVendedorNoEliminaTodos() {
        assertThrows(AccessDeniedException.class, () -> gestionMetodosPago.eliminarTodos("vendedor@esibuy.test"));
    }

    @Test
    @WithMockUser(username = "admin@esibuy.test", roles = "ADMINISTRADOR")
    @DisplayName("HU6-CP40: un administrador no elimina métodos de pago de clientes")
    void unAdministradorNoEliminaTodos() {
        assertThrows(AccessDeniedException.class, () -> gestionMetodosPago.eliminarTodos("admin@esibuy.test"));
    }

    @Test
    @DisplayName("HU6-CP40: sin sesión no se eliminan métodos de pago")
    void sinSesionNoSeEliminanTodos() {
        assertThrows(AuthenticationCredentialsNotFoundException.class,
                () -> gestionMetodosPago.eliminarTodos(CLIENTE));
        verifyNoInteractions(metodoPagoRepository, cifradoDatosPago, clienteEnSesion);
    }
}