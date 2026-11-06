package es.uclm.esibuy.backend.incidencias.infrastructure.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.time.Instant;
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
import org.springframework.test.web.servlet.ResultActions;

import es.uclm.esibuy.backend.incidencias.application.port.in.CreacionIncidenciasUseCase;
import es.uclm.esibuy.backend.incidencias.application.port.in.CrearIncidenciaCommand;
import es.uclm.esibuy.backend.incidencias.domain.exception.IncidenciaInvalidaException;
import es.uclm.esibuy.backend.incidencias.domain.model.EstadoIncidencia;
import es.uclm.esibuy.backend.incidencias.domain.model.Incidencia;
import es.uclm.esibuy.backend.shared.config.SecurityConfig;
import es.uclm.esibuy.backend.incidencias.application.port.in.ConsultaIncidenciasUseCase;
import es.uclm.esibuy.backend.incidencias.domain.model.IncidenciaPage;

// Crear una incidencia (HU44, T292): POST /api/perfil/incidencias, para el cliente y el vendedor. El caso de uso es
// un doble de prueba; la seguridad es la real (SecurityConfig: /api/perfil/** solo para CLIENTE y VENDEDOR, con CSRF).
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: casos HU44-CP13 a CP15, propuestos.
@WebMvcTest(IncidenciaController.class)
@Import(SecurityConfig.class)
class IncidenciaControllerTest {

    private static final String URL = "/api/perfil/incidencias";
    private static final String CLIENTE = "cliente@esibuy.test";
    private static final String VENDEDOR = "vendedor@esibuy.test";
    private static final String ADMINISTRADOR = "admin@esibuy.test";
    private static final Instant AHORA = Instant.parse("2026-11-06T10:15:30Z");
    // autorId en el cuerpo se ignora: el autor sale siempre de la sesión
    private static final String CUERPO =
            "{\"asunto\": \"No me llega el pedido\", \"descripcion\": \"Lo pedi hace una semana\", \"autorId\": \"otro\"}";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreacionIncidenciasUseCase creacionIncidencias;

    @MockitoBean
    private ConsultaIncidenciasUseCase consultaIncidencias;

    private static CrearIncidenciaCommand comando(String email) {
        return new CrearIncidenciaCommand(email, "No me llega el pedido", "Lo pedi hace una semana");
    }

    private static Incidencia incidenciaDe(String autorId) {
        return new Incidencia("i-1", autorId, "No me llega el pedido", "Lo pedi hace una semana",
                EstadoIncidencia.PENDIENTE, "", AHORA);
    }

    private ResultActions crearComoCliente(String cuerpo) throws Exception {
        return mockMvc.perform(post(URL).with(user(CLIENTE).roles("CLIENTE")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(cuerpo));
    }

    private ResultActions consultarComoCliente(String consulta) throws Exception {
        return mockMvc.perform(get(URL + consulta).with(user(CLIENTE).roles("CLIENTE")));
    }

    @Test
    @DisplayName("HU44-CP13: crear una incidencia responde 201 con la incidencia creada, con el cliente de la sesión")
    void crearUnaIncidenciaResponde201() throws Exception {
        when(creacionIncidencias.crear(comando(CLIENTE))).thenReturn(incidenciaDe("c-1"));

        crearComoCliente(CUERPO)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("i-1"))
                .andExpect(jsonPath("$.asunto").value("No me llega el pedido"))
                .andExpect(jsonPath("$.descripcion").value("Lo pedi hace una semana"))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.respuesta").value(""))
                .andExpect(jsonPath("$.fechaCreacion").value("2026-11-06T10:15:30Z"))
                .andExpect(jsonPath("$.autorId").doesNotExist());
    }

    @Test
    @DisplayName("HU44-CP13: un vendedor también crea incidencias: el caso de uso recibe su correo")
    void unVendedorCreaUnaIncidencia() throws Exception {
        when(creacionIncidencias.crear(comando(VENDEDOR))).thenReturn(incidenciaDe("v-1"));

        mockMvc.perform(post(URL).with(user(VENDEDOR).roles("VENDEDOR")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(CUERPO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("i-1"));
    }

    @Test
    @DisplayName("HU44-CP14: una incidencia que no cumple las reglas responde 400 VALIDATION_ERROR con un mensaje genérico")
    void unaIncidenciaInvalidaResponde400() throws Exception {
        when(creacionIncidencias.crear(any())).thenThrow(new IncidenciaInvalidaException("detalle interno"));

        crearComoCliente(CUERPO)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.mensaje").value("La incidencia es inválida"));
    }

    @Test
    @DisplayName("HU44-CP14: un cuerpo sin asunto ni descripción llega al caso de uso vacío y responde 400")
    void unCuerpoSinCamposResponde400() throws Exception {
        when(creacionIncidencias.crear(new CrearIncidenciaCommand(CLIENTE, null, null)))
                .thenThrow(new IncidenciaInvalidaException("sin asunto"));

        crearComoCliente("{}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("HU44-CP14: un cuerpo que no se puede leer responde 400 VALIDATION_ERROR y no llega al caso de uso")
    void unCuerpoIlegibleResponde400() throws Exception {
        crearComoCliente("{no es json")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        verify(creacionIncidencias, never()).crear(any());
    }

    @Test
    @DisplayName("HU44-CP15: sin sesión crear una incidencia responde 401")
    void sinSesionResponde401() throws Exception {
        mockMvc.perform(post(URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(CUERPO))
                .andExpect(status().isUnauthorized());

        verify(creacionIncidencias, never()).crear(any());
    }

    @Test
    @DisplayName("HU44-CP15: el administrador no crea incidencias: responde 403 con el code ACCESS_DENIED")
    void elAdministradorRecibe403() throws Exception {
        mockMvc.perform(post(URL).with(user(ADMINISTRADOR).roles("ADMINISTRADOR")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(CUERPO))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));

        verify(creacionIncidencias, never()).crear(any());
    }

    @Test
    @DisplayName("HU44-CP15: sin token CSRF la petición se rechaza")
    void sinCsrfSeRechaza() throws Exception {
        mockMvc.perform(post(URL).with(user(CLIENTE).roles("CLIENTE"))
                        .contentType(MediaType.APPLICATION_JSON).content(CUERPO))
                .andExpect(status().isForbidden());

        verify(creacionIncidencias, never()).crear(any());
    }

    @Test
    @DisplayName("HU44-CP20: consultar las incidencias responde 200 con la página, sin el autor, y por defecto pide la 1 de tamaño 10")
    void consultarResponde200ConLaPagina() throws Exception {
        when(consultaIncidencias.consultarPropias(CLIENTE, 0, 10))
                .thenReturn(new IncidenciaPage(List.of(incidenciaDe("c-1")), 0, 10, 1));

        consultarComoCliente("")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].id").value("i-1"))
                .andExpect(jsonPath("$.items[0].asunto").value("No me llega el pedido"))
                .andExpect(jsonPath("$.items[0].descripcion").value("Lo pedi hace una semana"))
                .andExpect(jsonPath("$.items[0].estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.items[0].respuesta").value(""))
                .andExpect(jsonPath("$.items[0].fechaCreacion").value("2026-11-06T10:15:30Z"))
                .andExpect(jsonPath("$.items[0].autorId").doesNotExist())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalItems").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    @DisplayName("HU44-CP20: la página empieza en 1: page=3 y size=2 piden la página 2 (desde 0) y se devuelve con page 3")
    void laPaginaEmpiezaEnUno() throws Exception {
        when(consultaIncidencias.consultarPropias(CLIENTE, 2, 2))
                .thenReturn(new IncidenciaPage(List.of(incidenciaDe("c-1")), 2, 2, 5));

        consultarComoCliente("?page=3&size=2")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(3))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalItems").value(5))
                .andExpect(jsonPath("$.totalPages").value(3));
    }

    @Test
    @DisplayName("HU44-CP20: sin incidencias responde 200 con una lista vacía y cero páginas")
    void sinIncidenciasResponde200ConUnaListaVacia() throws Exception {
        when(consultaIncidencias.consultarPropias(CLIENTE, 0, 10)).thenReturn(new IncidenciaPage(List.of(), 0, 10, 0));

        consultarComoCliente("")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(0))
                .andExpect(jsonPath("$.totalItems").value(0))
                .andExpect(jsonPath("$.totalPages").value(0));
    }

    @Test
    @DisplayName("HU44-CP20: un vendedor también consulta las suyas: el caso de uso recibe su correo")
    void unVendedorConsultaLasSuyas() throws Exception {
        when(consultaIncidencias.consultarPropias(VENDEDOR, 0, 10))
                .thenReturn(new IncidenciaPage(List.of(incidenciaDe("v-1")), 0, 10, 1));

        mockMvc.perform(get(URL).with(user(VENDEDOR).roles("VENDEDOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value("i-1"));
    }

    @ParameterizedTest(name = "consulta {0}")
    @ValueSource(strings = {"?page=0", "?size=0", "?size=51"})
    @DisplayName("HU44-CP21: una página o un tamaño que el caso de uso rechaza responden 400 VALIDATION_ERROR")
    void unaPaginaInvalidaResponde400(String consulta) throws Exception {
        when(consultaIncidencias.consultarPropias(any(), anyInt(), anyInt()))
                .thenThrow(new IncidenciaInvalidaException("detalle interno"));

        consultarComoCliente(consulta)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("HU44-CP21: una página que no es un número responde 400 y no llega al caso de uso")
    void unaPaginaQueNoEsUnNumeroResponde400() throws Exception {
        consultarComoCliente("?page=abc").andExpect(status().isBadRequest());

        verify(consultaIncidencias, never()).consultarPropias(any(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("HU44-CP22: sin sesión consultar las incidencias responde 401")
    void sinSesionLaConsultaResponde401() throws Exception {
        mockMvc.perform(get(URL)).andExpect(status().isUnauthorized());

        verify(consultaIncidencias, never()).consultarPropias(any(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("HU44-CP22: el administrador no consulta incidencias por aquí: responde 403 con el code ACCESS_DENIED")
    void elAdministradorRecibe403EnLaConsulta() throws Exception {
        mockMvc.perform(get(URL).with(user(ADMINISTRADOR).roles("ADMINISTRADOR")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));

        verify(consultaIncidencias, never()).consultarPropias(any(), anyInt(), anyInt());
    }
}