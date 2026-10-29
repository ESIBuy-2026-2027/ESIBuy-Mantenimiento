package es.uclm.esibuy.backend.pedidos.infrastructure.adapter.in.web;

import jakarta.validation.constraints.NotBlank;

import es.uclm.esibuy.backend.pedidos.application.port.in.RealizarPedidoCommand;

/**
 * Cuerpo de POST /api/customer/orders: el proveedor del pedido, la dirección de envío y el método de pago, por su
 * id. Un clienteId en el cuerpo se ignora: el cliente sale de la sesión. Los productos y precios salen del carrito.
 */
public record RealizarPedidoRequest(@NotBlank String vendedorId, @NotBlank String direccionId,
        @NotBlank String paymentMethodId) {

    RealizarPedidoCommand aComando(String emailCliente) {
        return new RealizarPedidoCommand(emailCliente, vendedorId, direccionId, paymentMethodId);
    }
}
