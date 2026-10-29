package es.uclm.esibuy.backend.pedidos.domain.model;

import java.util.regex.Pattern;

import es.uclm.esibuy.backend.pedidos.domain.exception.PedidoInvalidoException;

/**
 * Número de pedido que ve el cliente (HU10): {@code ESB-AAAA-NNNNNN}, con el año y un correlativo que vuelve a
 * empezar cada año (decisión del equipo).
 */
public record NumeroPedido(String valor) {

    private static final Pattern FORMATO = Pattern.compile("^ESB-\\d{4}-\\d{6}$");
    private static final int CORRELATIVO_MAXIMO = 999_999;

    public NumeroPedido {
        if (valor == null || !FORMATO.matcher(valor).matches()) {
            throw new PedidoInvalidoException("El número de pedido no tiene el formato ESB-AAAA-NNNNNN");
        }
    }

    public static NumeroPedido de(int anio, long correlativo) {
        if (correlativo < 1 || correlativo > CORRELATIVO_MAXIMO) {
            throw new PedidoInvalidoException("El correlativo del pedido debe estar entre 1 y " + CORRELATIVO_MAXIMO);
        }
        return new NumeroPedido(String.format("ESB-%04d-%06d", anio, correlativo));
    }
}
