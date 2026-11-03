package es.uclm.esibuy.backend.valoraciones.application.port.out;

/** Lo que valoraciones necesita del módulo de pedidos: si el cliente ha comprado el producto. */
public interface ComprasPort {

    boolean haComprado(String emailCliente, String productoId);
}
