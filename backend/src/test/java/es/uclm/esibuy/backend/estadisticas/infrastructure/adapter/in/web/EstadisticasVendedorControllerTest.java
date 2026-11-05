package es.uclm.esibuy.backend.estadisticas.infrastructure.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
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

import es.uclm.esibuy.backend.estadisticas.application.port.in.EstadisticasVendedorUseCase;
import es.uclm.esibuy.backend.estadisticas.domain.model.EstadisticasVendedor;
import es.uclm.esibuy.backend.estadisticas.domain.model.ProductoMasVendido;
import es.uclm.esibuy.backend.estadisticas.domain.model.ValoracionProducto;
import es.uclm.esibuy.backend.shared.config.JacksonConfig;
import es.uclm.esibuy.backend.shared.config.SecurityConfig;

// Estadísticas del vendedor (HU17, T279): GET /api/seller/statistics. El caso de uso es un doble de prueba; la
// seguridad es la real (SecurityConfig: /api/seller/** solo para VENDEDOR).
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: casos HU17-CP11 y CP12, propuestos.
@WebMvcTest(EstadisticasVendedorController.class)
@Import({SecurityConfig.class, JacksonConfig.class})
class EstadisticasVendedorControllerTest {

    private static final String URL = "/api/seller/statistics";
    private static final String VENDEDOR = "vendedor@esibuy.test";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EstadisticasVendedorUseCase estadisticasVendedor;

    @Test
    @DisplayName("HU17-CP11: el vendedor recibe sus estadísticas de ventas, valoraciones y stock con el formato del contrato")
    void vendedorRecibeSusEstadisticas() throws Exception {
        when(estadisticasVendedor.consultar(VENDEDOR)).thenReturn(new EstadisticasVendedor(3, 7,
                new BigDecimal("155.50"),
                List.of(new ProductoMasVendido("p-1", "Teclado", 5, new BigDecimal("120.00"))),
                4.25, 8, List.of(new ValoracionProducto("p-1", "Teclado", 4.5, 6)), 4, 1, 2, 1));

        mockMvc.perform(get(URL).with(user(VENDEDOR).roles("VENDEDOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ventas.pedidos").value(3))
                .andExpect(jsonPath("$.ventas.unidadesVendidas").value(7))
                .andExpect(jsonPath("$.ventas.ingresos").value(155.5))
                .andExpect(jsonPath("$.ventas.masVendidos[0].productoId").value("p-1"))
                .andExpect(jsonPath("$.ventas.masVendidos[0].nombre").value("Teclado"))
                .andExpect(jsonPath("$.ventas.masVendidos[0].unidades").value(5))
                .andExpect(jsonPath("$.ventas.masVendidos[0].ingresos").value(120.0))
                .andExpect(jsonPath("$.valoraciones.media").value(4.25))
                .andExpect(jsonPath("$.valoraciones.total").value(8))
                .andExpect(jsonPath("$.valoraciones.porProducto[0].productoId").value("p-1"))
                .andExpect(jsonPath("$.valoraciones.porProducto[0].media").value(4.5))
                .andExpect(jsonPath("$.valoraciones.porProducto[0].numero").value(6))
                .andExpect(jsonPath("$.stock.productos").value(4))
                .andExpect(jsonPath("$.stock.sinStock").value(1))
                .andExpect(jsonPath("$.stock.stockBajo").value(2))
                .andExpect(jsonPath("$.stock.noDisponibles").value(1));
    }

    @Test
    @DisplayName("HU17-CP11: sin valoraciones, la media llega vacía")
    void sinValoracionesLaMediaLlegaVacia() throws Exception {
        when(estadisticasVendedor.consultar(VENDEDOR)).thenReturn(new EstadisticasVendedor(0, 0, BigDecimal.ZERO,
                List.of(), null, 0, List.of(), 0, 0, 0, 0));

        mockMvc.perform(get(URL).with(user(VENDEDOR).roles("VENDEDOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valoraciones.media").isEmpty())
                .andExpect(jsonPath("$.valoraciones.total").value(0))
                .andExpect(jsonPath("$.ventas.masVendidos").isEmpty());
    }

    @Test
    @DisplayName("HU17-CP12: sin sesión no se ven las estadísticas")
    void sinSesionDevuelve401() throws Exception {
        mockMvc.perform(get(URL))
                .andExpect(status().isUnauthorized());

        verify(estadisticasVendedor, never()).consultar(any());
    }

    @ParameterizedTest(name = "rol {0}")
    @ValueSource(strings = {"CLIENTE", "ADMINISTRADOR"})
    @DisplayName("HU17-CP12: un usuario que no es vendedor no ve las estadísticas")
    void otroRolDevuelve403(String rol) throws Exception {
        mockMvc.perform(get(URL).with(user("otro@esibuy.test").roles(rol)))
                .andExpect(status().isForbidden());

        verify(estadisticasVendedor, never()).consultar(any());
    }
}
