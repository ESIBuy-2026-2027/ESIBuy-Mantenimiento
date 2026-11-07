package es.uclm.esibuy.backend.incidencias.infrastructure.adapter.in.web;

import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;

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
import org.springframework.test.web.servlet.ResultActions;

import es.uclm.esibuy.backend.incidencias.application.port.in.SupervisionIncidenciasUseCase;
import es.uclm.esibuy.backend.incidencias.domain.exception.IncidenciaInvalidaException;
import es.uclm.esibuy.backend.incidencias.domain.model.AutorIncidencia;
import es.uclm.esibuy.backend.incidencias.domain.model.EstadoIncidencia;
import es.uclm.esibuy.backend.incidencias.domain.model.Incidencia;
import es.uclm.esibuy.backend.incidencias.domain.model.IncidenciaSupervisada;
import es.uclm.esibuy.backend.incidencias.domain.model.IncidenciaSupervisadaPage;
import es.uclm.esibuy.backend.shared.config.SecurityConfig;

// Supervisión de incidencias por el administrador (HU49, T306): GET /api/admin/incidencias. El caso de uso es un
// doble de prueba; la seguridad es la real (SecurityConfig: /api/admin/** solo para ADMINISTRADOR).
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: casos HU49-CP11 a CP14, propuestos.
@WebMvcTest(AdministracionIncidenciasController.class)
@Import(SecurityConfig.class)
class AdministracionIncidenciasControllerTest {

    private static final String URL = "/api/admin/incidencias";
    private static final String ADMINISTRADOR = "admin@esibuy.test";
    private static final Instant AHORA = Instant.parse("2026-11-06T10:15:30Z");
    private static final AutorIncidencia CLIENTE = new AutorIncidencia("cliente@esibuy.test", "CLIENTE");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SupervisionIncidenciasUseCase supervisionIncidencias;

    private static IncidenciaSupervisada supervisada(AutorIncidencia autor) {
        Incidencia incidencia = new Incidencia("i-1", "c-1", "No me llega el pedido", "Lo pedi hace una semana",
                EstadoIncidencia.PENDIENTE, "", AHORA);
        return new IncidenciaSupervisada(incidencia, autor);
    }

    private ResultActions consultarComoAdministrador(String consulta) throws Exception {
        return mockMvc.perform(get(URL + consulta).with(user(ADMINISTRADOR).roles("ADMINISTRADOR")));
    }

    @Test
    @DisplayName("HU49-CP11: responde 200 con la página y el autor de cada incidencia, y por defecto pide la 1 de tamaño 10")
    void responde200ConLaPaginaYElAutor() throws Exception {
        when(supervisionIncidencias.consultarTodas(0, 10))
                .thenReturn(new IncidenciaSupervisadaPage(List.of(supervisada(CLIENTE)), 0, 10, 1));

        consultarComoAdministrador("")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].id").value("i-1"))
                .andExpect(jsonPath("$.items[0].asunto").value("No me llega el pedido"))
                .andExpect(jsonPath("$.items[0].descripcion").value("Lo pedi hace una semana"))
                .andExpect(jsonPath("$.items[0].estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.items[0].respuesta").value(""))
                .andExpect(jsonPath("$.items[0].fechaCreacion").value("2026-11-06T10:15:30Z"))
                .andExpect(jsonPath("$.items[0].autor.email").value("cliente@esibuy.test"))
                .andExpect(jsonPath("$.items[0].autor.rol").value("CLIENTE"))
                .andExpect(jsonPath("$.items[0].autorId").doesNotExist())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalItems").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    @DisplayName("HU49-CP11: la página empieza en 1: page=3 y size=2 piden la página 2 (desde 0) y se devuelve con page 3")
    void laPaginaEmpiezaEnUno() throws Exception {
        when(supervisionIncidencias.consultarTodas(2, 2))
                .thenReturn(new IncidenciaSupervisadaPage(List.of(supervisada(CLIENTE)), 2, 2, 5));

        consultarComoAdministrador("?page=3&size=2")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(3))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalItems").value(5))
                .andExpect(jsonPath("$.totalPages").value(3));
    }

    @Test
    @DisplayName("HU49-CP11: sin incidencias responde 200 con una lista vacía y cero páginas")
    void sinIncidenciasResponde200ConUnaListaVacia() throws Exception {
        when(supervisionIncidencias.consultarTodas(0, 10)).thenReturn(new IncidenciaSupervisadaPage(List.of(), 0, 10, 0));

        consultarComoAdministrador("")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(0))
                .andExpect(jsonPath("$.totalItems").value(0))
                .andExpect(jsonPath("$.totalPages").value(0));
    }

    @Test
    @DisplayName("HU49-CP12: una incidencia cuya cuenta se ha eliminado viene con autor null")
    void unaCuentaEliminadaVieneConAutorNull() throws Exception {
        when(supervisionIncidencias.consultarTodas(0, 10))
                .thenReturn(new IncidenciaSupervisadaPage(List.of(supervisada(null)), 0, 10, 1));

        consultarComoAdministrador("")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value("i-1"))
                .andExpect(jsonPath("$.items[0]", hasKey("autor")))
                .andExpect(jsonPath("$.items[0].autor").value(nullValue()));
    }

    @ParameterizedTest(name = "consulta {0}")
    @ValueSource(strings = {"?page=0", "?size=0", "?size=51"})
    @DisplayName("HU49-CP13: una página o un tamaño que el caso de uso rechaza responden 400 VALIDATION_ERROR")
    void unaPaginaInvalidaResponde400(String consulta) throws Exception {
        when(supervisionIncidencias.consultarTodas(anyInt(), anyInt()))
                .thenThrow(new IncidenciaInvalidaException("detalle interno"));

        consultarComoAdministrador(consulta)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("HU49-CP13: una página que no es un número responde 400 y no llega al caso de uso")
    void unaPaginaQueNoEsUnNumeroResponde400() throws Exception {
        consultarComoAdministrador("?page=abc").andExpect(status().isBadRequest());

        verify(supervisionIncidencias, never()).consultarTodas(anyInt(), anyInt());
    }

    @Test
    @DisplayName("HU49-CP14: sin sesión responde 401")
    void sinSesionResponde401() throws Exception {
        mockMvc.perform(get(URL)).andExpect(status().isUnauthorized());

        verify(supervisionIncidencias, never()).consultarTodas(anyInt(), anyInt());
    }

    @ParameterizedTest(name = "rol {1}")
    @CsvSource({"cliente@esibuy.test,CLIENTE", "vendedor@esibuy.test,VENDEDOR"})
    @DisplayName("HU49-CP14: un cliente o un vendedor reciben 403 con el code ACCESS_DENIED")
    void clienteYVendedorReciben403(String email, String rol) throws Exception {
        mockMvc.perform(get(URL).with(user(email).roles(rol)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));

        verify(supervisionIncidencias, never()).consultarTodas(anyInt(), anyInt());
    }
}
