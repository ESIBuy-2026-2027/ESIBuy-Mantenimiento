package es.uclm.esibuy.backend.pedidos.application.port.out;

/**
 * El método de pago elegido para un pedido: su tipo y su máscara, nunca los datos de pago. El tipo es el nombre del
 * tipo de método (TARJETA, PAYPAL o BIZUM).
 */
public record MetodoPagoPedido(String tipo, String mascara) {
}
