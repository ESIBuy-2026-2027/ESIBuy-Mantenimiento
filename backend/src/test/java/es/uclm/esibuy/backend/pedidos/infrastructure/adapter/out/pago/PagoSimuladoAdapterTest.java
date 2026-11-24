package es.uclm.esibuy.backend.pedidos.infrastructure.adapter.out.pago;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.uclm.esibuy.backend.pedidos.application.port.out.MetodoPagoPedido;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

// Pago simulado (HU10): no se mueve dinero. Solo mira el tipo y la máscara del método, nunca sus datos de pago
class PagoSimuladoAdapterTest {

    private static final BigDecimal IMPORTE = new BigDecimal("197.32");

    private final PagoSimuladoAdapter pago = new PagoSimuladoAdapter();

    @ParameterizedTest
    @ValueSource(strings = {"•••• 4242", "•••• 1111", "•••• 0001"})
    @DisplayName("HU10-CP26: una tarjeta cualquiera se acepta, salvo la de prueba acabada en 0000")
    void unaTarjetaNormalSeAcepta(String mascara) {
        assertTrue(pago.cobrar(new MetodoPagoPedido("TARJETA", mascara), IMPORTE));
    }

    @Test
    @DisplayName("HU10-CP26: la tarjeta de prueba acabada en 0000 se rechaza")
    void laTarjetaDePruebaAcabadaEnCerosSeRechaza() {
        assertFalse(pago.cobrar(new MetodoPagoPedido("TARJETA", "•••• 0000"), IMPORTE));
    }

    @Test
    @DisplayName("HU10-CP26: Bizum y PayPal siempre se aceptan, aunque su máscara acabe en 0000")
    void bizumYPayPalSiempreSeAceptan() {
        assertTrue(pago.cobrar(new MetodoPagoPedido("BIZUM", "•••• 0000"), IMPORTE));
        assertTrue(pago.cobrar(new MetodoPagoPedido("PAYPAL", "•••• 0000"), IMPORTE));
    }
}
