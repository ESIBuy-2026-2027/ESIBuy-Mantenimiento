package es.uclm.esibuy.backend.alertas.application.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.List;

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

import es.uclm.esibuy.backend.alertas.application.port.in.CambioProducto;
import es.uclm.esibuy.backend.alertas.application.port.in.CambioProducto.Situacion;
import es.uclm.esibuy.backend.alertas.application.port.in.ConsultaAlertasUseCase;
import es.uclm.esibuy.backend.alertas.application.port.in.GeneracionAlertasUseCase;
import es.uclm.esibuy.backend.alertas.application.port.out.AlertaRepositoryPort;
import es.uclm.esibuy.backend.alertas.application.port.out.ClienteEnSesionPort;
import es.uclm.esibuy.backend.alertas.application.port.out.FavoritosPort;
import es.uclm.esibuy.backend.alertas.application.port.out.TiposClientePort;

// Seguridad en la capa de negocio (R8): un cliente solo consulta sus alertas, y las alertas solo se generan desde un
// cambio de producto hecho por un vendedor (modificar o activar) o por un cliente (cancelar un pedido repone stock).
// Las anotaciones solo se evalúan con Spring: contexto mínimo (sin MongoDB) con los servicios reales y dobles.
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: caso HU529-CP10, propuesto.
@SpringJUnitConfig(SeguridadAlertasServiceTest.Configuracion.class)
class SeguridadAlertasServiceTest {

    private static final String CLIENTE = "cliente@esibuy.test";
    private static final CambioProducto SIN_CAMBIOS = new CambioProducto("p-1", "Teclado",
            new Situacion(BigDecimal.TEN, BigDecimal.TEN, true), new Situacion(BigDecimal.TEN, BigDecimal.TEN, true));

    @Configuration
    @EnableMethodSecurity
    static class Configuracion {
        @Bean
        AlertaRepositoryPort alertaRepository() {
            return mock(AlertaRepositoryPort.class);
        }

        @Bean
        ClienteEnSesionPort clienteEnSesion() {
            return mock(ClienteEnSesionPort.class);
        }

        @Bean
        FavoritosPort favoritos() {
            return mock(FavoritosPort.class);
        }

        @Bean
        TiposClientePort tiposCliente() {
            return mock(TiposClientePort.class);
        }

        @Bean
        ConsultaAlertasUseCase consulta(AlertaRepositoryPort repositorio, ClienteEnSesionPort sesion) {
            return new ConsultaAlertasService(repositorio, sesion);
        }

        @Bean
        GeneracionAlertasUseCase generacion(AlertaRepositoryPort repositorio, FavoritosPort favoritos,
                TiposClientePort tipos) {
            return new GeneracionAlertasService(repositorio, favoritos, tipos, Clock.systemUTC());
        }
    }

    @Autowired
    private AlertaRepositoryPort alertaRepository;
    @Autowired
    private ClienteEnSesionPort clienteEnSesion;
    @Autowired
    private ConsultaAlertasUseCase consulta;
    @Autowired
    private GeneracionAlertasUseCase generacion;

    @BeforeEach
    void limpiarDobles() {
        reset(alertaRepository, clienteEnSesion);
    }

    @Test
    @WithMockUser(username = CLIENTE, roles = "CLIENTE")
    @DisplayName("HU529-CP10: el cliente consulta sus propias alertas")
    void elClienteConsultaLasSuyas() {
        when(clienteEnSesion.idPorCorreo(CLIENTE)).thenReturn("c-1");
        when(alertaRepository.buscarPorCliente("c-1")).thenReturn(List.of());

        assertDoesNotThrow(() -> consulta.consultar(CLIENTE));
    }

    @Test
    @WithMockUser(username = CLIENTE, roles = "CLIENTE")
    @DisplayName("HU529-CP10: un cliente no puede consultar las alertas de otro cliente")
    void unClienteNoConsultaLasDeOtro() {
        assertThrows(AccessDeniedException.class, () -> consulta.consultar("otro@esibuy.test"));
        verifyNoInteractions(clienteEnSesion, alertaRepository);
    }

    @Test
    @WithMockUser(username = CLIENTE, roles = "VENDEDOR")
    @DisplayName("HU529-CP10: un vendedor no consulta alertas de clientes, aunque use el mismo correo")
    void unVendedorNoConsultaAlertas() {
        assertThrows(AccessDeniedException.class, () -> consulta.consultar(CLIENTE));
    }

    @Test
    @WithMockUser(username = CLIENTE, roles = "ADMINISTRADOR")
    @DisplayName("HU529-CP10: un administrador no consulta alertas de clientes")
    void unAdministradorNoConsultaAlertas() {
        assertThrows(AccessDeniedException.class, () -> consulta.consultar(CLIENTE));
    }

    @Test
    @DisplayName("HU529-CP10: sin sesión no se consultan ni se generan alertas")
    void sinSesionSeRechaza() {
        assertThrows(AuthenticationCredentialsNotFoundException.class, () -> consulta.consultar(CLIENTE));
        assertThrows(AuthenticationCredentialsNotFoundException.class, () -> generacion.notificarCambio(SIN_CAMBIOS));
    }

    @Test
    @WithMockUser(username = "vendedor@esibuy.test", roles = "VENDEDOR")
    @DisplayName("HU529-CP10: un vendedor genera alertas al cambiar su producto")
    void unVendedorGeneraAlertas() {
        assertDoesNotThrow(() -> generacion.notificarCambio(SIN_CAMBIOS));
    }

    @Test
    @WithMockUser(username = CLIENTE, roles = "CLIENTE")
    @DisplayName("HU529-CP10: un cliente genera alertas al reponerse stock por la cancelación de su pedido")
    void unClienteGeneraAlertas() {
        assertDoesNotThrow(() -> generacion.notificarCambio(SIN_CAMBIOS));
    }

    @Test
    @WithMockUser(username = "admin@esibuy.test", roles = "ADMINISTRADOR")
    @DisplayName("HU529-CP10: un administrador no genera alertas: solo lee los productos de los vendedores")
    void unAdministradorNoGeneraAlertas() {
        assertThrows(AccessDeniedException.class, () -> generacion.notificarCambio(SIN_CAMBIOS));
    }
}
