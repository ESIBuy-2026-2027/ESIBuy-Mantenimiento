package es.uclm.esibuy.backend.pedidos.infrastructure.adapter.out.metodospago;

import es.uclm.esibuy.backend.metodospago.application.port.in.GestionMetodosPagoUseCase;
import es.uclm.esibuy.backend.metodospago.application.port.in.MetodoPagoParaPedido;
import es.uclm.esibuy.backend.pedidos.application.port.out.MetodoPagoPedido;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

// Adaptador de pedidos hacia los métodos de pago (HU10), sin Spring
@ExtendWith(MockitoExtension.class)
class MetodosPagoAdapterTest {

    private static final String EMAIL = "cliente@esibuy.test";

    @Mock
    private GestionMetodosPagoUseCase gestionMetodosPago;

    @Test
    @DisplayName("HU10-CP16: el método de pago se pide al módulo de métodos de pago y se traduce a lo que usa el pedido")
    void consultarPreguntaAlModuloDeMetodosDePago() {
        when(gestionMetodosPago.consultarParaPedido(EMAIL, "mp-1"))
                .thenReturn(Optional.of(new MetodoPagoParaPedido("TARJETA", "•••• 4242")));

        assertEquals(Optional.of(new MetodoPagoPedido("TARJETA", "•••• 4242")),
                new MetodosPagoAdapter(gestionMetodosPago).consultar(EMAIL, "mp-1"));
    }

    @Test
    @DisplayName("HU10-CP16: un método de pago que no es del cliente no se devuelve")
    void unMetodoAjenoNoSeDevuelve() {
        when(gestionMetodosPago.consultarParaPedido(EMAIL, "mp-ajeno")).thenReturn(Optional.empty());

        assertEquals(Optional.empty(), new MetodosPagoAdapter(gestionMetodosPago).consultar(EMAIL, "mp-ajeno"));
    }
}
