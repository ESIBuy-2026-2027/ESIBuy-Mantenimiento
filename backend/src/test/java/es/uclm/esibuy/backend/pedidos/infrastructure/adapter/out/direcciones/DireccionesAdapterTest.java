package es.uclm.esibuy.backend.pedidos.infrastructure.adapter.out.direcciones;

import es.uclm.esibuy.backend.direcciones.application.port.in.DireccionParaPedido;
import es.uclm.esibuy.backend.direcciones.application.port.in.GestionDireccionesUseCase;
import es.uclm.esibuy.backend.pedidos.domain.model.DireccionEnvioPedido;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

// Adaptador de pedidos hacia las direcciones de envío (HU10), sin Spring: copia los datos de la dirección
@ExtendWith(MockitoExtension.class)
class DireccionesAdapterTest {

    @Mock
    private GestionDireccionesUseCase gestionDirecciones;

    @Test
    @DisplayName("HU10-CP16: los datos de la dirección se piden al módulo de direcciones y se copian")
    void losDatosDeLaDireccionSeCopian() {
        when(gestionDirecciones.consultarParaPedido("cliente@esibuy.test", "d-1")).thenReturn(Optional.of(
                new DireccionParaPedido("Carlos", "Calle Mayor 1", null, "13001", "Ciudad Real", "Ciudad Real",
                        "611222333")));

        assertEquals(Optional.of(new DireccionEnvioPedido("Carlos", "Calle Mayor 1", null, "13001", "Ciudad Real",
                        "Ciudad Real", "611222333")),
                new DireccionesAdapter(gestionDirecciones).consultar("cliente@esibuy.test", "d-1"));
    }

    @Test
    @DisplayName("HU10-CP16: una dirección que no es del cliente no se devuelve")
    void unaDireccionAjenaNoSeDevuelve() {
        when(gestionDirecciones.consultarParaPedido("cliente@esibuy.test", "d-9")).thenReturn(Optional.empty());

        assertEquals(Optional.empty(), new DireccionesAdapter(gestionDirecciones).consultar("cliente@esibuy.test", "d-9"));
    }
}
