package es.uclm.esibuy.backend.pedidos.infrastructure.adapter.out.pago;

import java.math.BigDecimal;

import es.uclm.esibuy.backend.pedidos.application.port.out.MetodoPagoPedido;
import es.uclm.esibuy.backend.pedidos.application.port.out.PagoPort;

/**
 * Pago simulado (HU10): no se mueve dinero ni se contacta con ninguna pasarela. Acepta todos los pagos salvo los de
 * una tarjeta cuya máscara acabe en 0000, que hace de tarjeta de prueba rechazada, como las de los entornos de prueba
 * de las pasarelas reales. Bizum y PayPal siempre se aceptan. Solo mira el tipo y la máscara del método, que ya se
 * muestran al cliente, nunca sus datos de pago. La caducidad no se mira: una tarjeta guardada que caduque después se
 * sigue pudiendo cargar (decisión del equipo en HU6).
 */
public class PagoSimuladoAdapter implements PagoPort {

    private static final String TIPO_TARJETA = "TARJETA";
    private static final String TERMINACION_RECHAZADA = "0000";

    @Override
    public boolean cobrar(MetodoPagoPedido metodo, BigDecimal importe) {
        return !(TIPO_TARJETA.equals(metodo.tipo()) && metodo.mascara().endsWith(TERMINACION_RECHAZADA));
    }
}
