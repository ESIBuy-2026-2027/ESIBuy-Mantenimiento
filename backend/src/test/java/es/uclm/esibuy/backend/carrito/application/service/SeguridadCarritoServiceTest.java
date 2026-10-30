package es.uclm.esibuy.backend.carrito.application.service;

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

import es.uclm.esibuy.backend.carrito.application.port.in.GestionCarritoUseCase;
import es.uclm.esibuy.backend.carrito.application.port.out.CarritoRepositoryPort;
import es.uclm.esibuy.backend.carrito.application.port.out.ClienteEnSesionPort;
import es.uclm.esibuy.backend.carrito.application.port.out.ProductosPort;
import es.uclm.esibuy.backend.carrito.application.port.out.VendedoresPort;

// Seguridad en la capa de negocio (R8): el carrito exige el rol CLIENTE y que el correo recibido sea el de la
// sesión. Las anotaciones solo se evalúan con Spring, así que se usa un contexto mínimo (sin MongoDB) con el
// servicio real y dobles de prueba de los puertos de salida.
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: caso HU9-CP20, propuesto.
@SpringJUnitConfig(SeguridadCarritoServiceTest.Configuracion.class)
class SeguridadCarritoServiceTest {

    private static final String CLIENTE = "cliente@esibuy.test";
    private static final String OTRO_CLIENTE = "otro@esibuy.test";

    @Configuration
    @EnableMethodSecurity
    static class Configuracion {
        @Bean
        CarritoRepositoryPort carritoRepository() {
            return mock(CarritoRepositoryPort.class);
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
        VendedoresPort vendedores() {
            return mock(VendedoresPort.class);
        }

        @Bean
        GestionCarritoUseCase gestionCarrito(CarritoRepositoryPort repositorio, ClienteEnSesionPort sesion,
                ProductosPort productos, VendedoresPort vendedores) {
            return new GestionCarritoService(repositorio, sesion, productos, vendedores, Clock.systemUTC());
        }
    }

    @Autowired
    private CarritoRepositoryPort carritoRepository;
    @Autowired
    private ClienteEnSesionPort clienteEnSesion;
    @Autowired
    private ProductosPort productos;
    @Autowired
    private VendedoresPort vendedores;
    @Autowired
    private GestionCarritoUseCase gestionCarrito;

    @BeforeEach
    void limpiarDobles() {
        reset(carritoRepository, clienteEnSesion, productos, vendedores);
    }

    private void comprobarQueRechaza(String email) {
        assertAll(
                () -> assertThrows(AccessDeniedException.class, () -> gestionCarrito.consultar(email)),
                () -> assertThrows(AccessDeniedException.class, () -> gestionCarrito.anadir(email, "p-1", 1)),
                () -> assertThrows(AccessDeniedException.class, () -> gestionCarrito.fijarCantidad(email, "p-1", 1)),
                () -> assertThrows(AccessDeniedException.class, () -> gestionCarrito.quitar(email, "p-1")),
                () -> assertThrows(AccessDeniedException.class, () -> gestionCarrito.vaciar(email)));
        verifyNoInteractions(carritoRepository, clienteEnSesion, productos, vendedores);
    }

    @Test
    @WithMockUser(username = "vendedor@esibuy.test", roles = "VENDEDOR")
    @DisplayName("HU9-CP20: un vendedor no puede usar el carrito de un cliente")
    void unVendedorNoUsaElCarrito() {
        comprobarQueRechaza("vendedor@esibuy.test");
    }

    @Test
    @WithMockUser(username = "admin@esibuy.test", roles = "ADMINISTRADOR")
    @DisplayName("HU9-CP20: un administrador no puede usar el carrito de un cliente")
    void unAdministradorNoUsaElCarrito() {
        comprobarQueRechaza("admin@esibuy.test");
    }

    @Test
    @WithMockUser(username = CLIENTE, roles = "CLIENTE")
    @DisplayName("HU9-CP20: un cliente no opera con el correo de otro cliente y no se toca nada")
    void unClienteNoOperaConElCorreoDeOtro() {
        comprobarQueRechaza(OTRO_CLIENTE);
    }

    @Test
    @WithMockUser(username = CLIENTE, roles = "CLIENTE")
    @DisplayName("HU9-CP20: un cliente sí consulta su propio carrito")
    void unClienteConsultaSuPropioCarrito() {
        assertDoesNotThrow(() -> gestionCarrito.consultar(CLIENTE));
    }

    @Test
    @DisplayName("HU9-CP20: sin sesión se rechaza cualquier operación del carrito")
    void sinSesionSeRechazaTodo() {
        assertAll(
                () -> assertThrows(AuthenticationCredentialsNotFoundException.class,
                        () -> gestionCarrito.consultar(CLIENTE)),
                () -> assertThrows(AuthenticationCredentialsNotFoundException.class,
                        () -> gestionCarrito.anadir(CLIENTE, "p-1", 1)));
        verifyNoInteractions(carritoRepository, clienteEnSesion, productos, vendedores);
    }
}