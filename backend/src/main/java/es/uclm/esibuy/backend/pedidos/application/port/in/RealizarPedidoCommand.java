package es.uclm.esibuy.backend.pedidos.application.port.in;

import java.math.BigDecimal;

/**
 * Lo que el cliente elige para su pedido: el vendedor, la dirección de envío y el método de pago, y el total que ha
 * visto al confirmar. Si ese total ya no es el real, el pedido no se hace.
 */
public record RealizarPedidoCommand(String emailCliente, String vendedorId, String direccionId,
        String paymentMethodId, BigDecimal totalEsperado) {
}
