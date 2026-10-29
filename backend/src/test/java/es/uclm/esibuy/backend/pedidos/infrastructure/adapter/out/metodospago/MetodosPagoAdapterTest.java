package es.uclm.esibuy.backend.pedidos.infrastructure.adapter.out.metodospago;

import es.uclm.esibuy.backend.metodospago.application.port.in.GestionMetodosPagoUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

// Adaptador de pedidos hacia los métodos de pago (HU10), sin Spring
@ExtendWith(MockitoExtension.class)
class MetodosPagoAdapterTest {

    @Mock
    private GestionMetodosPagoUseCase gestionMetodosPago;

    @Test
    @DisplayName("HU10-CP16: si el método de pago es del cliente se pregunta al módulo de métodos de pago")
    void esDelClientePreguntaAlModuloDeMetodosDePago() {
        when(gestionMetodosPago.esDelCliente("cliente@esibuy.test", "mp-1")).thenReturn(true);

        assertTrue(new MetodosPagoAdapter(gestionMetodosPago).esDelCliente("cliente@esibuy.test", "mp-1"));
    }
}
