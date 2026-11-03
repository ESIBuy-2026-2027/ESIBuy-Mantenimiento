package es.uclm.esibuy.backend.valoraciones.infrastructure.adapter.out.pedidos;

import es.uclm.esibuy.backend.pedidos.application.port.in.ConsultaComprasUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

// Adaptador de valoraciones hacia el módulo de pedidos (HU13, T370), sin Spring. Pregunta por su puerto de entrada,
// sin usar su dominio.
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: caso HU13-CP13, propuesto.
@ExtendWith(MockitoExtension.class)
class ComprasAdapterTest {

    private static final String EMAIL = "cliente@esibuy.test";

    @Mock
    private ConsultaComprasUseCase consultaCompras;

    @Test
    @DisplayName("HU13-CP13: si el cliente ha comprado un producto se pregunta al módulo de pedidos")
    void haCompradoPreguntaAlModuloDePedidos() {
        when(consultaCompras.haComprado(EMAIL, "p-1")).thenReturn(true);

        assertTrue(new ComprasAdapter(consultaCompras).haComprado(EMAIL, "p-1"));
    }

    @Test
    @DisplayName("HU13-CP13: si pedidos responde que no lo ha comprado, el adaptador también")
    void siPedidosRespondeQueNoElAdaptadorTambien() {
        when(consultaCompras.haComprado(EMAIL, "p-2")).thenReturn(false);

        assertFalse(new ComprasAdapter(consultaCompras).haComprado(EMAIL, "p-2"));
    }
}
