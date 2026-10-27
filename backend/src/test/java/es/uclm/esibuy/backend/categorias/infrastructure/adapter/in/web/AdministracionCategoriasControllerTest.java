package es.uclm.esibuy.backend.categorias.infrastructure.adapter.in.web;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import es.uclm.esibuy.backend.categorias.application.port.in.AdministracionCategoriasUseCase;
import es.uclm.esibuy.backend.categorias.application.port.in.CategoriaConProductos;
import es.uclm.esibuy.backend.categorias.domain.model.Categoria;
import es.uclm.esibuy.backend.categorias.domain.model.EstadoCategoria;
import es.uclm.esibuy.backend.shared.config.SecurityConfig;

// Listado de las categorías del administrador (HU39, T331): GET /api/admin/categorias. El caso de uso es un
// doble de prueba; la seguridad es la real (SecurityConfig).
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: casos HU39-CP01 y CP04, propuestos.
@WebMvcTest(AdministracionCategoriasController.class)
@Import(SecurityConfig.class)
class AdministracionCategoriasControllerTest {

    private static final String RUTA = "/api/admin/categorias";

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
}
