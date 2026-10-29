package es.uclm.esibuy.backend.pedidos.application.port.in;

/** Lo que el cliente elige para su pedido: el proveedor, la dirección de envío y el método de pago. */
public record RealizarPedidoCommand(String emailCliente, String vendedorId, String direccionId,
        String paymentMethodId) {
}
