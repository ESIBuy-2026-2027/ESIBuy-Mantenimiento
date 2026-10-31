package es.uclm.esibuy.backend.productos.application.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

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

import es.uclm.esibuy.backend.productos.application.port.in.CatalogoProductosUseCase;
import es.uclm.esibuy.backend.productos.application.port.in.ConteoProductosUseCase;
import es.uclm.esibuy.backend.productos.application.port.in.CrearProductoCommand;
import es.uclm.esibuy.backend.productos.application.port.in.ExistenciasProductosUseCase;
import es.uclm.esibuy.backend.productos.application.port.in.GestionProductosUseCase;
import es.uclm.esibuy.backend.productos.application.port.in.ListadoProductosUseCase;
import es.uclm.esibuy.backend.productos.application.port.in.ModificarProductoCommand;
import es.uclm.esibuy.backend.productos.application.port.in.SupervisionProductosUseCase;
import es.uclm.esibuy.backend.productos.application.port.out.ProductoRepositoryPort;
import es.uclm.esibuy.backend.productos.application.port.out.UsuarioEnSesionPort;
import es.uclm.esibuy.backend.productos.application.port.out.VendedorPort;
import es.uclm.esibuy.backend.productos.domain.exception.ProductoNoEncontradoException;

// Seguridad en la capa de negocio (R8): cada caso de uso de productos exige su rol y, si recibe el correo del
// vendedor, que sea el de la sesión. Las anotaciones solo se evalúan con Spring, así que se usa un contexto mínimo
// (sin MongoDB) con los servicios reales y dobles de prueba de los puertos de salida.
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: caso HU31-CP28, propuesto.
@SpringJUnitConfig(SeguridadProductosServiceTest.Configuracion.class)
class SeguridadProductosServiceTest {

    private static final String VENDEDOR = "vendedor@esibuy.test";
    private static final String OTRO_VENDEDOR = "otro@esibuy.test";

    @Configuration
    @EnableMethodSecurity
    static class Configuracion {
        @Bean
        ProductoRepositoryPort productoRepository() {
            return mock(ProductoRepositoryPort.class);
        }

        @Bean
        UsuarioEnSesionPort usuarioEnSesion() {
            return mock(UsuarioEnSesionPort.class);
        }

        @Bean
        VendedorPort vendedorPort() {
            return mock(VendedorPort.class);
        }

        @Bean
        GestionProductosUseCase gestion(ProductoRepositoryPort repositorio, UsuarioEnSesionPort sesion) {
            return new GestionProductosService(repositorio, sesion);
        }

        @Bean
        ListadoProductosUseCase listado(ProductoRepositoryPort repositorio, UsuarioEnSesionPort sesion) {
            return new ListadoProductosService(repositorio, sesion);
        }

        @Bean
        SupervisionProductosUseCase supervision(ProductoRepositoryPort repositorio, VendedorPort vendedores) {
            return new SupervisionProductosService(repositorio, vendedores);
        }

        @Bean
        ConteoProductosUseCase conteo(ProductoRepositoryPort repositorio) {
            return new ConteoProductosService(repositorio);
        }

        @Bean
        CatalogoProductosUseCase catalogo(ProductoRepositoryPort repositorio) {
            return new CatalogoProductosService(repositorio);
        }

        @Bean
        ExistenciasProductosUseCase existencias(ProductoRepositoryPort repositorio) {
            return new ExistenciasProductosService(repositorio);
        }
    }

    @Autowired
    private ProductoRepositoryPort productoRepository;
    @Autowired
    private GestionProductosUseCase gestion;
    @Autowired
    private ListadoProductosUseCase listado;
    @Autowired
    private SupervisionProductosUseCase supervision;
    @Autowired
    private ConteoProductosUseCase conteo;
    @Autowired
    private CatalogoProductosUseCase catalogo;
    @Autowired
    private ExistenciasProductosUseCase existencias;

    @BeforeEach
    void limpiarDobles() {
        reset(productoRepository);
    }

    private void comprobarQueElVendedorRechaza() {
        assertAll(
                () -> assertThrows(AccessDeniedException.class, () -> gestion.crear(null)),
                () -> assertThrows(AccessDeniedException.class, () -> gestion.consultarMiProducto(VENDEDOR, "p-1")),
                () -> assertThrows(AccessDeniedException.class, () -> gestion.modificarMiProducto(null)),
                () -> assertThrows(AccessDeniedException.class, () -> gestion.eliminarMiProducto(VENDEDOR, "p-1")),
                () -> assertThrows(AccessDeniedException.class, () -> listado.listarMisProductos(VENDEDOR, null)));
    }

    @Test
    @WithMockUser(username = "cliente@esibuy.test", roles = "CLIENTE")
    @DisplayName("HU31-CP28: un cliente no puede usar los casos de uso del vendedor")
    void unClienteNoUsaLosCasosDeUsoDelVendedor() {
        comprobarQueElVendedorRechaza();
    }

    @Test
    @WithMockUser(username = "admin@esibuy.test", roles = "ADMINISTRADOR")
    @DisplayName("HU31-CP28: un administrador no puede crear, modificar ni eliminar productos de un vendedor")
    void unAdministradorNoUsaLosCasosDeUsoDelVendedor() {
        comprobarQueElVendedorRechaza();
    }

    @Test
    @WithMockUser(username = VENDEDOR, roles = "VENDEDOR")
    @DisplayName("HU31-CP28: un vendedor no opera con el correo de otro vendedor y no se toca nada")
    void unVendedorNoOperaConElCorreoDeOtro() {
        CrearProductoCommand crear = mock(CrearProductoCommand.class);
        when(crear.emailVendedor()).thenReturn(OTRO_VENDEDOR);
        ModificarProductoCommand modificar = mock(ModificarProductoCommand.class);
        when(modificar.emailVendedor()).thenReturn(OTRO_VENDEDOR);

        assertAll(
                () -> assertThrows(AccessDeniedException.class, () -> gestion.crear(crear)),
                () -> assertThrows(AccessDeniedException.class, () -> gestion.consultarMiProducto(OTRO_VENDEDOR, "p-1")),
                () -> assertThrows(AccessDeniedException.class, () -> gestion.modificarMiProducto(modificar)),
                () -> assertThrows(AccessDeniedException.class, () -> gestion.eliminarMiProducto(OTRO_VENDEDOR, "p-1")),
                () -> assertThrows(AccessDeniedException.class, () -> listado.listarMisProductos(OTRO_VENDEDOR, null)));
        verifyNoInteractions(productoRepository);
    }

    @Test
    @WithMockUser(username = VENDEDOR, roles = "VENDEDOR")
    @DisplayName("HU31-CP28: un vendedor sí opera con su propio correo (un producto que no es suyo sigue siendo 404)")
    void unVendedorOperaConSuPropioCorreo() {
        assertThrows(ProductoNoEncontradoException.class, () -> gestion.eliminarMiProducto(VENDEDOR, "p-1"));
    }

    @Test
    @WithMockUser(username = VENDEDOR, roles = "VENDEDOR")
    @DisplayName("HU31-CP28: solo el administrador supervisa los productos de un vendedor y cuenta los productos por categoría")
    void soloElAdministradorSupervisa() {
        assertAll(
                () -> assertThrows(AccessDeniedException.class, () -> supervision.listarProductosDeVendedor("u-1", 1, 10)),
                () -> assertThrows(AccessDeniedException.class, () -> conteo.contarPorCategoria()));
    }

    @Test
    @WithMockUser(username = "admin@esibuy.test", roles = "ADMINISTRADOR")
    @DisplayName("HU31-CP28: el administrador sí cuenta los productos por categoría")
    void elAdministradorCuentaLosProductos() {
        assertDoesNotThrow(() -> conteo.contarPorCategoria());
    }

    @Test
    @WithMockUser(username = VENDEDOR, roles = "VENDEDOR")
    @DisplayName("HU31-CP28: un vendedor no usa el catálogo ni las existencias, que son del flujo de compra del cliente")
    void unVendedorNoUsaElCatalogoNiLasExistencias() {
        assertAll(
                () -> assertThrows(AccessDeniedException.class, () -> catalogo.buscarProductos(null)),
                () -> assertThrows(AccessDeniedException.class, () -> catalogo.consultarComprable("p-1", false)),
                () -> assertThrows(AccessDeniedException.class, () -> existencias.descontar("p-1", 1)),
                () -> assertThrows(AccessDeniedException.class, () -> existencias.reponer("p-1", 1)));
    }

    @Test
    @WithMockUser(username = "cliente@esibuy.test", roles = "CLIENTE")
    @DisplayName("HU31-CP28: un cliente sí usa el catálogo")
    void unClienteUsaElCatalogo() {
        assertDoesNotThrow(() -> catalogo.consultarComprable("p-1", false));
    }

    @Test
    @DisplayName("HU31-CP28: sin sesión se rechaza cualquier caso de uso de productos")
    void sinSesionSeRechazaTodo() {
        assertAll(
                () -> assertThrows(AuthenticationCredentialsNotFoundException.class,
                        () -> gestion.consultarMiProducto(VENDEDOR, "p-1")),
                () -> assertThrows(AuthenticationCredentialsNotFoundException.class,
                        () -> catalogo.consultarComprable("p-1", false)),
                () -> assertThrows(AuthenticationCredentialsNotFoundException.class, () -> conteo.contarPorCategoria()));
    }
}