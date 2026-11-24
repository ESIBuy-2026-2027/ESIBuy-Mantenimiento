package es.uclm.esibuy.backend.metodospago.application.port.in;

/**
 * Lo que el pedido necesita saber del método de pago elegido (HU10), sin el dominio de metodospago: el tipo
 * (TARJETA, PAYPAL o BIZUM) y la máscara que ya se muestra al cliente. Nunca lleva los datos de pago.
 */
public record MetodoPagoParaPedido(String tipo, String mascara) {
}
