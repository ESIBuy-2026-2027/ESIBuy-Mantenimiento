package es.uclm.esibuy.backend.pedidos.domain.model;

/**
 * Estado de un pedido. Todos nacen PAGADO: el pago se simula antes de crear el pedido (decisión del equipo).
 * El orden es el de la máquina de estados de envío que ve el cliente (HU11); los hará avanzar el vendedor.
 * CANCELADO no es un paso del envío: lo usará la cancelación (HU12).
 */
public enum EstadoPedido {
    PAGADO,
    PREPARANDO,
    ENVIADO,
    ENTREGADO,
    CANCELADO
}
