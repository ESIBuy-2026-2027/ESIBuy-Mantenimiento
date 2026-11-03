package es.uclm.esibuy.backend.categorias.application.service;

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
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import es.uclm.esibuy.backend.categorias.application.port.in.AdministracionCategoriasUseCase;
import es.uclm.esibuy.backend.categorias.application.port.in.ConsultaCategoriasUseCase;
import es.uclm.esibuy.backend.categorias.application.port.in.CrearCategoriaCommand;
import es.uclm.esibuy.backend.categorias.application.port.in.EditarCategoriaCommand;
import es.uclm.esibuy.backend.categorias.application.port.out.CategoriaRepositoryPort;
import es.uclm.esibuy.backend.categorias.application.port.out.ProductosPorCategoriaPort;
import es.uclm.esibuy.backend.categorias.application.port.out.VendedoresPorCategoriaPort;
import es.uclm.esibuy.backend.categorias.domain.exception.CategoriaInvalidaException;
import es.uclm.esibuy.backend.categorias.domain.exception.CategoriaNoEncontradaException;

// Seguridad en la capa de negocio (R8): consultar las categorías es público (lo usan el registro del vendedor y
// el desplegable de producto, sin sesión); administrarlas exige el rol ADMINISTRADOR. Las anotaciones solo se
// evalúan con Spring, así que se usa un contexto mínimo (sin MongoDB) con los servicios reales y dobles de
// prueba de los puertos de salida.
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: casos HU40-CP09, HU41-CP06 y HU42-CP05, propuestos.
@SpringJUnitConfig(SeguridadCategoriasServiceTest.Configuracion.class)
class SeguridadCategoriasServiceTest {

    // Sin datos: el dominio la rechaza por inválida, lo que demuestra que la seguridad ha dejado pasar la llamada
    private static final CrearCategoriaCommand CREACION = new CrearCategoriaCommand(null, null, null);

    // Una categoría que no existe: el dominio responde «no encontrada», lo que demuestra que la seguridad ha
    // dejado pasar la llamada (el doble del repositorio devuelve vacío)
    private static final EditarCategoriaCommand EDICION = new EditarCategoriaCommand("c-1", "Hogar", "");

    @Configuration
    @EnableMethodSecurity
    static class Configuracion {
        @Bean
        CategoriaRepositoryPort categoriaRepository() {
            return mock(CategoriaRepositoryPort.class);
        }

        @Bean
        ProductosPorCategoriaPort productosPorCategoria() {
            return mock(ProductosPorCategoriaPort.class);
        }

        @Bean
        VendedoresPorCategoriaPort vendedoresPorCategoria() {
            return mock(VendedoresPorCategoriaPort.class);
        }

        @Bean
        ConsultaCategoriasUseCase consultaCategorias(CategoriaRepositoryPort repositorio) {
            return new ConsultaCategoriasService(repositorio);
        }

        @Bean
        AdministracionCategoriasUseCase administracionCategorias(CategoriaRepositoryPort repositorio,
                ProductosPorCategoriaPort productos, VendedoresPorCategoriaPort vendedores) {
            return new AdministracionCategoriasService(repositorio, productos, vendedores);
        }
    }

    @Autowired
    private AdministracionCategoriasUseCase administracionCategorias;
    @Autowired
    private CategoriaRepositoryPort categoriaRepository;
    @Autowired
    private ConsultaCategoriasUseCase consultaCategorias;
    @Autowired
    private ProductosPorCategoriaPort productosPorCategoria;
    @Autowired
    private VendedoresPorCategoriaPort vendedoresPorCategoria;

    @BeforeEach
    void limpiarDobles() {
                reset(categoriaRepository, productosPorCategoria, vendedoresPorCategoria);
    }

    private void comprobarQueRechazaLaAdministracion() {
        assertAll(
                () -> assertThrows(AccessDeniedException.class, () -> administracionCategorias.listarConProductos()),
                () -> assertThrows(AccessDeniedException.class, () -> administracionCategorias.crear(CREACION)));
        verifyNoInteractions(categoriaRepository, productosPorCategoria);
    }

    private void comprobarQueRechazaLaEliminacion() {
        assertThrows(AccessDeniedException.class, () -> administracionCategorias.eliminar("c-1"));
        verifyNoInteractions(categoriaRepository, productosPorCategoria, vendedoresPorCategoria);
    }

    private void comprobarQueRechazaLaEdicion() {
        assertThrows(AccessDeniedException.class, () -> administracionCategorias.editar(EDICION));
        verifyNoInteractions(categoriaRepository, productosPorCategoria);
    }

    @Test
    @WithAnonymousUser
    @DisplayName("HU40-CP09: el listado público de categorías se puede usar sin sesión")
    void elListadoPublicoNoNecesitaSesion() {
        assertDoesNotThrow(() -> consultaCategorias.listar());
    }

    @Test
    @WithMockUser(username = "cliente@esibuy.test", roles = "CLIENTE")
    @DisplayName("HU40-CP09: un cliente no puede administrar las categorías")
    void unClienteNoAdministraCategorias() {
        comprobarQueRechazaLaAdministracion();
    }

    @Test
    @WithMockUser(username = "vendedor@esibuy.test", roles = "VENDEDOR")
    @DisplayName("HU40-CP09: un vendedor no puede administrar las categorías")
    void unVendedorNoAdministraCategorias() {
        comprobarQueRechazaLaAdministracion();
    }

    @Test
    @WithAnonymousUser
    @DisplayName("HU40-CP09: un usuario anónimo no puede administrar las categorías")
    void unAnonimoNoAdministraCategorias() {
        comprobarQueRechazaLaAdministracion();
    }

    @Test
    @WithMockUser(username = "admin@esibuy.test", roles = "ADMINISTRADOR")
    @DisplayName("HU40-CP09: un administrador sí administra las categorías (los datos inválidos los rechaza el dominio)")
    void unAdministradorAdministraCategorias() {
        assertAll(
                () -> assertDoesNotThrow(() -> administracionCategorias.listarConProductos()),
                () -> assertThrows(CategoriaInvalidaException.class, () -> administracionCategorias.crear(CREACION)));
    }

    @Test
    @DisplayName("HU40-CP09: sin sesión se rechaza la administración de categorías")
    void sinSesionSeRechazaLaAdministracion() {
        assertAll(
                () -> assertThrows(AuthenticationCredentialsNotFoundException.class,
                        () -> administracionCategorias.listarConProductos()),
                () -> assertThrows(AuthenticationCredentialsNotFoundException.class,
                        () -> administracionCategorias.crear(CREACION)));
        verifyNoInteractions(categoriaRepository, productosPorCategoria);
    }

    @Test
    @WithMockUser(username = "cliente@esibuy.test", roles = "CLIENTE")
    @DisplayName("HU41-CP06: un cliente no puede editar categorías")
    void unClienteNoEditaCategorias() {
        comprobarQueRechazaLaEdicion();
    }

    @Test
    @WithMockUser(username = "vendedor@esibuy.test", roles = "VENDEDOR")
    @DisplayName("HU41-CP06: un vendedor no puede editar categorías")
    void unVendedorNoEditaCategorias() {
        comprobarQueRechazaLaEdicion();
    }

    @Test
    @WithAnonymousUser
    @DisplayName("HU41-CP06: un usuario anónimo no puede editar categorías")
    void unAnonimoNoEditaCategorias() {
        comprobarQueRechazaLaEdicion();
    }

    @Test
    @WithMockUser(username = "admin@esibuy.test", roles = "ADMINISTRADOR")
    @DisplayName("HU41-CP06: un administrador sí edita categorías (una que no existe la rechaza el dominio)")
    void unAdministradorEditaCategorias() {
        assertThrows(CategoriaNoEncontradaException.class, () -> administracionCategorias.editar(EDICION));
    }

    @Test
    @DisplayName("HU41-CP06: sin sesión se rechaza la edición de categorías")
    void sinSesionSeRechazaLaEdicion() {
        assertThrows(AuthenticationCredentialsNotFoundException.class,
                () -> administracionCategorias.editar(EDICION));
        verifyNoInteractions(categoriaRepository, productosPorCategoria);
    }

        @Test
    @WithMockUser(username = "cliente@esibuy.test", roles = "CLIENTE")
    @DisplayName("HU42-CP05: un cliente no puede eliminar categorías")
    void unClienteNoEliminaCategorias() {
        comprobarQueRechazaLaEliminacion();
    }

    @Test
    @WithMockUser(username = "vendedor@esibuy.test", roles = "VENDEDOR")
    @DisplayName("HU42-CP05: un vendedor no puede eliminar categorías")
    void unVendedorNoEliminaCategorias() {
        comprobarQueRechazaLaEliminacion();
    }

    @Test
    @WithAnonymousUser
    @DisplayName("HU42-CP05: un usuario anónimo no puede eliminar categorías")
    void unAnonimoNoEliminaCategorias() {
        comprobarQueRechazaLaEliminacion();
    }

    @Test
    @WithMockUser(username = "admin@esibuy.test", roles = "ADMINISTRADOR")
    @DisplayName("HU42-CP05: un administrador sí elimina categorías (una que no existe la rechaza el dominio)")
    void unAdministradorEliminaCategorias() {
        assertThrows(CategoriaNoEncontradaException.class, () -> administracionCategorias.eliminar("c-1"));
    }

    @Test
    @DisplayName("HU42-CP05: sin sesión se rechaza la eliminación de categorías")
    void sinSesionSeRechazaLaEliminacion() {
        assertThrows(AuthenticationCredentialsNotFoundException.class,
                () -> administracionCategorias.eliminar("c-1"));
        verifyNoInteractions(categoriaRepository, productosPorCategoria, vendedoresPorCategoria);
    }
}