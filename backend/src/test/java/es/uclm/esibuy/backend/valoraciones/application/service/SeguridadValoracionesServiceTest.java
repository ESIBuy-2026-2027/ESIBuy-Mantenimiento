package es.uclm.esibuy.backend.valoraciones.application.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

import es.uclm.esibuy.backend.valoraciones.application.port.in.ConsultaValoracionesUseCase;
import es.uclm.esibuy.backend.valoraciones.application.port.in.ValoracionProductosUseCase;
import es.uclm.esibuy.backend.valoraciones.application.port.in.ValorarProductoCommand;
import es.uclm.esibuy.backend.valoraciones.application.port.out.ClienteEnSesionPort;
import es.uclm.esibuy.backend.valoraciones.application.port.out.ComprasPort;
import es.uclm.esibuy.backend.valoraciones.application.port.out.ValoracionRepositoryPort;
import es.uclm.esibuy.backend.valoraciones.domain.exception.ProductoNoCompradoException;

// Seguridad en la capa de negocio (R8): valorar un producto exige el rol CLIENTE y que el correo del comando sea el
// de la sesión; consultar las valoraciones de un producto, el rol CLIENTE. Las anotaciones solo se evalúan con
// Spring, así que se usa un contexto mínimo (sin MongoDB) con los servicios reales y dobles de los puertos de salida.
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: casos HU13-CP12 y HU13-CP16, propuestos.
@SpringJUnitConfig(SeguridadValoracionesServiceTest.Configuracion.class)
class SeguridadValoracionesServiceTest {

    private static final String CLIENTE = "cliente@esibuy.test";
    private static final String OTRO_CLIENTE = "otro@esibuy.test";

    @Configuration
    @EnableMethodSecurity
    static class Configuracion {
        @Bean
        ValoracionRepositoryPort valoracionRepository() {
            return mock(ValoracionRepositoryPort.class);
        }

        @Bean
        ClienteEnSesionPort clienteEnSesion() {
            return mock(ClienteEnSesionPort.class);
        }

        @Bean
        ComprasPort compras() {
            return mock(ComprasPort.class);
        }

        @Bean
        ValoracionProductosUseCase valoracionProductos(ValoracionRepositoryPort valoracionRepository,
                ClienteEnSesionPort clienteEnSesion, ComprasPort compras) {
            return new ValoracionProductosService(valoracionRepository, clienteEnSesion, compras);
        }

        @Bean
        ConsultaValoracionesUseCase consultaValoraciones(ValoracionRepositoryPort valoracionRepository) {
            return new ConsultaValoracionesService(valoracionRepository);
        }
    }

    @Autowired
    private ValoracionRepositoryPort valoracionRepository;
    @Autowired
    private ClienteEnSesionPort clienteEnSesion;
    @Autowired
    private ComprasPort compras;
    @Autowired
    private ValoracionProductosUseCase valoracionProductos;
    @Autowired
    private ConsultaValoracionesUseCase consultaValoraciones;

    @BeforeEach
    void limpiarDobles() {
        reset(valoracionRepository, clienteEnSesion, compras);
    }

    private static ValorarProductoCommand valoracionDe(String email) {
        return new ValorarProductoCommand(email, "p-1", 4, "Buena calidad");
    }

    private void comprobarQueNoSeTocaNada() {
        verifyNoInteractions(valoracionRepository, clienteEnSesion, compras);
    }

    @Test
    @WithMockUser(username = "vendedor@esibuy.test", roles = "VENDEDOR")
    @DisplayName("HU13-CP12: un vendedor no puede valorar un producto")
    void unVendedorNoValora() {
        ValorarProductoCommand valoracion = valoracionDe("vendedor@esibuy.test");
        assertThrows(AccessDeniedException.class, () -> valoracionProductos.valorar(valoracion));
        comprobarQueNoSeTocaNada();
    }

    @Test
    @WithMockUser(username = "admin@esibuy.test", roles = "ADMINISTRADOR")
    @DisplayName("HU13-CP12: un administrador no puede valorar un producto")
    void unAdministradorNoValora() {
        ValorarProductoCommand valoracion = valoracionDe("admin@esibuy.test");
        assertThrows(AccessDeniedException.class, () -> valoracionProductos.valorar(valoracion));
        comprobarQueNoSeTocaNada();
    }

    @Test
    @WithMockUser(username = CLIENTE, roles = "CLIENTE")
    @DisplayName("HU13-CP12: un cliente no puede valorar con el correo de otro cliente y no se toca nada")
    void unClienteNoValoraConElCorreoDeOtro() {
        ValorarProductoCommand valoracion = valoracionDe(OTRO_CLIENTE);
        assertThrows(AccessDeniedException.class, () -> valoracionProductos.valorar(valoracion));
        comprobarQueNoSeTocaNada();
    }

    @Test
    @WithMockUser(username = CLIENTE, roles = "CLIENTE")
    @DisplayName("HU13-CP12: un cliente sí valora con su propio correo (el rechazo que llega es el del negocio, no el de seguridad)")
    void unClienteValoraConSuPropioCorreo() {
        ValorarProductoCommand valoracion = valoracionDe(CLIENTE);
        assertThrows(ProductoNoCompradoException.class, () -> valoracionProductos.valorar(valoracion));
    }

    @Test
    @DisplayName("HU13-CP12: sin sesión se rechaza valorar un producto")
    void sinSesionSeRechazaValorar() {
        ValorarProductoCommand valoracion = valoracionDe(CLIENTE);
        assertThrows(AuthenticationCredentialsNotFoundException.class,
                () -> valoracionProductos.valorar(valoracion));
        comprobarQueNoSeTocaNada();
    }

    @Test
    @WithMockUser(username = "vendedor@esibuy.test", roles = "VENDEDOR")
    @DisplayName("HU13-CP16: un vendedor no puede consultar las valoraciones de un producto")
    void unVendedorNoConsultaValoraciones() {
        assertThrows(AccessDeniedException.class, () -> consultaValoraciones.consultarPorProducto("p-1"));
        comprobarQueNoSeTocaNada();
    }

    @Test
    @WithMockUser(username = "admin@esibuy.test", roles = "ADMINISTRADOR")
    @DisplayName("HU13-CP16: un administrador no puede consultar las valoraciones de un producto")
    void unAdministradorNoConsultaValoraciones() {
        assertThrows(AccessDeniedException.class, () -> consultaValoraciones.consultarPorProducto("p-1"));
        comprobarQueNoSeTocaNada();
    }

    @Test
    @WithMockUser(username = CLIENTE, roles = "CLIENTE")
    @DisplayName("HU13-CP16: un cliente sí consulta las valoraciones de un producto")
    void unClienteConsultaValoraciones() {
        assertTrue(consultaValoraciones.consultarPorProducto("p-1").isEmpty());
    }

    @Test
    @DisplayName("HU13-CP16: sin sesión se rechaza consultar las valoraciones de un producto")
    void sinSesionSeRechazaConsultarValoraciones() {
        assertThrows(AuthenticationCredentialsNotFoundException.class,
                () -> consultaValoraciones.consultarPorProducto("p-1"));
        comprobarQueNoSeTocaNada();
    }
}
