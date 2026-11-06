package es.uclm.esibuy.backend.incidencias.infrastructure.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
}