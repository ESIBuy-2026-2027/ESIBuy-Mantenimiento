package es.uclm.esibuy.backend.productos.infrastructure.adapter.in.web;

import es.uclm.esibuy.backend.productos.application.port.in.BuscarCatalogoQuery;
import es.uclm.esibuy.backend.productos.application.port.in.CatalogoProductosUseCase;
import es.uclm.esibuy.backend.productos.application.port.in.CriterioOrdenCatalogo;
import es.uclm.esibuy.backend.productos.application.port.in.DetalleProductoCatalogo;
import es.uclm.esibuy.backend.productos.application.port.in.PaginaCatalogo;
import es.uclm.esibuy.backend.productos.domain.exception.ProductoNoEncontradoException;
import es.uclm.esibuy.backend.productos.domain.model.Descuento;
import es.uclm.esibuy.backend.productos.domain.model.Producto;
import es.uclm.esibuy.backend.productos.domain.model.ProductoPage;
import es.uclm.esibuy.backend.shared.config.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;



import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Catálogo de productos del cliente (HU7, T353): GET /api/products. Detalle de un producto (HU8, T355):
// GET /api/products/{id}. El caso de uso es un doble de prueba; la seguridad es la real (SecurityConfig).
// La media y el número de valoraciones (HU13, T415).
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: casos HU7-CP16 a CP19, HU8-CP13 a CP15 y HU13-CP26, propuestos.
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
        when(catalogoProductos.buscarProductos(any(), any()))
                .thenReturn(new PaginaCatalogo(new ProductoPage(List.of(producto), 0, 10, 11), false));

        mockMvc.perform(get(URL).with(user(CLIENTE).roles("CLIENTE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value("p-1"))
                .andExpect(jsonPath("$.items[0].nombre").value("Teclado mecánico"))
                .andExpect(jsonPath("$.items[0].imagen").value("https://esibuy.test/teclado.png"))
                .andExpect(jsonPath("$.items[0].categoriaId").value("cat-2"))
                // El precio que paga el cliente: sin descuento, el precio final es el precio
                .andExpect(jsonPath("$.items[0].precio").value(89.5))
                .andExpect(jsonPath("$.items[0].descuento").value(nullValue()))
                .andExpect(jsonPath("$.items[0].precioFinal").value(89.5))
                // Un producto sin valoraciones: el campo existe, pero vale null, y el número es 0
                .andExpect(jsonPath("$.items[0].valoracion").value(nullValue()))
                .andExpect(jsonPath("$.items[0].numValoraciones").value(0))
                .andExpect(jsonPath("$.items[0].stock").doesNotExist())
                .andExpect(jsonPath("$.items[0].vendedorId").doesNotExist())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalItems").value(11))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    @DisplayName("HU7-CP17: los parámetros llegan al caso de uso traducidos: página desde 0, criterio y sentido")
    void traduceLosParametrosALaConsulta() throws Exception {
        when(catalogoProductos.buscarProductos(any(), any()))
                .thenReturn(new PaginaCatalogo(new ProductoPage(List.of(), 1, 10, 0), false));

        mockMvc.perform(get(URL).param("page", "2").param("size", "10").param("search", "teclado")
                        .param("category", "cat-2").param("minRating", "4").param("sort", "valoracion")
                        .param("order", "desc").with(user(CLIENTE).roles("CLIENTE")))
                .andExpect(status().isOk());

        // La página llega al caso de uso empezando en 0; sort y order se traducen al enum y al boolean. El cliente
        // sale de la sesión, para darle su precio
        verify(catalogoProductos).buscarProductos(new BuscarCatalogoQuery("teclado", "cat-2", 4,
                CriterioOrdenCatalogo.VALORACION, true, 1, 10), CLIENTE);
    }

    // Que minRating=6 no sea válido es una propuesta: la valoración va de 1 a 5.
    @ParameterizedTest(name = "{0}={1}")
    @CsvSource({"page, 0", "size, 0", "sort, precio", "order, arriba", "minRating, 6"})
    @DisplayName("HU7-CP18: un parámetro no válido devuelve 400 y no llega al caso de uso")
    void parametroNoValidoDevuelve400(String parametro, String valor) throws Exception {
        // Con el caso de uso respondiendo, un 200 indicaría que el parámetro se ha aceptado
        when(catalogoProductos.buscarProductos(any(), any()))
                .thenReturn(new PaginaCatalogo(new ProductoPage(List.of(), 0, 10, 0), false));

        mockMvc.perform(get(URL).param(parametro, valor).with(user(CLIENTE).roles("CLIENTE")))
                .andExpect(status().isBadRequest());

        verify(catalogoProductos, never()).buscarProductos(any(), any());
    }

    @Test
    @DisplayName("HU7-CP19: sin sesión no se ve el catálogo")
    void sinSesionDevuelve401() throws Exception {
        mockMvc.perform(get(URL))
                .andExpect(status().isUnauthorized());

        verify(catalogoProductos, never()).buscarProductos(any(), any());
    }

    @ParameterizedTest(name = "rol {0}")
    @ValueSource(strings = {"VENDEDOR", "ADMINISTRADOR"})
    @DisplayName("HU7-CP19: un usuario que no es cliente no ve el catálogo")
    void otroRolDevuelve403(String rol) throws Exception {
        // Con el caso de uso respondiendo, un 200 indicaría que el rol tiene acceso
        when(catalogoProductos.buscarProductos(any(), any()))
                .thenReturn(new PaginaCatalogo(new ProductoPage(List.of(), 0, 10, 0), false));

        mockMvc.perform(get(URL).with(user("usuario@esibuy.test").roles(rol)))
                .andExpect(status().isForbidden());

        verify(catalogoProductos, never()).buscarProductos(any(), any());
    }

    @Test
    @DisplayName("HU13-CP26: el catálogo devuelve la media y el número de valoraciones de cada producto")
    void elCatalogoDevuelveLaMediaYElNumeroDeValoraciones() throws Exception {
        Producto valorado = new Producto("p-1", "v-1", "Teclado mecánico", "", new BigDecimal("89.50"), 12, "cat-2",
                "", true, Descuento.sinDescuento(), Descuento.sinDescuento(), 4.5, 2);
        when(catalogoProductos.buscarProductos(any(), any()))
                .thenReturn(new PaginaCatalogo(new ProductoPage(List.of(valorado), 0, 10, 1), false));

        mockMvc.perform(get(URL).with(user(CLIENTE).roles("CLIENTE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].valoracion").value(4.5))
                .andExpect(jsonPath("$.items[0].numValoraciones").value(2));
    }

    @Test
    @DisplayName("HU13-CP26: el detalle devuelve la media y el número de valoraciones del producto")
    void elDetalleDevuelveLaMediaYElNumeroDeValoraciones() throws Exception {
        when(catalogoProductos.consultarDetalle("p-1", CLIENTE)).thenReturn(new DetalleProductoCatalogo("p-1",
                "Teclado mecánico", "", "", "cat-2", "u-2", "TecnoVera", new BigDecimal("50.00"), null,
                new BigDecimal("50.00"), 7, true, 4.5, 2));

        mockMvc.perform(get(URL + "/p-1").with(user(CLIENTE).roles("CLIENTE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valoracion").value(4.5))
                .andExpect(jsonPath("$.numValoraciones").value(2));
    }

    private static DetalleProductoCatalogo detalle(BigDecimal porcentajeDescuento, BigDecimal precioFinal) {
        return new DetalleProductoCatalogo("p-1", "Teclado mecánico", "Con cable", "https://esibuy.test/teclado.png",
                "cat-2", "u-2", "TecnoVera", new BigDecimal("50.00"), porcentajeDescuento, precioFinal, 7, true,
                null, 0);
    }

    @Test
    @DisplayName("HU8-CP13: el cliente recibe el detalle con el formato del contrato y sin valoraciones todavía")
    void clienteRecibeElDetalleConElFormatoDelContrato() throws Exception {
        // El correo de la sesión llega al caso de uso, que decide con él el descuento del cliente
        when(catalogoProductos.consultarDetalle("p-1", CLIENTE))
                .thenReturn(detalle(new BigDecimal("10"), new BigDecimal("45.00")));

        mockMvc.perform(get(URL + "/p-1").with(user(CLIENTE).roles("CLIENTE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("p-1"))
                .andExpect(jsonPath("$.nombre").value("Teclado mecánico"))
                .andExpect(jsonPath("$.descripcion").value("Con cable"))
                .andExpect(jsonPath("$.imagen").value("https://esibuy.test/teclado.png"))
                .andExpect(jsonPath("$.categoriaId").value("cat-2"))
                .andExpect(jsonPath("$.vendedor.id").value("u-2"))
                .andExpect(jsonPath("$.vendedor.nombreComercial").value("TecnoVera"))
                .andExpect(jsonPath("$.precio").value(50.0))
                .andExpect(jsonPath("$.descuento.porcentaje").value(10))
                .andExpect(jsonPath("$.precioFinal").value(45.0))
                .andExpect(jsonPath("$.stock").value(7))
                .andExpect(jsonPath("$.disponible").value(true))
                // Un producto sin valoraciones: el campo existe, pero vale null, y el número es 0
                .andExpect(jsonPath("$.valoracion").value(nullValue()))
                .andExpect(jsonPath("$.numValoraciones").value(0))
                .andExpect(jsonPath("$.vendedorId").doesNotExist())
                .andExpect(jsonPath("$.nombreComercial").doesNotExist());
    }

    @Test
    @DisplayName("HU8-CP13: sin descuento para el cliente, el descuento del detalle es null y paga el precio")
    void sinDescuentoElDescuentoDelDetalleEsNull() throws Exception {
        when(catalogoProductos.consultarDetalle("p-1", CLIENTE)).thenReturn(detalle(null, new BigDecimal("50.00")));

        mockMvc.perform(get(URL + "/p-1").with(user(CLIENTE).roles("CLIENTE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descuento").value(nullValue()))
                .andExpect(jsonPath("$.precioFinal").value(50.0));
    }

    @Test
    @DisplayName("HU8-CP14: un producto que no existe o no está disponible devuelve 404 con el código PRODUCT_NOT_FOUND")
    void productoNoEncontradoDevuelve404() throws Exception {
        // El servicio trata igual un producto inexistente que uno no disponible: lanza la misma excepción
        when(catalogoProductos.consultarDetalle("p-404", CLIENTE)).thenThrow(new ProductoNoEncontradoException());

        mockMvc.perform(get(URL + "/p-404").with(user(CLIENTE).roles("CLIENTE")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"));
    }

    @Test
    @DisplayName("HU8-CP15: sin sesión no se ve el detalle de un producto")
    void sinSesionElDetalleDevuelve401() throws Exception {
        mockMvc.perform(get(URL + "/p-1"))
                .andExpect(status().isUnauthorized());

        verify(catalogoProductos, never()).consultarDetalle(any(), any());
    }

    @ParameterizedTest(name = "rol {0}")
    @ValueSource(strings = {"VENDEDOR", "ADMINISTRADOR"})
    @DisplayName("HU8-CP15: un usuario que no es cliente no ve el detalle de un producto")
    void otroRolElDetalleDevuelve403(String rol) throws Exception {
        // Con el caso de uso respondiendo, un 200 indicaría que el rol tiene acceso
        when(catalogoProductos.consultarDetalle(any(), any()))
                .thenReturn(detalle(new BigDecimal("10"), new BigDecimal("45.00")));

        mockMvc.perform(get(URL + "/p-1").with(user("usuario@esibuy.test").roles(rol)))
                .andExpect(status().isForbidden());

        verify(catalogoProductos, never()).consultarDetalle(any(), any());
    }
}
