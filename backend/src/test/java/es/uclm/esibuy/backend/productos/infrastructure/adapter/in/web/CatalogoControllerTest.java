package es.uclm.esibuy.backend.productos.infrastructure.adapter.in.web;

import es.uclm.esibuy.backend.productos.application.port.in.CatalogoProductosUseCase;
import es.uclm.esibuy.backend.productos.domain.model.Descuento;
import es.uclm.esibuy.backend.productos.domain.model.Producto;
import es.uclm.esibuy.backend.productos.domain.model.ProductoPage;
import es.uclm.esibuy.backend.shared.config.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Catálogo de productos del cliente (HU7, T353): GET /api/products. El caso de uso es un doble de prueba;
// la seguridad es la real (SecurityConfig: /api/products solo para CLIENTE).
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: casos HU7-CP16 a CP19, propuestos.
@WebMvcTest(CatalogoController.class)
@Import(SecurityConfig.class)
class CatalogoControllerTest {

    private static final String URL = "/api/products";
    private static final String CLIENTE = "cliente@esibuy.test";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CatalogoProductosUseCase catalogoProductos;

    @Test
    @DisplayName("HU7-CP16: el cliente recibe el catálogo con el formato del contrato y sin valoraciones todavía")
    void clienteRecibeElCatalogoConElFormatoDelContrato() throws Exception {
        Producto producto = new Producto("p-1", "v-1", "Teclado mecánico", "", new BigDecimal("89.50"), 12, "cat-2",
                "https://esibuy.test/teclado.png", true, Descuento.sinDescuento(), Descuento.sinDescuento());
        when(catalogoProductos.buscarProductos(any())).thenReturn(new ProductoPage(List.of(producto), 0, 10, 11));

        mockMvc.perform(get(URL).with(user(CLIENTE).roles("CLIENTE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value("p-1"))
                .andExpect(jsonPath("$.items[0].nombre").value("Teclado mecánico"))
                .andExpect(jsonPath("$.items[0].imagen").value("https://esibuy.test/teclado.png"))
                .andExpect(jsonPath("$.items[0].categoriaId").value("cat-2"))
                // Hasta HU13 no hay valoraciones: el campo existe, pero vale null, y el número es 0
                .andExpect(jsonPath("$.items[0].valoracion").value(nullValue()))
                .andExpect(jsonPath("$.items[0].numValoraciones").value(0))
                .andExpect(jsonPath("$.items[0].precio").doesNotExist())
                .andExpect(jsonPath("$.items[0].stock").doesNotExist())
                .andExpect(jsonPath("$.items[0].vendedorId").doesNotExist())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalItems").value(11))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    @DisplayName("HU7-CP17: los parámetros llegan al caso de uso traducidos: página desde 0, criterio y sentido")
    void traduceLosParametrosALaConsulta() {
        // TODO: GET ?page=2&size=10&search=teclado&category=cat-2&minRating=4&sort=valoracion&order=desc;
        // con ArgumentCaptor: pagina = 1, tamano = 10, busqueda, categoriaId, valoracionMinima = 4,
        // criterio = VALORACION, descendente = true.
        fail("Funcionalidad no implementada");
    }

    @Test
    @DisplayName("HU7-CP18: un parámetro no válido devuelve 400 y no llega al caso de uso")
    void parametroNoValidoDevuelve400() {
        // TODO: parametrizado con page=0, size=0, sort=precio, order=arriba y minRating=6 → 400 y verify(never()).
        fail("Funcionalidad no implementada");
    }

    @Test
    @DisplayName("HU7-CP19: sin sesión no se ve el catálogo")
    void sinSesionDevuelve401() {
        // TODO: GET sin usuario → 401.
        fail("Funcionalidad no implementada");
    }

    @Test
    @DisplayName("HU7-CP19: un usuario que no es cliente no ve el catálogo")
    void otroRolDevuelve403() {
        // TODO: parametrizado con VENDEDOR y ADMINISTRADOR → 403.
        fail("Funcionalidad no implementada");
    }
}
