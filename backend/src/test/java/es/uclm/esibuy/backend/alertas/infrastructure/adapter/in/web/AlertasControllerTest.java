package es.uclm.esibuy.backend.alertas.infrastructure.adapter.in.web;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
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

import es.uclm.esibuy.backend.alertas.application.port.in.ConsultaAlertasUseCase;
import es.uclm.esibuy.backend.alertas.domain.model.Alerta;
import es.uclm.esibuy.backend.alertas.domain.model.DetalleAlerta;
import es.uclm.esibuy.backend.alertas.domain.model.TipoAlerta;
import es.uclm.esibuy.backend.shared.config.SecurityConfig;

// Alertas del cliente (AB#529): GET /api/customer/alerts. El caso de uso es un doble de prueba; la seguridad es la
// real (SecurityConfig: /api/customer/** solo para CLIENTE) y los errores los traduce ManejadorGlobalErrores.
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: casos HU529-CP17 y CP18, propuestos.
@WebMvcTest(AlertasController.class)
@Import(SecurityConfig.class)
class AlertasControllerTest {

    private static final String URL = "/api/customer/alerts";
    private static final String CLIENTE = "cliente@esibuy.test";
    private static final Instant HOY = Instant.parse("2026-11-26T10:00:00Z");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ConsultaAlertasUseCase consultaAlertas;

    @Test
    @DisplayName("HU529-CP17: el cliente en sesión recibe 200 con un mensaje de éxito y sus alertas, con el producto de cada una")
    void elClienteRecibeSusAlertas() throws Exception {
        when(consultaAlertas.consultar(CLIENTE)).thenReturn(List.of(
                new Alerta("a-2", "c-1", "p-2", TipoAlerta.BAJADA_PRECIO, HOY, false,
                        new DetalleAlerta("Ratón", "https://img.test/p.png", new BigDecimal("20.00"), new BigDecimal("15.50"))),
                new Alerta("a-1", "c-1", "p-1", TipoAlerta.DISPONIBILIDAD, HOY, true,
                        new DetalleAlerta("Teclado", "https://img.test/p.png", null, null))));

        mockMvc.perform(get(URL).with(user(CLIENTE).roles("CLIENTE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Alertas consultadas correctamente"))
                .andExpect(jsonPath("$.alertas.length()").value(2))
                .andExpect(jsonPath("$.alertas[0].id").value("a-2"))
                .andExpect(jsonPath("$.alertas[0].tipo").value("BAJADA_PRECIO"))
                .andExpect(jsonPath("$.alertas[0].fecha").value("2026-11-26T10:00:00Z"))
                .andExpect(jsonPath("$.alertas[0].leida").value(false))
                .andExpect(jsonPath("$.alertas[0].producto.id").value("p-2"))
                .andExpect(jsonPath("$.alertas[0].producto.nombre").value("Ratón"))
                .andExpect(jsonPath("$.alertas[0].producto.imagen").value("https://img.test/p.png"))
                .andExpect(jsonPath("$.alertas[0].producto.precioAnterior").value(20.00))
                .andExpect(jsonPath("$.alertas[0].producto.precioNuevo").value(15.50))
                .andExpect(jsonPath("$.alertas[1].tipo").value("DISPONIBILIDAD"))
                .andExpect(jsonPath("$.alertas[1].leida").value(true));
    }

    @Test
    @DisplayName("HU529-CP17: un cliente sin alertas recibe 200 con una lista vacía y un mensaje que lo dice")
    void unClienteSinAlertasRecibeUnaListaVacia() throws Exception {
        when(consultaAlertas.consultar(CLIENTE)).thenReturn(List.of());

        mockMvc.perform(get(URL).with(user(CLIENTE).roles("CLIENTE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("No tienes alertas"))
                .andExpect(jsonPath("$.alertas.length()").value(0));
    }

    @Test
    @DisplayName("HU529-CP18: sin sesión no se consultan alertas (401)")
    void sinSesionResponde401() throws Exception {
        mockMvc.perform(get(URL)).andExpect(status().isUnauthorized());
        verifyNoInteractions(consultaAlertas);
    }

    @ParameterizedTest
    @ValueSource(strings = { "VENDEDOR", "ADMINISTRADOR" })
    @DisplayName("HU529-CP18: un usuario que no es cliente no consulta alertas (403 ACCESS_DENIED)")
    void otroRolResponde403(String rol) throws Exception {
        mockMvc.perform(get(URL).with(user(CLIENTE).roles(rol)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
        verifyNoInteractions(consultaAlertas);
    }
}
