package es.uclm.esibuy.backend.pedidos.domain.model;

/**
 * Estado de un pedido. Por ahora solo PAGADO: el pago se simula antes de crear el pedido (decisión del equipo).
 * Los demás estados llegarán con el seguimiento (HU11) y la cancelación (HU12).
 */
public enum EstadoPedido {
    PAGADO
}
