package es.uclm.esibuy.backend.pedidos.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import es.uclm.esibuy.backend.pedidos.domain.exception.PedidoInvalidoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class NumeroPedidoTest {

    @Test
    @DisplayName("HU10-CP13: el número de pedido es ESB, el año y el correlativo con seis cifras")
    void elNumeroLlevaElAnoYElCorrelativo() {
        assertEquals("ESB-2026-000001", NumeroPedido.de(2026, 1).valor());
        assertEquals("ESB-2027-123456", NumeroPedido.de(2027, 123456).valor());
    }

    @Test
    @DisplayName("HU10-CP13: el correlativo empieza en 1 y no pasa de seis cifras")
    void elCorrelativoEstaEntreUnoYSeisCifras() {
        assertThrows(PedidoInvalidoException.class, () -> NumeroPedido.de(2026, 0));
        assertThrows(PedidoInvalidoException.class, () -> NumeroPedido.de(2026, 1_000_000));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"ESB-26-000001", "ESB-2026-1", "XYZ-2026-000001"})
    @DisplayName("HU10-CP13: un número guardado con otro formato no es válido")
    void unNumeroConOtroFormatoNoEsValido(String valor) {
        assertThrows(PedidoInvalidoException.class, () -> new NumeroPedido(valor));
    }
}
