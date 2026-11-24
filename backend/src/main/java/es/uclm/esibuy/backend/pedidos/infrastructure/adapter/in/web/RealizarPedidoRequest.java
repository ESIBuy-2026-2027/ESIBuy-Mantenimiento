package es.uclm.esibuy.backend.pedidos.infrastructure.adapter.in.web;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import es.uclm.esibuy.backend.pedidos.application.port.in.RealizarPedidoCommand;

/**
 * Cuerpo de POST /api/customer/orders: el vendedor del pedido, la dirección de envío y el método de pago, por su
 * id, y el total que el cliente ha visto al confirmar. Un clienteId en el cuerpo se ignora: el cliente sale de la
 * sesión. Los productos y precios salen del carrito, no de la petición: el total solo sirve para comprobar que lo que
 * se cobra es lo que el cliente vio.
 */
public record RealizarPedidoRequest(@NotBlank String vendedorId, @NotBlank String direccionId,
        @NotBlank String paymentMethodId, @NotNull @PositiveOrZero BigDecimal totalEsperado) {

    RealizarPedidoCommand aComando(String emailCliente) {
        return new RealizarPedidoCommand(emailCliente, vendedorId, direccionId, paymentMethodId, totalEsperado);
    }
}
