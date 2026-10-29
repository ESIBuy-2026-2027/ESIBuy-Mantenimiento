package es.uclm.esibuy.backend.direcciones.application.port.in;

/**
 * Los datos de una dirección de envío para copiarlos en un pedido (HU10), sin el dominio de direcciones. Es lo
 * que guarda el pedido en su direccionEnvio: sin el id ni la marca de principal.
 */
public record DireccionParaPedido(String destinatario, String calle, String detalles, String codigoPostal,
        String ciudad, String provincia, String telefono) {
}
