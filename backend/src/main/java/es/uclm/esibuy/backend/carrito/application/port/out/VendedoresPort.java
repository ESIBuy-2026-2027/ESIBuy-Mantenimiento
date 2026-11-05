package es.uclm.esibuy.backend.carrito.application.port.out;

/** Lo que el carrito necesita del módulo de usuarios para la cabecera de cada proveedor. */
@FunctionalInterface
public interface VendedoresPort {

    String nombreComercial(String vendedorId);
}
