package es.uclm.esibuy.backend.pedidos.application.port.out;

import java.math.BigDecimal;

/** Lo que pedidos necesita para cobrar un pedido. Hoy el cobro es simulado: no se mueve dinero. */
@FunctionalInterface
public interface PagoPort {

    // Devuelve false si el pago se rechaza: entonces el pedido no se confirma
    boolean cobrar(MetodoPagoPedido metodo, BigDecimal importe);
}
