package es.uclm.esibuy.backend.estadisticas.infrastructure.adapter.in.web;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
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

import es.uclm.esibuy.backend.estadisticas.application.port.in.EstadisticasAdministradorResultado;
import es.uclm.esibuy.backend.estadisticas.application.port.in.EstadisticasAdministradorUseCase;
import es.uclm.esibuy.backend.estadisticas.domain.model.ProductoMasVendido;
import es.uclm.esibuy.backend.estadisticas.domain.model.ValoracionProducto;
import es.uclm.esibuy.backend.shared.config.JacksonConfig;
import es.uclm.esibuy.backend.shared.config.SecurityConfig;

@WebMvcTest(EstadisticasAdministradorController.class)
@Import({SecurityConfig.class, JacksonConfig.class})
class EstadisticasAdministradorControllerTest {

    private static final String URL = "/api/admin/statistics";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EstadisticasAdministradorUseCase estadisticasAdministrador;

    @Test
    @DisplayName("HU546-CP01: cualquier administrador puede consultar los tres rankings de la plataforma")
    void administradorRecibeLosTresRankings() throws Exception {
        when(estadisticasAdministrador.consultar()).thenReturn(new EstadisticasAdministradorResultado(
                List.of(new ProductoMasVendido("p-1", "Teclado", 12, new BigDecimal("120.00"))),
                List.of(new ValoracionProducto("p-2", "Ratón", 4.8, 5)),
                List.of(new EstadisticasAdministradorResultado.VendedorMejorValorado(
                        "v-1", "Tienda Vera", 4.6, 8))));

        mockMvc.perform(get(URL).with(user("admin@esibuy.test").roles("ADMINISTRADOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productosMasVendidos[0].productoId").value("p-1"))
                .andExpect(jsonPath("$.productosMasVendidos[0].unidades").value(12))
                .andExpect(jsonPath("$.productosMejorValorados[0].media").value(4.8))
                .andExpect(jsonPath("$.productosMejorValorados[0].numeroValoraciones").value(5))
                .andExpect(jsonPath("$.vendedoresMejorValorados[0].nombreComercial").value("Tienda Vera"))
                .andExpect(jsonPath("$.vendedoresMejorValorados[0].media").value(4.6));

        verify(estadisticasAdministrador).consultar();
    }

    @Test
    @DisplayName("HU546-CP02: sin sesión no se puede consultar el ranking")
    void sinSesionDevuelve401() throws Exception {
        mockMvc.perform(get(URL)).andExpect(status().isUnauthorized());

        verify(estadisticasAdministrador, never()).consultar();
    }

    @ParameterizedTest
    @ValueSource(strings = { "CLIENTE", "VENDEDOR" })
    @DisplayName("HU546-CP02: solo el rol administrador puede consultar el ranking")
    void otrosRolesReciben403(String rol) throws Exception {
        mockMvc.perform(get(URL).with(user("otro@esibuy.test").roles(rol)))
                .andExpect(status().isForbidden());

        verify(estadisticasAdministrador, never()).consultar();
    }
}
