package es.uclm.esibuy.backend.pedidos.domain.model;

/**
 * Copia de la dirección de envío dentro del pedido (campo direccionEnvio de la colección pedidos). Se copia al
 * crear el pedido: si el cliente cambia o borra después su dirección (HU5), el pedido conserva a dónde se envió.
 * Ya llega validada por el módulo de direcciones.
 */
public record DireccionEnvioPedido(String destinatario, String calle, String detalles, String codigoPostal,
        String ciudad, String provincia, String telefono) {
}
