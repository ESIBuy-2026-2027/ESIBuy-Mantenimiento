package es.uclm.esibuy.backend.productos.infrastructure.adapter.in.web;

import es.uclm.esibuy.backend.productos.application.port.in.CrearProductoCommand;
import es.uclm.esibuy.backend.productos.application.port.in.GestionProductosUseCase;
import es.uclm.esibuy.backend.productos.application.port.in.ListadoProductosUseCase;
import es.uclm.esibuy.backend.productos.application.port.in.ListarProductosQuery;
import es.uclm.esibuy.backend.productos.application.port.in.ModificarProductoCommand;
import es.uclm.esibuy.backend.productos.domain.exception.ProductoDuplicadoException;
import es.uclm.esibuy.backend.productos.domain.exception.ProductoInvalidoException;
import es.uclm.esibuy.backend.productos.domain.exception.ProductoNoEncontradoException;
import es.uclm.esibuy.backend.productos.domain.model.Descuento;
import es.uclm.esibuy.backend.productos.domain.model.Producto;
import es.uclm.esibuy.backend.productos.domain.model.ProductoPage;
import es.uclm.esibuy.backend.shared.config.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Listado de los productos propios del vendedor (HU32, T184): GET /api/seller/products, y alta de un producto
// (HU31, T205 y T179): POST /api/seller/products, y consulta de un producto propio (HU33, T348):
// GET /api/seller/products/{id}, modificación de un producto propio (HU34, T342): PUT /api/seller/products/{id},
// eliminación de un producto propio (HU35, T345): DELETE /api/seller/products/{id}, y activación y desactivación
// de un producto propio (HU16, T282 y T283): PATCH /api/seller/products/{id}/availability.
// Los casos de uso son dobles de prueba; la seguridad es la real
// (SecurityConfig: /api/seller/** solo para VENDEDOR, con CSRF).
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: casos HU32-CP01 a CP05 y HU31-CP17 a CP22, propuestos.
@WebMvcTest(ProductoController.class)
@Import(SecurityConfig.class)
class ProductoControllerTest {

    private static final String URL = "/api/seller/products";
    private static final String VENDEDOR = "vendedor@esibuy.test";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ListadoProductosUseCase listadoProductos;

    @MockitoBean
    private GestionProductosUseCase gestionProductos;

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

    // ---- Alta de producto (HU31) ----

    private static final String SIN_DESCUENTO = "{\"activo\": false, \"porcentaje\": null}";

    @Test
    @DisplayName("HU31-CP17: el vendedor crea un producto y recibe 201 con el producto creado")
    void vendedorCreaUnProducto() throws Exception {
        Producto creado = new Producto("p-9", "v-1", "Lámpara de pie", "Con regulador", new BigDecimal("45.50"), 7,
                "cat-2", "https://esibuy.test/lampara.png", true, new Descuento(true, new BigDecimal("10")),
                Descuento.sinDescuento());
        when(gestionProductos.crear(any())).thenReturn(creado);

        mockMvc.perform(post(URL).with(csrf()).with(user(VENDEDOR).roles("VENDEDOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre": "Lámpara de pie", "descripcion": "Con regulador", "precio": 45.5,
                                 "stock": 7, "categoriaId": "cat-2", "imagen": "https://esibuy.test/lampara.png",
                                 "disponible": true, "descuento": {"activo": true, "porcentaje": 10},
                                 "descuentoPremium": {"activo": false, "porcentaje": null}}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("p-9"))
                .andExpect(jsonPath("$.nombre").value("Lámpara de pie"))
                .andExpect(jsonPath("$.descripcion").value("Con regulador"))
                .andExpect(jsonPath("$.precio").value(45.5))
                .andExpect(jsonPath("$.stock").value(7))
                .andExpect(jsonPath("$.categoriaId").value("cat-2"))
                .andExpect(jsonPath("$.imagen").value("https://esibuy.test/lampara.png"))
                .andExpect(jsonPath("$.disponible").value(true))
                .andExpect(jsonPath("$.descuento.activo").value(true))
                .andExpect(jsonPath("$.descuento.porcentaje").value(10))
                .andExpect(jsonPath("$.descuentoPremium.activo").value(false))
                .andExpect(jsonPath("$.vendedorId").doesNotExist());

        ArgumentCaptor<CrearProductoCommand> captor = ArgumentCaptor.forClass(CrearProductoCommand.class);
        verify(gestionProductos).crear(captor.capture());
        CrearProductoCommand comando = captor.getValue();
        assertEquals(VENDEDOR, comando.emailVendedor());
        assertEquals("Lámpara de pie", comando.nombre());
        assertEquals(0, new BigDecimal("45.5").compareTo(comando.precio()));
        assertEquals(7, comando.stock());
        assertEquals("cat-2", comando.categoriaId());
        assertEquals(true, comando.descuento().activo());
        assertEquals(0, BigDecimal.TEN.compareTo(comando.descuento().porcentaje()));
        assertEquals(false, comando.descuentoPremium().activo());
    }

    @Test
    @DisplayName("HU31-CP18: el vendedor sale de la sesión: un vendedorId en el cuerpo se ignora")
    void elVendedorSaleDeLaSesionYNoDelCuerpo() throws Exception {
        Producto creado = new Producto("p-9", "v-1", "Teclado", "", new BigDecimal("10"), 1, "cat-1", "", true,
                Descuento.sinDescuento(), Descuento.sinDescuento());
        when(gestionProductos.crear(any())).thenReturn(creado);
        String conVendedor = cuerpoValido().replace("{\"nombre\"", "{\"vendedorId\": \"v-otro\", \"nombre\"");

        mockMvc.perform(post(URL).with(csrf()).with(user(VENDEDOR).roles("VENDEDOR"))
                        .contentType(MediaType.APPLICATION_JSON).content(conVendedor))
                .andExpect(status().isCreated());

        ArgumentCaptor<CrearProductoCommand> captor = ArgumentCaptor.forClass(CrearProductoCommand.class);
        verify(gestionProductos).crear(captor.capture());
        assertEquals(VENDEDOR, captor.getValue().emailVendedor());
    }

    @Test
    @DisplayName("HU31-CP19: sin sesión no se puede crear un producto")
    void sinSesionNoSeCreaUnProducto() throws Exception {
        mockMvc.perform(post(URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(cuerpoValido()))
                .andExpect(status().isUnauthorized());

        verify(gestionProductos, never()).crear(any());
    }

    @ParameterizedTest(name = "rol {0}")
    @ValueSource(strings = {"CLIENTE", "ADMINISTRADOR"})
    @DisplayName("HU31-CP19: un usuario que no es vendedor no puede crear un producto")
    void noVendedorNoCreaUnProducto(String rol) throws Exception {
        mockMvc.perform(post(URL).with(csrf()).with(user("usuario@esibuy.test").roles(rol))
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpoValido()))
                .andExpect(status().isForbidden());

        verify(gestionProductos, never()).crear(any());
    }

    @Test
    @DisplayName("HU31-CP19: una petición sin token CSRF se rechaza")
    void sinTokenCsrfNoSeCreaUnProducto() throws Exception {
        mockMvc.perform(post(URL).with(user(VENDEDOR).roles("VENDEDOR"))
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpoValido()))
                .andExpect(status().isForbidden());

        verify(gestionProductos, never()).crear(any());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("cuerposInvalidos")
    @DisplayName("HU31-CP20: un cuerpo con datos no válidos devuelve 400 y no llega al caso de uso")
    void cuerpoInvalidoDevuelve400(String descripcion, String cuerpo) throws Exception {
        mockMvc.perform(post(URL).with(csrf()).with(user(VENDEDOR).roles("VENDEDOR"))
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        verify(gestionProductos, never()).crear(any());
    }

    @Test
    @DisplayName("HU31-CP20: un cuerpo que no es JSON válido devuelve 400")
    void cuerpoQueNoEsJsonDevuelve400() throws Exception {
        mockMvc.perform(post(URL).with(csrf()).with(user(VENDEDOR).roles("VENDEDOR"))
                        .contentType(MediaType.APPLICATION_JSON).content("{esto no es json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.mensaje").value("Los datos enviados no son válidos"));

        verify(gestionProductos, never()).crear(any());
    }

    @Test
    @DisplayName("HU31-CP21: una regla del dominio incumplida devuelve 400 con un mensaje genérico")
    void reglaDeDominioIncumplidaDevuelve400Generico() throws Exception {
        when(gestionProductos.crear(any()))
                .thenThrow(new ProductoInvalidoException("detalle interno que no debe salir"));

        mockMvc.perform(post(URL).with(csrf()).with(user(VENDEDOR).roles("VENDEDOR"))
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpoValido()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.mensaje").value(not(containsString("detalle interno"))));
    }

    @Test
    @DisplayName("HU31-CP22: un nombre repetido del mismo vendedor devuelve 409 con el código PRODUCT_NAME_ALREADY_EXISTS")
    void nombreRepetidoDevuelve409() throws Exception {
        when(gestionProductos.crear(any())).thenThrow(new ProductoDuplicadoException());

        mockMvc.perform(post(URL).with(csrf()).with(user(VENDEDOR).roles("VENDEDOR"))
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpoValido()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PRODUCT_NAME_ALREADY_EXISTS"));
    }

    // ---- Consulta de un producto propio (HU33) ----

    @Test
    @DisplayName("HU33-CP04: el vendedor recibe 200 con su producto en el formato del contrato y sin el id del vendedor")
    void vendedorConsultaSuProducto() throws Exception {
        Producto propio = new Producto("p-9", "v-1", "Lámpara de pie", "Con regulador", new BigDecimal("45.50"), 7,
                "cat-2", "https://esibuy.test/lampara.png", true, new Descuento(true, new BigDecimal("10")),
                Descuento.sinDescuento());
        when(gestionProductos.consultarMiProducto(VENDEDOR, "p-9")).thenReturn(propio);

        mockMvc.perform(get(URL + "/p-9").with(user(VENDEDOR).roles("VENDEDOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("p-9"))
                .andExpect(jsonPath("$.nombre").value("Lámpara de pie"))
                .andExpect(jsonPath("$.descripcion").value("Con regulador"))
                .andExpect(jsonPath("$.precio").value(45.5))
                .andExpect(jsonPath("$.stock").value(7))
                .andExpect(jsonPath("$.categoriaId").value("cat-2"))
                .andExpect(jsonPath("$.imagen").value("https://esibuy.test/lampara.png"))
                .andExpect(jsonPath("$.disponible").value(true))
                .andExpect(jsonPath("$.descuento.activo").value(true))
                .andExpect(jsonPath("$.descuento.porcentaje").value(10))
                .andExpect(jsonPath("$.descuentoPremium.activo").value(false))
                .andExpect(jsonPath("$.vendedorId").doesNotExist());
    }

    @Test
    @DisplayName("HU33-CP05: un producto inexistente o de otro vendedor devuelve 404 con el código PRODUCT_NOT_FOUND")
    void productoNoEncontradoDevuelve404() throws Exception {
        when(gestionProductos.consultarMiProducto(VENDEDOR, "p-404")).thenThrow(new ProductoNoEncontradoException());

        mockMvc.perform(get(URL + "/p-404").with(user(VENDEDOR).roles("VENDEDOR")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"));
    }

    @Test
    @DisplayName("HU33-CP06: sin sesión no se consulta un producto")
    void sinSesionNoSeConsultaUnProducto() throws Exception {
        mockMvc.perform(get(URL + "/p-9"))
                .andExpect(status().isUnauthorized());

        verify(gestionProductos, never()).consultarMiProducto(any(), any());
    }

    @ParameterizedTest(name = "rol {0}")
    @ValueSource(strings = {"CLIENTE", "ADMINISTRADOR"})
    @DisplayName("HU33-CP06: un usuario que no es vendedor no consulta un producto")
    void noVendedorNoConsultaUnProducto(String rol) throws Exception {
        mockMvc.perform(get(URL + "/p-9").with(user("usuario@esibuy.test").roles(rol)))
                .andExpect(status().isForbidden());

        verify(gestionProductos, never()).consultarMiProducto(any(), any());
    }

    // ---- Modificación de un producto propio (HU34) ----

    @Test
    @DisplayName("HU34-CP06: el vendedor modifica su producto y recibe 200 con el producto actualizado; el vendedor sale de la sesión")
    void vendedorModificaSuProducto() throws Exception {
        Producto modificado = new Producto("p-9", "v-1", "Lámpara de pie LED", "Con regulador", new BigDecimal("39.90"),
                3, "cat-2", "https://esibuy.test/lampara.png", false, Descuento.sinDescuento(),
                new Descuento(true, new BigDecimal("15")));
        when(gestionProductos.modificarMiProducto(any())).thenReturn(modificado);
        String conVendedor = cuerpo("\"Lámpara de pie LED\"", "39.9", "3", "\"cat-2\"", "\"\"", SIN_DESCUENTO)
                .replace("{\"nombre\"", "{\"vendedorId\": \"v-otro\", \"nombre\"");

        mockMvc.perform(put(URL + "/p-9").with(csrf()).with(user(VENDEDOR).roles("VENDEDOR"))
                        .contentType(MediaType.APPLICATION_JSON).content(conVendedor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("p-9"))
                .andExpect(jsonPath("$.nombre").value("Lámpara de pie LED"))
                .andExpect(jsonPath("$.precio").value(39.9))
                .andExpect(jsonPath("$.stock").value(3))
                .andExpect(jsonPath("$.disponible").value(false))
                .andExpect(jsonPath("$.descuentoPremium.porcentaje").value(15))
                .andExpect(jsonPath("$.vendedorId").doesNotExist());

        ArgumentCaptor<ModificarProductoCommand> captor = ArgumentCaptor.forClass(ModificarProductoCommand.class);
        verify(gestionProductos).modificarMiProducto(captor.capture());
        ModificarProductoCommand comando = captor.getValue();
        assertEquals(VENDEDOR, comando.emailVendedor());
        assertEquals("p-9", comando.productoId());
        assertEquals("Lámpara de pie LED", comando.nombre());
        assertEquals(0, new BigDecimal("39.9").compareTo(comando.precio()));
        assertEquals(3, comando.stock());
        assertEquals("cat-2", comando.categoriaId());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("cuerposInvalidos")
    @DisplayName("HU34-CP07: un cuerpo con datos no válidos devuelve 400 y no llega al caso de uso")
    void cuerpoInvalidoNoModificaElProducto(String descripcion, String cuerpo) throws Exception {
        mockMvc.perform(put(URL + "/p-9").with(csrf()).with(user(VENDEDOR).roles("VENDEDOR"))
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        verify(gestionProductos, never()).modificarMiProducto(any());
    }

    @Test
    @DisplayName("HU34-CP07: una regla del dominio incumplida al modificar devuelve 400 con un mensaje genérico")
    void reglaDeDominioIncumplidaAlModificarDevuelve400Generico() throws Exception {
        when(gestionProductos.modificarMiProducto(any()))
                .thenThrow(new ProductoInvalidoException("detalle interno que no debe salir"));

        mockMvc.perform(put(URL + "/p-9").with(csrf()).with(user(VENDEDOR).roles("VENDEDOR"))
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpoValido()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.mensaje").value(not(containsString("detalle interno"))));
    }

    @Test
    @DisplayName("HU34-CP08: cambiar el nombre por uno repetido del mismo vendedor devuelve 409 con el código PRODUCT_NAME_ALREADY_EXISTS")
    void nombreRepetidoAlModificarDevuelve409() throws Exception {
        when(gestionProductos.modificarMiProducto(any())).thenThrow(new ProductoDuplicadoException());

        mockMvc.perform(put(URL + "/p-9").with(csrf()).with(user(VENDEDOR).roles("VENDEDOR"))
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpoValido()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PRODUCT_NAME_ALREADY_EXISTS"));
    }

    @Test
    @DisplayName("HU34-CP09: modificar un producto inexistente o de otro vendedor devuelve 404 con el código PRODUCT_NOT_FOUND")
    void modificarUnProductoNoEncontradoDevuelve404() throws Exception {
        when(gestionProductos.modificarMiProducto(any())).thenThrow(new ProductoNoEncontradoException());

        mockMvc.perform(put(URL + "/p-404").with(csrf()).with(user(VENDEDOR).roles("VENDEDOR"))
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpoValido()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"));
    }

    @Test
    @DisplayName("HU34-CP10: sin sesión no se puede modificar un producto")
    void sinSesionNoSeModificaUnProducto() throws Exception {
        mockMvc.perform(put(URL + "/p-9").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(cuerpoValido()))
                .andExpect(status().isUnauthorized());

        verify(gestionProductos, never()).modificarMiProducto(any());
    }

    @ParameterizedTest(name = "rol {0}")
    @ValueSource(strings = {"CLIENTE", "ADMINISTRADOR"})
    @DisplayName("HU34-CP10: un usuario que no es vendedor no puede modificar un producto")
    void noVendedorNoModificaUnProducto(String rol) throws Exception {
        mockMvc.perform(put(URL + "/p-9").with(csrf()).with(user("usuario@esibuy.test").roles(rol))
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpoValido()))
                .andExpect(status().isForbidden());

        verify(gestionProductos, never()).modificarMiProducto(any());
    }

    @Test
    @DisplayName("HU34-CP10: una modificación sin token CSRF se rechaza")
    void sinTokenCsrfNoSeModificaUnProducto() throws Exception {
        mockMvc.perform(put(URL + "/p-9").with(user(VENDEDOR).roles("VENDEDOR"))
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpoValido()))
                .andExpect(status().isForbidden());

        verify(gestionProductos, never()).modificarMiProducto(any());
    }

    // ---- Eliminación de un producto propio (HU35) ----

    @Test
    @DisplayName("HU35-CP05: el vendedor elimina su producto y recibe 204 sin cuerpo; el vendedor sale de la sesión")
    void vendedorEliminaSuProducto() throws Exception {
        mockMvc.perform(delete(URL + "/p-9").with(csrf()).with(user(VENDEDOR).roles("VENDEDOR")))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(gestionProductos).eliminarMiProducto(VENDEDOR, "p-9");
    }

    @Test
    @DisplayName("HU35-CP06: eliminar un producto inexistente o de otro vendedor devuelve 404 con el código PRODUCT_NOT_FOUND")
    void eliminarUnProductoNoEncontradoDevuelve404() throws Exception {
        doThrow(new ProductoNoEncontradoException()).when(gestionProductos).eliminarMiProducto(VENDEDOR, "p-404");

        mockMvc.perform(delete(URL + "/p-404").with(csrf()).with(user(VENDEDOR).roles("VENDEDOR")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"));
    }

    @Test
    @DisplayName("HU35-CP07: sin sesión no se puede eliminar un producto")
    void sinSesionNoSeEliminaUnProducto() throws Exception {
        mockMvc.perform(delete(URL + "/p-9").with(csrf()))
                .andExpect(status().isUnauthorized());

        verify(gestionProductos, never()).eliminarMiProducto(any(), any());
    }

    @ParameterizedTest(name = "rol {0}")
    @ValueSource(strings = {"CLIENTE", "ADMINISTRADOR"})
    @DisplayName("HU35-CP07: un usuario que no es vendedor no puede eliminar un producto")
    void noVendedorNoEliminaUnProducto(String rol) throws Exception {
        mockMvc.perform(delete(URL + "/p-9").with(csrf()).with(user("usuario@esibuy.test").roles(rol)))
                .andExpect(status().isForbidden());

        verify(gestionProductos, never()).eliminarMiProducto(any(), any());
    }

    @Test
    @DisplayName("HU35-CP07: una eliminación sin token CSRF se rechaza")
    void sinTokenCsrfNoSeEliminaUnProducto() throws Exception {
        mockMvc.perform(delete(URL + "/p-9").with(user(VENDEDOR).roles("VENDEDOR")))
                .andExpect(status().isForbidden());

        verify(gestionProductos, never()).eliminarMiProducto(any(), any());
    }

    @Test
    @DisplayName("HU16-CP11: el vendedor desactiva su producto y recibe 200 con el producto actualizado; el vendedor sale de la sesión")
    void vendedorCambiaLaDisponibilidadDeSuProducto() throws Exception {
        Producto desactivado = new Producto("p-9", "v-1", "Teclado mecánico", "Con cable", new BigDecimal("89.50"),
                12, "cat-1", "", false, Descuento.sinDescuento(), Descuento.sinDescuento());
        when(gestionProductos.cambiarDisponibilidadMiProducto(VENDEDOR, "p-9", false)).thenReturn(desactivado);

        mockMvc.perform(patch(URL + "/p-9/availability").with(csrf()).with(user(VENDEDOR).roles("VENDEDOR"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"disponible\": false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("p-9"))
                .andExpect(jsonPath("$.nombre").value("Teclado mecánico"))
                .andExpect(jsonPath("$.disponible").value(false))
                .andExpect(jsonPath("$.vendedorId").doesNotExist());

        verify(gestionProductos).cambiarDisponibilidadMiProducto(VENDEDOR, "p-9", false);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("cuerposDeDisponibilidadInvalidos")
    @DisplayName("HU16-CP12: un cuerpo sin disponible o con un valor que no es booleano devuelve 400 y no llega al caso de uso")
    void cuerpoDeDisponibilidadInvalidoNoCambiaElProducto(String descripcion, String cuerpo) throws Exception {
        mockMvc.perform(patch(URL + "/p-9/availability").with(csrf()).with(user(VENDEDOR).roles("VENDEDOR"))
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.mensaje").value("Los datos enviados no son válidos"));

        verify(gestionProductos, never()).cambiarDisponibilidadMiProducto(any(), any(), anyBoolean());
    }

    @Test
    @DisplayName("HU16-CP13: cambiar la disponibilidad de un producto inexistente o de otro vendedor devuelve 404 con el código PRODUCT_NOT_FOUND")
    void cambiarLaDisponibilidadDeUnProductoNoEncontradoDevuelve404() throws Exception {
        when(gestionProductos.cambiarDisponibilidadMiProducto(VENDEDOR, "p-404", true))
                .thenThrow(new ProductoNoEncontradoException());

        mockMvc.perform(patch(URL + "/p-404/availability").with(csrf()).with(user(VENDEDOR).roles("VENDEDOR"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"disponible\": true}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"));
    }

    @Test
    @DisplayName("HU16-CP14: sin sesión no se puede cambiar la disponibilidad de un producto")
    void sinSesionNoSeCambiaLaDisponibilidad() throws Exception {
        mockMvc.perform(patch(URL + "/p-9/availability").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"disponible\": false}"))
                .andExpect(status().isUnauthorized());

        verify(gestionProductos, never()).cambiarDisponibilidadMiProducto(any(), any(), anyBoolean());
    }

    @ParameterizedTest(name = "rol {0}")
    @ValueSource(strings = {"CLIENTE", "ADMINISTRADOR"})
    @DisplayName("HU16-CP14: un usuario que no es vendedor no puede cambiar la disponibilidad de un producto")
    void noVendedorNoCambiaLaDisponibilidad(String rol) throws Exception {
        mockMvc.perform(patch(URL + "/p-9/availability").with(csrf()).with(user("usuario@esibuy.test").roles(rol))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"disponible\": false}"))
                .andExpect(status().isForbidden());

        verify(gestionProductos, never()).cambiarDisponibilidadMiProducto(any(), any(), anyBoolean());
    }

    @Test
    @DisplayName("HU16-CP14: un cambio de disponibilidad sin token CSRF se rechaza")
    void sinTokenCsrfNoSeCambiaLaDisponibilidad() throws Exception {
        mockMvc.perform(patch(URL + "/p-9/availability").with(user(VENDEDOR).roles("VENDEDOR"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"disponible\": false}"))
                .andExpect(status().isForbidden());

        verify(gestionProductos, never()).cambiarDisponibilidadMiProducto(any(), any(), anyBoolean());
    }

    private static Stream<Arguments> cuerposDeDisponibilidadInvalidos() {
        return Stream.of(
                Arguments.of("sin disponible", "{}"),
                Arguments.of("disponible nulo", "{\"disponible\": null}"),
                Arguments.of("disponible que no es booleano", "{\"disponible\": \"abc\"}"),
                Arguments.of("disponible como texto \"true\"", "{\"disponible\": \"true\"}"),
                Arguments.of("disponible como número 1", "{\"disponible\": 1}"),
                Arguments.of("JSON mal formado", "{\"disponible\": "));
    }

    private static Stream<Arguments> cuerposInvalidos() {
        String nombreLargo = "\"" + "a".repeat(101) + "\"";
        return Stream.of(
                Arguments.of("nombre vacío", cuerpo("\"\"", "89.5", "12", "\"cat-1\"", "\"\"", SIN_DESCUENTO)),
                Arguments.of("nombre de más de 100 caracteres",
                        cuerpo(nombreLargo, "89.5", "12", "\"cat-1\"", "\"\"", SIN_DESCUENTO)),
                Arguments.of("precio cero", cuerpo("\"Teclado\"", "0", "12", "\"cat-1\"", "\"\"", SIN_DESCUENTO)),
                Arguments.of("precio negativo", cuerpo("\"Teclado\"", "-5", "12", "\"cat-1\"", "\"\"", SIN_DESCUENTO)),
                Arguments.of("precio ausente", cuerpo("\"Teclado\"", "null", "12", "\"cat-1\"", "\"\"", SIN_DESCUENTO)),
                Arguments.of("stock negativo", cuerpo("\"Teclado\"", "89.5", "-1", "\"cat-1\"", "\"\"", SIN_DESCUENTO)),
                Arguments.of("categoría vacía", cuerpo("\"Teclado\"", "89.5", "12", "\"\"", "\"\"", SIN_DESCUENTO)),
                Arguments.of("imagen con esquema javascript",
                        cuerpo("\"Teclado\"", "89.5", "12", "\"cat-1\"", "\"javascript:alert(1)\"", SIN_DESCUENTO)),
                Arguments.of("descuento activo sin porcentaje",
                        cuerpo("\"Teclado\"", "89.5", "12", "\"cat-1\"", "\"\"",
                                "{\"activo\": true, \"porcentaje\": null}")),
                Arguments.of("descuento con porcentaje mayor que 100",
                        cuerpo("\"Teclado\"", "89.5", "12", "\"cat-1\"", "\"\"",
                                "{\"activo\": true, \"porcentaje\": 101}")));
    }

    private static String cuerpoValido() {
        return cuerpo("\"Teclado mecánico\"", "89.5", "12", "\"cat-1\"", "\"\"", SIN_DESCUENTO);
    }

    // Cada argumento es un valor JSON ya escrito (con sus comillas), para poder probar también los nulos.
    private static String cuerpo(String nombre, String precio, String stock, String categoriaId, String imagen,
            String descuento) {
        return """
                {"nombre": %s, "descripcion": "", "precio": %s, "stock": %s, "categoriaId": %s, "imagen": %s,
                 "disponible": true, "descuento": %s,
                 "descuentoPremium": {"activo": false, "porcentaje": null}}
                """.formatted(nombre, precio, stock, categoriaId, imagen, descuento);
    }
}
