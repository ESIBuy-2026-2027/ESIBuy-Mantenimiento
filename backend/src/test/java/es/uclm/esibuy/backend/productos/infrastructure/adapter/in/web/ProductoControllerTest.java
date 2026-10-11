package es.uclm.esibuy.backend.productos.infrastructure.adapter.in.web;

import es.uclm.esibuy.backend.productos.application.port.in.ListadoProductosUseCase;
import es.uclm.esibuy.backend.productos.application.port.in.ListarProductosQuery;
import es.uclm.esibuy.backend.productos.domain.model.Descuento;
import es.uclm.esibuy.backend.productos.domain.model.Producto;
import es.uclm.esibuy.backend.productos.domain.model.ProductoPage;
import es.uclm.esibuy.backend.shared.config.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Listado de los productos propios del vendedor (HU32, T184): GET /api/seller/products. El caso de uso es un
// doble de prueba; la seguridad es la real (SecurityConfig: /api/seller/** solo para VENDEDOR).
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: casos HU32-CP01 a CP05, propuestos a partir de los criterios de aceptación.
@WebMvcTest(ProductoController.class)
@Import(SecurityConfig.class)
class ProductoControllerTest {

    private static final String URL = "/api/seller/products";
    private static final String VENDEDOR = "vendedor@esibuy.test";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ListadoProductosUseCase listadoProductos;

    @Test
    @DisplayName("HU32-CP01: el vendedor recibe su listado con el formato del contrato y sin el id del vendedor")
    void vendedorRecibeSuListado() throws Exception {
        Producto producto = new Producto("p-1", "v-1", "Teclado mecánico", "", new BigDecimal("89.50"), 12, "cat-2",
                "", true, Descuento.sinDescuento(), Descuento.sinDescuento());
        when(listadoProductos.listarMisProductos(any(), any()))
                .thenReturn(new ProductoPage(List.of(producto), 1, 10, 11));

        mockMvc.perform(get(URL).param("page", "2").param("size", "10").param("search", "teclado")
                        .param("category", "cat-2").with(user(VENDEDOR).roles("VENDEDOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value("p-1"))
                .andExpect(jsonPath("$.items[0].nombre").value("Teclado mecánico"))
                .andExpect(jsonPath("$.items[0].precio").value(89.5))
                .andExpect(jsonPath("$.items[0].stock").value(12))
                .andExpect(jsonPath("$.items[0].disponible").value(true))
                .andExpect(jsonPath("$.items[0].categoriaId").value("cat-2"))
                .andExpect(jsonPath("$.items[0].vendedorId").doesNotExist())
                .andExpect(jsonPath("$.page").value(2))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalItems").value(11))
                .andExpect(jsonPath("$.totalPages").value(2));

        // El vendedor sale de la sesión; la página llega al caso de uso empezando en 0
        verify(listadoProductos).listarMisProductos(VENDEDOR, new ListarProductosQuery("teclado", "cat-2", 1, 10));
    }

    @Test
    @DisplayName("HU32-CP03: sin productos, el listado viene vacío y con 0 páginas")
    void listadoVacioTieneCeroPaginas() throws Exception {
        when(listadoProductos.listarMisProductos(any(), any())).thenReturn(new ProductoPage(List.of(), 0, 10, 0));

        mockMvc.perform(get(URL).with(user(VENDEDOR).roles("VENDEDOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isEmpty())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.totalItems").value(0))
                .andExpect(jsonPath("$.totalPages").value(0));
    }

    @ParameterizedTest(name = "{0}=0")
    @ValueSource(strings = {"page", "size"})
    @DisplayName("HU32-CP04: una página o un tamaño menor que 1 devuelve 400")
    void paginaOTamanoMenorQueUnoDevuelve400(String parametro) throws Exception {
        mockMvc.perform(get(URL).param(parametro, "0").with(user(VENDEDOR).roles("VENDEDOR")))
                .andExpect(status().isBadRequest());

        verify(listadoProductos, never()).listarMisProductos(any(), any());
    }

    @Test
    @DisplayName("HU32-CP05: sin sesión no se ve el listado")
    void sinSesionDevuelve401() throws Exception {
        mockMvc.perform(get(URL))
                .andExpect(status().isUnauthorized());

        verify(listadoProductos, never()).listarMisProductos(any(), any());
    }

    @ParameterizedTest(name = "rol {0}")
    @ValueSource(strings = {"CLIENTE", "ADMINISTRADOR"})
    @DisplayName("HU32-CP05: un usuario que no es vendedor no ve el listado")
    void noVendedorNoVeElListado(String rol) throws Exception {
        mockMvc.perform(get(URL).with(user("usuario@esibuy.test").roles(rol)))
                .andExpect(status().isForbidden());

        verify(listadoProductos, never()).listarMisProductos(any(), any());
    }
}
