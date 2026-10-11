package es.uclm.esibuy.backend.categorias.infrastructure.adapter.in.web;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import es.uclm.esibuy.backend.categorias.application.port.in.ConsultaCategoriasUseCase;
import es.uclm.esibuy.backend.categorias.domain.model.Categoria;
import es.uclm.esibuy.backend.shared.config.SecurityConfig;

// Consulta de las categorías (AB#43): GET /api/categorias, con el formato que ya usan los formularios del
// frontend ([{id, nombre}]). El caso de uso es un doble de prueba; la seguridad es la real (SecurityConfig).
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: caso HU22-CP15, propuesto.
@WebMvcTest(CategoriaController.class)
@Import(SecurityConfig.class)
class CategoriaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ConsultaCategoriasUseCase consultaCategorias;

    // El registro del vendedor elige su categoría antes de tener cuenta: el listado es público.
    @Test
    @DisplayName("HU22-CP15: sin sesión se reciben las categorías con su id y su nombre")
    void sinSesionSeRecibenLasCategorias() throws Exception {
        when(consultaCategorias.listar()).thenReturn(List.of(new Categoria("c-1", "Hogar")));

        mockMvc.perform(get("/api/categorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value("c-1"))
                .andExpect(jsonPath("$[0].nombre").value("Hogar"));
    }

    @Test
    @DisplayName("HU22-CP15: un administrador recibe las categorías para editar a un vendedor")
    void unAdministradorRecibeLasCategorias() throws Exception {
        when(consultaCategorias.listar()).thenReturn(List.of(new Categoria("c-1", "Hogar")));

        mockMvc.perform(get("/api/categorias").with(user("admin").roles("ADMINISTRADOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Hogar"));
    }
}
