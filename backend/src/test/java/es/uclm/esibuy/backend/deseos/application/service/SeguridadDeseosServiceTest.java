package es.uclm.esibuy.backend.deseos.application.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

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

import es.uclm.esibuy.backend.deseos.application.port.in.GestionListaDeseosUseCase;
import es.uclm.esibuy.backend.deseos.application.port.out.ClienteEnSesionPort;
import es.uclm.esibuy.backend.deseos.application.port.out.ListaDeseosRepositoryPort;
import es.uclm.esibuy.backend.deseos.application.port.out.ProductosPort;

// Seguridad en la capa de negocio (R8): la lista de deseos exige el rol CLIENTE y que el correo recibido sea el de
// la sesión. Las anotaciones solo se evalúan con Spring, así que se usa un contexto mínimo (sin MongoDB) con el
// servicio real y dobles de prueba de los puertos de salida.
@SpringJUnitConfig(SeguridadDeseosServiceTest.Configuracion.class)
class SeguridadDeseosServiceTest {

    private static final String CLIENTE = "cliente@esibuy.test";
    private static final String OTRO_CLIENTE = "otro@esibuy.test";

    @Configuration
    @EnableMethodSecurity
    static class Configuracion {
        @Bean
        ListaDeseosRepositoryPort listaDeseosRepository() {
            return mock(ListaDeseosRepositoryPort.class);
        }

        @Bean
        ClienteEnSesionPort clienteEnSesion() {
            return mock(ClienteEnSesionPort.class);
        }

        @Bean
        ProductosPort productos() {
            return mock(ProductosPort.class);
        }

        @Bean
        GestionListaDeseosUseCase gestionListaDeseos(ListaDeseosRepositoryPort repositorio,
                ClienteEnSesionPort sesion, ProductosPort productos) {
            return new GestionListaDeseosService(repositorio, sesion, productos, Clock.systemUTC());
        }
    }

    @Autowired
    private ListaDeseosRepositoryPort listaDeseosRepository;
    @Autowired
    private ClienteEnSesionPort clienteEnSesion;
    @Autowired
    private ProductosPort productos;
    @Autowired
    private GestionListaDeseosUseCase gestionListaDeseos;

    @BeforeEach
    void limpiarDobles() {
        reset(listaDeseosRepository, clienteEnSesion, productos);
    }

    private void comprobarQueRechaza(String email) {
        assertThrows(AccessDeniedException.class, () -> gestionListaDeseos.anadir(email, "p-1"));
        verifyNoInteractions(listaDeseosRepository);
    }

    @Test
    @WithMockUser(username = CLIENTE, roles = "CLIENTE")
    @DisplayName("HU14-CP05: el cliente añade a su lista con su propio correo")
    void elClienteAnadeConSuPropioCorreo() {
        when(productos.estaDisponible("p-1")).thenReturn(true);

        assertDoesNotThrow(() -> gestionListaDeseos.anadir(CLIENTE, "p-1"));
    }

    @Test
    @WithMockUser(username = CLIENTE, roles = "CLIENTE")
    @DisplayName("HU14-CP05: un cliente no añade con el correo de otro cliente")
    void unClienteNoAnadeConElCorreoDeOtro() {
        comprobarQueRechaza(OTRO_CLIENTE);
    }

    @Test
    @WithMockUser(username = CLIENTE, roles = "VENDEDOR")
    @DisplayName("HU14-CP05: un vendedor no tiene lista de deseos")
    void unVendedorNoAnade() {
        comprobarQueRechaza(CLIENTE);
    }

    @Test
    @WithMockUser(username = CLIENTE, roles = "ADMINISTRADOR")
    @DisplayName("HU14-CP05: un administrador no tiene lista de deseos")
    void unAdministradorNoAnade() {
        comprobarQueRechaza(CLIENTE);
    }

    @Test
    @WithMockUser(username = CLIENTE, roles = "CLIENTE")
    @DisplayName("HU14-CP15: el cliente consulta su lista con su propio correo")
    void elClienteConsultaConSuPropioCorreo() {
        assertDoesNotThrow(() -> gestionListaDeseos.consultar(CLIENTE));
    }

    @Test
    @WithMockUser(username = CLIENTE, roles = "CLIENTE")
    @DisplayName("HU14-CP15: un cliente no consulta la lista con el correo de otro cliente")
    void unClienteNoConsultaConElCorreoDeOtro() {
        assertThrows(AccessDeniedException.class, () -> gestionListaDeseos.consultar(OTRO_CLIENTE));
    }

    @Test
    @WithMockUser(username = CLIENTE, roles = "VENDEDOR")
    @DisplayName("HU14-CP15: un vendedor no consulta listas de deseos")
    void unVendedorNoConsulta() {
        assertThrows(AccessDeniedException.class, () -> gestionListaDeseos.consultar(CLIENTE));
    }

    @Test
    @WithMockUser(username = CLIENTE, roles = "ADMINISTRADOR")
    @DisplayName("HU14-CP15: un administrador no consulta listas de deseos")
    void unAdministradorNoConsulta() {
        assertThrows(AccessDeniedException.class, () -> gestionListaDeseos.consultar(CLIENTE));
    }

    @Test
    @DisplayName("HU14-CP15: sin sesión no se consulta la lista")
    void sinSesionNoSeConsulta() {
        assertThrows(AuthenticationCredentialsNotFoundException.class, () -> gestionListaDeseos.consultar(CLIENTE));
    }

    @Test
    @WithMockUser(username = CLIENTE, roles = "CLIENTE")
    @DisplayName("HU14-CP28: el cliente quita de su lista con su propio correo")
    void elClienteQuitaConSuPropioCorreo() {
        assertDoesNotThrow(() -> gestionListaDeseos.quitar(CLIENTE, "p-1"));
    }

    @Test
    @WithMockUser(username = CLIENTE, roles = "CLIENTE")
    @DisplayName("HU14-CP28: un cliente no quita con el correo de otro cliente")
    void unClienteNoQuitaConElCorreoDeOtro() {
        assertThrows(AccessDeniedException.class, () -> gestionListaDeseos.quitar(OTRO_CLIENTE, "p-1"));
    }

    @Test
    @WithMockUser(username = CLIENTE, roles = "VENDEDOR")
    @DisplayName("HU14-CP28: un vendedor no quita de una lista de deseos")
    void unVendedorNoQuita() {
        assertThrows(AccessDeniedException.class, () -> gestionListaDeseos.quitar(CLIENTE, "p-1"));
    }

    @Test
    @WithMockUser(username = CLIENTE, roles = "ADMINISTRADOR")
    @DisplayName("HU14-CP28: un administrador no quita de una lista de deseos")
    void unAdministradorNoQuita() {
        assertThrows(AccessDeniedException.class, () -> gestionListaDeseos.quitar(CLIENTE, "p-1"));
    }

    @Test
    @DisplayName("HU14-CP28: sin sesión no se quita de la lista")
    void sinSesionNoSeQuita() {
        assertThrows(AuthenticationCredentialsNotFoundException.class,
                () -> gestionListaDeseos.quitar(CLIENTE, "p-1"));
    }

    @Test
    @DisplayName("HU14-CP05: sin sesión se rechaza")
    void sinSesionSeRechaza() {
        assertThrows(AuthenticationCredentialsNotFoundException.class,
                () -> gestionListaDeseos.anadir(CLIENTE, "p-1"));
        verifyNoInteractions(listaDeseosRepository);
    }
}
