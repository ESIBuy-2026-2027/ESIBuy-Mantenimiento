package es.uclm.esibuy.backend.pedidos.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verifyNoInteractions;

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

import es.uclm.esibuy.backend.pedidos.application.port.in.ConsultaComprasUseCase;
import es.uclm.esibuy.backend.pedidos.application.port.in.RealizacionPedidoUseCase;
import es.uclm.esibuy.backend.pedidos.application.port.in.RealizarPedidoCommand;
import es.uclm.esibuy.backend.pedidos.application.port.out.CarritoPort;
import es.uclm.esibuy.backend.pedidos.application.port.out.ClienteEnSesionPort;
import es.uclm.esibuy.backend.pedidos.application.port.out.ContadorPedidosPort;
import es.uclm.esibuy.backend.pedidos.application.port.out.DireccionesPort;
import es.uclm.esibuy.backend.pedidos.application.port.out.ExistenciasPort;
import es.uclm.esibuy.backend.pedidos.application.port.out.MetodosPagoPort;
import es.uclm.esibuy.backend.pedidos.application.port.out.PedidoRepositoryPort;
import es.uclm.esibuy.backend.pedidos.domain.exception.MetodoPagoNoValidoException;

// Seguridad en la capa de negocio (R8): realizar un pedido y consultar si se ha comprado un producto exigen el rol
// CLIENTE y que el correo sea el de la sesión. Las anotaciones solo se evalúan con Spring, así que se usa un
// contexto mínimo (sin MongoDB) con los servicios reales y dobles de prueba de los puertos de salida.
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: casos HU10-CP20, HU13-CP07 y HU11-CP06, propuestos.
@SpringJUnitConfig(SeguridadPedidosServiceTest.Configuracion.class)
class SeguridadPedidosServiceTest {

    private static final String CLIENTE = "cliente@esibuy.test";
    private static final String OTRO_CLIENTE = "otro@esibuy.test";

    @Configuration
    @EnableMethodSecurity
    static class Configuracion {
        @Bean
        PedidoRepositoryPort pedidoRepository() {
            return mock(PedidoRepositoryPort.class);
        }

        @Bean
        ClienteEnSesionPort clienteEnSesion() {
            return mock(ClienteEnSesionPort.class);
        }

        @Bean
        CarritoPort carrito() {
            return mock(CarritoPort.class);
        }

        @Bean
        MetodosPagoPort metodosPago() {
            return mock(MetodosPagoPort.class);
        }

        @Bean
        DireccionesPort direcciones() {
            return mock(DireccionesPort.class);
        }

        @Bean
        ExistenciasPort existencias() {
            return mock(ExistenciasPort.class);
        }

        @Bean
        ContadorPedidosPort contador() {
            return mock(ContadorPedidosPort.class);
        }

        @Bean
        RealizacionPedidoUseCase realizacionPedido(PedidoRepositoryPort pedidoRepository,
                ClienteEnSesionPort clienteEnSesion, CarritoPort carrito, MetodosPagoPort metodosPago,
                DireccionesPort direcciones, ExistenciasPort existencias, ContadorPedidosPort contador) {
            return new RealizacionPedidoService(pedidoRepository, clienteEnSesion, carrito, metodosPago,
                    direcciones, existencias, contador, Clock.systemUTC());
        }

        @Bean
        ConsultaComprasUseCase consultaCompras(PedidoRepositoryPort pedidoRepository,
                ClienteEnSesionPort clienteEnSesion) {
            return new ConsultaComprasService(pedidoRepository, clienteEnSesion);
        }
    }

    @Autowired
    private PedidoRepositoryPort pedidoRepository;
    @Autowired
    private ClienteEnSesionPort clienteEnSesion;
    @Autowired
    private CarritoPort carrito;
    @Autowired
    private MetodosPagoPort metodosPago;
    @Autowired
    private DireccionesPort direcciones;
    @Autowired
    private ExistenciasPort existencias;
    @Autowired
    private ContadorPedidosPort contador;
    @Autowired
    private RealizacionPedidoUseCase realizacionPedido;
    @Autowired
    private ConsultaComprasUseCase consultaCompras;

    @BeforeEach
    void limpiarDobles() {
        reset(pedidoRepository, clienteEnSesion, carrito, metodosPago, direcciones, existencias, contador);
    }

    private static RealizarPedidoCommand pedidoDe(String email) {
        return new RealizarPedidoCommand(email, "v-1", "d-1", "m-1");
    }

    private void comprobarQueNoSeTocaNada() {
        verifyNoInteractions(pedidoRepository, clienteEnSesion, carrito, metodosPago, direcciones, existencias,
                contador);
    }

    @Test
    @WithMockUser(username = "vendedor@esibuy.test", roles = "VENDEDOR")
    @DisplayName("HU10-CP20: un vendedor no puede realizar un pedido")
    void unVendedorNoRealizaPedidos() {
        RealizarPedidoCommand pedido = pedidoDe("vendedor@esibuy.test");
        assertThrows(AccessDeniedException.class, () -> realizacionPedido.realizar(pedido));
        comprobarQueNoSeTocaNada();
    }

    @Test
    @WithMockUser(username = "admin@esibuy.test", roles = "ADMINISTRADOR")
    @DisplayName("HU10-CP20: un administrador no puede realizar un pedido")
    void unAdministradorNoRealizaPedidos() {
        RealizarPedidoCommand pedido = pedidoDe("admin@esibuy.test");
        assertThrows(AccessDeniedException.class, () -> realizacionPedido.realizar(pedido));
        comprobarQueNoSeTocaNada();
    }

    @Test
    @WithMockUser(username = CLIENTE, roles = "CLIENTE")
    @DisplayName("HU10-CP20: un cliente no puede realizar un pedido con el correo de otro cliente y no se toca nada")
    void unClienteNoPideConElCorreoDeOtro() {
        RealizarPedidoCommand pedido = pedidoDe(OTRO_CLIENTE);
        assertThrows(AccessDeniedException.class, () -> realizacionPedido.realizar(pedido));
        comprobarQueNoSeTocaNada();
    }

    @Test
    @WithMockUser(username = CLIENTE, roles = "CLIENTE")
    @DisplayName("HU10-CP20: un cliente sí pide con su propio correo (el rechazo que llega es el del negocio, no el de seguridad)")
    void unClientePideConSuPropioCorreo() {
        RealizarPedidoCommand pedido = pedidoDe(CLIENTE);
        assertThrows(MetodoPagoNoValidoException.class, () -> realizacionPedido.realizar(pedido));
    }

    @Test
    @DisplayName("HU10-CP20: sin sesión se rechaza realizar un pedido")
    void sinSesionSeRechazaElPedido() {
        RealizarPedidoCommand pedido = pedidoDe(CLIENTE);
        assertThrows(AuthenticationCredentialsNotFoundException.class,
                () -> realizacionPedido.realizar(pedido));
        comprobarQueNoSeTocaNada();
    }

    @Test
    @WithMockUser(username = "vendedor@esibuy.test", roles = "VENDEDOR")
    @DisplayName("HU13-CP07: un vendedor no puede consultar si se ha comprado un producto")
    void unVendedorNoConsultaCompras() {
        assertThrows(AccessDeniedException.class, () -> consultaCompras.haComprado("vendedor@esibuy.test", "p-1"));
        comprobarQueNoSeTocaNada();
    }

    @Test
    @WithMockUser(username = "admin@esibuy.test", roles = "ADMINISTRADOR")
    @DisplayName("HU13-CP07: un administrador no puede consultar si se ha comprado un producto")
    void unAdministradorNoConsultaCompras() {
        assertThrows(AccessDeniedException.class, () -> consultaCompras.haComprado("admin@esibuy.test", "p-1"));
        comprobarQueNoSeTocaNada();
    }

    @Test
    @WithMockUser(username = CLIENTE, roles = "CLIENTE")
    @DisplayName("HU13-CP07: un cliente no puede consultar las compras de otro cliente y no se toca nada")
    void unClienteNoConsultaLasComprasDeOtro() {
        assertThrows(AccessDeniedException.class, () -> consultaCompras.haComprado(OTRO_CLIENTE, "p-1"));
        comprobarQueNoSeTocaNada();
    }

    @Test
    @WithMockUser(username = CLIENTE, roles = "CLIENTE")
    @DisplayName("HU13-CP07: un cliente sí consulta sus propias compras")
    void unClienteConsultaSusPropiasCompras() {
        assertFalse(consultaCompras.haComprado(CLIENTE, "p-1"));
    }

    @Test
    @DisplayName("HU13-CP07: sin sesión se rechaza consultar si se ha comprado un producto")
    void sinSesionSeRechazaConsultarCompras() {
        assertThrows(AuthenticationCredentialsNotFoundException.class,
                () -> consultaCompras.haComprado(CLIENTE, "p-1"));
        comprobarQueNoSeTocaNada();
    }

    @Test
    @WithMockUser(username = "vendedor@esibuy.test", roles = "VENDEDOR")
    @DisplayName("HU11-CP06: un vendedor no puede consultar el historial de pedidos de un cliente")
    void unVendedorNoConsultaElHistorial() {
        assertThrows(AccessDeniedException.class, () -> consultaCompras.listarPedidos("vendedor@esibuy.test"));
        comprobarQueNoSeTocaNada();
    }

    @Test
    @WithMockUser(username = "admin@esibuy.test", roles = "ADMINISTRADOR")
    @DisplayName("HU11-CP06: un administrador no puede consultar el historial de pedidos de un cliente")
    void unAdministradorNoConsultaElHistorial() {
        assertThrows(AccessDeniedException.class, () -> consultaCompras.listarPedidos("admin@esibuy.test"));
        comprobarQueNoSeTocaNada();
    }

    @Test
    @WithMockUser(username = CLIENTE, roles = "CLIENTE")
    @DisplayName("HU11-CP06: un cliente no puede consultar el historial ni el detalle de los pedidos de otro cliente")
    void unClienteNoConsultaLosPedidosDeOtro() {
        assertThrows(AccessDeniedException.class, () -> consultaCompras.listarPedidos(OTRO_CLIENTE));
        assertThrows(AccessDeniedException.class, () -> consultaCompras.consultarPedido(OTRO_CLIENTE, "o-1"));
        comprobarQueNoSeTocaNada();
    }

    @Test
    @WithMockUser(username = CLIENTE, roles = "CLIENTE")
    @DisplayName("HU11-CP06: un cliente sí consulta su propio historial")
    void unClienteConsultaSuPropioHistorial() {
        assertEquals(List.of(), consultaCompras.listarPedidos(CLIENTE));
    }

    @Test
    @DisplayName("HU11-CP06: sin sesión se rechaza consultar el historial y el detalle de los pedidos")
    void sinSesionSeRechazaConsultarPedidos() {
        assertThrows(AuthenticationCredentialsNotFoundException.class, () -> consultaCompras.listarPedidos(CLIENTE));
        assertThrows(AuthenticationCredentialsNotFoundException.class,
                () -> consultaCompras.consultarPedido(CLIENTE, "o-1"));
        comprobarQueNoSeTocaNada();
    }
}
