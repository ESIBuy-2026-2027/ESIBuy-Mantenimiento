package es.uclm.esibuy.backend.categorias.infrastructure.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import es.uclm.esibuy.backend.categorias.application.port.in.AdministracionCategoriasUseCase;
import es.uclm.esibuy.backend.categorias.application.port.in.CategoriaConProductos;
import es.uclm.esibuy.backend.categorias.application.port.in.CrearCategoriaCommand;
import es.uclm.esibuy.backend.categorias.domain.exception.CategoriaDuplicadaException;
import es.uclm.esibuy.backend.categorias.domain.exception.CategoriaInvalidaException;
import es.uclm.esibuy.backend.categorias.domain.model.Categoria;
import es.uclm.esibuy.backend.categorias.domain.model.EstadoCategoria;
import es.uclm.esibuy.backend.shared.config.SecurityConfig;

// Gestión de las categorías del administrador: listado (HU39, T331, GET /api/admin/categorias) y creación
// (HU40, T322, POST /api/admin/categorias). El caso de uso es un doble de prueba; la seguridad es la real
// (SecurityConfig) y los errores los traduce ManejadorGlobalErrores.
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: casos HU39-CP01 y CP04 y HU40-CP05 a CP08, propuestos.
@WebMvcTest(AdministracionCategoriasController.class)
@Import(SecurityConfig.class)
class AdministracionCategoriasControllerTest {

    private static final String RUTA = "/api/admin/categorias";
    private static final String CATEGORIA_VALIDA = """
            {"nombre": "Hogar", "descripcion": "Muebles y decoración", "estado": "INACTIVA"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdministracionCategoriasUseCase administracionCategorias;

    @Test
    @DisplayName("HU39-CP01: un administrador recibe cada categoría con su nombre, descripción, estado y número de productos")
    void unAdministradorRecibeLasCategoriasConSuNumeroDeProductos() throws Exception {
        when(administracionCategorias.listarConProductos()).thenReturn(List.of(
                new CategoriaConProductos(
                        new Categoria("c-1", "Hogar", "Muebles y decoración", EstadoCategoria.ACTIVA), 4),
                new CategoriaConProductos(new Categoria("c-2", "Juguetes", "", EstadoCategoria.INACTIVA), 0)));

        mockMvc.perform(get(RUTA).with(user("admin").roles("ADMINISTRADOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("c-1"))
                .andExpect(jsonPath("$[0].nombre").value("Hogar"))
                .andExpect(jsonPath("$[0].descripcion").value("Muebles y decoración"))
                .andExpect(jsonPath("$[0].estado").value("ACTIVA"))
                .andExpect(jsonPath("$[0].numeroProductos").value(4))
                .andExpect(jsonPath("$[1].estado").value("INACTIVA"))
                .andExpect(jsonPath("$[1].numeroProductos").value(0));
    }

    @ParameterizedTest(name = "rol {0}")
    @ValueSource(strings = {"CLIENTE", "VENDEDOR"})
    @DisplayName("HU39-CP04: un usuario sin rol de administrador recibe 403")
    void otroRolRecibe403(String rol) throws Exception {
        mockMvc.perform(get(RUTA).with(user("usuario").roles(rol)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(administracionCategorias);
    }

    @Test
    @DisplayName("HU39-CP04: sin sesión se recibe 401")
    void sinSesionRecibe401() throws Exception {
        mockMvc.perform(get(RUTA))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(administracionCategorias);
    }

    // Creación de categorías (HU40, T322).

    private static MockHttpServletRequestBuilder crear(String cuerpo) {
        return post(RUTA).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(cuerpo);
    }

    @Test
    @DisplayName("HU40-CP05: un administrador crea una categoría con el estado elegido y la recibe con 0 productos")
    void unAdministradorCreaUnaCategoria() throws Exception {
        when(administracionCategorias.crear(
                new CrearCategoriaCommand("Hogar", "Muebles y decoración", EstadoCategoria.INACTIVA)))
                .thenReturn(new Categoria("c-9", "Hogar", "Muebles y decoración", EstadoCategoria.INACTIVA));

        mockMvc.perform(crear(CATEGORIA_VALIDA).with(user("admin").roles("ADMINISTRADOR")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("c-9"))
                .andExpect(jsonPath("$.nombre").value("Hogar"))
                .andExpect(jsonPath("$.descripcion").value("Muebles y decoración"))
                .andExpect(jsonPath("$.estado").value("INACTIVA"))
                .andExpect(jsonPath("$.numeroProductos").value(0));
    }

    @Test
    @DisplayName("HU40-CP06: crear una categoría con un nombre que ya existe devuelve 409")
    void crearUnNombreRepetidoDevuelve409() throws Exception {
        when(administracionCategorias.crear(any())).thenThrow(new CategoriaDuplicadaException());

        mockMvc.perform(crear(CATEGORIA_VALIDA).with(user("admin").roles("ADMINISTRADOR")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CATEGORY_NAME_ALREADY_EXISTS"));
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {
            "{\"nombre\": \" \", \"estado\": \"ACTIVA\"}",
            "{\"nombre\": \"Hogar\"}",
            "{\"nombre\": \"Hogar\", \"estado\": \"BORRADA\"}"})
    @DisplayName("HU40-CP07: crear sin nombre, sin estado o con un estado desconocido devuelve 400")
    void crearConDatosInvalidosDevuelve400(String cuerpo) throws Exception {
        mockMvc.perform(crear(cuerpo).with(user("admin").roles("ADMINISTRADOR")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_FIELD"));

        verifyNoInteractions(administracionCategorias);
    }

    @Test
    @DisplayName("HU40-CP07: crear una categoría que incumple una regla del dominio devuelve 400")
    void crearConUnaReglaDelDominioIncumplidaDevuelve400() throws Exception {
        when(administracionCategorias.crear(any()))
                .thenThrow(new CategoriaInvalidaException("El nombre no puede superar los 50 caracteres"));

        mockMvc.perform(crear(CATEGORIA_VALIDA).with(user("admin").roles("ADMINISTRADOR")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @ParameterizedTest(name = "rol {0}")
    @ValueSource(strings = {"CLIENTE", "VENDEDOR"})
    @DisplayName("HU40-CP08: un usuario sin rol de administrador no puede crear categorías (403)")
    void crearConOtroRolDevuelve403(String rol) throws Exception {
        mockMvc.perform(crear(CATEGORIA_VALIDA).with(user("usuario").roles(rol)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(administracionCategorias);
    }

    @Test
    @DisplayName("HU40-CP08: sin sesión no se pueden crear categorías (401)")
    void crearSinSesionDevuelve401() throws Exception {
        mockMvc.perform(crear(CATEGORIA_VALIDA))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(administracionCategorias);
    }
}
