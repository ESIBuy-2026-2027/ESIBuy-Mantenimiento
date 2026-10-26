package es.uclm.esibuy.backend.metodospago.application.port.in;

import es.uclm.esibuy.backend.metodospago.domain.model.TipoMetodoPago;

/**
 * Datos de entrada del alta de un método de pago (HU6). Lleva texto plano y es el servicio quien construye
 * los value objects del dominio. Solo se usan los campos del tipo elegido: número, titular y caducidad para
 * una tarjeta, la URL para PayPal y el teléfono para Bizum.
 *
 * <p>No lleva CVV: se valida en la petición y nunca llega a la aplicación, para que no pueda guardarse.
 */
public record AnadirMetodoPagoCommand(
        String emailCliente,
        TipoMetodoPago tipo,
        String numeroTarjeta,
        String titular,
        String caducidad,
        String urlPayPal,
        String telefonoBizum) {
}
