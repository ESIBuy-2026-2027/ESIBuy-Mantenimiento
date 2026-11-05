package es.uclm.esibuy.backend.pedidos.application.port.out;

/** Correlativo de los números de pedido: uno por año, sin repetir aunque se pidan dos a la vez. */
public interface ContadorPedidosPort {

    long siguiente(int anio);
}
