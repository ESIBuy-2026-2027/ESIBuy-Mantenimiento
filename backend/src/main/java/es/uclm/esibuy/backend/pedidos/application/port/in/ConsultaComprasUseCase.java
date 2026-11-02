package es.uclm.esibuy.backend.pedidos.application.port.in;

/**
 * Consulta de las compras del cliente (HU13): si ha comprado un producto, para poder valorarlo. El cliente sale
 * siempre de la sesión.
 */
public interface ConsultaComprasUseCase {

    /** Si el cliente tiene algún pedido pagado con ese producto. */
    boolean haComprado(String emailCliente, String productoId);
}
